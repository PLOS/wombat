package org.ambraproject.wombat;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private Gson gson;
  @Autowired
  private URL soa;

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home(Locale locale, Model model) throws IOException, URISyntaxException {
    logger.info("Welcome home! The client locale is {}.", locale);

    Date date = new Date();
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

    String formattedDate = dateFormat.format(date);
    model.addAttribute("serverTime", formattedDate);

    // Fetch something from the service
    // (Yes, calls to the SOA in the controller layer would be very bad form if this weren't just a proof of concept.)
    URL target = new URL(soa, "config");
    HttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(target.toURI());
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      InputStream instream = entity.getContent();
      try {
        String config = IOUtils.toString(instream);
        model.addAttribute("testObject", config);
      } finally {
        instream.close();
      }
    }

    return "home";
  }

}
