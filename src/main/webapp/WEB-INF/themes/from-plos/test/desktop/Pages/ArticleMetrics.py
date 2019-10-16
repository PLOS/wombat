#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Copyright (c) 2017 Public Library of Science
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.

__author__ = 'jfesenko@plos.org'

import logging
from urllib.parse import unquote

from selenium.webdriver.common.by import By

from .Article import Article
from ...Base import Utils
from ...Base.Config import rhino_url, base_url


# Variable definitions

class ArticleMetrics(Article):
    """
    Model Article Metrics Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticleMetrics, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._tab_alm = (By.ID, 'tabMetrics')
        self._alm_container = (By.ID, 'article-metrics')
        self._alm_header_viewed = (By.ID, 'viewedHeader')
        self._alm_header_viewed_icon = (
            By.CSS_SELECTOR, '#viewedHeader > a.ir')
        self._alm_viewed_container = (By.ID, 'views')
        self._alm_viewed_summary_total = (
            By.CSS_SELECTOR, '#pageViewsSummary .totalCount')
        self._alm_viewed_chart = (By.ID, 'chart')
        self._alm_viewed_filter_bar = (By.ID, 'averageViewsSummary')
        self._alm_viewed_filter_dropdown = (By.ID, 'subject_areas')
        self._alm_viewed_filter_link = (By.ID, 'linkToRefset')
        self._alm_viewed_filter_icon = (
            By.CSS_SELECTOR, '#averageViewsSummary a.ir')
        self._alm_viewed_disclaimer = (By.CSS_SELECTOR, '#usage > p')
        self._alm_viewed_table_plos_total = (
            By.XPATH, '//table[@id=\'pageViewsTable\']/tbody/tr[2]/td[5]')
        self._alm_viewed_table_pmc_total = (
            By.XPATH, '//table[@id=\'pageViewsTable\']/tbody/tr[3]/td[5]')
        self._alm_viewed_table_total = (
            By.XPATH, '//table[@id=\'pageViewsTable\']/tbody/tr[4]/td[5]')
        self._alm_viewed_sources = ['figshare']
        self._alm_viewed_figshare_image = (
            By.ID, 'figshareImageOnArticleMetricsTab')
        self._alm_viewed_figshare_dropdown_link = (
            By.XPATH, '//div[@id=\'dropdown-figshare\']/table/tbody/tr/td/a')

        self._alm_header_cited = (By.CSS_SELECTOR, 'h2#citedHeader')
        self._alm_citations_section = (By.ID, 'citations')
        self._alm_header_cited_icon = (By.CSS_SELECTOR, '#citedHeader > a.ir')
        self._alm_cited_container = (By.ID, 'relatedCites')
        self._alm_cited_sources = ['scopus', 'crossref', 'wos', 'datacite']
        self._alm_tile_google_scholar = (
            By.CSS_SELECTOR, '#google-scholarOnArticleMetricsTab '
                             '.metrics_tile_footer')
        self._alm_tile_scopus = (
            By.CSS_SELECTOR, '#scopusOnArticleMetricsTab .metrics_tile_footer')

        self._alm_header_saved = (By.CSS_SELECTOR, 'h2#savedHeader')
        self._alm_header_saved_icon = (By.CSS_SELECTOR, '#savedHeader > a.ir')
        self._alm_saved_container = (By.CSS_SELECTOR, 'div#relatedBookmarks')
        self._alm_saved_sources = ['connotea', 'mendeley']

        self._alm_saved_mendeley_image = (
            By.ID, 'mendeleyImageOnArticleMetricsTab')
        self._alm_saved_mendeley_tooltip = (
            By.CSS_SELECTOR, '#mendeleyOnArticleMetricsTab table.tileTooltip')

        self._alm_header_discussed = (By.ID, 'discussedHeader')
        self._alm_header_discussed_icon = (
            By.CSS_SELECTOR, '#discussedHeader > a.ir')
        self._alm_discussed_container = (By.ID, 'relatedBlogPosts')
        self._alm_discussed_sources = ['wikipedia', 'twitter', 'facebook']
        self._alm_discussed_facebook_image = (
            By.ID, 'facebookImageOnArticleMetricsTab')
        self._alm_discussed_facebook_tooltip = (
            By.CSS_SELECTOR, '#facebookOnArticleMetricsTab table.tileTooltip')
        self._alm_tile_comments = (
            By.CSS_SELECTOR,
            '#notesAndCommentsOnArticleMetricsTab .metrics_tile_footer')

        self._alm_footer_link1 = (
            By.LINK_TEXT, 'Information on PLOS Article-Level Metrics')
        self._alm_footer_link2 = (By.LINK_TEXT, 'Please let us know.')

        self._tiles_with_footer_links = ['twitter']
        # these tiles have links manually added within the javascript

    # POM Actions

    def assert_metrics_tab_active(self):
        assert 'active' in self._get(self._tab_alm).get_attribute('class')
        return self

    def validate_viewed_section(self):
        self._get(self._alm_container)
        self._get(self._alm_header_viewed)
        self._is_link_valid(self._get(self._alm_header_viewed_icon))
        assert 'More information' in self._get(
            self._alm_header_viewed_icon).get_attribute('title')
        alm_viewed = self.return_alm_data_sources('viewed')
        if alm_viewed:
            # check for presence of major elements

            # chart
            self._get(self._alm_viewed_chart)

            # relative metrics (only when data present)
            if self.return_alm_metrics_total('relativemetric'):
                self._get(self._alm_viewed_filter_bar)
                self._get(self._alm_viewed_filter_dropdown)
                self._get(
                    self._alm_viewed_filter_link)  # this currently is a
                # broken link
                # (see DPRO-2310), so no check for OK status
                assert 'More information' in \
                       self._get(self._alm_viewed_filter_icon).get_attribute(
                           'title')
            self._get(self._alm_viewed_disclaimer)

            # check counts against ALM  and Counter data

            plos_total = self.return_counter_data()
            assert Utils.to_int(
                self._get(self._alm_viewed_table_plos_total)) == plos_total

            pmc_total = self.return_alm_metrics_total('pmc')
            assert Utils.to_int(
                self._get(self._alm_viewed_table_pmc_total)) == pmc_total

            grand_total = plos_total + pmc_total
            assert grand_total is not None, "ALM data for total view count " \
                                            "not found"
            assert Utils.to_int(
                self._get(self._alm_viewed_summary_total)) == grand_total

            # check for auto-generated metrics tiles
            self._validate_metrics_tiles(self._alm_viewed_sources)

            # check for clickable dropdown for figshare
            if self.return_alm_metrics_total('figshare'):
                self._check_for_invisible_element(
                    self._alm_viewed_figshare_dropdown_link)
                self._get(self._alm_viewed_figshare_image).click()
                dropdown_link = self._get(
                    self._alm_viewed_figshare_dropdown_link)
                # assert 'Supporting Info' in dropdown_link.text
                self._is_link_valid(dropdown_link)
                # click again to close figshare dropdown
                self._get(self._alm_viewed_figshare_image).click()
        else:
            assert 'not available' in self._get(
                self._alm_viewed_container).text

        return self

    def validate_cited_section(self):
        self._get(self._alm_container)
        self._get(self._alm_header_cited)
        self._is_link_valid(self._get(self._alm_header_cited_icon))
        assert 'More information' in self._get(
            self._alm_header_cited_icon).get_attribute('title')

        # check for auto-generated metrics tiles
        self._validate_metrics_tiles(self._alm_cited_sources)

        citations_section = self._iget(self._alm_citations_section)
        self._scroll_into_view(citations_section)

        # check for manually generated tile
        google_scholar_tile = self._get(self._alm_tile_google_scholar)

        link_to_check = google_scholar_tile.find_element_by_tag_name('a')
        expected_url = unquote(link_to_check.get_attribute('href'))
        google_scholar_url = self.click_on_link_same_window_and_back(
            link_to_check, 'Google Scholar', 'google', base_url,
            check_title=False)
        self.validate_text_contains(unquote(google_scholar_url), expected_url,
                                    'Incorrect link to google scholar page')

        return self

    def validate_saved_section(self):
        self._get(self._alm_container)
        self._get(self._alm_header_saved)
        self._is_link_valid(self._get(self._alm_header_saved_icon))
        assert 'More information' in self._get(
            self._alm_header_saved_icon).get_attribute('title')

        # check for auto-generated metrics tiles
        if self._validate_metrics_tiles(self._alm_saved_sources):
            if self.return_alm_metrics_total('mendeley'):
                # additional check for Mendeley hover-over action
                alm_saved_image = self._get(self._alm_saved_mendeley_image)
                self._check_for_invisible_element(
                    self._alm_saved_mendeley_tooltip)
                self._scroll_into_view(alm_saved_image)
                self._actions.move_to_element(alm_saved_image).perform()
        else:
            assert 'not available' in self._get(self._alm_saved_container).text

        return self

    def validate_discussed_section(self):
        self._get(self._alm_container)
        self._get(self._alm_header_discussed)
        self._is_link_valid(self._get(self._alm_header_discussed_icon))
        assert 'More information' in self._get(
            self._alm_header_discussed_icon).get_attribute(
            'title')

        # check for auto-generated metrics tiles
        if self._validate_metrics_tiles(self._alm_discussed_sources):
            if self.return_alm_metrics_total('facebook'):
                # additional check for Facebook hover-over action
                self._check_for_invisible_element(
                    self._alm_discussed_facebook_tooltip)
                self._actions.move_to_element(
                    self._get(self._alm_discussed_facebook_image)).perform()
                self._get(self._alm_discussed_facebook_tooltip)

        # check for comments tile (should always be present)
        page_comments_count = Utils.to_int(self._get(
            self._alm_tile_comments))
        # check against rhino metadata for matching comment number
        logging.info('Call rhino endpoint: {0}/articles/{1}/comments?count='
                     .format(rhino_url, self.extract_page_doi()))
        expected_comments_count = \
            self.get_rhino_comments_count(self.extract_page_escaped_doi())
        assert page_comments_count == expected_comments_count, \
            '{0!r} not equal to expected {1!r}' \
                .format(page_comments_count, expected_comments_count)

        # 'Let us know' - validating email link
        partial_email_link = "mailto:webmaster@plos.org"
        actual_link = self._get(self._alm_footer_link2).get_attribute("href")
        assert partial_email_link in actual_link, \
            'partial link: {0} is not in actual link: {1}'.format(
                partial_email_link, actual_link)

    def _validate_metrics_tiles(self, source_names):
        """
        Args:
          source_names: a list of ALM source names for the metrics tiles to
          be validated
        Returns:
          True if at least one tile was validated as being present, False if
          no tiles were present
        """
        tiles_found = False
        for source_name in source_names:
            tile_locator = (By.ID, '%sOnArticleMetricsTab')
            # check for presence of tile (only when views > 0)
            metrics_total = self.return_alm_metrics_total(source_name)
            if metrics_total:
                has_link = self.return_alm_data_source(source_name).get(
                    'events_url') \
                           is not None or source_name in \
                           self._tiles_with_footer_links
                class_suffix = '' if has_link else '_no_link'
                tile_footer_locator = (By.CSS_SELECTOR,
                                       '#%sOnArticleMetricsTab '
                                       '.metrics_tile_footer%s' % (
                                           source_name, class_suffix))
                assert Utils.to_int(
                    self._get(tile_footer_locator)) == metrics_total
                tiles_found = True
                if has_link:
                    # TODO: remove 'if' once ALM-992 gets resolved
                    if source_name not in ('mendeley', 'scopus'):
                        self._is_link_valid(self._get(tile_footer_locator)
                                            .find_element_by_tag_name('a'))
            else:
                assert self._wait_for_not_element(
                    tile_locator, multiplier=0.05), \
                    "Metrics tile for {} found despite zero count" \
                        .format(source_name)
        return tiles_found
