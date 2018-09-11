/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.controller;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.EmailMessage;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.HoneypotService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for emailing an article.
 */
@Controller
public class EmailArticleController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(EmailArticleController.class);

  private static final int MAX_TO_EMAILS = 5;

  @Autowired
  private HoneypotService honeypotService;
  @Autowired
  private FreeMarkerConfig freeMarkerConfig;
  @Autowired
  private FreemarkerMailService freemarkerMailService;
  @Autowired
  private JavaMailSender javaMailSender;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;


  @RequestMapping(name = "email", value = "/article/email")
  public String renderEmailThisArticle(HttpServletRequest request, Model model, @SiteParam Site site,
                                       RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("email")
        .populate(request, model);
    model.addAttribute("maxEmails", MAX_TO_EMAILS);
    return site + "/ftl/article/email";
  }

  /**
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "emailPost", value = "/article/email", method = RequestMethod.POST)
  public String emailArticle(HttpServletRequest request, HttpServletResponse response, Model model,
                             @SiteParam Site site,
                             RequestedDoiVersion articleId,
                             @RequestParam("articleUri") String articleUri,
                             @RequestParam("emailToAddresses") String emailToAddresses,
                             @RequestParam("emailFrom") String emailFrom,
                             @RequestParam("senderName") String senderName,
                             @RequestParam("note") String note,
                             @RequestParam(value = "authorPhone", required = false) String authorPhone,
                             @RequestParam(value = "authorAffiliation", required = false) String authorAffiliation)
      throws IOException, MessagingException {
    requireNonemptyParameter(articleUri);

    model.addAttribute("emailToAddresses", emailToAddresses);
    model.addAttribute("emailFrom", emailFrom);
    model.addAttribute("senderName", senderName);
    model.addAttribute("note", note);
    model.addAttribute("articleUri", articleUri);

    List<InternetAddress> toAddresses = Splitter.on(CharMatcher.anyOf("\n\r")).omitEmptyStrings()
        .splitToList(emailToAddresses).stream()
        .map(email -> EmailMessage.createAddress(null /*name*/, email))
        .collect(Collectors.toList());

    Set<String> errors = validateEmailArticleInput(toAddresses, emailFrom, senderName);
    if (applyValidation(response, model, errors)) {
      return renderEmailThisArticle(request, model, site, articleId);
    }

    Map<String, ?> articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility("emailPost")
        .getIngestionMetadata();

    model.addAttribute("article", articleMetadata);
    model.addAttribute("journalName", site.getJournalName());

    if (honeypotService.checkHoneypot(request, authorPhone, authorAffiliation)) {
      response.setStatus(HttpStatus.CREATED.value());
      return site + "/ftl/article/emailSuccess";
    }

    Multipart content = freemarkerMailService.createContent(site, "emailThisArticle", model);

    String title = articleMetadata.get("title").toString();
    title = title.replaceAll("<[^>]+>", "");

    EmailMessage message = EmailMessage.builder()
        .addToEmailAddresses(toAddresses)
        .setSenderAddress(EmailMessage.createAddress(senderName, emailFrom))
        .setSubject("An Article from " + site.getJournalName() + ": " + title)
        .setContent(content)
        .setEncoding(freeMarkerConfig.getConfiguration().getDefaultEncoding())
        .build();

    message.send(javaMailSender);

    response.setStatus(HttpStatus.CREATED.value());
    return site + "/ftl/article/emailSuccess";
  }

  private Set<String> validateEmailArticleInput(List<InternetAddress> emailToAddresses,
                                                String emailFrom, String senderName) throws IOException {

    Set<String> errors = new HashSet<>();
    if (StringUtils.isBlank(emailFrom)) {
      errors.add("emailFromMissing");
    } else if (!EmailValidator.getInstance().isValid(emailFrom)) {
      errors.add("emailFromInvalid");
    }

    if (emailToAddresses.isEmpty()) {
      errors.add("emailToAddressesMissing");
    } else if (emailToAddresses.size() > MAX_TO_EMAILS) {
      errors.add("tooManyEmailToAddresses");
    } else if (emailToAddresses.stream()
        .noneMatch(email -> EmailValidator.getInstance().isValid(email.toString()))) {
      errors.add("emailToAddressesInvalid");
    }

    if (StringUtils.isBlank(senderName)) {
      errors.add("senderNameMissing");
    }

    return errors;
  }

}
