package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.util.Citations;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Applies standard formatting for abbreviations of authors' given names. Abbreviates names to initials, with hyphens
 * (or other dash characters) between names preserved as hyphens between initials.
 */
public class AbbreviatedNameDirective implements TemplateDirectiveModel {

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    StringWriter givenNames = new StringWriter();
    body.render(givenNames);
    String abbreviation = Citations.abbreviateAuthorGivenNames(givenNames.toString());
    env.getOut().write(abbreviation);
  }

}
