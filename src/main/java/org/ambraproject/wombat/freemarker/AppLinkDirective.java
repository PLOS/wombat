package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class AppLinkDirective implements TemplateDirectiveModel {

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {

    Object pathObj = params.get("path");
    if (!(pathObj instanceof TemplateScalarModel)) {
      throw new RuntimeException("path parameter required");
    }

    String path = ((TemplateScalarModel) pathObj).getAsString();

    HttpServletRequest request = ((HttpRequestHashModel) env.getDataModel().get("Request")).getRequest();
    String link = request.getContextPath() + "/" + path;

    if (loopVars.length == 0) {
      env.getOut().write(link);
    } else if (loopVars.length == 1) {
      loopVars[0] = env.getObjectWrapper().wrap(link);
      body.render(env.getOut());
    } else {
      throw new TemplateModelException("AppLinkDirective does not take more than 1 loopVar");
    }

  }
}
