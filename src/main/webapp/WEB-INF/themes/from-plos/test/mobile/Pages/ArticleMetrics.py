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

import logging

from selenium.webdriver.common.by import By

from .Article import Article
from ...Base import Utils
from ...Base.Config import rhino_url, base_url_mobile

__author__ = 'gtimonina@plos.org'


# Variable definitions

class ArticleMetrics(Article):
    """
    Model Article Metrics Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticleMetrics, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._alm_container = (By.ID, 'viewedCard')
        self._alm_header_viewed = (By.ID, 'viewedHeader')
        self._alm_header_viewed_icon = \
            (By.CSS_SELECTOR, '#viewedHeader > a.ir')
        self._alm_viewed_container = (By.ID, 'views')
        self._alm_viewed_summary_total = (By.CSS_SELECTOR, 'div.totalCount')

        self._alm_viewed_disclaimer = (By.CSS_SELECTOR, '#usage > p')
        self._alm_viewed_table_totals = \
            (By.CSS_SELECTOR, 'table#pageViewsTable td.total + td.total')
        self._alm_viewed_sources = \
            [
                self._create_dict('figshare'),
                ]
        self._alm_header_cited = (By.CSS_SELECTOR, 'h2#citedHeader')
        self._alm_header_cited_icon = (By.CSS_SELECTOR, '#citedHeader > a')
        self._alm_cited_sources = \
            [
                self._create_dict('scopus'),
                self._create_dict('crossref'),
                self._create_dict('wos'),
                self._create_dict('datacite'),
                ]
        self._alm_tile_google_scholar = \
            (By.CSS_SELECTOR, '#google-scholarOnArticleMetricsTab '
                              '.metrics_tile_footer')

        self._alm_header_saved = (By.CSS_SELECTOR, 'h2#savedHeader')
        self._alm_header_saved_icon = (By.CSS_SELECTOR, '#savedHeader > a')
        self._alm_saved_container = (By.CSS_SELECTOR, 'div#relatedBookmarks')
        self._alm_saved_sources = \
            [
                # TODO: check that link is disable after AMBR-94
                # self._create_dict('citeulike'),
                self._create_dict('connotea'),
                self._create_dict('mendeley'),
                ]

        self._alm_saved_mendeley_tooltip = \
            (By.CSS_SELECTOR, '#mendeleyOnArticleMetricsTab table.tileTooltip')
        self._alm_saved_mendeley_readers = \
            (By.CSS_SELECTOR, '#mendeleyOnArticleMetricsTab td.data1')

        self._alm_header_discussed = (By.ID, 'discussedHeader')
        self._alm_header_discussed_icon = \
            (By.CSS_SELECTOR, '#discussedHeader > a.ir')
        self._alm_discussed_sources = \
            [
                self._create_dict('wikipedia'),
                self._create_dict('twitter'),
                self._create_dict('facebook'),
                ]

        self._alm_discussed_facebok_numbers = \
            (By.CSS_SELECTOR, '#facebookOnArticleMetricsTab td')
        self._alm_tile_comments = \
            (By.CSS_SELECTOR,
             '#commentsOnArticleMetricsTab .metrics_tile_footer')

        self._alm_footer_link1 = \
            (By.LINK_TEXT, 'Information on PLOS Article-Level Metrics')
        self._alm_footer_link2 = (By.LINK_TEXT, 'Please let us know.')

        self._tiles_with_footer_links = ['twitter']
        # these tiles have links manually added within the javascript

    # POM Actions

    def _create_dict(self, source_name):
        """
        The method to create dictionary based on the name of the metric to
        validate the metric
        :param source_name: the name of specific metric, string
        :return: dictionary with source name, tile locator and tile footer
        locator
        """
        source_dict = \
            {
                'source': source_name,
                'tile_locator': (By.CSS_SELECTOR,
                                 'div#{0}OnArticleMetricsTab'.format(
                                     source_name)),
                'tile_footer_locator':
                    (By.CSS_SELECTOR,
                     'div#{0}OnArticleMetricsTab > div['
                     'class^=metrics_tile_footer]'
                     .format(source_name))
                }
        return source_dict

    def validate_viewed_section(self):
        self._get(self._alm_container)
        self._get(self._alm_header_viewed)
        self._is_link_valid(self._get(self._alm_header_viewed_icon))
        assert 'More information' in self._get(
            self._alm_header_viewed_icon).get_attribute('title')
        alm_viewed = self.return_alm_data_sources('viewed')
        if alm_viewed:
            # check for presence of major elements
            self._get(self._alm_viewed_disclaimer)

            # check counts against ALM  and Counter data
            totals = self._gets(self._alm_viewed_table_totals)
            assert len(
                totals) == 3, '3 results expected for total article views ' \
                              'table, ' \
                              'found: {0!s}'.format(len(totals))
            table_plos_total = totals[0]
            table_pmc_total = totals[1]
            table_total = totals[2]

            logging.info('Validating article views table')
            # validate total article wiews for PLOS
            plos_total = self.return_counter_data()
            actual_plos_total = Utils.to_int(table_plos_total)
            assert actual_plos_total == plos_total, \
                'Incorrect PLOS Total for article views, expected: {0!s}, ' \
                'found: {1!s}' \
                .format(plos_total, actual_plos_total)

            # validate total article wiews for PMC
            pmc_total = self.return_alm_metrics('pmc')
            actual_pmc_total = Utils.to_int(table_pmc_total)
            assert actual_pmc_total == pmc_total, \
                'Incorrect PMC Total for article views, expected: {0!s}, ' \
                'found: {1!s}' \
                .format(pmc_total, actual_pmc_total)

            # validate grand total article wiews
            grand_total = plos_total + pmc_total
            actual_grand_total = Utils.to_int(table_total)
            assert actual_grand_total == grand_total, \
                'Incorrect grand Total for article views, expected: {0!s}, ' \
                'found: {1!s}' \
                .format(grand_total, actual_grand_total)

            grand_total = plos_total + pmc_total
            assert grand_total is not None, "ALM data for total view count " \
                                            "not found"
            assert Utils.to_int(table_total) == grand_total

            summary_total = self._get(self._alm_viewed_summary_total)
            summary_total_int = Utils.to_int(summary_total)
            assert summary_total_int == grand_total, \
                'Incorrect total in the header, expected: {0!s}, found: {1!s}'\
                .format(grand_total, summary_total_int)

            # check for auto-_validate_metrics_tilesgenerated metrics tiles
            self._validate_metrics_tiles(self._alm_viewed_sources)

        else:
            assert 'not available' in self._get(
                self._alm_viewed_container).text

        return self

    def validate_cited_section(self):

        logging.info('Validating article Cited section')
        self._get(self._alm_container)
        self._get(self._alm_header_cited)
        self._is_link_valid(self._get(self._alm_header_cited_icon))
        assert 'More information' in self._get(
            self._alm_header_cited_icon).get_attribute('title')

        # check for auto-generated metrics tiles
        self._validate_metrics_tiles(self._alm_cited_sources)

        # check for manually generated tile
        google_scholar_tile = self._get(self._alm_tile_google_scholar)
        self._scroll_into_view(google_scholar_tile)

        link_to_check = google_scholar_tile.find_element_by_tag_name('a')
        google_scholar_url = self.click_on_link_same_window_and_back(
            link_to_check, 'Google Scholar', 'google', base_url_mobile,
            check_title=False)
        self.validate_text_contains(google_scholar_url,
                                    "https://scholar.google.com",
                                    'Incorrect link to google scholar page')

        return self

    def validate_saved_section(self):

        logging.info('Validating article Saved section')
        self._get(self._alm_container)
        self._get(self._alm_header_saved)
        self._is_link_valid(self._get(self._alm_header_saved_icon))
        assert 'More information' in self._get(
            self._alm_header_saved_icon).get_attribute('title')

        # check for auto-generated metrics tiles
        if self._validate_metrics_tiles(self._alm_saved_sources):
            if self.return_alm_metrics('mendeley'):
                # additional check for Mendeley table
                self._get(self._alm_saved_mendeley_tooltip)
                alm_readers = self.return_alm_metrics('mendeley', 'readers')
                if alm_readers:
                    readers = self._get(self._alm_saved_mendeley_readers)
                    readers_count = Utils.to_int(readers)
                    assert readers_count == alm_readers, \
                        'Incorrect Individuals count in Mendeley table, ' \
                        'expected: {0!s}, ' \
                        'found: {1!s}'.format(alm_readers, readers_count)
        else:
            assert 'not available' in self._get(self._alm_saved_container).text

        return self

    def validate_discussed_section(self):

        logging.info('Validating article Discussed section')
        self._get(self._alm_container)
        self._get(self._alm_header_discussed)
        self._is_link_valid(self._get(self._alm_header_discussed_icon))
        assert 'More information' in self._get(
            self._alm_header_discussed_icon).get_attribute(
            'title')

        # check for auto-generated metrics tiles
        if self._validate_metrics_tiles(self._alm_discussed_sources):
            if self.return_alm_metrics('facebook'):
                # additional check for Facebook hover-over action
                alm_likes = self.return_alm_metrics('facebook', 'likes')
                alm_shares = self.return_alm_metrics('facebook', 'readers')
                alm_posts = self.return_alm_metrics('facebook', 'comments')
                expected_numbers = (alm_likes, alm_shares, alm_posts)
                facebook_actual_numbers = self._gets(
                    self._alm_discussed_facebok_numbers)
                if facebook_actual_numbers:
                    likes = Utils.to_int(facebook_actual_numbers[0])
                    shares = Utils.to_int(facebook_actual_numbers[1])
                    posts = Utils.to_int(facebook_actual_numbers[2])
                    actual_numbers = (likes, shares, posts)
                    assert actual_numbers == expected_numbers, \
                        'Incorrect counts in Discussed Facebook table, ' \
                        'expected: (likes, shares, ' \
                        'posts): ({0!s}, {1!s}, {2!s}), found: ({3!s}, ' \
                        '{4!s}, {5!s})' \
                        .format(*(expected_numbers + actual_numbers))

        # check for comments tile (should always be present)
        int_page_comments_count = Utils.to_int(self._get(
            self._alm_tile_comments))
        page_comments_count = int_page_comments_count
        # check against rhino metadata for matching comment number
        logging.info('Call rhino endpoint: {0}/articles/{1}/comments?count='
                     .format(rhino_url, self.extract_page_doi()))
        expected_comments_count = \
            self.get_rhino_comments_count(self.extract_page_escaped_doi())

        assert page_comments_count == expected_comments_count, \
            '{0!r} not equal to expected {1!r}' \
            .format(page_comments_count, expected_comments_count)

        # footer links
        # TODO: uncomment next line once SRE-296/AMBR-725 gets resolved
        # self._is_link_valid(self._get(self._alm_footer_link1))

        # 'Let us know' - validating email link
        partial_email_link = "mailto:webmaster@plos.org"
        actual_link = self._get(self._alm_footer_link2).get_attribute("href")
        assert partial_email_link in actual_link, \
            'partial link: {0} is not in actual link: {1}'.format(
                partial_email_link, actual_link)

    def _validate_metrics_tiles(self, source_names):
        """

        :param source_names: a list of ALM source names for the metrics
        tiles to be validated
        :return: True if at least one tile was validated as being present,
        False if no tiles were present
        """
        tiles_found = False
        for source_name in source_names:
            tile_locator = source_name['tile_locator']
            metrics_total = self.return_alm_metrics(source_name['source'])
            if metrics_total == 0 and source_name['source'] == 'crossref':
                by_month_list = self.return_alm_data_source(
                    source_name['source']).get('by_month')
                metrics_total = sum([item['total'] for item in by_month_list])

            if metrics_total:
                has_link = self.return_alm_data_source(
                    source_name['source']).get(
                    'events_url') is not \
                           None or \
                           source_name[
                               'source'] in self._tiles_with_footer_links
                tile_footer_locator = source_name['tile_footer_locator']
                assert Utils.to_int(
                    self._get(tile_footer_locator)) == metrics_total
                tiles_found = True
                if has_link:
                    # TODO: remove 'if' once ALM-992 gets resolved
                    if source_name['source'] not in ('mendeley', 'scopus'):
                        self._is_link_valid(
                            self._get(tile_footer_locator)
                            .find_element_by_tag_name('a'))
            else:
                assert self._wait_for_not_element(tile_locator,
                                                  multiplier=0.05), \
                    "Metrics tile for {0} found despite zero count".format(
                        source_name['source'])
        return tiles_found
