package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import org.ambraproject.wombat.config.site.RequestMappingContext;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A pseudo-controller that is dispatched to by {@link org.ambraproject.wombat.controller.ExceptionHandlerAdvisor}.
 * Autowired as a normal Spring bean.
 */
public class AppRootPage {

  private static final Logger log = LoggerFactory.getLogger(AppRootPage.class);

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private ServletContext servletContext;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  /**
   * Show a page in response to the application root.
   * <p>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  ModelAndView serveAppRoot() {
    ModelAndView mav = new ModelAndView("//approot");
    mav.addObject("siteKeys", siteSet.getSiteKeys());
    mav.addObject("mappingTable", buildMappingTable());
    try {
      mav.addObject("imageCode", getResourceAsBase64("/WEB-INF/themes/root/app/wombat.jpg"));
    } catch (IOException e) {
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
