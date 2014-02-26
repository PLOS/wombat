package org.ambraproject.wombat.freemarker;

import com.google.common.base.Preconditions;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Generates an insecure random integer, between the params "minValue" (inclusive) and "maxValue" (exclusive).
 */
public class RandomIntegerDirective implements TemplateDirectiveModel {

  private static final Integer DEFAULT_MIN = 0;
  private static final Integer DEFAULT_MAX = 10000;

  private final Random random = new Random(); // needs SecureRandom if these numbers are used cryptographically

  /**
   * Interpret a FreeMarker number as a primitive integer.
   *
   * @param number       a FreeMarker number value
   * @param defaultValue the value to use if {@code number == null}
   * @return the integer value
   * @throws TemplateModelException
   * @throws java.lang.ClassCastException if {@code number} is non-null and not a FreeMarker number
   */
  private static int asInteger(Object number, int defaultValue) throws TemplateModelException {
    if (number == null) return defaultValue;
    return ((TemplateNumberModel) number).getAsNumber().intValue();
  }

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    int minValue = asInteger(params.get("minValue"), DEFAULT_MIN);
    int maxValue = asInteger(params.get("maxValue"), DEFAULT_MAX);
    Preconditions.checkArgument(minValue < maxValue);

    int randomValue = random.nextInt(maxValue - minValue) + minValue;

    env.getOut().write(String.valueOf(randomValue));
  }

}
