package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.EmailMessage;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class FeedbackController extends WombatController {

  @Autowired
  private FreeMarkerConfig freeMarkerConfig;
  @Autowired
  private FreemarkerMailService freemarkerMailService;
  @Autowired
  private CaptchaService captchaService;
  @Autowired
  private JavaMailSender javaMailSender; // TODO

  private static Map<String, Object> getFeedbackConfig(Site site) {
    return (Map<String, Object>) site.getTheme().getConfigMap("email").get("feedback");
  }

  private static void validateFeedbackConfig(Site site) {
    if (getFeedbackConfig(site).get("destination") == null) {
      throw new NotFoundException("Feedback is not configured with a destination address");
    }
  }

  @RequestMapping(name = "feedback", value = "/feedback", method = RequestMethod.GET)
  public String serveFeedbackPage(Model model, @SiteParam Site site) throws IOException {
    validateFeedbackConfig(site);
    model.addAttribute("captchaHtml", captchaService.getCaptchaHtml(site, Optional.empty()));
    return site + "/ftl/feedback/feedback";
  }

  @RequestMapping(name = "feedbackPost", value = "/feedback", method = RequestMethod.POST)
  public String receiveFeedback(HttpServletRequest request, HttpServletResponse response,
                                Model model, @SiteParam Site site,
                                @RequestParam("fromEmailAddress") String fromEmailAddress,
                                @RequestParam("note") String note,
                                @RequestParam("subject") String subject,
                                @RequestParam("name") String name,
                                @RequestParam("userId") String userId,
                                @RequestParam(RECAPTCHA_CHALLENGE_FIELD) String captchaChallenge,
                                @RequestParam(RECAPTCHA_RESPONSE_FIELD) String captchaResponse)
      throws IOException, MessagingException {
    validateFeedbackConfig(site);

    // Fill input parameters into model. (These can be used in two ways: in the generated email if all input is valid,
    // or in the form in case we need to display validation errors.)
    model.addAttribute("fromEmailAddress", fromEmailAddress);
    model.addAttribute("note", note);
    model.addAttribute("name", name);
    model.addAttribute("subject", subject);

    Set<String> errors = validateInput(fromEmailAddress, note, subject, name);
    if (!captchaService.validateCaptcha(site, request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
      errors.add("captchaError");
    }
    if (applyValidation(response, model, errors)) {
      return serveFeedbackPage(model, site);
    }


    if (subject.isEmpty()) {
      model.addAttribute("subject", (String) getFeedbackConfig(site).get("defaultSubject"));
    }

    model.addAttribute("id", userId);
    model.addAttribute("userInfo", formatUserInfo(request));

    Multipart content = freemarkerMailService.createContent(site, "feedback", model);

    String destinationAddress = (String) getFeedbackConfig(site).get("destination");
    EmailMessage message = EmailMessage.builder()
        .addToEmailAddress(EmailMessage.createAddress(null, destinationAddress))
        .setSenderAddress(EmailMessage.createAddress(name, fromEmailAddress))
        .setSubject(subject)
        .setContent(content)
        .setEncoding(freeMarkerConfig.getConfiguration().getDefaultEncoding())
        .build();

    message.send(javaMailSender);
    return site + "/ftl/feedback/success";
  }

  /**
   * Validate form parameters.
   *
   * @return a set of error flags to be added to the FTL model (empty if all input is valid)
   */
  private static Set<String> validateInput(String fromEmailAddress,
                                           String note,
                                           String subject,
                                           String name) {
    Set<String> errors = new HashSet<>();
    if (StringUtils.isBlank(subject)) {
      errors.add("subjectError");
    }
    if (StringUtils.isBlank(name)) {
      errors.add("nameError");
    }
    if (StringUtils.isBlank(fromEmailAddress)) {
      errors.add("emailAddressMissingError");
    } else if (!EmailValidator.getInstance().isValid(fromEmailAddress)) {
      errors.add("emailAddressInvalidError");
    }
    if (StringUtils.isBlank(note)) {
      errors.add("noteError");
    }
    return errors;
  }


  private static String formatUserInfo(HttpServletRequest request) {
    return getUserSessionAttributes(request).entrySet().stream()
        .map((Map.Entry<String, String> entry) -> (entry.getKey() + " ---> " + entry.getValue()))
        .collect(Collectors.joining("<br/>\n"));
  }

  public static Map<String, String> getUserSessionAttributes(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<String, String>();

    for (String headerName : Collections.list(request.getHeaderNames())) {
      List<String> headerValues = Collections.list(request.getHeaders(headerName));
      headers.put(headerName, headerValues.stream().collect(joinWithComma()));
    }

    headers.put("server-name", request.getServerName() + ":" + request.getServerPort());
    headers.put("remote-addr", request.getRemoteAddr());
    headers.put("local-addr", request.getLocalAddr() + ":" + request.getLocalPort());

    /*
     * Keeping this in case more values get passed from the client other than just the visible form
     * fields
     */
    for (String paramName : Collections.list(request.getParameterNames())) {
      String[] paramValues = request.getParameterValues(paramName);
      headers.put(paramName, Stream.of(paramValues).collect(joinWithComma()));
    }

    return headers;
  }

  private static Collector<CharSequence, ?, String> joinWithComma() {
    return Collectors.joining(",");
  }

}
