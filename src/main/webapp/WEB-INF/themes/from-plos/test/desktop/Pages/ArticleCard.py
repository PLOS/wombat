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
import random

from selenium.webdriver.common.by import By

from test.desktop.Pages.WombatPage import WombatPage


class ArticleCard(WombatPage):
    """
    Model an article card component (used by Plos One homepage and subject area landing page)
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticleCard, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._article_card = (By.CLASS_NAME, "article-block")
        self._card_link = (By.CLASS_NAME, "article-url")
        self._card_details = (By.CSS_SELECTOR, ".article-block .details")
        self._card_details_title = (By.CSS_SELECTOR, ".article-block .details h2.title")
        self._card_details_title_link = (By.CSS_SELECTOR, ".article-block .details h2.title a")
        self._card_details_title_popup = (
        By.CSS_SELECTOR, ".article-block .details h2.title a[title]")
        self._card_authors = (By.CSS_SELECTOR, ".article-block .details p .author")
        self._card_authors_popup = (By.CSS_SELECTOR, ".article-block .details p[title]")
        self._card_action_abstract = (By.CSS_SELECTOR, ".article-block .actions a.abstract")
        self._card_action_figures = (By.CSS_SELECTOR, ".article-block .actions a.figures")
        self._card_action_full_text = (By.CSS_SELECTOR, ".article-block .actions a.full-text")

    # POM Actions

    def validate_article_card(self):
        card = random.choice(self._gets(self._article_card))
        logging.info('selected card: {0!s}'.format(card.text))
        self._scroll_into_view(card)
        offset_x = card.size.get('width') - 10
        offset_y = card.size.get('height') - 10
        print('Verifying article card bottom popup menu bar')
        self._actions.move_to_element_with_offset(card, offset_x,
                                                  offset_y).perform()  # bottom right near popup menu
        # TODO: Per DPRO-2441 the following lines will be commented out. The abstract, figures and full text links have
        # TODO: been removed for now. Once these are available again, uncomment these lines.
        # self._wait_for_element(card.find_element(*self._card_action_abstract))
        # self._wait_for_element(card.find_element(*self._card_action_figures))
        # self._wait_for_element(card.find_element(*self._card_action_full_text))
        self._wait_for_element(card.find_element(*self._card_details))
        self._wait_for_element(card.find_element(*self._card_details_title))
        self._wait_for_element(card.find_element(*self._card_details_title_link))
        self._wait_for_element(card.find_element(*self._card_details_title_popup))
        self._wait_for_element(card.find_element(*self._card_authors))
        self._wait_for_element(card.find_element(*self._card_authors_popup))

        return self
