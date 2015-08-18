package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates FreeMarker environment info in order to represent site-specific information.
 */
public class SitePageContext {

  private static final Logger log = LoggerFactory.getLogger(SitePageContext.class);
  private final Site site;
  private final HttpServletRequest request;
  private final Environment environment;
  private final HandlerMappingConfiguration handlerMappingConfig;
  private static final String PATH_TEMPLATE_VAR = "path";
  private static final String SITE_TEMPLATE_VAR = "site";
  private static final String DEFAULT_URL_PATH = "/";

  private HttpServletRequest extractRequest() throws TemplateModelException {
    return ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
  }

  private Site findSite(SiteResolver siteResolver)
      throws TemplateModelException {
    // Recover it from the model if possible
    TemplateModel site = environment.getDataModel().get("site");
    if (site instanceof BeanModel) {
      Object wrappedObject = ((BeanModel) site).getWrappedObject();
      if (wrappedObject instanceof Site) {
        return (Site) wrappedObject;
      }
    }

    // Else, delegate to the resolver
    return siteResolver.resolveSite(request);
  }

  public SitePageContext(SiteResolver siteResolver, HandlerMappingConfiguration handlerMappingConfig,
                         Environment environment) {
    try {
      this.environment = environment;
      this.request = extractRequest();
      this.site = findSite(siteResolver);
      this.handlerMappingConfig = handlerMappingConfig;
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  public Site getSite() {
    return site;
  }

  public String buildLink(String path) {
    return site.getRequestScheme().buildLink(request, path);
  }

  public String buildLink(SiteSet siteSet, String journalKey, String path) {
    try {
      Site targetSite = site.getTheme().resolveForeignJournalKey(siteSet, journalKey);
      return targetSite.getRequestScheme().buildLink(extractRequest(), path);
    } catch (Exception e) {
      log.error("Error building link for path={}. Error detail:{}", path, e.getMessage());
      return buildLink(path);
    }
  }

  public String buildLink(String handlerName, Map<String, Object> params) {
    return buildLinkToSite(site, handlerName, params);
  }

  public String buildLink(SiteSet siteSet, String journalKey, String handlerName, Map<String, Object> params) {
    try {
      Site targetSite = site.getTheme().resolveForeignJournalKey(siteSet, journalKey);
      return buildLinkToSite(targetSite, handlerName, params);
    } catch (Exception e) {
      log.error("Error building link for journalKey:handlerName={}. Error detail:{}",
              journalKey.concat(":").concat(handlerName), e.getMessage());
      return buildLinkToSite(site, handlerName, params);
    }
  }

  private String buildLinkToSite(Site targetSite, String handlerName, final Map<String, Object> params) {
    try {
      Set<String> urlPatterns = handlerMappingConfig.getValidPatternsForSite(handlerName, targetSite.getKey());
      if (urlPatterns.isEmpty()) {
        log.error("Error building link for site:handlerName={}. Error detail: No patterns returned from handler mapping");
        return buildLink(DEFAULT_URL_PATH);
      }
      String urlPattern = urlPatterns.iterator().next(); // TODO: Sort via AntPathMatcher.AntPatternComparator first?
      if (urlPatterns.size() > 1) {
        // Technically there is no reason a controller can't be reached using several namespaces, so it seemed best
        // to make this a warning instead of an error condition, however, it's probably best practice not to do so.
        log.warn("Warning building link for site:handlerName={}. Warning detail: {}",
                targetSite.toString().concat(":").concat(handlerName), String.valueOf(urlPatterns.size()).
                        concat(" URL patterns returned from handler mapping: ".concat(urlPatterns.toString())).
                        concat(" Used pattern: ").concat(urlPattern));
      }

      // replace * or ** with the path URI template var to allow expansion when using ANT-style wildcards
      // TODO: support multiple wildcards using {path__0}, {path__1}?
      urlPattern = urlPattern.replaceFirst("\\*+","{".concat(PATH_TEMPLATE_VAR).concat("}"));

      boolean pathIncludesSiteToken = urlPattern.matches(".*\\{".concat(SITE_TEMPLATE_VAR).concat("\\}.*"));

      UriComponentsBuilder builder = ServletUriComponentsBuilder.fromPath(urlPattern);
      UriComponents.UriTemplateVariables uriVariables = new UriComponents.UriTemplateVariables() {
        @Override
        public Object getValue(String name) {
          if (!params.containsKey(name)) {
            if (name.equals(SITE_TEMPLATE_VAR)) {
              return site.getKey();
            } else {
              throw new RuntimeException("Missing required parameter " + name);
            }
          }
          return params.remove(name); // consume params as they are used
        }
      };

      String path = builder.build().expand(uriVariables).encode().toString();

      if (!params.isEmpty()) {
        // any parameters not consumed by the path variable assignment process are assumed to be query params
        UrlParamBuilder paramBuilder = UrlParamBuilder.params();
        for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
          paramBuilder.add(paramEntry.getKey(), paramEntry.getValue().toString());
        }
        path = path.concat("?").concat(paramBuilder.format());
      }

      return targetSite.getRequestScheme().buildLink(extractRequest(), path, pathIncludesSiteToken);

    } catch (Exception e) {
      log.error("Error building link for site:handlerName={}. Error detail:{}",
              targetSite.toString().concat(":").concat(handlerName), e.getMessage());
      return buildLink(DEFAULT_URL_PATH);
    }
  }

}
