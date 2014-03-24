package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.util.BuildInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

public class BuildInfoDirective implements TemplateDirectiveModel {

  @Autowired
  private BuildInfoService buildInfoService;

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    String component = params.get("component").toString();
    BuildInfo info;
    switch (component) {
      case "webapp":
        info = buildInfoService.getWebappBuildInfo();
        break;
      case "service":
        info = buildInfoService.getServiceBuildInfo();
        break;
      default:
        throw new TemplateModelException("component required");
    }

    String value;
    if (info == null) {
      value = null;
    } else {
      String field = params.get("field").toString();
      switch (field) {
        case "version":
          value = info.getVersion();
          break;
        case "date":
          value = info.getDate();
          break;
        case "user":
          value = info.getUser();
          break;
        default:
          throw new TemplateModelException("field required");
      }
    }

    env.getOut().write(value != null ? value : "?");
  }

}
