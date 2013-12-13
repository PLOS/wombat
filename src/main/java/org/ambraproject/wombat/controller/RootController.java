package org.ambraproject.wombat.controller;

import com.google.common.io.Closer;
import org.ambraproject.wombat.config.SiteSet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles requests to the application root.
 */
@Controller
public class RootController extends WombatController {

  @Autowired
  private ServletContext servletContext;

  private String getResourceAsBase64(String path) throws IOException {
    Closer closer = Closer.create();
    byte[] bytes;
    try {
      InputStream stream = closer.register(servletContext.getResourceAsStream(path));
      bytes = IOUtils.toByteArray(stream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    return Base64.encodeBase64String(bytes);
  }

  /**
   * Show a page in response to the application root.
   * <p/>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home(Model model) throws Exception {
    model.addAttribute("siteKeys", siteSet.getSiteKeys());

    /*
     * Must supply this image data embedded (if at all), because there's no URL at which we could put it
     * without colliding with the site namespace.
     */
    model.addAttribute("imageCode", getResourceAsBase64("/WEB-INF/views/app/wombat.jpg"));

    return "//approot";
  }

}
