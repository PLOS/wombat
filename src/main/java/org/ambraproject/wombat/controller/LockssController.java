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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.Siteless;
import org.ambraproject.wombat.service.ArticleArchiveService;
import org.ambraproject.wombat.service.TopLevelLockssManifestService;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.ambraproject.wombat.service.remote.SolrSearchApi.MAXIMUM_SOLR_RESULT_COUNT;

/**
 * Responsible for providing the publication year range, months and article DOIs published in a given year and month
 */
@Controller
public class LockssController extends WombatController {

  @Autowired
  private ArticleArchiveService articleArchiveServiceImpl;
  @Autowired
  private TopLevelLockssManifestService topLevelLockssManifestService;

  @RequestMapping(name = "lockssPermission", value = "/lockss.txt", method = RequestMethod.GET)
  public String getLockssPermission(@SiteParam Site site) {
    return site + "/ftl/lockss/permission";
  }

  /*
   * Note that this mapping value ("/lockss-manifest") is the same as for the "lockssYears" handler, which causes
   * mapping conflicts on any site that is configured without a PathTokenPredicate. For this reason, this handler is
   * disabled by default in {@code WEB-INF/themes/root/config/mappings.yaml} and should be re-enabled in a site's theme
   * only if that site is mapped to requests with a path token.
   *
   * See comments in TopLevelLockssManifestService.
   */
  @Siteless
  @RequestMapping(value = "/lockss-manifest", method = RequestMethod.GET)
  public ResponseEntity<?> getSiteManifestLinks(HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.TEXT_HTML)
        .body(topLevelLockssManifestService.getPageText(request));
  }

  @RequestMapping(name = "lockssYears", value = "/lockss-manifest", method = RequestMethod.GET)
  public String getYearsForJournal(@SiteParam Site site, Model model) throws IOException, ParseException {
    Range<Date> dateRange = articleArchiveServiceImpl.getDatesForJournal(site);
    model.addAttribute("minYear", dateRange.getMinimum().getYear() + 1900);
    model.addAttribute("maxYear", dateRange.getMaximum().getYear() + 1900);
    return site + "/ftl/lockss/years";
  }

  @RequestMapping(name = "lockssMonths", value = "/lockss-manifest/vol_{year}", method = RequestMethod.GET)
  public String getMonthsForYear(@SiteParam Site site, @PathVariable String year, Model model) {
    int yearValue;
    try {
      yearValue = Integer.parseInt(year);
    } catch (NumberFormatException e) {
      throw new NotFoundException(e);
    }

    List<String> months = articleArchiveServiceImpl.getMonthsForYear(yearValue);
    model.addAttribute("year", year);
    model.addAttribute("months", months);
    return site + "/ftl/lockss/months";
  }

  @RequestMapping(name = "lockssArticles", value = "/lockss-manifest/vol_{year}/{month}",
      method = RequestMethod.GET, params = {"cursor", "pageNumber"})
  public String getArticlesPerMonth(@SiteParam Site site, @PathVariable String year,
                                    @PathVariable String month, @RequestParam String cursor,
                                    @RequestParam String pageNumber, Model model)
      throws IOException, ParseException {
    SolrSearchApi.Result searchResult = articleArchiveServiceImpl.getArticleDoisPerMonth(site, year, month, cursor);

    model.addAttribute("month", month);
    model.addAttribute("year", year);
    model.addAttribute("docs", searchResult.getDocs());
    model.addAttribute("nextCursorMark", Optional.ofNullable(searchResult.getNextCursorMark()).orElse(""));

    int pageNumberCount = Integer.parseInt(pageNumber);
    final int listStartNumber = pageNumberCount * MAXIMUM_SOLR_RESULT_COUNT + 1;
    model.addAttribute("listStart", listStartNumber);

    final boolean isLastPage = searchResult.getNumFound()
        < listStartNumber + MAXIMUM_SOLR_RESULT_COUNT;
    model.addAttribute("showMoreLink", !isLastPage);

    pageNumberCount = pageNumberCount + 1;
    model.addAttribute("pageNumber", pageNumberCount);

    return site + "/ftl/lockss/dois";
  }
}
