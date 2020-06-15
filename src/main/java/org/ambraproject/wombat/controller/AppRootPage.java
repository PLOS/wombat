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

package org.ambraproject.wombat.controller;

import com.bugsnag.Bugsnag;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.io.ByteStreams;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContext;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.ThemeGraph;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A pseudo-controller that is dispatched to by {@link org.ambraproject.wombat.controller.ExceptionHandlerAdvisor}.
 * Autowired as a normal Spring bean.
 */
public class AppRootPage {

  /**
   * A Singleton View used to serve static HTML files outside of the web application.
   */
  private static enum HtmlFileView implements View {
    INSTANCE;

    @Override
    public String getContentType() {
      return MediaType.TEXT_HTML.toString();
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
      try (FileInputStream fileInputStream = new FileInputStream((File) model.get("rootPage"));
           ServletOutputStream outputStream = response.getOutputStream()) {
        ByteStreams.copy(fileInputStream, outputStream);
      }
    }
  }


  private static final Logger log = LogManager.getLogger(AppRootPage.class);

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private ThemeGraph themeGraph;
  @Autowired
  private ServletContext servletContext;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private Bugsnag bugsnag;

  /**
   * Show a page in response to the application root.
   */
  ModelAndView serveAppRoot() {
    if (runtimeConfiguration.showDebug()) {
      return serveDebugPage();
    } else {
      RedirectView rv = new RedirectView();
      rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
      rv.setUrl(runtimeConfiguration.getRootRedirect());
      return new ModelAndView(rv);
    }
  }

  private ModelAndView serveDebugPage() {
    ModelAndView mav = new ModelAndView("/NULLSITE/approot");
    mav.addObject("siteKeys", siteSet.getSiteKeys());
    mav.addObject("mappingTable", buildMappingTable());
    mav.addObject("themeTable", ImmutableList.copyOf(themeGraph.describe(siteSet)));
    try {
      mav.addObject("imageCode", getResourceAsBase64("/WEB-INF/themes/root/app/wombat.jpg"));
    } catch (IOException e) {
      bugsnag.notify(e);
      log.error("Error displaying root page image", e);
    }
    return mav;
  }

  private String getResourceAsBase64(String path) throws IOException {
    byte[] bytes;
    try (InputStream stream = servletContext.getResourceAsStream(path)) {
      bytes = IOUtils.toByteArray(stream);
    }
    return Base64.encodeBase64String(bytes);
  }

  private ImmutableList<MappingTableRow> buildMappingTable() {
    Table<RequestMappingContext, Site, String> table = HashBasedTable.create();
    Set<RequestMappingContext> sitelessMappings = new HashSet<>();
    ImmutableList<Site> allSites = siteSet.getSites().asList();
    for (RequestMappingContextDictionary.MappingEntry entry : requestMappingContextDictionary.getAll()) {
      RequestMappingContext mapping = entry.getMapping();
      Optional<Site> site = entry.getSite();
      String handlerName = entry.getHandlerName();

      if (site.isPresent()) {
        table.put(mapping, site.get(), handlerName);
      } else {
        sitelessMappings.add(mapping);
        for (Site s : allSites) {
          table.put(mapping, s, handlerName);
        }
      }
    }

    Set<RequestMappingContext> mappings = table.rowKeySet();
    List<MappingTableRow> rows = new ArrayList<>(mappings.size());
    for (final RequestMappingContext mapping : mappings) {
      final List<String> row = new ArrayList<>(allSites.size());
      for (Site site : allSites) {
        String cell = table.get(mapping, site);
        row.add(Strings.nullToEmpty(cell));
      }
      final String mappingRepresentation = represent(mapping);
      final boolean isGlobal = sitelessMappings.contains(mapping);

      rows.add(new MappingTableRow() {
        @Override
        public String getPattern() {
          return mappingRepresentation;
        }

        @Override
        public List<String> getRow() {
          return row;
        }

        @Override
        public boolean isGlobal() {
          return isGlobal;
        }
      });
    }

    return ROW_ORDERING.immutableSortedCopy(rows);
  }

  private static String represent(RequestMappingContext mapping) {
    StringBuilder sb = new StringBuilder().append(mapping.getPattern());
    boolean atFirst = true;
    for (String requiredParam : mapping.getRequiredParams()) {
      sb.append(atFirst ? '?' : '&').append(requiredParam);
      atFirst = false;
    }
    return sb.toString();
  }

  public static interface MappingTableRow {
    String getPattern();

    List<String> getRow();

    boolean isGlobal();
  }

  private static final Ordering<MappingTableRow> ROW_ORDERING = Ordering.natural()
      .onResultOf(MappingTableRow::getPattern);

}
