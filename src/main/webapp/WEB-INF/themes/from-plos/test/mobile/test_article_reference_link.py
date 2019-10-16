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

# This test case validates the PLOS mobile site article reference links.

import pytest
import logging

from .Pages.PlosOneArticlePage import PlosOneArticlePage
from .Pages.PlosMedicineArticlePage import PlosMedicineArticlePage
from .Pages.PlosGeneticsArticlePage import PlosGeneticsArticlePage
from .Pages.PlosBiologyArticlePage import PlosBiologyArticlePage
from .Pages.PlosPathogensArticlePage import PlosPathogensArticlePage
from .Pages.PlosNeglectedArticlePage import PlosNeglectedArticlePage
from .Pages.PlosCompBiolArticlePage import PlosCompBiolArticlePage


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("page_under_test", [
    PlosOneArticlePage,
    PlosMedicineArticlePage,
    PlosGeneticsArticlePage,
    PlosBiologyArticlePage,
    PlosPathogensArticlePage,
    PlosNeglectedArticlePage,
    PlosCompBiolArticlePage,
])
class TestArticleReferenceLink:
    def test_article_test_reference_link(self, page_under_test):
        """
        Validating references on Introduction section
        and references section
        :param page_under_test: article page
        :return: void function
        """
        plos_page = page_under_test(self.driver)
        plos_page.assert_article_title()
        # close abstraction section
        plos_page.click_on_section(plos_page._abstract_section)

        # validating introduction section
        self.validate_introduction_refs(plos_page)

        # validating references section
        self.validate_references(plos_page)

        # click on logo to go to journal page
        plos_page.click_on_journal_logo()
        plos_page._article_section_ready(plos_page._article_list_block)

    def validate_references(self, plos_page):
        """
        helper method to validate references
        :param plos_page: page instance
        :return: void function
        """
        plos_page.click_on_section(plos_page._references_section)
        plos_page._article_section_ready(plos_page._references_section)
        references_list = plos_page.get_references_list()
        assert references_list
        assert '1.' in references_list[0].text
        plos_page.click_on_section(plos_page._references_section)

    def validate_introduction_refs(self, plos_page):
        """
        helper method to validate references on introduction section
        :param plos_page: article page
        :return: void function
        """
        plos_page.click_on_section(plos_page._introduction_section)
        logging.info('Launching \'Introduction\' section to test reference panel')
        plos_page._article_section_ready(plos_page._section1_first_ref)
        plos_page.click_on_introduction_ref_link()
        label_text = plos_page.get_introduction_ref_label_text()
        assert label_text == '1.'
        plos_page.click_on_section(plos_page._introduction_section)
