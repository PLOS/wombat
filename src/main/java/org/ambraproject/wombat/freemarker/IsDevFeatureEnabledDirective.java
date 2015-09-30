package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Tests whether a given development feature is enabled for dev and test purposes
 */
public class IsDevFeatureEnabledDirective extends VariableLookupDirective<Boolean> {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  protected Boolean getValue(Environment env, Map params) throws TemplateModelException, IOException {
    Object featureObj = params.get("feature");
    if (!(featureObj instanceof TemplateScalarModel)) {
      throw new RuntimeException("feature parameter required");
    }
    String feature = ((TemplateScalarModel) featureObj).getAsString();

    return runtimeConfiguration.getEnabledDevFeatures().contains(feature);
  }

}
