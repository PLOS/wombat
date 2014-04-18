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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class FetchHtmlDirective implements TemplateDirectiveModel {

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

    try (InputStream stream = leopardService.requestStream(pathObj.toString());
         Reader reader = new InputStreamReader(stream)) {
      IOUtils.copy(reader, env.getOut());
    }
  }

}
