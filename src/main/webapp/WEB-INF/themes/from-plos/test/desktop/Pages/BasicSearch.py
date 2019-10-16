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

__author__ = 'achoe@plos.org'

import logging
from selenium.webdriver.common.by import By
from .WombatPage import WombatPage
import random

positive_inputs = ['Therapy', 'nervous system', 'Person' + u'\u003a', '%20']
negative_inputs = ['fwlkjlasd', '%', '!', ' ', u'\uac15', '<', u'\u03a3', u'\u03bc' + 'm22',
                   u'\u96fb\u8996',
                   'CTGCTGCAGCGCCGATGGGGGTGTCGAATTCTGTCGATGTTTCACGTGAAA'
                   'CATTCATCGTCGGATTGTGCGCGGCCTCAGGCGTCGGTGTC'
                   ]


# Note: some search inputs, such as Chinese characters and some math symbols, will return no
# results, though they are present within the article body's text.
# We will need to see if we can make these terms searchable, and then update the list of
# inputs afterward.

class BasicSearch(WombatPage):
    """
    Model the Search bar
    """

    def __init__(self, driver, url_suffix=''):
        super(BasicSearch, self).__init__(driver, url_suffix)

        # Locators - Instance members

        self._search_bar = (By.ID, 'search')
        self._search_button_ambra = (By.CSS_SELECTOR, 'div[class="wrap"] > input[type="image"]')
        self._search_button = (By.ID, 'headerSearchButton')

        self._advanced_search_link = (By.ID, 'advSearch')
        self._number_of_search_results = (By.CLASS_NAME, 'results-number')
        # self._search_results_journal_pill = (By.XPATH,
        # '//form[@id="searchControlBarForm"]/div[@class="filter-block"]/div[1]')
        self._search_results_filter_block = (By.CLASS_NAME, 'filter-block')
        self._no_results_message = (By.CLASS_NAME, 'search-results-none-found')
        self._search_results_list_links = (
            By.XPATH, '//dl[@id="searchResultsList"]/dt[@class="search-results-title"]/a')
        self._page_body_full = (By.XPATH, '//body')
        self._article_title = (By.ID, 'artTitle')
        # TODO: Find out how to locate the tooltip for an empty search term,
        # "Please fill out this field"

    # POM Actions

    def validate_search_bar(self):
        print('Validating search bar...')
        self._get(self._search_bar)
        return self

    def validate_search_placeholder_text(self):
        print('Validating search placeholder text...')
        expected_placeholder_text = 'SEARCH'
        placeholder = self._get(self._search_bar).get_attribute("placeholder")
        assert expected_placeholder_text == placeholder

    def validate_search_button(self):
        print('Validating search button...')
        self._get(self._search_bar).find_element(*self._search_button)
        return self

    def validate_search_button_ambra(self):
        print('Validating search button...')
        self._get(self._search_bar).find_element(*self._search_button_ambra)
        return self

    def validate_advanced_search_link(self):
        print('Validating advanced search link...')
        self._get(self._advanced_search_link)
        return self

    def submit_search_term(self, search_term):
        self._get(self._search_bar).clear()
        self._get(self._search_bar).send_keys(search_term)
        self.click_search_button()

    def submit_search_term_ambra(self, search_term):
        self._get(self._search_bar).clear()
        self._get(self._search_bar).send_keys(search_term)
        self.click_search_button_ambra()

    def positive_search(self):
        search_term = random.choice(positive_inputs)
        logging.info('selected search term: {0!r}'.format(search_term))
        self.submit_search_term(search_term)
        number_of_results = self._get(self._number_of_search_results)
        search_result_term = number_of_results.text[number_of_results.text.index('for') + 4:]
        assert search_term == search_result_term, "%s is not equal to %s" % (
            search_term, search_result_term.text)
        links = self._get(self._search_results_list_links)
        search_article_title = links.text

        links.click()
        page_text = self._get(self._page_body_full).text
        assert 'Page Not Found' not in page_text
        actual_article_title_text = self._get(self._article_title).text
        # ignore difference in ascii symbols, like '2006â2010' vs '2006–2010'
        self.validate_text_exact(actual_text=actual_article_title_text.encode('ascii', 'ignore'),
                                 expected_text=search_article_title.encode('ascii', 'ignore'),
                                 message='Incorrect article title')
        self._driver.back()

    def negative_search(self):
        search_term = random.choice(negative_inputs)
        self.submit_search_term(search_term)
        expected_no_search_results_text = \
            'There were no results; please refine your search above and try again.'
        no_search_results_text = self._get(self._no_results_message).text
        assert expected_no_search_results_text in no_search_results_text
        self._driver.back()

    def click_search_button(self):
        search_button = self._get(self._search_button)
        search_button.click()

    def click_search_button_ambra(self):
        search_button = self._get(self._search_button_ambra)
        search_button.click()

    def click_advanced_search_link(self):
        advanced_search_link = self._get(self._advanced_search_link)
        advanced_search_link.click()

    def validate_default_journal_filter(self, journal_name):
        # Validates that header search results default to the current journal.
        # For header search results in PLOS Collections, the default behavior is to filter by all
        # journals. This will mean that no journal filters are displayed.
        if journal_name != 'PLOS Collections':
            active_filter_list = self._get(self._search_results_filter_block)
            active_filter_items = active_filter_list.find_elements_by_class_name('filter-item')
            active_journal_filters = []
            for filter_item in active_filter_items:
                filter_item_type = filter_item.find_element_by_tag_name('a').get_attribute(
                    'data-filter-param-name')
                if filter_item_type == 'filterJournals':
                    active_journal_filters.append(filter_item.text.rstrip(' '))
            print(active_journal_filters)
            assert journal_name == active_journal_filters[0], \
                'Search results are not filtering to the current journal by default' \
                'Current Journal: {0}' \
                'Journal Filter: {1}'.format(journal_name, active_journal_filters[0])
        else:
            filter_block_is_present = self.is_element_present_class('filter-block')
            assert filter_block_is_present is False, \
                'filter block should not be present for {0} because it searches by all journals' \
                    .format(journal_name)
        self._driver.back()
