package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Reads a theme config value into a FreeMarker variable. The param {@code "map"} is the name of the theme config map to
 * read, and the param {@code "value"} is the key of the value to read from the map.
 * <p/>
 * Example invocation:
 * <pre>
 *   <@themeConfig map="widget" value="widgetCount" ; c>
 *     You have ${c}
 *     <#if c == 1>widget<#else>widgets</#if>
 *   </@themeConfig>
 * </pre>
 * This would read the {@code widget.yaml} (or {@code widget.json}) from the current page's theme, extract the {@code
 * "widgetCount"} value from that map, and set that value as {@code c} for the interior of the body.
 * <p/>
 * The optional {@code "journal"} param will look up the theme for another site, based on the site's configured journal
 * key, and return a config value from that theme instead.
 */
public class ThemeConfigDirective extends VariableLookupDirective<Object> {

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private HandlerMappingConfiguration handlerMappingConfig;

  @Override
  protected Object getValue(Environment env, Map params) throws TemplateException, IOException {
    Object mapNameObj = params.get("map");
    if (mapNameObj == null) throw new TemplateModelException("map param required");

    Object valueNameObj = params.get("value");
    if (valueNameObj == null) throw new TemplateModelException("value param required");

    Object journalKeyObj = params.get("journal");

    Theme theme = new SitePageContext(siteResolver, handlerMappingConfig, env).getSite().getTheme();
    if (journalKeyObj != null) {
      theme = theme.resolveForeignJournalKey(siteSet, journalKeyObj.toString()).getTheme();
    }
    Map<String, Object> configMap = theme.getConfigMap(mapNameObj.toString());
    if (configMap == null) {
      throw new TemplateModelException("No config map exists for: " + mapNameObj);
    }

    return configMap.get(valueNameObj.toString());
  }

}
