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
This test case validates the PLOS desktop site's Site Content pages.

"""

import logging
import pytest

from .Pages.PlosBiologySCPage import PlosBiologySCPage
from .Pages.PlosCompBiolSCPage import PlosCompBiolSCPage
from .Pages.PlosGeneticsSCPage import PlosGeneticsSCPage
from .Pages.PlosMedicineSCPage import PlosMedicineSCPage
from .Pages.PlosNeglectedSCPage import PlosNeglectedSCPage
from .Pages.PlosOneSCPage import PlosOneSCPage
from .Pages.PlosPathogensSCPage import PlosPathogensSCPage

__author__ = 'jgray@plos.org'


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("plos_page", [
    PlosBiologySCPage,
    PlosCompBiolSCPage,
    PlosGeneticsSCPage,
    PlosMedicineSCPage,
    PlosNeglectedSCPage,
    PlosOneSCPage,
    PlosPathogensSCPage,
])
class TestSiteContentPage:
    def test_sc_cms_blocks(self, plos_page):
        """
        Validates the presence of the following CMS derived elements:
          Main page body
          Main page div
          Left Hand Navigation Menu div
          Left Hand Navigation UL
        :param plos_page: populated via DDT
        :return: 0 on success
        """
        plos_page = plos_page(self.driver)
        logging.info('test_sc_cms_blocks: navigating to {0}'.format(plos_page.get_current_url()))
        plos_page.validate_cms_body()
        plos_page.validate_cms_main_div()
        plos_page.validate_main_page_heading()
        try:
            plos_page.validate_cms_two_col_layout()
            expect_nav = 1
        except AssertionError:
            plos_page.validate_cms_full_width_layout()
            expect_nav = 0
        if expect_nav == 1:
            logging.info('validating CMS 2-column layout for page: '
                         '{0}'.format(plos_page.get_current_url()))
            plos_page.validate_lh_nav()
            plos_page.validate_nav_list()
