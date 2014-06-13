package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.controller.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Formats a path, relative to the root of a site, into an absolute link.
 * <p/>
 * If there is no loop var, render the link as the output of the macro. For example:
 * <pre>
 *   <link rel="stylesheet" type="text/css" href="<@siteLink path="resource/css/screen.css" />"/>
 * </pre>
 * The directive may be invoked with one loop var, in which case the directive will assign the link to that variable and
 * render the body with it. For example:
 * <pre>
 *   <@siteLink path="resource/css/screen.css" ; link>
 *     <link rel="stylesheet" type="text/css" href="${link}"/>
 *   </@siteLink>
 * </pre>
 */
public class SiteLinkDirective extends VariableLookupDirective<String> {

  @Autowired
  private SiteResolver siteResolver;

  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException {
    Object pathObj = params.get("path");
    if (!(pathObj instanceof TemplateScalarModel)) {
      throw new RuntimeException("path parameter required");
    }
    String path = ((TemplateScalarModel) pathObj).getAsString();
    return new SitePageContext(siteResolver, env).buildLink(path);
  }

}
