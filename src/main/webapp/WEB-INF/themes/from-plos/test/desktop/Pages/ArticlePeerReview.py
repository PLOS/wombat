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

__author__ = 'gtimonina@plos.org'

from selenium.webdriver.common.by import By

from .Article import Article
from ...Base import ParseXML


# Variable definitions

class ArticlePeerReview(Article):
    """
    Model Article Metrics Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticlePeerReview, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._tab_peer_review = (By.ID, 'tabPeerReview')
        self._tab_authors = (By.ID, 'tabAuthors')
        self._peer_review_history_table = (By.CLASS_NAME, 'review-history')
        self._peer_review_page_title = (By.CLASS_NAME, 'page-title')
        self._peer_review_letters = (By.CSS_SELECTOR, '.letter > div')
        self._peer_review_hrefs = (By.CSS_SELECTOR, ".review-history  a[href^='http']")

    # POM Actions

    def peer_review_tab_is_visible(self):
        self._wait_for_element(self._get(self._tab_peer_review))
        return self

    def no_peer_review_tab(self):
        self._wait_for_not_element(self._tab_peer_review, 0.1)
        return self

    def peer_review_tab_is_active(self):
        class_attr = self._get(self._tab_peer_review).get_attribute('class')
        return 'active' in class_attr

    def get_article_peer_review_sections(self):
        article_xml = self.get_article_xml()
        sub_articles = ParseXML.ParseXML().get_sub_articles(article_xml)
        return sub_articles

    def click_on_article_tab(self, tab_to_click):
        article_tab = self._get(tab_to_click)
        article_tab.click()
        self.page_ready()

    def get_tab_names_list(self):
        tabs_list = self._gets(self._article_tabs_list)
        tab_names = [el.text for el in tabs_list]
        return tab_names

    def get_peer_review_letters(self):
        letters = self._gets(self._peer_review_letters)
        class_names = [el.get_attribute('class') for el in letters]
        return class_names

    def page_ready(self):
        self._wait_for_element(self._get(self._logo), 1)

    def validate_peer_review_tab_style(self, journal_brand_color):
        self.validate_tabs_style(journal_brand_color)
        self.click_on_article_tab(self._tab_peer_review)
        self.validate_tabs_style(journal_brand_color)
