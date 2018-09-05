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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.RemoteService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for submitting media curation requests.
 */
@Controller
public class MediaCurationController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(MediaCurationController.class);

  @Autowired
  private RemoteService<Reader> remoteReader;
  @Autowired
  private JsonService jsonService;

  /**
   * Serves as a POST endpoint to submit media curation requests
   *
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "submitMediaCurationRequest", value = "/article/submitMediaCurationRequest", method = RequestMethod.POST)
  @ResponseBody
  public String submitMediaCurationRequest(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @RequestParam("doi") String doi,
                                           @RequestParam("link") String link,
                                           @RequestParam("comment") String comment,
                                           @RequestParam("title") String title,
                                           @RequestParam("publishedOn") String publishedOn,
                                           @RequestParam("name") String name,
                                           @RequestParam("email") String email,
                                           @RequestParam("consent") String consent)
      throws IOException {
    requireNonemptyParameter(doi);

    if (!link.matches("^\\w+://.*")) {
      link = "http://" + link;
    }

    if (!validateMediaCurationInput(model, link, name, email, title, publishedOn, consent)) {
      //return model for error reporting
      return jsonService.serialize(model);
    }

    String linkComment = name + ", " + email + "\n" + comment;

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("doi", doi.replaceFirst("info:doi/", "")));
    params.add(new BasicNameValuePair("link", link));
    params.add(new BasicNameValuePair("comment", linkComment));
    params.add(new BasicNameValuePair("title", title));
    params.add(new BasicNameValuePair("publishedOn", publishedOn));

    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = (String) site.getTheme().getConfigMap("mediaCuration").get("mediaCurationUrl");
    if (mediaCurationUrl == null) {
      throw new RuntimeException("Media curation URL is not configured");
    }

    HttpPost httpPost = new HttpPost(mediaCurationUrl);
    httpPost.setEntity(entity);
    StatusLine statusLine = null;
    try (CloseableHttpResponse response = remoteReader.getResponse(httpPost)) {
      statusLine = response.getStatusLine();
    } catch (ServiceRequestException e) {
      //This exception is thrown when the submitted link is already present for the article.
      if (e.getStatusCode() == HttpStatus.CONFLICT.value()
          && e.getResponseBody().equals("The link already exists")) {
        model.addAttribute("formError", "This link has already been submitted. Please submit a different link");
        model.addAttribute("isValid", false);
      } else {
        throw new RuntimeException(e);
      }
    } finally {
      httpPost.releaseConnection();
    }

    if (statusLine != null && statusLine.getStatusCode() != HttpStatus.CREATED.value()) {
      throw new RuntimeException("bad response from media curation server: " + statusLine);
    }

    return jsonService.serialize(model);
  }

  /**
   * Validate the input from the form
   *
   * @param model data passed in from the view
   * @param link  link pointing to media content relating to the article
   * @param name  name of the user submitting the media curation request
   * @param email email of the user submitting the media curation request
   * @return true if everything is ok
   */

  private boolean validateMediaCurationInput(Model model, String link, String name,
                                             String email, String title, String publishedOn, String consent)
      throws IOException {

    boolean isValid = true;

    UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    if (consent == null || !"true".equals(consent)) {
      model.addAttribute("consentError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(link)) {
      model.addAttribute("linkError", "This field is required.");
      isValid = false;
    } else if (!urlValidator.isValid(link)) {
      model.addAttribute("linkError", "Invalid Media link URL");
      isValid = false;
    }

    if (StringUtils.isBlank(name)) {
      model.addAttribute("nameError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(title)) {
      model.addAttribute("titleError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(publishedOn)) {
      model.addAttribute("publishedOnError", "This field is required.");
      isValid = false;
    } else {
      try {
        LocalDate.parse(publishedOn);
      } catch (DateTimeParseException e) {
        model.addAttribute("publishedOnError", "Invalid Date Format, should be YYYY-MM-DD");
        isValid = false;
      }
    }

    if (StringUtils.isBlank(email)) {
      model.addAttribute("emailError", "This field is required.");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(email)) {
      model.addAttribute("emailError", "Invalid e-mail address");
      isValid = false;
    }

    model.addAttribute("isValid", isValid);
    return isValid;
  }

}
