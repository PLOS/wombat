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
