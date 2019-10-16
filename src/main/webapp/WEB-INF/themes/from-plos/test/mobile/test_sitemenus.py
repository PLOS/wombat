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
This test case validates the PLOS mobile site's menu link contents
"""

import pytest

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosComputationalHomePage import PlosComputationalHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage

__author__ = 'jgray@plos.org'
__original_author = 'jkrzemien@plos.org'


@pytest.mark.usefixtures("driver_get")
class TestSiteMenus:
    @pytest.mark.parametrize("page_under_test", [
        PlosBiologyHomePage,
        PlosMedicineHomePage,
        PlosNeglectedHomePage,
        PlosPathogensHomePage,
    ])
    def test_plos_site_menu_with_manuscript(self, page_under_test):
        """
        Validate Browse, Publish, About and Submit your manuscript links in mobile menu
        """
        plos_page = page_under_test(self.driver)
        plos_page.click_menu_button()
        plos_page.click_browse()
        plos_page.validate_browse_links()
        plos_page.click_browse()
        plos_page.click_submissions()
        plos_page.validate_submissions_links()
        plos_page.click_submissions()
        plos_page.click_policies()
        plos_page.validate_policies_links()
        plos_page.click_policies()
        plos_page.click_manu_review()
        plos_page.validate_manu_review_links()
        plos_page.click_manu_review()
        plos_page.click_about()
        plos_page.validate_about_links()
        plos_page.click_about()
        plos_page.validate_submit_your_manuscript_section()
        plos_page.click_get_started_link()

    @pytest.mark.parametrize("page_under_test", [
        PlosComputationalHomePage,
        PlosGeneticsHomePage,
    ])
    def test_plos_site_menu_no_manuscript(self, page_under_test):
        """
        Validate Browse, Publish, About in mobile menu
        """
        plos_page = page_under_test(self.driver)
        plos_page.click_menu_button()
        plos_page.click_browse()
        plos_page.validate_browse_links()
        plos_page.click_browse()
        plos_page.click_submissions()
        plos_page.validate_submissions_links()
        plos_page.click_submissions()
        plos_page.click_policies()
        plos_page.validate_policies_links()
        plos_page.click_policies()
        plos_page.click_manu_review()
        plos_page.validate_manu_review_links()
        plos_page.click_manu_review()
        plos_page.click_about()
        plos_page.validate_about_links()
        plos_page.click_about()
        plos_page.click_menu_button()

    @pytest.mark.parametrize("page_under_test", [
        PlosOneHomePage,
    ])
    def test_plos_one_site_menu_with_manuscript(self, page_under_test):
        """
        Validate Browse, Publish, About and Submit your manuscript links in mobile menu
        """
        plos_page = page_under_test(self.driver)
        plos_page.click_menu_button()
        plos_page.click_submissions()
        plos_page.validate_submissions_links()
        plos_page.click_submissions()
        plos_page.click_policies()
        plos_page.validate_policies_links()
        plos_page.click_policies()
        plos_page.click_manu_review()
        plos_page.validate_manu_review_links()
        plos_page.click_manu_review()
        plos_page.click_about()
        plos_page.validate_about_links()
        plos_page.click_about()
        plos_page.validate_submit_your_manuscript_section()
        plos_page.click_get_started_link()
        plos_page.click_menu_button()
        plos_page.click_callout_link()
