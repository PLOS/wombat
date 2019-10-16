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

from datetime import datetime
import json
import logging
import os
import random
import re
import requests
import time
import wget

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond
from selenium.webdriver.support.select import Select

from ...Base import Config
from .PaginationAjax import PaginationAjax
from ..resources import dx_doi_url
from .WombatPage import WombatPage

__author__ = 'achoe@plos.org'

search_terms = [
    'organic compound'
]


class SearchResultsPage(WombatPage):
    """
    Model the Search Results page
    """
    if Config.collections_url == Config.base_url:
        url_suffix = '/DesktopPlosCollections/'
    else:
        url_suffix = ''

    base_url = Config.base_url
    CURRENT_URL = (Config.collections_url + url_suffix)
    PROD_URL = 'http://www.ploscollections.org/'

    def __init__(self, driver, url_suffix=url_suffix):
        super(SearchResultsPage, self).__init__(driver, url_suffix)

        # Locators - instance members

        self._search_results_controls = (By.CLASS_NAME, 'search-results-controls')
        self._search_bar = (By.ID, 'controlBarSearch')
        self._search_button = (By.ID, 'searchFieldButton')
        self._advanced_search_button = (By.ID, 'advancedSearchLink')
        self._filter_unavailable_message = (By.CLASS_NAME, 'search-results-disabled-message')
        self._message_advanced_search_link = (
            By.XPATH, '//span[@class="search-results-disabled-message"]/a')
        self._filter_button = (By.ID, 'filterByButton')
        self._clear_filter_button = (By.ID, 'clearAllFiltersButton')
        self._filter_block = (By.CLASS_NAME, 'filter-block')
        self._sort_order_dropdown_list = (By.ID, 'sortOrder')
        self._date_filter = (By.ID, 'filter-date')
        self._journal_filter = (By.CLASS_NAME, 'filter-item')
        self._number_of_results = (By.CLASS_NAME, 'results-number')
        self._original_search_term = (By.XPATH, '//div[@class="results-number"]/strong')
        self._search_results_list = (By.ID, 'searchResultsList')
        self._article_info_list = (By.XPATH, '//dl[@id="searchResultsList"]/dd/p[2]')
        self._search_result_none_found = (By.CLASS_NAME, 'search-results-none-found')
        self._journal_filter_section = (By.XPATH, '//aside[@id="searchFilters"]/div[1]')
        self._journal_active_filter_list = (By.ID, 'active-filterJournals')
        self._journal_inactive_filter_list = (By.ID, 'inactive-filterJournals')
        self._results_per_page_dropdown = (By.ID, 'resultsPerPageDropdown')
        self._results_per_page_value = (By.XPATH, '//select[@id="')
        self._article_pagination_links = (By.ID, 'article-pagination')
        self._previous_page_link = (By.ID, 'prevPageLink')
        self._next_page_link = (By.ID, 'nextPageLink')
        self._footer_nav_special = (By.XPATH, '//footer[@id="pageftr"]/div[1]/div[1]/p[1]')
        self._advanced_search_query_box = (By.ID, 'unformattedQueryId')
        self._advanced_search_date_categories = ['publication_date', 'accepted_date',
                                                 'received_date']

    # POM Actions

    def validate_search_bar(self):
        print('Validating search bar...')
        self._get(self._search_bar)
        return self

    def validate_search_button(self):
        print('Validating search button...')
        self._get(self._search_button)
        return self

    def validate_advanced_search_button(self):
        print('Validating advanced search button...')
        self._get(self._advanced_search_button)
        return self

    def click_sort_order_dropdown(self):
        self._get(self._sort_order_dropdown_list).click()
        return self

    def validate_prepopulated_search_term(self):
        self.wait_until_ajax_complete()
        original_search_term = self._get(self._original_search_term)
        prepopulated_search_term = self._get(self._search_bar).get_attribute("value")
        print(prepopulated_search_term)
        assert original_search_term.text == prepopulated_search_term, '{0} is not equal to {1}'\
            .format(original_search_term, prepopulated_search_term)

    def validate_search_results_black_bar(self):
        self.validate_search_bar()
        self.validate_search_button()
        self.validate_advanced_search_button()

    def select_sort_order(self, option):
        Select(self._get(self._sort_order_dropdown_list)).select_by_value(option)
        return self

    def validate_sort_results_by_date_newest(self):
        self.select_sort_order('DATE_NEWEST_FIRST')
        self.wait_until_ajax_complete()
        date_list_newest_first = self.extract_date_list()
        assert sorted(date_list_newest_first, reverse=True) == date_list_newest_first, \
            'Sorted list: |{0}| does not match with actual list: |{1}|'.format(
                    sorted(date_list_newest_first, reverse=True),
                    date_list_newest_first)

    def validate_sort_results_by_date_oldest(self):
        self.select_sort_order('DATE_OLDEST_FIRST')
        self.wait_until_ajax_complete()
        date_list_oldest_first = self.extract_date_list()
        assert sorted(date_list_oldest_first) == date_list_oldest_first, \
            'Sorted list: |{0}| does not match with actual list: |{1}|'.format(
                    sorted(date_list_oldest_first),
                    date_list_oldest_first)

    def validate_sort_results_by_date_newest_second_page(self):
        self.select_sort_order('DATE_NEWEST_FIRST')
        self.wait_until_ajax_complete()
        # Click on next page, and verify that sorting continues
        num_results = self._get(self._number_of_results)
        if int(num_results.text.split()[0].replace(',', '')) > 15:
            pagination = PaginationAjax(self._driver)
            pagination.click_next_page()
            self.wait_until_ajax_complete()
            date_list_newest_first = self.extract_date_list()
            assert sorted(date_list_newest_first, reverse=True) == date_list_newest_first, \
                'Sorted list: |{0}| does not match with actual list: |{1}|' \
                .format(sorted(date_list_newest_first, reverse=True), date_list_newest_first)

    def validate_sort_results_by_date_oldest_second_page(self):
        self.select_sort_order('DATE_OLDEST_FIRST')
        self.wait_until_ajax_complete()
        # Click on next page, and verify that sorting continues
        num_results = self._get(self._number_of_results)
        if int(num_results.text.split()[0].replace(',', '')) > 15:
            pagination = PaginationAjax(self._driver)
            pagination.click_next_page()
            self.wait_until_ajax_complete()
            date_list_oldest_first = self.extract_date_list()
            assert sorted(date_list_oldest_first) == date_list_oldest_first, \
                'Sorted list: |{0}| does not match with actual list: |{1}|'\
                .format(sorted(date_list_oldest_first), date_list_oldest_first)

    def validate_sort_results_by_date(self):
        self.validate_sort_results_by_date_newest()
        self.validate_sort_results_by_date_oldest()

    def validate_sort_results_by_date_second_page(self):
        self.validate_sort_results_by_date_newest_second_page()
        self.validate_sort_results_by_date_oldest_second_page()

    def extract_date_list(self):
        # article_info_list = self._gets(self._article_info_list)
        # date_list = []
        # for article_info in article_info_list:
        #   print date_list
        #   print article_info.text
        #   date_list.append(datetime.strptime(article_info.text.split('|')[1][11:22], '%d %b %Y'))
        # return date_list
        search_results_list = self._get(self._search_results_list)
        article_results = search_results_list.find_elements_by_tag_name('dd')
        date_list = []
        for article_result in article_results:
            pub_date_text = article_result.find_element_by_id(
                    'article-result-{0}-date'.format(article_results.index(article_result)))
            pub_date = re.sub('published ', '', pub_date_text.text).rstrip(' |')
            date_list.append(datetime.strptime(pub_date, '%d %b %Y'))
        return date_list

    def validate_date_filter(self):
        """
        Note: The ability to select a specific filter is not yet available on the wombat search 
        results page.
        This method will validate the filtering logic. Once the date filtering UI is implemented, 
        we can revisit this and call use this method to test the date filtering.
        """
        date_filter_pill = self._get(self._date_filter)
        start_date = datetime.strptime(
                date_filter_pill.text.split('TO')[0].rstrip(' ').replace(',', ''), '%b %d %Y')
        end_date = datetime.strptime(
                date_filter_pill.text.split('TO')[1].lstrip(' ').replace(',', ''), '%b %d %Y')
        article_pub_dates = self.extract_date_list()
        article_pub_date = random.choice(article_pub_dates)
        assert start_date <= article_pub_date <= end_date, \
            'Article published date: {0} is not within date range filter: {1}' \
            .format(article_pub_date, date_filter_pill.text)

    def select_inactive_journal_filter(self):
        inactive_journal_filters = self._get(self._journal_inactive_filter_list)
        if inactive_journal_filters.text != '':
            inactive_journal_filter_items = inactive_journal_filters.find_elements_by_tag_name('li')
            journal_filter_to_be_selected = random.choice(inactive_journal_filter_items)
            journal_filter_to_be_selected.find_element_by_xpath('./a/input').click()

    def validate_journal_active_filter_display(self):
        # Check to see if there are any active journal filters.
        active_journal_filters_is_present = self.is_element_present('active-filterJournals')
        if active_journal_filters_is_present:
            # Extract journal filters into a list
            active_journal_filters = self._get(self._journal_active_filter_list)
            active_journal_filter_items = active_journal_filters.find_elements_by_tag_name('li')
            # Select one of the applied journal filters at random and verify that is it checked 
            # and has gray text with font weight 700
            applied_journal_filter = random.choice(active_journal_filter_items)
            applied_journal_filter_checkbox = applied_journal_filter.find_element_by_xpath(
                    './a/input')
            assert applied_journal_filter_checkbox.get_attribute(
                    'checked'), 'Journal Filter is applied but not checked.'
            applied_journal_filter_text = applied_journal_filter.find_element_by_xpath('./a/span')
            self._check_element_font_weight(applied_journal_filter_text, 'bold')
            applied_journal_filter_text_color = \
                applied_journal_filter_text.value_of_css_property("color")
            assert applied_journal_filter_text_color in \
                   ('rgb(96, 96, 96)', 'rgba(96, 96, 96, 1)'), \
                   'font color is not rgb(96, 96, 96) or rgba(96, 96, 96, 1): {0}'\
                   .format(applied_journal_filter_text_color)

    def validate_journal_inactive_filter_display(self):
        """
        DPRO-1599
        AC:
    
        Header for Journal section reads "Journal" in left-hand filters column.
          - Done
        Search results page defaults to only include results from the current journal.
          - already handled by validate_filter_journal_pill()
        User sees a list of all journals.
          - Note: if there are no hits for a journal, it will not appear in the list of filters
        Journals ordered by number of hits (descending)
          - Done
        Should not be slower than current implementation
        """
        # Validate that the filter header reads "Journal"
        journal_filter_section = self._get(self._journal_filter_section)
        journal_filter_section_header = journal_filter_section.find_element_by_tag_name('h3')
        expected_header_text = 'Journal'
        assert expected_header_text == journal_filter_section_header.text, \
            'Section header text {0} does not match expected text {1}'\
                .format(journal_filter_section_header.text, expected_header_text)

        # Validate that inactive filter items are ordered by the number of hits
        # Extract the inactive journal filters into a list
        inactive_journal_filters = self._get(self._journal_inactive_filter_list)
        if inactive_journal_filters.text != '':
            journal_filter_items = inactive_journal_filters.find_elements_by_tag_name('li')
            journal_filter_list = []
            for journal_filter_item in journal_filter_items:
                filter_item_link = journal_filter_item.find_element_by_tag_name('a')
        if filter_item_link.get_attribute('data-filter-param') == 'filterJournals' \
                and journal_filter_item.get_attribute('data-visibility') != 'none':
            journal_filter_list.append(filter_item_link.text)
            print(journal_filter_list)
            # Extract number of hits per journal and verify that these values are sorted by hits
            # descending
            journal_hits = []
            for journal_filter in journal_filter_list:
                journal_hits.append(int(journal_filter.rsplit(None, 1)[1]
                                        .strip('()').replace(',', '')))
            assert journal_hits == sorted(journal_hits, reverse=True), \
                '{0} hits per journal are not ordered descending'.format(journal_hits)
        else:
            print('All journal filters are active.')

    def validate_subject_area_filter(self):
        """
        DPRO-1270
        Acceptance Criteria:
    
        Searches URLs that contain a filterSubjects parameter should only show results from that 
        subject. Searches URLs that contain a filterSubjects parameter should show the subject as 
        filter label in the UI.
        If no filterSubjects param is present, search results should not be constrained by subject.
        Mobile search functionality should be unaffected
    
        """
        # Note: subject area filtering in the UI has not been implemented yet. As of now, subject 
        # area filters are applied when clicking on the Article RHC subject area links, or when 
        # using advanced search. We will need to add another method to validate the selection of 
        # the subject area filter in the UI when it is implemented.
        # Also, only one subject area will be supported initially

        # Get the subject area/category text from the search results page
        active_filter_list = self._get(self._filter_block)
        active_filter_items = active_filter_list.find_elements_by_class_name('filter-item')
        active_subject_area_filters = []
        for filter_item in active_filter_items:
            filter_type = filter_item.find_element_by_tag_name('a').get_attribute(
                    'data-filter-param-name')
            if filter_type == 'filterSubjects':
                active_subject_area_filters.append(filter_item.text.rstrip(' '))
        print(active_subject_area_filters)
        subject_area_text = random.choice(active_subject_area_filters)

        # Pick a random article doi from the search results page
        active_search_results_list_is_present = self.is_element_present('searchResultsList')
        if active_search_results_list_is_present:
            search_results_list = self._get(self._search_results_list)
            article_doi_text = random.choice(
                    search_results_list.find_elements_by_class_name(
                            'search-results-doi')).text.lstrip(
                    dx_doi_url)
            article_doi_text = article_doi_text.replace('/', '++')
            # Get rhino endpoint for this article doi and find its categories
            categories_url = Config.rhino_url + '/articles/' + article_doi_text + '/categories'
            logging.info('Using endpont: {0}'.format(categories_url))
            response = requests.get(categories_url)

            # check if the article ingested in test environment, if not - fetch and ingest
            if 'Article not found' in response.text:
                self.fetch_and_ingest_article(article_doi_text)
                # Get rhino endpoint for this article doi and find its categories
                categories_url = Config.rhino_url + '/articles/' + article_doi_text + '/categories'
                logging.info('Using endpont: {0}'.format(categories_url))
                response = requests.get(categories_url)
                self.delete_zip(article_doi_text)

            json_response = json.loads(response.text)
            categories = json_response

            # Check each category, and determine if the subject area is within it. If so, increment
            # the counter.
            counter = 0
            found_list = \
                [category for category in categories if subject_area_text in category['path']]
            counter = len(found_list)
            assert counter >= 1, 'Article {0} does not have subject area {1}. ' \
                                 'Search results did not constrain by the subject area filter' \
                .format(article_doi_text, subject_area_text)

            # Validate the results text - should be 'results' instead of 'results for ...'
            number_found_text = self._get(self._number_of_results)

            # DPRO-2767 Uncomment below line once issue is resolved
            # assert 'results for' not in number_found_text.text, 
            # 'results text should not contain for'

    def extract_article_list(self):
        search_results_list = self._get(self._search_results_list)
        article_list = search_results_list

    def perform_new_search(self):
        self._get(self._search_bar).clear()
        self._get(self._search_bar).send_keys(random.choice(search_terms))
        self._get(self._search_button).click()
        return self

    def validate_control_bar_search(self):
        self.perform_new_search()
        self.validate_total_number_returned()
        return self

    def validate_total_number_returned(self):
        self.wait_until_ajax_complete()
        prepopulated_search_term = self._get(self._search_bar).get_attribute("value")
        self.validate_total_num_search_results_displayed(prepopulated_search_term)
        results_text = self._get(self._number_of_results).text
        total_num = int(results_text.split()[0].replace(',', ''))
        if total_num == 1:
            assert '1 result' in results_text, \
                'Result number text is {0!r}, it was expected to start with {1!r}'.format(
                        results_text, '1 result')
            # def click_advanced_search_button(self):
            #   original_search_term = self._get(self._original_search_term)
            #   original_search_term_text = original_search_term.text
            #   advanced_search_button = self._get(self._advanced_search_button)
            #   advanced_search_button.click()
            #   advanced_search_term = self._get(self._advanced_search_query_box)
            #   assert original_search_term_text == advanced_search_term.text, 
            # '%s is not equal to %s' % (original_search_term_text, advanced_search_term.text)
        self._driver.back()

    def click_filter_button(self):
        filter_button = self._get(self._filter_button)
        filter_button.click()
        return self

    def click_clear_all_filters_button(self):
        clear_all_filters_button = self._get(self._clear_filter_button)
        clear_all_filters_button.click()
        return self

    def validate_filter_journal_pill(self, journal_title):
        if journal_title != 'PLOS Collections':
            filter_journal_pill = self._get(self._journal_filter)
            assert journal_title == filter_journal_pill.text.rstrip(
                    ' '), 'Journal title {0} does not match the journal filter' \
                          'pill {1}'.format(journal_title, filter_journal_pill.text)

    def validate_total_num_search_results_displayed(self, search_term):
        self._wait.until(exp_cond.text_to_be_present_in_element(self._number_of_results,
                                                                search_term))
        total_num_search_results_text = self._get(self._number_of_results).text
        print(total_num_search_results_text)
        print(search_term)
        assert "results for {0}".format(search_term) in total_num_search_results_text or \
               "result for {0}".format(search_term) in total_num_search_results_text

    def validate_search_results_list(self):
        self._get(self._search_results_list)
        return self

    def validate_search_result_term(self, search_term):
        search_result_term = self._get(self._original_search_term)
        assert search_term == search_result_term.text, 'Search terms do not match!'

    def validate_no_search_results(self):
        expected_no_search_results_text = \
            'There were no results; please refine your search above and try again.'
        no_results_text = self._get(self._search_result_none_found).text
        print(no_results_text)
        assert expected_no_search_results_text in no_results_text

    def select_results_per_page(self, option):
        print('Selecting number of results per page...')
        Select(self._get(self._results_per_page_dropdown)).select_by_value(option)
        return self

    def print_article_description(self):
        search_results_list = self._get(self._search_results_list)
        article_descriptions = search_results_list.find_elements_by_tag_name('dd')
        print(article_descriptions[0].text)

    def retrieve_number_of_results_per_page(self):
        results_per_page_dropdown = self._get(self._results_per_page_dropdown)
        options = results_per_page_dropdown.find_elements_by_tag_name('option')
        number_of_results_per_page = 15
        for option in options:
            if option.get_attribute('selected') == 'selected':
                number_of_results_per_page = option.text
        return number_of_results_per_page

    def validate_search_pagination(self):
        self.wait_until_ajax_complete()
        pagination = PaginationAjax(self._driver)
        pagination.validate_pagination()
        return self

    def _is_advanced_search_box_open(self):
        search_fieldset = self._driver.find_element_by_class_name('search-field')
        search_fieldset_classes = search_fieldset.get_attribute('class').split(' ')

        return 'disabled' in search_fieldset_classes

    def _open_advanced_search_box(self):
        if not self._is_advanced_search_box_open():
            advance_search_button = self._driver.find_element_by_id('advancedSearchLink')
            advance_search_button.click()

        return self

    def _close_advance_search_box(self):
        if self._is_advanced_search_box_open():
            simple_search_button = self._driver.find_element_by_id('simpleSearchLink')
            simple_search_button.click()

        return self

    def _clear_all_advanced_search_fields(self):
        self._open_advanced_search_box()
        rows = self._driver.find_elements_by_class_name('advanced-search-row')
        if len(rows) > 1:
            remove_row_buttons = self._driver.find_elements_by_class_name('remove-row-button')
            i = 1
            for button in remove_row_buttons[::-1]:
                if i < len(rows):
                    button.click()
                i += 1

        inputs = self._driver.find_elements_by_class_name('query-condition-value')
        for input in inputs:
            input.clear()

        return self

    def _add_advanced_search_field(self, category, value, operator=''):
        self._open_advanced_search_box()
        rows = self._driver.find_elements_by_class_name('advanced-search-row')
        if len(rows) == 1:
            row = rows[0]
            value_input = row.find_element_by_class_name('query-condition-value')
            if value_input.get_attribute('value'):
                row.find_element_by_class_name('add-row-button').click()
        else:
            row = rows[len(rows) - 1]
            row.find_element_by_class_name('add-row-button').click()

        last_row = self._driver.find_element_by_css_selector('.advanced-search-row:last-child')
        category_selector = last_row.find_element_by_class_name('category')
        Select(category_selector).select_by_value(category)

        if category not in self._advanced_search_date_categories:
            value_input = last_row.find_element_by_class_name('query-condition-value')
            value_input.send_keys(value)
        else:
            from_date_input = last_row.find_element_by_id('date-search-query-input-from')
            to_date_input = last_row.find_element_by_id('date-search-query-input-to')
            from_date_input.send_keys(value.get('from'))
            to_date_input.send_keys(value.get('to'))

        if operator:
            operator_selector = last_row.find_element_by_class_name('operator')
            Select(operator_selector).select_by_value(operator)

        return self

    def _submit_advanced_search(self):
        # Fix for closing datepicker
        search_wrapper = self._driver.find_element_by_css_selector('.search-results-controls')
        search_wrapper.click()

        submit_button = search_wrapper.find_element_by_css_selector('.search-button')
        submit_button.click()
        return self

    def _assert_advanced_search_text(self, query):
        expected_query_string = self._create_advanced_search_query_string(query)
        search_results_header = self._driver.find_element_by_css_selector('.search-results-header')

        if search_results_header.is_displayed():
            records_counter = search_results_header.find_element_by_css_selector(
                    '.results-number strong')
            assert records_counter.text == expected_query_string, \
                'The counter text: {0} is not the expected: {1}' \
                .format(records_counter.text, expected_query_string)

        return self

    def validate_advanced_search_fields(self):
        popular_fields_value = {
            'everything': ['cell', 'plant', 'john', 'hiv', 'earth'],
            'title': ['cell', 'cancer'],
            'body': ['cell', 'plant', 'fitting'],
            'abstract': ['cell', 'plant', 'fitting'],
            'subject': ['Biology and life sciences'],
            'author': ['john', 'michael', 'carlos'],
            'publication_date': [
                {
                    'from': '2010-10-02',
                    'to': '2011-11-22'
                },
                {
                    'from': '2014-02-21',
                    'to': '2015-09-10'
                }
            ],
            'accepted_date': [
                {
                    'from': '2010-10-02',
                    'to': '2011-11-22'
                },
                {
                    'from': '2014-02-21',
                    'to': '2015-09-10'
                }
            ],
            'id': ['10.1371/journal.pone.0005723'],
            'article_type': ['Research Article', 'Correction'],
            'author_affiliate': ['Forschungsinstitut Senckenberg', 'Museum of Anthropology'],
            'competing_interest': ["journal", "editor"],
            'conclusions': ['when', 'question', 'benefits'],
            'editor': ['john', 'andrew'],
            'figure_table_caption': ['viewed', 'comparison'],
            'financial_disclosure': ['grant'],
            'introduction': ['when', 'question', 'benefits'],
            'received_date': [
                {
                    'from': '2010-10-02',
                    'to': '2011-11-22'
                },
                {
                    'from': '2014-02-21',
                    'to': '2015-09-10'
                }
            ]
        }

        self.wait_until_ajax_complete()

        for (category, input_value) in popular_fields_value.items():
            sorted_value = random.choice(input_value)
            self._clear_all_advanced_search_fields()
            self._add_advanced_search_field(category, sorted_value)
            self._submit_advanced_search()
            self.wait_until_ajax_complete()
            self._assert_advanced_search_text([{'category': category, 'value': sorted_value}])

        return self

    def _create_advanced_search_query_string(self, query):
        query_string = ""
        query_parts = []

        for part in query:
            if part.get('category') in self._advanced_search_date_categories:
                date_value = part.get('value')
                value = '[{0}T00:00:00Z TO {1}T23:59:59Z]'.format(
                        date_value.get('from'), date_value.get('to'))
            elif ' ' in part.get('value'):
                value = '"' + part.get('value') + '"'
            else:
                value = part.get('value')

            if part.get('condition'):
                query_parts.append(
                        "{0} {1}:{2}".format(part.get('condition'), part.get('category'), value))
            else:
                query_parts.append("{0}:{1}".format(part.get('category'), value))

        i = 1
        if len(query_parts) > 1:
            for part in query_parts:
                if i == 1:
                    ii = 1
                    while ii < len(query_parts):
                        query_string = query_string + "("
                        ii += 1
                    query_string = query_string + part
                else:
                    query_string = query_string + ' ' + part

                if i < len(query_parts):
                    query_string = query_string + ')'

                i += 1
        else:
            query_string = query_parts[0]

        return query_string

    def validate_advanced_search_query_condition(self):
        queries = [
            [{'category': 'title', 'value': 'cell'},
             {'category': 'subject', 'value': 'Biology and life sciences', 'condition': 'AND'},
             {'category': 'author', 'value': 'john', 'condition': 'AND'}],
            [{'category': 'title', 'value': 'cell'},
             {'category': 'author', 'value': 'john', 'condition': 'OR'}],
            [{'category': 'title', 'value': 'cell'},
             {'category': 'author', 'value': 'john', 'condition': 'NOT'}]
        ]

        for query in queries:
            self._clear_all_advanced_search_fields()
            for part in query:
                self._add_advanced_search_field(part.get('category'), part.get('value'),
                                                part.get('condition'))
            self._submit_advanced_search()
            self.wait_until_ajax_complete()
            self._assert_advanced_search_text(query)

        return self

    def validate_edit_query_link(self):
        input_fieldset = self._driver.find_element_by_class_name('search-field')
        input_fieldset_classes = input_fieldset.get_attribute('class').split(' ')

        if 'disabled' not in input_fieldset_classes:
            advanced_search_link = self._driver.find_element_by_id('advancedSearchLink')
            advanced_search_link.click()

        search_input = self._driver.find_element_by_id('controlBarSearch')
        assert not search_input.is_enabled(), 'The search input is enabled, expected to be disabled'

        edit_query_link = self._driver.find_element_by_class_name('edit-query')
        assert edit_query_link, 'Edit query link is not present'
        edit_query_link.click()
        self._wait_for_element(search_input)
        assert search_input.is_enabled(), 'The search input is disabled, expected to be enabled'

        return self

    def validate_add_and_remove_advanced_search_row(self):
        self._open_advanced_search_box()
        self._clear_all_advanced_search_fields()

        i = 1
        while i <= 49:
            add_row_button = self._driver.find_element_by_css_selector(
                    '.advanced-search-row:last-child .add-row-button')
            add_row_button.click()
            i += 1

        rows = self._driver.find_elements_by_css_selector('.advanced-search-row')
        expected_rows_quantity = 50
        assert len(rows) == expected_rows_quantity, \
            'The rows quantity: {0} is not the expected: {1}' \
            .format(str(len(rows)), str(expected_rows_quantity))

        self._clear_all_advanced_search_fields()
        rows = self._driver.find_elements_by_css_selector('.advanced-search-row')
        expected_rows_quantity = 1
        assert len(rows) == expected_rows_quantity, \
            'The rows quantity: {0} is not the expected: {1}' \
            .format(str(len(rows)), str(expected_rows_quantity))

        return self

    def validate_items_per_page_select(self):
        items_per_page_select = self._driver.find_element_by_id('resultsPerPageDropdown')
        new_select_value = '30'
        Select(items_per_page_select).select_by_value(new_select_value)
        self.wait_until_ajax_complete()
        pagination = PaginationAjax(self._driver)

        if pagination.has_pagination:
            assert int(
                    new_select_value) == pagination.get_items_per_page(), \
                'Items per page in the pagination: {0} is not the expected: {1}' \
                .format(str(pagination.get_items_per_page()), new_select_value)

        return self

    def validate_advanced_search_header_link(self):

        old_page = self._driver.find_element_by_tag_name('html')
        advanced_search_header_link = self._driver.find_element_by_id('advancedSearchLink')
        advanced_search_header_link_top = self._driver.find_element_by_id('advSearch')

        advanced_search_header_link.click()

        # Wait until the html is gone
        # self._wait.until(EC.staleness_of(old_page))
        # Wait until the new page is loaded
        # self._wait.until(EC.element_to_be_clickable((By.ID, 'simpleSearchLink')))

        assert self._is_advanced_search_box_open(), 'Advanced search box is not open as expected'

        main_search_field = self._driver.find_element_by_id('controlBarSearch')
        assert not main_search_field.is_enabled(), 'Search input is not disabled as expected'

        rows = self._driver.find_elements_by_class_name('advanced-search-row')
        expected_rows = 1
        assert len(rows) == expected_rows, 'The row length: {0} is not the expected: {1}' \
            .format(str(len(rows)), str(expected_rows))
        category_select = Select(rows[0].find_element_by_class_name('category'))
        selected_option = category_select.first_selected_option
        selected_option_text = selected_option.text
        expected_selected_option_text = "All Fields"
        assert selected_option_text == expected_selected_option_text, \
            'Selected category text: {0} is not the expected: {1}' \
            .format(selected_option_text, expected_selected_option_text)

        # assert rows[0].find_element_by_class_name('query-condition-value') ==
        #  self._driver.switch_to.active_element, 'The first row value input is not focused'
        advanced_search_header_link_top.click()
        enter_search_term_text = self._driver.find_element_by_css_selector(
                '.searchResults h2.no-term').text
        expected_enter_search_term_text = 'Please enter your search term above.'

        assert enter_search_term_text == expected_enter_search_term_text, \
            'Enter search term text: {0} is not the expected: {1}' \
            .format(enter_search_term_text, expected_enter_search_term_text)
