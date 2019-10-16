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
This test case validates the PLOSOne desktop site's taxonomy browser
"""
import logging
import pytest
import random

from ..Base import JournalArticleSet
from .Pages.TaxBrowse import TaxBrowser

__author__ = 'jgray@plos.org'

tax_browse_static_pages = [
    '/DesktopPlosOne/',
    '/DesktopPlosOne/s/accepted-manuscripts',
    '/DesktopPlosOne/s/advisory-groups',
    '/DesktopPlosOne/s/animal-research',
    '/DesktopPlosOne/s/contact',
    '/DesktopPlosOne/s/criteria-for-publication',
    '/DesktopPlosOne/s/editorial-and-peer-review-process',
    '/DesktopPlosOne/s/figures',
    '/DesktopPlosOne/s/getting-started',
    '/DesktopPlosOne/s/human-subjects-research',
    '/DesktopPlosOne/s/journal-information',
    '/DesktopPlosOne/s/materials-and-software-sharing',
    '/DesktopPlosOne/s/other-article-types',
    '/DesktopPlosOne/s/press-and-media',
    '/DesktopPlosOne/s/resources',
    '/DesktopPlosOne/s/reviewer-guidelines',
    '/DesktopPlosOne/s/revising-your-manuscript',
    '/DesktopPlosOne/s/section-editors',
    '/DesktopPlosOne/s/submission-guidelines',
    '/DesktopPlosOne/s/submit-now'
]

tax_browse_pages = JournalArticleSet.JournalArticleSet.build_article_paths('PLoSONE') \
                   + tax_browse_static_pages


@pytest.mark.usefixtures("driver_get")
class TestTaxBrowser:
    """
    test_taxbrowser.py: For PlosONE, validate the content and function of the Taxonomy Browser.
    """

    def test_smoke_plos_one_tax_browser_elements(self):
        """
        validate elements and style of the taxonomy browser,
        check back-and-forth navigation,
        check scrolling up and down in a single taxonomy panel using the buttons as well as the
        scrollbar/wheel,
        check clicking a link and displaying a SALP
        """
        chosen_page = random.choice(tax_browse_pages)
        pone = TaxBrowser(self.driver, chosen_page)
        logging.info('Validating the content, menu and controls of the Taxonomy Browser, '
                     'selected page: {0!r}'.format(chosen_page))
        pone.validate_taxbrowse_menu()
        pone.open_taxbrowser_validate_heading()
        pone.validate_level_display()
        pone.validate_navigation_controls()

    def test_core_plos_one_tax_browser_level_navigation(self):
        """
        The test navigates through the taxonomy browser and check the following:
        navigate at least four levels deep and check that the number of results displayed
        next to a term in the browser matches the results on the SALP if the link is clicked on
        """
        chosen_page = random.choice(tax_browse_pages)
        pone = TaxBrowser(self.driver, chosen_page)
        logging.info('Validating navigation levels of the Taxonomy Browser, '
                     'selected page: {0!r}'.format(chosen_page))
        pone.validate_level_navigation()
