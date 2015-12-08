package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;

/**
 * Interface for a captcha implementation
 */
public interface CaptchaService {

  /**
   * Validate the given challenge and response
   *
   * @param ip the current user's IP address
   * @param challenge challenge (from the html form snippet)
   * @param response response (from the html form snippet)
   *
   * @return true if the captcha is valid
   *
   * @throws Exception
   */
  public boolean validateCaptcha(Site site, String ip, String challenge, String response) throws IOException;

  /**
   * @return Returns a captchaHTML block to insert into a web page
   */
  public String getCaptchaHTML(Site site) throws IOException;

  public String getPublicKey(Site site) throws IOException;
}

