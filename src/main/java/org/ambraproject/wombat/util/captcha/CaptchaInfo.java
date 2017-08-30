package org.ambraproject.wombat.util.captcha;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@AutoValue
public abstract class CaptchaInfo {

  /** Returns the list of choices. */
  public abstract ImmutableMap<String, CaptchaAnswer> choices();

  /** Returns the valid choice. */
  public abstract String validChoice();

  /** Returns the field name. */
  public abstract String fieldName();

  /** Returns the audio field name. */
  public abstract String audioFieldName();

  /** Returns the audio answer. */
  public abstract CaptchaAnswer audioAnswer();

  /** Returns the choices as a list. */
  public List<CaptchaAnswer> choicesAsList() {
    final ImmutableMap<String, CaptchaAnswer> items = choices();
    if (items == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(items.values());
  }

  /** Returns the builder. */
  public static Builder builder() {
    return new AutoValue_CaptchaInfo.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    /** Sets the list of choices. */
    public abstract Builder setChoices(Map<String, CaptchaAnswer> choices);

    /** Sets the valid choice. */
    public abstract Builder setValidChoice(String validChoice);

    /** Sets the field name. */
    public abstract Builder setFieldName(String fieldName);

    /** Sets the audio field name. */
    public abstract Builder setAudioFieldName(String audioFieldName);

    /** Sets the audio answer. */
    public abstract Builder setAudioAnswer(CaptchaAnswer audioAnswer);

    /**
     * Builds the map from the list of choices.
     *
     * @param choices the list of choices
     *
     * @return The builder
     */
    public Builder setChoices(List<CaptchaAnswer> choices) {
      checkNotNull(choices, "'choices' cannot be null");
      ImmutableMap.Builder<String, CaptchaAnswer> mapper = ImmutableMap.builder();
      for (CaptchaAnswer answer : choices) {
        mapper.put(answer.obfuscatedName(), answer);
      }
      return setChoices(mapper.build());
    }

    /** Creates the <b>CaptchaInfo</b> instance. */
    public abstract CaptchaInfo build();
  }
}
