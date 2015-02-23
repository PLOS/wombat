package org.ambraproject.wombat.freemarker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public class FetchHtmlDirective implements TemplateDirectiveModel {
  private static final Logger log = LoggerFactory.getLogger(FetchHtmlDirective.class);

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private EditorialContentService editorialContentService;

  private static final String SUBST_ATTR_NAME = "data-subst";

  private static final ImmutableSetMultimap<String, HtmlAttributeTransformation> attributeTransforms =
          ImmutableSetMultimap.of(
                  "homepage", HtmlAttributeTransformation.IMAGE,
                  "homepage", HtmlAttributeTransformation.ARTICLE,
                  "siteContent", HtmlAttributeTransformation.IMAGE,
                  "siteContent", HtmlAttributeTransformation.LINK,
                  "siteContent", HtmlAttributeTransformation.ASSET
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

    Set<HtmlAttributeTransformation> transformations = attributeTransforms.get(pageType);
    if (transformations == null) {
      throw new TemplateModelException(String.format("type parameter '%s' is invalid.", pageType));
    }

    ImmutableList<HtmlElementSubstitution> substitutions = HtmlElementSubstitution.buildList(body, SUBST_ATTR_NAME);

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);

    try (Reader html = editorialContentService.readHtml(sitePageContext, pageType, pathObj.toString(),
            transformations, substitutions)) {
      IOUtils.copy(html, env.getOut());
    } catch (EntityNotFoundException e) {
      // TODO: Allow themes to provide custom, user-visible error blocks
      log.error("Could not retrieve HTML of type \"{}\" at path \"{}\"", typeObj, pathObj);
    }
  }


}
