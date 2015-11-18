package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.EmailMessage;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class FeedbackController {

  @Autowired
  private FreeMarkerConfig freeMarkerConfig;
  @Autowired
  private FreemarkerMailService freemarkerMailService;
  @Autowired
  private JavaMailSender javaMailSender; // TODO

  private static Map<String, Object> getFeedbackConfig(Site site) {
    try {
      return site.getTheme().getConfigMap("feedback");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void validateFeedbackConfig(Site site) {
    if (getFeedbackConfig(site).get("destination") == null) {
      throw new NotFoundException("Feedback is not configured with a destination address");
    }
  }

  @RequestMapping(name = "feedback", value = "/feedback", method = RequestMethod.GET)
  public String serveFeedbackPage(@SiteParam Site site) {
    validateFeedbackConfig(site);
    return site + "/ftl/feedback"; // TODO
  }

  @RequestMapping(name = "feedbackPost", value = "/feedback", method = RequestMethod.POST)
  public ResponseEntity<?> receiveFeedback(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @RequestParam("page") String page,
                                           @RequestParam("fromEmailAddress") String fromEmailAddress,
                                           @RequestParam("note") String note,
                                           @RequestParam("subject") String subject,
                                           @RequestParam("name") String name)
      throws IOException, MessagingException {
    validateFeedbackConfig(site);
    model.addAttribute("page", page);
    model.addAttribute("fromEmailAddress", fromEmailAddress);
    model.addAttribute("note", note);
    model.addAttribute("name", name);

    if (Strings.isNullOrEmpty(subject)) {
      subject = (String) getFeedbackConfig(site).get("defaultSubject");
    }
    model.addAttribute("subject", subject);

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
    return new ResponseEntity<>(HttpStatus.CREATED);
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
