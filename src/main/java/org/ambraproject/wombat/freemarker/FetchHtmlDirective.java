package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.service.remote.FetchHtmlService;
import org.ambraproject.wombat.service.remote.StoredHomepageService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class FetchHtmlDirective implements TemplateDirectiveModel {

  @Autowired
  private StoredHomepageService storedHomepageService;

  private FetchHtmlService getService(String typeKey) {
    FetchHtmlService service;
    switch (typeKey) {
      case "homepage":
        service = storedHomepageService;
        break;
      default:
        throw new IllegalArgumentException("fetchHtml type param not matched to a service: " + typeKey);
    }
    if (service == null) {
      throw new IllegalStateException("Service is not configured for fetchHtml type param: " + typeKey);
    }
    return service;
  }

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
    Object typeObj = params.get("type");
    if (typeObj == null) {
      throw new TemplateModelException("type parameter required");
    }
    FetchHtmlService service = getService(typeObj.toString());

    Object pathObj = params.get("path");
    if (pathObj == null) {
      throw new TemplateModelException("path parameter required");
    }

    try (Reader html = service.readHtml(pathObj.toString())) {
      IOUtils.copy(html, env.getOut());
    }
  }

}
