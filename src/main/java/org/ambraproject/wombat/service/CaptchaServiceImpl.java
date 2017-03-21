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

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Implementation for a captcha
 *
 * Uses google ReCaptcha.
 */
public class CaptchaServiceImpl implements CaptchaService {

  /** {@inheritDoc}
   */
  @Override
  public boolean validateCaptcha(Site site, String ip, String challenge, String response)
      throws IOException {
    ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
    String privateKey = getPrivateKey(site);

    if(privateKey == null) {
      throw new RuntimeException("No private key specified for recaptcha to be enabled.");
    }

    reCaptcha.setPrivateKey(privateKey);

    return reCaptcha.checkAnswer(ip, challenge, response).isValid();
  }

  /** {@inheritDoc}
   */
  @Override
  public String getCaptchaHtml(Site site, Optional<String> captchaTheme) throws IOException {
    String publicKey = getPublicKey(site);
    String privateKey = getPrivateKey(site);

    if(publicKey == null || privateKey == null) {
      throw new RuntimeException("No keys specified for recaptcha to be enabled.");
    }

    ReCaptcha c = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);

    Properties properties = new Properties();
    properties.setProperty("theme", captchaTheme.orElse("white"));
    return c.createRecaptchaHtml(null, properties);
  }

  @Override
  public String getPublicKey(Site site) throws IOException {
    return site.getTheme().getConfigMap("captcha").get("publicKey").toString();
  }

  private String getPrivateKey(Site site) throws IOException {
    return site.getTheme().getConfigMap("captcha").get("privateKey").toString();
  }
}
