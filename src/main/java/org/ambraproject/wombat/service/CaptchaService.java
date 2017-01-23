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

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.Optional;

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
  public String getCaptchaHtml(Site site, Optional<String> captchaTheme) throws IOException;

  public String getPublicKey(Site site) throws IOException;
}

