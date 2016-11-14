package org.ambraproject.wombat.freemarker;

import com.google.common.collect.ListMultimap;
import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Creates a link to a page within a site.
 * <p/>
 * The returned link can be either printed as page output or assigned to a loopvar. See documentation for {@link
 * VariableLookupDirective}.
 * <p/>
 * By default, the link is to a page within the same site. If a {@code journalKey} argument is provided, the link will
 * resolve to another site with that journal key.
 * <p/>
 * The link may be built directly to a path, with a {@code path} argument. Or, it may link dynamically to a request
 * handler to generate a path mapped to the handler, if it has a {@code handlerName} argument. Each invocation must have
 * a {@code path} or {@code handlerName} argument, and must not have both.
 * <p/>
 * If the invocation has a {@code handlerName} argument, the directive will resolve the pattern configured for that
 * handler, which may contain variables or wildcards that describe a set of request URLs. If it does, in order to narrow
 * it down to the single URL to build, you must provide additional arguments:
 * <ul>
 *   <li>
 * <b>{@code pathVariables}:</b> A map from path variables (in curly braces) to the values to substitute for them. Must
 * contain a matching key for each path variable name in the pattern.
 *   </li>
 *   <li>
 * <b>{@code queryParameters}:</b> A map of key-value pairs to be appended as query parameters to the end of the URL. If
 * a map value is a sequence, append multiple query parameters with the same name and those values. For a parameter name
 * with no value, use an empty string as the value.
 *   </li>
 *   <li>
 * <b>{@code wildcardValues}:</b> A sequence of raw strings for each wildcard ({@code *} or {@code **}) in the pattern,
 * to be substituted in the same order. Should be omitted (or an empty sequence) if the pattern has no wildcards. If
 * there is exactly one wildcard, may be a non-sequence value, which is treated as a sequence of 1.
 *   </li>
 * </ul>
 * <p/>
 * Any of these arguments may be omitted, in which case they are treated as empty.
 * <p/>
 * For example, the directive may be invoked as follows:
 * <pre>
 *   &lt;@siteLink
 *           handlerName="serveThing" journalKey="TopologyToday"
 *           pathVariables={"section": "graphs"}
 *           wildcardValues=["resources/style.css"]
 *           queryParameters={"id": 224, "type": ["torus", "mobius"], "minimal": ""}
 *       ; link>
 *     &lt;a href="${link}">Go!&lt;/a>
 *   &lt;/@siteLink>
 * </pre>
 * Suppose that {@code "serveThing"}, on the {@code "TopologyToday"} journal, is mapped to the following pattern:
 * <pre>
 *   /article/{section}/file/**
 * </pre>
 * Then, the final output would be:
 * <pre>
 *   &lt;a href="/article/graphs/file/resources/style.css?id=224&amp;type=torus&amp;type=mobius&amp;minimal">Go!&lt;/a>
 * </pre>
 */
public class SiteLinkDirective extends VariableLookupDirective<String> {

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException, IOException {
    String path = getStringValue(params.get("path"));
    String targetJournal = getStringValue(params.get("journalKey"));
    String handlerName = getStringValue(params.get("handlerName"));
    boolean absoluteLink = TemplateModelUtil.getBooleanValue((TemplateModel) params.get("absoluteLink"));

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);
    Site site = sitePageContext.getSite();

    Link.Factory linkFactory = (targetJournal == null)
        ? (absoluteLink ? Link.toAbsoluteAddress(site) : Link.toLocalSite(site))
        : Link.toForeignSite(site, targetJournal, siteSet);
    final Link link;
    if (handlerName != null) {
      Map<String, ?> variables = TemplateModelUtil.getAsMap((TemplateModel) params.get("pathVariables"));
      ListMultimap<String, ?> queryParameters = TemplateModelUtil.getAsMultimap((TemplateModel) params.get("queryParameters"));
      List<?> wildcardValues = TemplateModelUtil.getAsList((TemplateModel) params.get("wildcardValues"));
      try {
        link = linkFactory.toPattern(requestMappingContextDictionary, handlerName,
            variables, queryParameters, wildcardValues);
      } catch (Link.PatternNotFoundException e) {
        if (TemplateModelUtil.getBooleanValue((TemplateModel) params.get("failQuietly"))) {
          return null;
        } else {
          throw new RuntimeException(e);
        }
      }
    } else if (path != null) {
      link = linkFactory.toPath(path);
    } else {
      throw new RuntimeException("Either a path or handlerName parameter is required");
    }

    return link.get(sitePageContext.getRequest());
  }

  private static String getStringValue(Object valueObj) throws TemplateModelException {
    return valueObj instanceof TemplateScalarModel ? ((TemplateScalarModel) valueObj).getAsString() : null;
  }

}
