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
This test case validates the Wombat Desktop Article Page Right Hand Column elements
"""
__author__ = 'jgray@plos.org'

import logging
import pytest

from .Pages.PlosBiologyArticleRHC import PlosBiologyArticleRHC
from .Pages.PlosMedicineArticleRHC import PlosMedicineArticleRHC
from .Pages.PlosCompBiolArticleRHC import PlosCompBiolArticleRHC
from .Pages.PlosGeneticsArticleRHC import PlosGeneticsArticleRHC
from .Pages.PlosPathogensArticleRHC import PlosPathogensArticleRHC
from .Pages.PlosNeglectedArticleRHC import PlosNeglectedArticleRHC
from .Pages.PlosOneArticleRHC import PlosOneArticleRHC
from .Pages.PlosClinicalTrialsArticleRHC import PlosClinicalTrialsArticleRHC


@pytest.mark.usefixtures("driver_get")
@pytest.mark.homepage
@pytest.mark.parametrize("journal_page", [
    PlosBiologyArticleRHC,
    PlosMedicineArticleRHC,
    PlosCompBiolArticleRHC,
    PlosGeneticsArticleRHC,
    PlosPathogensArticleRHC,
    PlosNeglectedArticleRHC,
    PlosOneArticleRHC,
    PlosClinicalTrialsArticleRHC,
])
class TestArticlePageRHC:

    def test_smoke_plos_article_rhc_download_article(self, journal_page):
        """
        Validates download PDF, citation and xml.
        DPRO-417
        AC:
        * PDF download with one click, whole button is target. - plos_page.validate_download_pdf()
        validates and compares link href to doi
        * Pull-down menu with link to Citation page and direct link to download XML.
        * Styled like Ambra, except with extra space below XML download trimmed a bit.
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        plos_page.assert_right_hand_column()
        plos_page.assert_download_div()
        plos_page.assert_download_pdf_button()
        plos_page.validate_download_pdf()
        plos_page.assert_download_menu()
        plos_page.validate_citation_download()
        plos_page.validate_xml_download()
        # DPRO-420
        # TODO: Logo linked to Crossmark for current doi, gives status popup when clicked.
        # Same styling and implementation as Ambra.
        plos_page.moveto_section_header_doi()
        plos_page.assert_crossmark_logo()

    def test_smoke_plos_article_rhc_print(self, journal_page):
        """
        Validates print functionality
        DPRO-418
        AC:
        print stylesheet works like Ambra  # I don't have a hook to verify this stylesheet
        ezreprint link works like Ambra (pre-populating odysseypress page) validating request
        type, doi, title  Styled like Ambra, with extra bottom spacing in menu trimmed a bit.
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        plos_page.moveto_section_header_doi()
        plos_page.assert_print_div()
        plos_page.validate_local_print()
        plos_page.validate_ezreprint()

    def test_core_plos_article_rhc_related_articles(self, journal_page):
        """
        DPRO-421
        AC:
        validate related articles links
        example: 10.1371/journal.pmed.0020007
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        plos_page.validate_related_articles()

    def test_core_plos_article_rhc_in_collection(self, journal_page):
        """
        DPRO-422
        AC:
        Styled and implemented like Ambra (Tooltip = Browse the Open-Access Collection;
            link: blue, underlined, 12pxi, arial, font-weight: 400; Heading: 14px arial,
            font-weight: 700).
        Links to specific Collection, not just Collections homepage. - Compare link text to h1 on
            target page. link text should be contained within the h1 heading of the target page.
        example: 10.1371/journal.pone.0008012
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        plos_page.validate_included_in_the_following_collection()

    def test_smoke_plos_article_rhc_share(self, journal_page):
        """
        DPRO-419
        AC:
        validate social media share functionality
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        plos_page.assert_share_menu()
        plos_page.validate_share_menu_items()

    def test_core_plos_article_rhc_subject_area(self, journal_page):
        """
        Validate subject area elements and style
        Check that top 8 subject areas displayed, ordered by greatest weight to heaviest weight
        Validate Links to search results for subject area
        Tooltip for each subject area reads "Search for articles in the subject area:'Subject area'"
        Validate Feedback mechanism (spec'd in DPRO-380):
        feedback on whether or not a subject area term is appropriately assigned to an article:
        - Each term has a target icon to the right.
        - Hovering over the target icon turns it blue.
        - Clicking the target icon opens the feedback box.
        - Clicking yes or no switches box state to "Thanks for your feedback"
        """
        plos_page = journal_page(self.driver)
        plos_page.page_ready('sign in')
        article_type_text = plos_page.return_article_type()
        no_subject_area_section = ['CORRECTION', 'RETRACTION', 'EXPRESSION OF CONCERN']
        if article_type_text not in no_subject_area_section:
            logging.info("Article should have subject areas: they should display on RHC.")
            plos_page.assert_right_hand_column()
            plos_page.validate_subject_areas()
        logging.info('test finished')
