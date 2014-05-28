package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.controller.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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
public class SiteLinkDirective implements TemplateDirectiveModel {

  @Autowired
  private SiteResolver siteResolver;

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object pathObj = params.get("path");
    if (!(pathObj instanceof TemplateScalarModel)) {
      throw new RuntimeException("path parameter required");
    }
    String path = ((TemplateScalarModel) pathObj).getAsString();
    String link = new SitePageContext(siteResolver, env).buildLink(path);

    if (loopVars.length == 0) {
      env.getOut().write(link);
    } else if (loopVars.length == 1) {
      loopVars[0] = env.getObjectWrapper().wrap(link);
      body.render(env.getOut());
    } else {
      throw new TemplateModelException("SiteLinkDirective does not take more than 1 loopVar");
    }
  }

}
