package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * A pseudo-controller that is dispatched to by {@link org.ambraproject.wombat.controller.ExceptionHandlerAdvisor}.
 * Autowired as a normal Spring bean.
 */
public class AppRootPage {

  private static final Logger log = LoggerFactory.getLogger(AppRootPage.class);

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private ServletContext servletContext;

  /**
   * Show a page in response to the application root.
   * <p/>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  ModelAndView serveAppRoot() {
    ModelAndView mav = new ModelAndView("//approot");
    mav.addObject("siteKeys", siteSet.getSiteKeys());
    try {
      mav.addObject("imageCode", getResourceAsBase64("/WEB-INF/themes/root/app/wombat.jpg"));
    } catch (IOException e) {
      log.error("Error displaying root page image", e);
    }
    return mav;
  }

  private String getResourceAsBase64(String path) throws IOException {
    byte[] bytes;
    try (InputStream stream = servletContext.getResourceAsStream(path)) {
      bytes = IOUtils.toByteArray(stream);
    }
    return Base64.encodeBase64String(bytes);
  }

}
