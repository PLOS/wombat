package org.ambraproject.wombat.freemarker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import freemarker.core.Environment;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import org.ambraproject.wombat.config.site.HandlerDirectory;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
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
    String targetJournal = getStringValue(params.get("journalKey"));
    String handlerName = getStringValue(params.get("handlerName"));

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);
    Site site = sitePageContext.getSite();

    Link.Factory linkFactory = (targetJournal == null)
        ? Link.toLocalSite(site)
        : Link.toForeignSite(site, targetJournal, siteSet);
    final Link link;
    if (handlerName != null) {
      Map<String, ?> variables = getValueAsMap(params.get("variables"));
      ListMultimap<String, ?> queryParameters = getValueAsMultimap(params.get("queryParameters"));
      List<?> wildcardValues = getValueAsList(params.get("wildcardValues"));
      link = linkFactory.toPattern(handlerDirectory, handlerName, variables, queryParameters, wildcardValues);
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

  private static ImmutableList<?> getValueAsList(Object value) throws TemplateModelException {
    if (value == null) return ImmutableList.of();
    if (value instanceof TemplateSequenceModel) {
      ImmutableList.Builder<Object> builder = ImmutableList.builder();
      TemplateSequenceModel sequenceModel = (TemplateSequenceModel) value;
      int size = sequenceModel.size();
      for (int i = 0; i < size; i++) {
        builder.add(sequenceModel.get(i));
      }
      return builder.build();
    } else {
      return ImmutableList.of(value);
    }
  }

  private static ImmutableMap<String, ?> getValueAsMap(Object value) throws TemplateModelException {
    if (value == null) return ImmutableMap.of();
    if (value instanceof TemplateHashModelEx) {
      TemplateHashModelEx ftlHash = (TemplateHashModelEx) value;
      ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
      for (TemplateModelIterator iterator = ftlHash.keys().iterator(); iterator.hasNext(); ) {
        String key = iterator.next().toString();
        builder.put(key, ftlHash.get(key));
      }
      return builder.build();
    }
    throw new TemplateModelException("Hash type expected");
  }

  private static ImmutableListMultimap<String, ?> getValueAsMultimap(Object value) throws TemplateModelException {
    if (value == null) return ImmutableListMultimap.of();
    if (value instanceof TemplateHashModelEx) {
      TemplateHashModelEx ftlHash = (TemplateHashModelEx) value;
      ImmutableListMultimap.Builder<String, Object> builder = ImmutableListMultimap.builder();
      for (TemplateModelIterator iterator = ftlHash.keys().iterator(); iterator.hasNext(); ) {
        String key = iterator.next().toString();
        TemplateModel model = ftlHash.get(key);
        if (model instanceof TemplateSequenceModel) {
          TemplateSequenceModel sequenceModel = (TemplateSequenceModel) model;
          int size = sequenceModel.size();
          for (int i = 0; i < size; i++) {
            builder.put(key, sequenceModel.get(i));
          }
        } else {
          builder.put(key, model);
        }
      }
      return builder.build();
    }
    throw new TemplateModelException("Hash type expected");
  }

}
