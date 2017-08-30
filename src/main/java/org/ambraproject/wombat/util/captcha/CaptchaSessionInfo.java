package org.ambraproject.wombat.util.captcha;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CaptchaSessionInfo implements Serializable {

  private static final long serialVersionUID = -234659669160894703L;

  private List<CaptchaAnswer> choices;
  private String validChoice;
  private String fieldName;
  private String audioFieldName;
  private CaptchaAnswer audioAnswer;

  public CaptchaSessionInfo(String fieldName, String validChoice, String audioFieldName,
      CaptchaAnswer audioAnswer, List<CaptchaAnswer> choices) {
    this.fieldName = fieldName;
    this.validChoice = validChoice;
    this.audioFieldName = audioFieldName;
    this.audioAnswer = audioAnswer;
    this.choices = new ArrayList<>(choices);
  }

  public CaptchaSessionInfo(String fieldName, String validChoice, String audioFieldName,
      CaptchaAnswer audioAnswer, CaptchaAnswer... choices) {
    this.fieldName = fieldName;
    this.validChoice = validChoice;
    this.audioFieldName = audioFieldName;
    this.audioAnswer = audioAnswer;
    this.choices = Arrays.asList(choices);
  }

  public List<CaptchaAnswer> getChoices() {
    return choices;
  }

  public String getValidChoice() {
    return validChoice;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getAudioFieldName() {
    return audioFieldName;
  }

  public CaptchaAnswer getAudioAnswer() {
    return audioAnswer;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("validChoice: ").append(fieldName).append(", ");
    builder.append("path: ").append(validChoice);
    return builder.toString();
  }
}
