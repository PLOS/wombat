/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
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

  @Override
  protected Object getValue(Environment env, Map params) throws TemplateException, IOException {
    Object mapNameObj = params.get("map");
    if (mapNameObj == null) throw new TemplateModelException("map param required");

    Object valueNameObj = params.get("value");
    if (valueNameObj == null) throw new TemplateModelException("value param required");

    Object journalKeyObj = params.get("journal");

    Theme theme = new SitePageContext(siteResolver, env).getSite().getTheme();
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
