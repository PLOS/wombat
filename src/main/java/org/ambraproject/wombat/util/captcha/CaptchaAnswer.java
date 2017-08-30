package org.ambraproject.wombat.util.captcha;

import com.google.auto.value.AutoValue;
import com.sun.istack.Nullable;


@AutoValue
public abstract class CaptchaAnswer {

  public CaptchaAnswer() {}

  /** Returns the value. */
  public abstract String value();

  /** Returns the path to the image. */
  public abstract String path();

  /** Returns the hashed answer. */
  public abstract @Nullable String obfuscatedName();

  /** Returns the builder. */
  public static Builder builder() {
    return new AutoValue_CaptchaAnswer.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    public abstract Builder setValue(String value);

    public abstract Builder setPath(String path);

    public abstract Builder setObfuscatedName(String obfuscatedName);

    /** Creates the <b>CaptchaAnswer</b> instance. */
    public abstract CaptchaAnswer build();
  }
}
