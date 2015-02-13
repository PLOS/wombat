package org.ambraproject.wombat.freemarker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.FetchHtmlService;
import org.ambraproject.wombat.util.HtmlAttributeTransformation;
import org.ambraproject.wombat.util.HtmlElementSubstitution;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Map;

public class FetchHtmlDirective implements TemplateDirectiveModel {
  private static final Logger log = LoggerFactory.getLogger(FetchHtmlDirective.class);

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private FetchHtmlService fetchHtmlService;

  private static final String SUBST_ATTR_NAME = "data-subst";

  private static final Map<String, EnumSet<HtmlAttributeTransformation>> attributeTransforms = ImmutableMap.of(
          "homepage", EnumSet.of(HtmlAttributeTransformation.IMAGE,
                  HtmlAttributeTransformation.ARTICLE),
          "staticContent", EnumSet.of(HtmlAttributeTransformation.IMAGE)
  );

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object typeObj = params.get("type");
    if (typeObj == null) {
      throw new TemplateModelException("type parameter required");
    }

    Object pathObj = params.get("path");
    if (pathObj == null) {
      throw new TemplateModelException("path parameter required");
    }

    String pageType = typeObj.toString();

    EnumSet<HtmlAttributeTransformation> transformations = attributeTransforms.get(pageType);
    if (transformations == null) {
      throw new TemplateModelException(String.format("type parameter '%s' is invalid.", pageType));
    }

    ImmutableList<HtmlElementSubstitution> substitutions = HtmlElementSubstitution.buildList(body, SUBST_ATTR_NAME);

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);

    try (Reader html = fetchHtmlService.readHtml(sitePageContext, pageType, pathObj.toString(),
            transformations, substitutions)) {
      IOUtils.copy(html, env.getOut());
    } catch (EntityNotFoundException e) {
      // TODO: Allow themes to provide custom, user-visible error blocks
      log.error("Could not retrieve HTML of type \"{}\" at path \"{}\"", typeObj, pathObj);
    }
  }


}
