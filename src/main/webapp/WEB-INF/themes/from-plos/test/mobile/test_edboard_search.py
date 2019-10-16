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
This test case validates the PLOS mobile site's home pages
for all PLOS journals and Plos Collections.
"""
__author__ = 'gtimonina@plos.org'

import logging
import pytest

from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.EditorialBoardPage import EditorialBoardPage


@pytest.mark.usefixtures("driver_get")
class TestEdBoardSearch:

    @pytest.fixture()
    def prepopulated_ed_page(self):
        """
        Fixture to navigate to Editorial Board search page with prepopulated search query
        """
        plos_page = PlosOneHomePage(self.driver)
        plos_page.click_menu_button()
        plos_page.click_about()
        plos_page.click_ed_board_link()
        ed_page = EditorialBoardPage(self.driver)

        ae_list, num_found = ed_page.retrieve_ae_from_solr()
        ed_page.page_ready(ae_list)

        return ed_page, ae_list, num_found

    def test_smoke_edboard_elements(self, prepopulated_ed_page):
        """
        Test case to validate Editorial Board search page elements
        """
        logging.info('Validating Editorial Board search: elements')
        ed_page, ae_list, num_found = prepopulated_ed_page

        logging.info('Validating page title')
        title = ed_page.get_page_title()
        subtitle = ed_page.get_page_subtitle()
        ed_page.validate_text_exact(title, 'Editorial Board', 'Incorrect page title')
        ed_page.validate_text_exact(subtitle, 'Academic Editors', 'Incorrect page subtitle')

        logging.info('Validating prepopulated search')

        names_list, editors_list, result_counters, item_text_list = ed_page.get_search_results()
        assert len(names_list) == 50
        ed_page.validate_text_contains(result_counters, 'Displaying 1-50 of')

        assert num_found, 'No results found for prepopulated search, expected: {0!s}' \
            .format(num_found)

        names_list, editors_list, result_counters, item_text_list = ed_page.get_search_results()
        num_displayed = min(len(ae_list), 50)
        assert len(names_list) == num_displayed
        ed_page.validate_text_contains(
                result_counters, 'Displaying 1-{0!s} of {1!s} Editors'
                .format(num_displayed, num_found))

    def test_smoke_edboard_search_pagination(self, prepopulated_ed_page):
        """
        Test case to validate Editorial Board search pagination
        """
        logging.info('Validating Editorial Board search: pagination')
        ed_page, ae_list, num_found = prepopulated_ed_page

        logging.info('Validating pagination for prepopulated search')

        assert num_found, 'No results found for prepopulated search, expected: {0!s}' \
            .format(num_found)

        prev_link_str, next_link_str, numbers_str_list, skip_str = \
            ed_page.get_search_pagination_text()
        ed_page.validate_text_exact(prev_link_str, 'Previous', 'Incorrect page title')
        ed_page.validate_text_exact(next_link_str, 'Next', 'Incorrect page title')
        ed_page.validate_text_exact(skip_str, '...', 'Incorrect text for skip pagination number')
        assert numbers_str_list[0:5] == ['1', '2', '3', '4', '5']
        last_page_number = numbers_str_list[-1]
        expected_last_page_number = int(num_found / 50) if num_found % 50 == 0 \
            else int(num_found / 50) + 1
        ed_page.validate_text_exact(last_page_number, str(expected_last_page_number),
                                    'Incorrect last page number')

        assert ed_page.get_active_page_number() == '1'
        result_counters = ed_page.get_result_counters_text()
        ed_page.validate_text_contains(
                result_counters, 'Displaying 1-50 of')

        assert not ed_page.link_is_enabled('previous')
        assert ed_page.link_is_enabled('next')

        ed_page.click_on_next_page()
        assert ed_page.get_active_page_number() == '2'

        result_counters = ed_page.get_result_counters_text()
        ed_page.validate_text_contains(
                result_counters, 'Displaying 51-100 of')

        ed_page.click_on_previous_page()
        assert ed_page.get_active_page_number() == '1'

        ed_page.click_on_page_with_number('5')
        assert ed_page.get_active_page_number() == '5'
        result_counters = ed_page.get_result_counters_text()
        ed_page.validate_text_contains(
                result_counters, 'Displaying 201-250 of')

        assert ed_page.link_is_enabled('next')
        assert ed_page.link_is_enabled('previous')

        ed_page.click_on_page_with_number(last_page_number)
        assert ed_page.get_active_page_number() == last_page_number
        assert not ed_page.link_is_enabled('next')
        assert ed_page.link_is_enabled('previous')

    @pytest.mark.parametrize("search_text, test_type", [
        ["John", "positive"],
        ["Bacteria", "positive"],
        ["women's health", "positive"],
        ["023j4", "negative"],
    ])
    def test_core_edboard_search(self, search_text, test_type, prepopulated_ed_page):
        """
        Test case to validate Editorial Board search results, positive and negative
        :param search_text: string, parameterized with pytest
        :param test_type: string, parameterized with pytest
        :param prepopulated_ed_page: fixture to navigate to Editorial Board search page
            with prepopulated search query
        :return: void function
        """

        logging.info('Validating Editorial Board search: results')
        ed_page, ae_list, num_found = prepopulated_ed_page

        logging.info('Validating Editorial board {0} search with text input: {1!r}'
                     .format(test_type, search_text))
        ed_page.enter_search_text(search_text)

        ed_page.click_on_page_subtitle()

        ed_page.click_on_search_button(test_type)

        ae_list, num_found = ed_page.retrieve_ae_from_solr(search_text)

        if num_found:
            names_list, editors_list, result_counters, item_text_list = ed_page.get_search_results()
            num_displayed = min(len(ae_list), 50)
            assert len(names_list) == num_displayed
            ed_page.validate_text_contains(
                    result_counters, 'Displaying 1-{0!s} of {1!s} Editors for \'{2}\''
                    .format(num_displayed, num_found, search_text))

            ae_subject_list_lowered = ' '.join(ae_list[0]['ae_subject']).lower()
            item_text_list_lowered = item_text_list[0].lower()

            found_in_names = search_text in ae_list[0]['ae_name'] and search_text in names_list[0]
            found_in_subject_list = search_text.lower() in ae_subject_list_lowered \
                and search_text.lower() in item_text_list_lowered
            assert found_in_names or found_in_subject_list, \
                'text to search {0!s} not found in names list and in subject list' \
                .format(search_text)

        else:
            result_counters = ed_page.get_result_counters_text()
            ed_page.validate_text_contains(
                    result_counters, 'No result found for {0!r}'
                    .format(search_text))

        ed_page, ae_list, num_found = prepopulated_ed_page

    # TODO: check and add next text once AMBR-482 gets resolved
    @pytest.mark.parametrize("search_text, test_type", [
        ["John", "positive"],
        ["023_4", "negative"],
    ])
    def rest_core_edboard_new_search_pagination(self, search_text, test_type, prepopulated_ed_page):
        """
        This test case was added due to error filed in AMBR-482:
        PLOS ONE Ed Board search pagination persists when new query entered (>page1)
        Test steps:
        1. Navigate to  ONE Editorial Board search page with prepopulated query results
        2. Click on last page results (last number page is active on pagination controls)
        3. Run new query
        Expected result: New search should run with new pagination (page number '1' is active)
        :param search_text:
        :param test_type:
        :return:
        """
        logging.info('Validating Editorial Board search: new pagination for new search')
        ed_page, ae_list, num_found = prepopulated_ed_page

        prev_link_str, next_link_str, numbers_str_list, skip_str = \
            ed_page.get_search_pagination_text()
        last_page_number = numbers_str_list[-1]

        ed_page.click_on_page_with_number(last_page_number)
        assert ed_page.get_active_page_number() == last_page_number

        # new search
        ed_page.enter_search_text(search_text)
        ed_page.click_on_search_button(test_type)

        ae_list, num_found = ed_page.retrieve_ae_from_solr(search_text)
        if num_found:  # positive search
            names_list, editors_list, result_counters, item_text_list = ed_page.get_search_results()
            num_displayed = min(len(ae_list), 50)
            expected_text = 'Displaying 1-{0!s} of {1!s} Editors for \'{2}\'' \
                .format(num_displayed, num_found, search_text)
            assert len(names_list) == num_displayed
            ed_page.validate_text_contains(result_counters, expected_text)
            assert ed_page.get_active_page_number() == '1'
            assert not ed_page.link_is_enabled('previous')
            assert ed_page.link_is_enabled('next')

        # validating pagination for new - prepopulated - search after query resetting:
        ed_page.reset_search()
        assert ed_page.get_active_page_number() == '1'
        assert not ed_page.link_is_enabled('previous')
        assert ed_page.link_is_enabled('next')
