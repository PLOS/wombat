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

package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Produces links to the LOCKSS manifest pages for all sites.
 */
public class TopLevelLockssManifestService {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private SiteSet siteSet;


  /**
   * Build the HTML content of a top-level LOCKSS manifest page.
   * <p>
   * We build it in this hacky way, instead of using the normal templating system, because the rest of the system's
   * templates are tied to the context of a site and its theme, and this page is rendered from a Siteless controller.
   * This would be a good thing to refactor -- by making the system able either to invoke the templating engine without
   * a site or to have a site with no journal -- but we're stuck with this in the meantime.
   *
   * @param request the HTTP request whose response we are building
   * @return the HTML text to put into the HTTP response body
   */
  public String getPageText(HttpServletRequest request) {
    StringWriter writer = new StringWriter(0x600);
    printPage(request, new PrintWriter(writer));
    return writer.toString();
  }

  /**
   * @param request the HTTP request whose response we are building
   * @param writer  the buffer into which to print the response
   */
  private void printPage(HttpServletRequest request, PrintWriter writer) {
    for (String line : PREAMBLE) {
      writer.println(line);
    }

    for (Site site : getSitesByDistinctJournalKey()) {
      String journalName = site.getJournalName();
      Link.Factory.PatternBuilder pattern = Link.toAbsoluteAddress(site).toPattern(requestMappingContextDictionary, "lockssYears");
      Link manifestLink;
      try {
        manifestLink = pattern.build();
      } catch (Link.PatternNotFoundException e) {
        continue; // omit link for this site
      }
      String manifestUrl = manifestLink.get(request);
      writer.println(String.format("        <li><a href=\"%s\">%s</a></li>", manifestUrl, journalName));
    }

    for (String line : POSTAMBLE) {
      writer.println(line);
    }
  }

  private static final ImmutableList<String> PREAMBLE = ImmutableList.copyOf(new String[]{
      "<!DOCTYPE html>",
      "<html xmlns=\"http://www.w3.org/1999/xhtml\"",
      "      xmlns:dc=\"http://purl.org/dc/terms/\"",
      "      xmlns:doi=\"http://dx.doi.org/\"",
      "      lang=\"en\" xml:lang=\"en\"",
      "      class=\"no-js\">",
      "",
      "  <body>",
      "    <h1>Journals</h1>",
      "      <ul>",
  });
  private static final ImmutableList<String> POSTAMBLE = ImmutableList.copyOf(new String[]{
      "      </ul>",
      "    <em>LOCKSS system has permission to collect, preserve, and serve this open access Archival Unit.</em>",
      "  </body>",
      "</html>",
  });

  /**
   * Iterate over, for each distinct journal key among sites in the system, one <em>arbitrary</em> {@link Site} object
   * with that journal key.
   * <p>
   * We generally expect that, if more than one site has the same journal key, its LOCKSS manifest page will look the
   * same. (The anticipated use case is that the sites will be desktop and mobile sites, and the LOCKSS manifest page
   * looks the same regardless.) So the URL to which we link doesn't matter (and would in fact be identical in PLOS's
   * production configuration).
   * <p>
   * We have no way to choose which of those sites to use, other than arbitrarily. So we choose one literally at random
   * in order to make the arbitrary nature of the choice abundantly clear. (It also helps to prevent rare bugs from
   * hiding, in case the choice were dependent on something like the order in which SiteSet yields the Site objects.)
   * <p>
   * If this makes you unhappy and you want to refactor it out, the first step is to make the page renderable from a
   * Site context, rather than a @Siteless request handler. Then, we could build the list of Sites by calling
   * Theme.resolveForeignJournalKey on each unique journal key.
   */
  private Iterable<Site> getSitesByDistinctJournalKey() {
    return () -> siteSet.getSites().stream()
        .collect(Collectors.groupingBy(Site::getJournalKey))
        .values().stream()
        .map((List<Site> siteGroup) -> {
          // The argument is a collection of sites that all share one journal key.
          if (siteGroup.size() <= 1) return siteGroup.get(0);
          int randomIndex = ThreadLocalRandom.current().nextInt(siteGroup.size());
          return siteGroup.get(randomIndex);
        })
        .sorted(Comparator.comparing(Site::getJournalName))
        .iterator();
  }

}
