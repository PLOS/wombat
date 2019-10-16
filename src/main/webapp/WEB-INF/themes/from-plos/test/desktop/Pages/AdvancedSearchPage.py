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
import random

from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

from ...Base.Config import base_url, collections_url
from ..Pages.WombatPage import WombatPage

search_terms = ['gene']


class AdvancedSearchPage(WombatPage):
    """
    Model the Advanced Search page
    """

    def __init__(self, driver, url_suffix=''):
        super(AdvancedSearchPage, self).__init__(driver, url_suffix)

        # Locators - instance members

        self._all_fields_dropdown = (By.ID, 'queryFieldId')
        self._search_term_box = (By.CSS_SELECTOR, 'input.query-condition-value')
        self._query_button_and = (By.ID, 'queryConjunctionAndId')
        self._query_button_or = (By.ID, 'queryConjunctionOrId')
        self._query_button_not = (By.ID, 'queryConjunctionNotId')
        self._unformatted_query_box = (By.ID, 'unformattedQueryId')
        self._top_search_button = (By.ID, 'searchFieldButton')
        self._clear_query_button = (By.ID, 'clearUnformattedQueryButtonId')
        self._advanced_search_block = (By.ID, 'search-adv-block')
        self._bottom_search_button = (By.CLASS_NAME, 'search-button')
        self._footer_brand_column = (
            By.XPATH, '//footer[@id="pageftr"]/div[@class="row"]/div[@class="brand-column"]/'
                      'p[@class="nav-special"]')

    # POM Actions

    def validate_advanced_search_block(self):
        self._get(self._advanced_search_block)
        return self

    def validate_search_term(self, search_term):
        text_area_search_term = self._get(self._unformatted_query_box).text
        print(text_area_search_term)
        assert search_term == text_area_search_term, '%s is not equal to %s!' % (
            search_term, text_area_search_term)

    def enter_search_term(self):
        search_term_box = self._get(self._search_term_box)
        search_term_box.send_keys(random.choice(search_terms))

    def submit_search(self):
        top_search_button = self._get(self._top_search_button)
        bottom_search_button = self._get(self._bottom_search_button)
        search_term_box = self._get(self._search_term_box)
        submit_method = random.choice(['Enter Key', 'Search Button'])
        logging.info('Submit clicking {0}'.format(submit_method))
        if submit_method == 'Enter Key':
            search_term_box.send_keys(Keys.ENTER)
        else:
            bottom_search_button.click()

    def validate_served_by_wombat(self, journal_name):
        # Get current url of search results page
        search_results_url = self._driver.current_url

        expected_url = '{0!s}/search'.format(collections_url.rstrip('/'))\
            if journal_name == 'ploscollections' \
            else '{0!s}/{1}/search'.format(base_url.rstrip('/'), journal_name)
        assert expected_url in search_results_url, \
            '{0} is not a valid wombat search results url'.format(search_results_url)
        self._driver.back()
