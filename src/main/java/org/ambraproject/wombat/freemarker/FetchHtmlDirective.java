package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.service.remote.LeopardService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class FetchHtmlDirective implements TemplateDirectiveModel {

  /*
   * This directive is capable of fetching HTML from any arbitrary HTTP endpoint. In FreeMarker code, an invocation
   * looks like <@fetchHtml path="..." /> and does not specify which service the path applies to.
   *
   * To keep things simple, the directive class is currently hard-coded to call only the leopardService bean, which
   * creates an implicit link between "fetchHtml" in FreeMarker code and "leopardService" in the config code. The link
   * is not intuitive, and will need to be generalized if this directive must ever connect to more than one service.
   *
   * However, we are choosing not to complicate it until we have a motivating case. So, leopardService remains
   * hard-coded as the one and only way to "fetch HTML" for now.
   */

  @Autowired
  private LeopardService leopardService;

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
    if (leopardService == null) {
      throw new RuntimeException("Leopard service is not configured");
    }

    Object pathObj = params.get("path");
    if (pathObj == null) {
      throw new TemplateModelException("path parameter required");
    }

    try (Reader reader = leopardService.readHtml(pathObj.toString())) {
      IOUtils.copy(reader, env.getOut());
    }
  }

}
