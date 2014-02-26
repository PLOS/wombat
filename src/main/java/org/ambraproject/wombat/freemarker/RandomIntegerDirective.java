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

public class RandomIntegerDirective implements TemplateDirectiveModel {

  private static final Integer DEFAULT_MIN = 0;
  private static final Integer DEFAULT_MAX = 10000;

  private final Random random = new Random();

  private static Integer asInteger(Object number, Integer defaultValue) throws TemplateModelException {
    if (number == null) return defaultValue;
    return ((TemplateNumberModel) number).getAsNumber().intValue();
  }

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Integer minValue = asInteger(params.get("minValue"), DEFAULT_MIN);
    Integer maxValue = asInteger(params.get("maxValue"), DEFAULT_MAX);
    Preconditions.checkArgument(minValue < maxValue);

    int randomValue = random.nextInt(maxValue - minValue) + minValue;

    env.getOut().write(String.valueOf(randomValue));
  }

}
