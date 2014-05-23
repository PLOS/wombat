package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.controller.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Formats a path, relative to the root of a site, into an absolute link.
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
    env.getOut().write(link);
  }

}
