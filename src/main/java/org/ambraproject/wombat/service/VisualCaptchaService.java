package org.ambraproject.wombat.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Optional;

import org.ambraproject.wombat.config.site.Site;

public class VisualCaptchaService implements CaptchaService {

  @Override
  public boolean validateCaptcha(Site site, String ip, String challenge, String response)
      throws IOException {
    checkNotNull(challenge, "The challenge cannot be null");
    checkNotNull(response, "The response cannot be null");

    return false;
  }

  @Override
  public String getCaptchaHtml(Site site, Optional<String> captchaTheme) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPublicKey(Site site) throws IOException {
    checkNotNull(site, "Site instance cannot be null");

    final String publicKey = site.getTheme().getConfigMap("captcha").get("privateKey").toString();
    return publicKey;
  }
}
