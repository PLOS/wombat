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

"""
This test case validates the PLOS desktop site's Collections pages.

"""

import pytest
import random

from .Pages.PlosCollectionsCollectionPage import PlosCollectionsCollectionPage
from .resources import collections

__author__ = 'jgray@plos.org'


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("page", [
    PlosCollectionsCollectionPage
])
class TestCollections:

    def test_smoke_collections_lemur_preamble(self, page):
        """
        Validates the presence of the following CMS derived elements:
          Main page body
          Header, Toolbar, billboard
        :param page: populated via pytest parametrization
        :return: void function
        """
        selected_collection = random.choice(collections)
        plos_page = page(self.driver, selected_collection)
        plos_page.validate_joey_container()
        plos_page.validate_collection_header()
        plos_page.validate_collection_toolbar()
        plos_page.validate_billboard()

    def test_smoke_collections_lemur_rhc(self, page):
        """
        Validates the presence of the following CMS derived elements:
          Main page body
          Main page div
          Right Hand Column:
              Ember widget list and function - There must be at least one widget defined,
                  there can be more than one of each type
        :param page: populated via pytest parametrization
        :return: void function
        """
        widget_count = 0
        selected_collection = random.choice(collections)
        plos_page = page(self.driver, selected_collection)
        plos_page.page_ready()
        plos_page.validate_joey_container()
        """
        AC:
        DONE: 1) There should be at least one widget on the page with class preview-textwidget
        DONE: 2) All elements with class preview-textwidget-title should have text
        DONE: 3) All elements with class preview-textwidget-blurb should have text
        DONE: 4) All elements with class button-text-widget should have text
        DONE: 5) All elements with class button-text-widget should have href that doesn't 404
        """
        widget_count += plos_page.validate_text_widget()
        """
        DPRO-1333
        AC:
        DONE: 1) An RSS widget has a title
        DONE: 2) An RSS widget is populated with a feed
        DONE: 3) Feed items contain post date/time, post title, post author
        DONE: The RSS Widget is an optional item, however, there must be one widget (RSS or Text)
          present for any collection
        """
        widget_count += plos_page.validate_rss_widget()
        # The following method MUST be called after the text and rss widget tests
        assert widget_count > 0, 'Right hand column not populated with any widgets!'

    def test_smoke_collections_lemur_collection_items(self, page):
        """
        Validates the presence of the following CMS derived elements:
          Main page body
          Main page div
          Individual collection items present on the main page
        :param page: populated via pytest parametrization
        :return: void function
        """
        selected_collection = random.choice(collections)
        plos_page = page(self.driver, selected_collection)

        plos_page.validate_joey_container()
        plos_page.validate_collection_items()
        plos_page.validate_collection_items_thumb()

    def test_core_collections_lemur_rhc_rss_feed(self, page):
        """
        Validates that any rss feed present for a collection is functional
        :param page: populated via pytest parametrization
        :return: void function
        """
        selected_collection = random.choice(collections)
        plos_page = page(self.driver, selected_collection)
        plos_page.validate_joey_container()
        result = plos_page.validate_rss_widget_function()
        assert result, 'Blog feed is busted. Please file a ticket against the Lemur (LMR) ' \
                       'project: Mammoth Component, collection: {0!r}'\
            .format(self.driver._driver.current_url)
