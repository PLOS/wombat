package org.ambraproject.wombat.util.captcha;

import java.util.List;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;


@AutoValue
public abstract class CaptchaFrontEndData {

  /** Returns the image name. */
  public abstract String imageName();

  /** Returns the image field name. */
  public abstract String imageFieldName();

  /** Returns the list of acceptable choices. */
  public abstract ImmutableList<String> values();

  /** Returns the audio field name. */
  public abstract String audioFieldName();

  /** Returns the builder. */
  public static Builder builder() {
    return new AutoValue_CaptchaFrontEndData.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    /** Sets the image name. */
    public abstract Builder setImageName(String imageName);

    /** Sets the image field name. */
    public abstract Builder setImageFieldName(String imageFieldName);

    /** Sets the list of acceptable choices. */
    public abstract Builder setValues(List<String> values);

    /** Sets the audio field name. */
    public abstract Builder setAudioFieldName(String audioFieldName);

    /** Returns the builder to instantiate the <b>CaptchaFrontEndData</b>. */
    public abstract CaptchaFrontEndData build();
  }
}
