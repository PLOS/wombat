package org.ambraproject.wombat.freemarker;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Pattern;

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
    String abbreviation = abbreviate(givenNames.toString());
    env.getOut().write(abbreviation);
  }

  private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE);
  private static final Pattern GIVEN_NAME_PATTERN = Pattern.compile(".*\\p{Pd}\\p{L}.*");
  private static final Pattern DASH_PATTERN = Pattern.compile("\\p{Pd}");

  private static String abbreviate(String rawGivenNameString) {
    Iterable<String> givenNames = WHITESPACE_SPLITTER.split(rawGivenNameString);
    StringBuilder abbreviation = new StringBuilder();
    for (String givenName : givenNames) {
      if (givenName.length() > 0) {
        if (GIVEN_NAME_PATTERN.matcher(givenName).matches()) {
          // Handle names with dash
          String[] sarr = DASH_PATTERN.split(givenName);
          for (int i = 0; i < sarr.length; i++) {
            if (i > 0) {
              abbreviation.append('-');
            }

            if (sarr[i].length() > 0) {
              abbreviation.append(sarr[i].charAt(0));
            }
          }
        } else {
          abbreviation.append(givenName.charAt(0));
        }
      }
    }

    return abbreviation.toString();
  }

}
