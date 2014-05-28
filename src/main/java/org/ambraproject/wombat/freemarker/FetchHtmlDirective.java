package org.ambraproject.wombat.freemarker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.FetchHtmlService;
import org.ambraproject.wombat.service.remote.StoredHomepageService;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FetchHtmlDirective implements TemplateDirectiveModel {
  private static final Logger log = LoggerFactory.getLogger(FetchHtmlDirective.class);

  @Autowired
  private SiteResolver siteResolver;
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
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object typeObj = params.get("type");
    if (typeObj == null) {
      throw new TemplateModelException("type parameter required");
    }
    FetchHtmlService service = getService(typeObj.toString());

    Object pathObj = params.get("path");
    if (pathObj == null) {
      throw new TemplateModelException("path parameter required");
    }

    ImmutableList<ElementSubstitution> substitutions = buildSubstitutions(body);

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);
    try (Reader html = service.readHtml(sitePageContext, pathObj.toString(), substitutions)) {
      IOUtils.copy(html, env.getOut());
    } catch (EntityNotFoundException e) {
      // TODO: Allow themes to provide custom, user-visible error blocks
      log.error("Could not retrieve HTML of type \"{}\" at path \"{}\"", typeObj, pathObj);
    }
  }

  private static ImmutableList<ElementSubstitution> buildSubstitutions(TemplateDirectiveBody body)
      throws IOException, TemplateException {
    if (body == null) return ImmutableList.of();
    StringWriter bodyHtml = new StringWriter();
    body.render(bodyHtml);
    Document bodyDoc = Jsoup.parseBodyFragment(bodyHtml.toString());

    Elements substElements = bodyDoc.getElementsByAttribute(SUBST_ATTR_KEY);
    Collection<ElementSubstitution> substitutionObjs = new ArrayList<>(substElements.size());
    for (Element substElement : substElements) {
      String substTarget = substElement.attr(SUBST_ATTR_KEY);
      substElement.removeAttr(SUBST_ATTR_KEY).attr("id", substTarget);
      substitutionObjs.add(new ElementSubstitution(substElement));
    }
    return ImmutableList.copyOf(substitutionObjs);
  }

  private static final String SUBST_ATTR_KEY = "data-subst";

  /**
   * Encapsulates one element to be inserted into a document, replacing an element with a particular ID.
   */
  public static class ElementSubstitution {
    private final Element replacementElement;

    private ElementSubstitution(Element replacementElement) {
      this.replacementElement = Preconditions.checkNotNull(replacementElement);
    }

    /**
     * Modify a document by replacing an element with a particular ID with a new element.
     *
     * @param document the document to modify
     */
    public void substitute(Document document) {
      Element elementToReplace = document.getElementById(replacementElement.id());
      if (elementToReplace != null) {
        elementToReplace.replaceWith(replacementElement);
      }
    }
  }

}
