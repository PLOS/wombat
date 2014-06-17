package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class AppLinkDirective extends VariableLookupDirective<String> {

  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException {
    Object pathObj = params.get("path");
    if (!(pathObj instanceof TemplateScalarModel)) {
      throw new RuntimeException("path parameter required");
    }

    String path = ((TemplateScalarModel) pathObj).getAsString();

    HttpServletRequest request = ((HttpRequestHashModel) env.getDataModel().get("Request")).getRequest();
    return request.getContextPath() + "/" + path;
  }

}
