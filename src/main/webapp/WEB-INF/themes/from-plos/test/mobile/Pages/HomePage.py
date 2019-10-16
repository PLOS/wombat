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

from selenium.webdriver.common.by import By

from .WombatPage import WombatPage

__author__ = 'jgray@plos.org'


class HomePage(WombatPage):
    """
    Model an abstract base Journal page.
    """

    def __init__(self, driver, url_suffix=''):
        super(HomePage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._browse_topic_menu = (By.ID, 'menu-browse')
        # Mobile Search globals
        self._search_div_icon = (By.CSS_SELECTOR, 'a.site-search-button > span')
        self._search_term_input_field = (By.ID, 'search-input')
        self._search_cancel_button = (By.ID, 'search-cancel')
        self._search_execute_button = (By.ID, 'search-execute')
        self._search_icon = (By.CSS_SELECTOR, 'span.icon')
        self._search_result_titles = (By.CSS_SELECTOR, 'a.article-title')
        self._search_filter_button = (By.CSS_SELECTOR, 'button.filter-button')
        # Display type selection buttons global
        self._article_type_menu = (By.ID, 'article-type-menu')
        self._recent_button = (By.XPATH, ".//li[@data-method='recent']")
        self._popular_button = (By.XPATH, ".//li[@data-method='popular']")

    # POM Actions

    def click_recent_button(self):
        """
        Switches to the recent article view of the mobile homepage
        """
        print('Click Recent button')
        recent_link = self._get(self._article_type_menu).find_element(*self._recent_button)
        recent_link.click()
        return self

    def click_popular_button(self):
        """
        Switches to the popular article view of the mobile homepage
        """
        print('Click Popular button')
        popular_link = self._get(self._article_type_menu).find_element(*self._popular_button)
        popular_link.click()
        return self
