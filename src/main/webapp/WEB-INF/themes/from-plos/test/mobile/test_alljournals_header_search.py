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
test_alljournals_header_search: This test exercises the PLOS mobile header search
Search terms and search term roots are specified externally in the resources.py file
"""
import logging
import pytest

from . import resources
import time

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosComputationalHomePage import PlosComputationalHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("page_under_test", [
    PlosBiologyHomePage,
    PlosMedicineHomePage,
    PlosComputationalHomePage,
    PlosGeneticsHomePage,
    PlosPathogensHomePage,
    PlosNeglectedHomePage,
    PlosOneHomePage,
])
class TestHeaderSearch:
    def test_header_test_search(self, page_under_test):
        """
        Method to validate search: elements and functionality,
        :param page_under_test: journals home page
        :return: void function
        """
        plos_page = page_under_test(self.driver)
        journal_url = plos_page.get_current_url()
        logging.info('Validating header search for {0!s}'.format(journal_url))
        plos_page.click_on_journal_logo()

        # For PLOS One only - need to validate the subject area browser
        if page_under_test == PlosOneHomePage:
            browse_topic_menu = plos_page._get(plos_page._browse_topic_menu)
            assert 'Browse Topics' == browse_topic_menu.text
        else:
            logging.debug('Not the PLOS One Journal, not looking for Subject Area Browser Link')

        search_icon = plos_page._get(plos_page._search_icon)
        search_icon.click()
        search_cancel_button = plos_page._get(plos_page._search_cancel_button)
        search_execute_button = plos_page._get(plos_page._search_execute_button)
        # Trying to stave off race condition allowing search widgets to animate in
        time.sleep(.5)
        search_cancel_button.click()
        search_icon = plos_page._get(plos_page._search_icon)
        search_icon.click()
        plos_page._wait_for_element(plos_page._get(plos_page._search_term_input_field))
        search_term = resources.search_term
        search_input = plos_page._get(plos_page._search_term_input_field)
        search_input.clear()
        search_input.send_keys(search_term)
        search_execute_button.click()
        search_result_title_list = plos_page._gets(plos_page._search_result_titles)
        assert resources.search_term.lower() in search_result_title_list[0].text.lower(), \
            'Search String {0!s} Not Found In Simple Search Result Set'.format(search_term)
        plos_page._get(plos_page._search_filter_button)
        plos_page.click_on_journal_logo()
