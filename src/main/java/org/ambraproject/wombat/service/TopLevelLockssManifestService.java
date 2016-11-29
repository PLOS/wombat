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

public class TopLevelLockssManifestService {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private SiteSet siteSet;

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

  private Iterable<Site> getSitesByDistinctJournalKey() {
    return () -> siteSet.getSites().stream()
        .collect(Collectors.groupingBy(Site::getJournalKey))
        .values().stream()
        .map((List<Site> siteGroup) -> {
          /*
           * The siteGroup argument contains a collection of sites that all have the same journal key.
           *
           * We generally expect that, if more than one site has the same journal key, its LOCKSS manifest page will
           * look the same. (The anticipated use case is that the sites will be desktop and mobile sites, and the
           * LOCKSS manifest page looks the same regardless.) So the URL to which we link doesn't matter (and would in
           * fact be identical in PLOS's production configuration).
           *
           * We have no way to choose which site to use, other than arbitrarily. So we choose one literally at random
           * in order to make the arbitrary nature of the choice abundantly clear. (It also helps to prevent rare bugs
           * from hiding, in case the choice were dependent on something like the order in which SiteSet yields the
           * Site objects.)
           *
           * If this makes you unhappy and you want to refactor it out, the first step is to make the page renderable
           * from a Site context, rather than a @Siteless request handler. Then, we could build the list of Sites by
           * calling Theme.resolveForeignJournalKey on each unique journal key.
           */
          int randomIndex = ThreadLocalRandom.current().nextInt(siteGroup.size());
          return siteGroup.get(randomIndex);
        })
        .sorted(Comparator.comparing(Site::getJournalName))
        .iterator();
  }

  public void printPage(HttpServletRequest request, PrintWriter writer) {
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

  public String getPageText(HttpServletRequest request) {
    StringWriter writer = new StringWriter();
    printPage(request, new PrintWriter(writer));
    return writer.toString();
  }

}
