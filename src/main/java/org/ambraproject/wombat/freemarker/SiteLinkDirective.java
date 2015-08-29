package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.config.site.HandlerDirectory;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Formats a path, relative to the root of a site, into an absolute link.
 * <p/>
 * If there is no loop var, render the link as the output of the macro. For example:
 * <pre>
 *   <link rel="stylesheet" type="text/css" href="<@siteLink path="resource/css/screen.css" />"/>
 * </pre>
 * The directive may be invoked with one loop var, in which case the directive will assign the link to that variable and
 * render the body with it. For example:
 * <pre>
 *   <@siteLink path="resource/css/screen.css" ; link>
 *     <link rel="stylesheet" type="text/css" href="${link}"/>
 *   </@siteLink>
 * </pre>
 */
public class SiteLinkDirective extends VariableLookupDirective<String> {

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private HandlerDirectory handlerDirectory;

  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException, IOException {
    String path = getStringValue(params.get("path"));
    String targetJournal = getStringValue(params.remove("journalKey")); // never used for url creation so remove
    String handlerName = getStringValue(params.remove("handlerName")); // never used for url creation so remove

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);
    Site site = sitePageContext.getSite();

    Link.Factory linkFactory = (targetJournal == null)
        ? Link.toLocalSite(site)
        : Link.toForeignSite(site, targetJournal, siteSet);
    final Link link;
    if (handlerName != null) {
      link = linkFactory.toPattern(handlerDirectory, handlerName, params);
    } else if (path != null) {
      link = linkFactory.toPath(path);
    } else {
      throw new RuntimeException("Either a path or handlerName parameter is required");
    }

    return link.get(sitePageContext.getRequest());
  }

  private String getStringValue(Object valueObj) throws TemplateModelException {
    return valueObj instanceof TemplateScalarModel ? ((TemplateScalarModel) valueObj).getAsString() : null;
  }

}
