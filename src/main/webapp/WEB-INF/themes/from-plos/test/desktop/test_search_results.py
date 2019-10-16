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

import pytest

from .Pages.AkitaLoginPage import AkitaLoginPage
from .Pages.PlosBiologyArticleRHC import PlosBiologyArticleRHC
from .Pages.PlosBiologySearchResults import PlosBiologySearchResults
from .Pages.PlosClinicalTrialsArticleRHC import PlosClinicalTrialsArticleRHC
from .Pages.PlosClinicalTrialsSearchResults import PlosClinicalTrialsSearchResults
from .Pages.PlosCollectionsSearchResults import PlosCollectionsSearchResults
from .Pages.PlosCompBiolArticleRHC import PlosCompBiolArticleRHC
from .Pages.PlosCompBiolSearchResults import PlosCompBiolSearchResults
from .Pages.PlosGeneticsArticleRHC import PlosGeneticsArticleRHC
from .Pages.PlosGeneticsSearchResults import PlosGeneticsSearchResults
from .Pages.PlosMedicineArticleRHC import PlosMedicineArticleRHC
from .Pages.PlosMedicineSearchResults import PlosMedicineSearchResults
from .Pages.PlosNeglectedArticleRHC import PlosNeglectedArticleRHC
from .Pages.PlosNeglectedSearchResults import PlosNeglectedSearchResults
from .Pages.PlosOneArticleRHC import PlosOneArticleRHC
from .Pages.PlosOneSearchResults import PlosOneSearchResults
from .Pages.PlosPathogensArticleRHC import PlosPathogensArticleRHC
from .Pages.PlosPathogensSearchResults import PlosPathogensSearchResults

__author__ = 'achoe@plos.org'

data_for_parameters = [
    (PlosBiologySearchResults),
    (PlosCompBiolSearchResults),
    (PlosGeneticsSearchResults),
    (PlosPathogensSearchResults),
    (PlosNeglectedSearchResults),
    (PlosMedicineSearchResults),
    (PlosOneSearchResults),
    (PlosCollectionsSearchResults),
    (PlosClinicalTrialsSearchResults),
]

@pytest.mark.usefixtures("driver_get")
class TestSearchResults:
    @pytest.mark.parametrize("plos_article_rhc_page, plos_search_results_page",[
        (PlosBiologyArticleRHC, PlosBiologySearchResults),
        (PlosCompBiolArticleRHC, PlosCompBiolSearchResults),
        (PlosGeneticsArticleRHC, PlosGeneticsSearchResults),
        (PlosPathogensArticleRHC, PlosPathogensSearchResults),
        (PlosNeglectedArticleRHC, PlosNeglectedSearchResults),
        (PlosMedicineArticleRHC, PlosMedicineSearchResults),
        (PlosOneArticleRHC, PlosOneSearchResults),
        (PlosClinicalTrialsArticleRHC, PlosClinicalTrialsSearchResults),
    ])
    def test_subject_area_search(self, plos_article_rhc_page, plos_search_results_page):
        article_rhc_page = plos_article_rhc_page(self.driver)
        article_rhc_page.page_ready('sign in')
        article_type_text = article_rhc_page.return_article_type()
        no_subject_area_section = ['CORRECTION', 'RETRACTION', 'EXPRESSION OF CONCERN']
        if article_type_text not in no_subject_area_section:
            article_rhc_page.click_subject_area_link()
            search_results_page = plos_search_results_page(self.driver)
            # search_results_page = plos_search_results_page(self.getDriver(browser))
            search_results_page.validate_subject_area_filter()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_results(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_search_results_black_bar()
        plos_page.validate_prepopulated_search_term()
        plos_page.validate_filter_journal_pill(plos_page.journal_title)

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_sort_results_by_date(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_sort_results_by_date()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_sort_results_by_date_second_page(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_sort_results_by_date_second_page()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_term_bar(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.perform_new_search()
        plos_page.validate_prepopulated_search_term()
        plos_page.validate_control_bar_search()

        # plos_page.click_advanced_search_button()
        """
        DPRO-1348 - the advanced search link points to the corresponding production site.
        It was determined that we do not have a way to link from wombat to an environment-specific
        ambra instance. This test will end on the live site when run against DPRO.
        """

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_term_bar_filters(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_journal_active_filter_display()
        plos_page.validate_journal_inactive_filter_display()
        """
        DPRO-1599
        AC:

        Header for Journal section reads "Journal" in left-hand filters column.
        Search results page defaults to only include results from the current journal.
        User sees a list of all journals.
        Journals ordered by number of hits (descending)
        Should not be slower than current implementation
        """

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_page_select(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_items_per_page_select()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_pagination(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_search_pagination()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_fields(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_advanced_search_fields()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_query(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_advanced_search_query_condition()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_edit_link(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_edit_query_link()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_search_row(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_add_and_remove_advanced_search_row()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_header_link_logout_state(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.validate_advanced_search_header_link()

    @pytest.mark.parametrize("journal_page", data_for_parameters)
    def test_search_advance_header_link_loggedin_state(self, journal_page):
        driver = self.driver
        plos_page = journal_page(driver)
        plos_page.click_sign_in()
        sign_in_page = AkitaLoginPage(driver)
        sign_in_page.successful_login()
        plos_page.validate_advanced_search_header_link()
