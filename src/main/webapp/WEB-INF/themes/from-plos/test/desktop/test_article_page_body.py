# !/usr/bin/env python3
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
This test case validates the Wombat Desktop Article Page article tab body elements
"""

import pytest

from .Pages.PlosBiologyArticleBody import PlosBiologyArticleBody
from .Pages.PlosMedicineArticleBody import PlosMedicineArticleBody
from .Pages.PlosCompBiolArticleBody import PlosCompBiolArticleBody
from .Pages.PlosGeneticsArticleBody import PlosGeneticsArticleBody
from .Pages.PlosPathogensArticleBody import PlosPathogensArticleBody
from .Pages.PlosNeglectedArticleBody import PlosNeglectedArticleBody
from .Pages.PlosOneArticleBody import PlosOneArticleBody
from .Pages.PlosClinicalTrialsArticleBody import PlosClinicalTrialsArticleBody

__author__ = 'jgray@plos.org'


@pytest.mark.usefixtures("driver_get")
class TestArticleBody:
    @pytest.mark.parametrize("journal_article_page", [
            PlosBiologyArticleBody,
            PlosMedicineArticleBody,
            PlosCompBiolArticleBody,
            PlosGeneticsArticleBody,
            PlosPathogensArticleBody,
            PlosNeglectedArticleBody,
            PlosOneArticleBody,
            PlosClinicalTrialsArticleBody,
        ])
    def test_plos_article_body(self, journal_article_page):
        plos_page = journal_article_page(self.driver)

        """
        DPRO-479
        AC:
        article amendment notice link displays (and is styled appropriately) only when an article 
        amendment exists. Bold for corrections, red and bold for retractions and expressions of 
        concern - NOT testing styles
        All article sections included in nav - DONE
        Interaction identical Ambra (floating behavior and animated scroll behavior) - NOT included
        in these tests 
        Styling matches new design - NOT included in automation tests
        Media curation always displays, even if no links yet to display. 
        Note that this is a change from the current implementation. - DONE
        when scrolling all the way to the bottom, there isn't an awkward collision with the footer 
        - NOT included in this test
        Reader Comments link includes a parenthetical count of how many comments have been left 
        on the article. - DONE
        Media Curation link includes a parenthetical count of how many media links exist for 
        the article. - DONE

        Article Amendment notice (#) - Number only for corrections
        Article sections
        ---------
        Reader Comments (#)
        Media Curation (#)
        Figures
        """

        plos_page.validate_article_body_nav_headings()
        self.validate_comments_count(plos_page)
        plos_page.validate_media_curation_link_number()

        """
        AC:
        styled and implemented like Ambra
        grey box for corrections, pink box for retractions and EOCs
        Corrections icon displays
        EOC and Retraction icon displays
        For corrections, correction "citation" displays, including date, DOI and link to 
        View Correction.
        For Retractions and EOCs, the full amendment text displays, followed by date, 
        "citation," DOI, and link to amendment itself.
        """

        plos_page.validate_article_amendment_notice()

        """
        DPRO-481
        AC
        All special characters display correctly - NOT included in automation tests
        All equations display correctly (not mathjax, just images) - NOT included in automation 
        tests
        Text headers styled appropriately per Ambra implementation - include insofar as one 
        validates all headers as h? tags 
        Block quotes display appropriately per Ambra implementation (see below for examples) 
          - NOT included
        Long strings of text wrap appropriately within the column. For example, long genetic 
        sequences without spaces sometimes present problems for display within the main article
        column. - NOT included in automation tests
        clicking anchor links updates the URL to reflect anchor. 
          - NOT included in automation tests
        clicking the back button after clicking an anchor link returns the user to where they 
        came from (and returns URL to previous stat) - NOT included in automation tests
        anchors have to work with floating article header (anchor destination needs to not be 
        covered by floating header) - NOT included in automation tests
        """

        plos_page.validate_article_body_headings()

        """
        DPRO-792
        Tests for the presence of each article metadata section heading, as well as the presence 
        of the surrounding metadata div.
        This test does not include validation of the text content in each article metadata section.
        """
        plos_page.validate_article_metadata_headings()

        """
        DPRO-801
        Test if figure ligtbox pops up when the user click on the:
        figure in the inline figure, figure in the figure carousel and
        figure link in the article floating nav
        """

        plos_page.validate_article_lightbox_image()

    @staticmethod
    def validate_comments_count(plos_page):
        expected_comments_count = \
            plos_page.get_rhino_comments_count(plos_page.extract_page_escaped_doi())
        actual_page_comments_count = plos_page.get_page_comments_count()

        assert actual_page_comments_count == expected_comments_count, \
            'Title comment count: {0!s} is not the expected: ' \
            '{1!s}'.format(actual_page_comments_count, expected_comments_count)
