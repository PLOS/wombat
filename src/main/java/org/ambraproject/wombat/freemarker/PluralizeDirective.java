package org.ambraproject.wombat.freemarker;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;


public class PluralizeDirective implements TemplateDirectiveModel {
  public static Map<String, String> customPlurals = ImmutableMap.<String, String>builder()
      .put("Expression of Concern", "Expressions of Concern").build();

  public static String pluralize(String singular) {
    if (customPlurals.containsKey(singular)) {
      return customPlurals.get(singular);
    } else if (singular.endsWith("y")) {
      return (singular.substring(0, singular.length() - 1) + "ies");
    } else if (singular.endsWith("Y")) {
      return (singular.substring(0, singular.length() - 1) + "IES");
    } else if (Character.isUpperCase(singular.charAt(singular.length() - 1))) {
      return singular + "S";
    } else {
      return singular + "s";
    }
  }

  public static int getParamAsInteger(Map params, String key) throws TemplateModelException {
    Object obj = params.get(key);
    if (!(obj instanceof TemplateNumberModel)) {
      throw new TemplateModelException("The " + key + " parameter must be a number.");
    }
    return ((TemplateNumberModel) obj).getAsNumber().intValue();
  }

  public static String getParamAsString(Map params, String key) throws TemplateModelException {
    Object obj = params.get(key);
    if (!(obj instanceof TemplateScalarModel)) {
      throw new TemplateModelException("The " + key + " parameter must be a string.");
    }
    return ((TemplateScalarModel) obj).getAsString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars,
      TemplateDirectiveBody body) throws TemplateException, IOException {
    String value = getParamAsString(params, "value");
    int count = getParamAsInteger(params, "count");
    if (count > 1) {
      environment.getOut().write(pluralize(value));
    } else {
      environment.getOut().write(value);
    }
  }
}
