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

import json
import logging
import time
import requests

from selenium.webdriver.common.by import By

from ...Base.Config import base_url_mobile, solr_url
from .WombatPage import WombatPage

__author__ = 'gtimonina@plos.org'


class EditorialBoardPage(WombatPage):
    """
    Model an Editorial Board page
    """
    PROD_URL = '{0!s}/plosone/static/editorial-board'.format(base_url_mobile.rstrip('/'))

    def __init__(self, driver, url_suffix=''):
        super(EditorialBoardPage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        # editorial board page locators
        self._page_title = (By.CSS_SELECTOR, '#content h1')
        self._page_subtitle = (By.CSS_SELECTOR, '#content h2')
        self._editorial_search_input = (By.ID, 'editors-search-input')
        self._editorial_search_button = (By.CLASS_NAME, 'editors-search-button')
        self._editorial_search_reset_button = (By.CLASS_NAME, 'editors-search-reset')
        self._editorial_search_editors_list = (By.CLASS_NAME, 'editors-list')
        self._editorial_search_results_list = (By.CSS_SELECTOR, 'ul.editors-list > li')
        self._editorial_search_names_list = (By.CSS_SELECTOR, 'ul.editors-list strong')
        self._editorial_search_counters = (By.CLASS_NAME, 'counters')
        self._editorial_search_pagination = (By.ID, 'article-pagination')
        self._editorial_search_prev_page_link = (By.ID, 'prevPageLink')
        self._editorial_search_next_page_link = (By.ID, 'nextPageLink')
        self._editorial_search_page_numbers = (By.CSS_SELECTOR, 'a.number[data-page]')
        self._editorial_search_active_page = (By.CSS_SELECTOR, 'a.active.number')
        self._editorial_search_page_skip = (By.CSS_SELECTOR, 'span.skip')
        self._spinner_loading = (By.ID, 'loading')
        self._submenu_open = (By.CSS_SELECTOR, 'div.tt-open')

        self._input_placeholder = 'People, Areas of Expertise,...'
        self._editorial_search_current_page = (By.CSS_SELECTOR, 'a.number')

    def page_ready(self, ae_list):
        """
        Method to ensure the page is fully ready for testing
        :param ae_list: solr request result: list of academic editors
        :return: void function
        """
        if ae_list:
            self.wait_until_ajax_complete()
            self._wait_for_element(self._get(self._editorial_search_results_list))
        else:
            self._wait_for_not_element(self._editorial_search_results_list)
        logging.info('Editorial Board page is ready to test.')

    def get_search_results(self):
        """
        Method to get search results on the current Editorial Board page
        :return: names_list: list of string: academic editors names
            editors_list: list of web elements: academic editors
            result_counters: web element: search result info
            item_text_list: list of string: academic editor info (full item's text)
        """
        result_counters = self.get_result_counters_text()
        editors_list = self._gets(self._editorial_search_results_list)
        item_text_list = [el.text.strip() for el in editors_list]
        webel_names_list = self._gets(self._editorial_search_names_list)
        names_list = [el.text.strip() for el in webel_names_list]
        return names_list, editors_list, result_counters, item_text_list

    def get_result_counters_text(self):
        """
        Method to get search result info (text) on the current Editorial Board page
        :return: result_counters: string
        """
        self._wait_for_element(self._get(self._editorial_search_counters))
        webel_result_counters = self._get(self._editorial_search_counters)
        result_counters = webel_result_counters.text.strip()
        return result_counters

    def click_on_search_button(self, test_type='positive'):
        """
        Method to click on search button
        :param test_type: string, possible values: 'positive', 'negative'
        :return: void function
        """
        first_ae_before = self._get(self._editorial_search_results_list)

        self._wait_for_element(self._get(self._editorial_search_button))
        counters_text_before = self.get_result_counters_text()
        logging.info('counters text before click on search button: {0!s}'
                     .format(counters_text_before))
        time.sleep(0.5)
        search_button = self._get(self._editorial_search_button)

        search_button.click()

        time.sleep(0.5)
        counters_text_after = self.get_result_counters_text()
        logging.info('counters text after click on search button: {0!s}'
                     .format(counters_text_after))
        if test_type == 'positive':
            assert self._get(self._editorial_search_results_list), \
                'No results found for positive search.'
            self._wait_on_lambda(
                lambda: self._get(self._editorial_search_results_list) != first_ae_before)
        else:
            self._wait_for_not_element(self._editorial_search_results_list, 0.5)

    def get_page_title(self):
        """
        Method to get page title
        :return: string
        """
        page_title = self._get(self._page_title)
        return page_title.text.strip()

    def get_page_subtitle(self):
        """
        Method to get page subtitle
        :return: string
        """
        page_subtitle = self._get(self._page_subtitle)
        return page_subtitle.text.strip()

    def click_on_page_subtitle(self):
        """
        Method to click on page subtitle
        :return: void function
        """
        page_subtitle = self._get(self._page_subtitle)
        page_subtitle.click()

    def get_search_pagination(self):
        """
        Method to get pagination info (web elements)
        :return: web elements: prev_link, next_link, numbers, skip_number, pagination
        """
        pagination = self._get(self._editorial_search_pagination)
        numbers = pagination.find_elements(*self._editorial_search_page_numbers)
        prev_link = self._get(self._editorial_search_prev_page_link)
        next_link = self._get(self._editorial_search_next_page_link)
        skip_number = self._get(self._editorial_search_page_skip)
        return prev_link, next_link, numbers, skip_number, pagination

    def get_search_pagination_text(self):
        """
        Method to get pagination info (text)
        :return: prev_link_str, next_link_str, numbers_str_list, skip_str
        """
        prev_link, next_link, numbers, skip_number, pagination = self.get_search_pagination()
        numbers_str_list = [el.text for el in numbers]
        prev_link_str = prev_link.text.strip()
        next_link_str = next_link.text.strip()
        skip_str = skip_number.text.strip()
        return prev_link_str, next_link_str, numbers_str_list, skip_str

    def get_input(self):
        """
        Method to get search input field and placeholder to check
        :return: ed_search_input: web element: search input field
            input_placeholder: 'placeholder' attribute
        """
        ed_search_input = self._get(self._editorial_search_input)
        input_placeholder = ed_search_input.get_attribute("placeholder")
        return ed_search_input, input_placeholder

    def enter_search_text(self, text_to_search):
        """
        Method to enter text to search
        """
        self.wait_until_ajax_complete()
        ed_search_input, input_placeholder = self.get_input()
        ed_search_input.send_keys(text_to_search)
        self.wait_until_ajax_complete()

        self._wait_for_text_to_be_present_in_element_value(
                self._editorial_search_input, text_to_search, multiplier=0.5)
        return self

    @staticmethod
    def retrieve_ae_from_solr(search_text=''):
        """
        Method to retrieve academic editors list and the total number of results found from the
        solr query that is run by the Editorial Board search page.
        :param search_text: string: text to search
        :return: ae_list, num_found
        """
        fq='fq=doc_type:(section_editor OR academic_editor)'
        fq_filter = \
            '&fq=ae_subject_facet:"{0}" OR ae_name_facet:"{0}"'.format(search_text) if \
            search_text else ''
        fq += fq_filter
        par = {
            'q': 'q=*:*',
            'rows': 'rows=50',
            'fq': fq,
            'fl': 'fl=ae_name,ae_institute,ae_subject,ae_country',
            'wt': 'wt=json',
            'sort': 'sort=ae_last_name+asc',
        }

        facet_url = "?{q}&{rows}&{fq}&{fl}&{wt}&{sort}".format(**par)
        logging.info('get request to solr: "{0}{1}"'.format(solr_url,facet_url))
        response = requests.get(solr_url + facet_url, params=None)
        json_response = json.loads(response.text)
        num_found = json_response['response']['numFound'] \
            if json_response['response']['numFound'] else 0
        ae_list = json_response['response']['docs'] \
            if json_response['response']['docs'] else []

        return ae_list, num_found

    def get_active_page(self):
        """
        Returns active page number for search results pagination
        :return: number: web element, active page number
        """
        self._wait_for_element(self._get(self._editorial_search_pagination))
        pagination = self._get(self._editorial_search_pagination)
        number = pagination.find_element(*self._editorial_search_active_page)
        return number

    def get_active_page_number(self):
        """
        Returns active page number for search results pagination as a string
        :return: number: string, active page number
        """
        active_page = self.get_active_page()
        return active_page.text

    def click_on_next_page(self):
        """clicks on 'next page' link """
        first_ae_before = self._get(self._editorial_search_results_list)
        self._wait_for_element(self._get(self._editorial_search_next_page_link))
        next_page_button = self._get(self._editorial_search_next_page_link)
        next_page_button.click()
        self._wait_on_lambda(
                lambda: self._get(self._editorial_search_results_list) != first_ae_before)

    def click_on_previous_page(self):
        """clicks on 'previous page' link """
        first_ae_before = self._get(self._editorial_search_results_list)
        self._wait_for_element(self._get(self._editorial_search_prev_page_link))
        next_page_button = self._get(self._editorial_search_prev_page_link)
        next_page_button.click()
        self._wait_on_lambda(
                lambda: self._get(self._editorial_search_results_list) != first_ae_before)

    def click_on_page_with_number(self, page_number):
        """
        pagination: clicks on the page link using specified number
        :param page_number: string: specific page number as a string
        :return: void function
        """
        first_ae_before = self._get(self._editorial_search_results_list)

        pagination = self._get(self._editorial_search_pagination)
        editorial_search_current_page = \
            (self._editorial_search_current_page[0], '{0}[data-page="{1!s}"]'.format(
                    self._editorial_search_current_page[1], page_number))
        logging.info(editorial_search_current_page)
        new_page = pagination.find_element(*editorial_search_current_page)

        logging.info('localized page number {0} to click'.format(new_page))
        assert new_page
        new_page.click()
        self._wait_on_lambda(
                lambda: self._get(self._editorial_search_results_list) != first_ae_before)

    def link_is_enabled(self, page_link='next'):
        """
        Pagination: Method to define if 'next' or 'previous' link is enabled (active)
        :param page_link: string, must be 'next' or 'previous'
        :return: boolean: True if the link is enabled, False if disabled
        """
        link_to_click_locator = self._editorial_search_next_page_link if page_link == 'next' \
            else self._editorial_search_prev_page_link
        link_to_click = self._get(link_to_click_locator)
        is_active = 'disabled' not in link_to_click.get_attribute('class')
        return is_active

    def reset_search(self):
        """
        Clicks on 'x' to reset search which runs prepopulated search query
        :return: void function
        """
        reset_search_button = self._get(self._editorial_search_reset_button)
        reset_search_button.click()
        ae_list, num_found = self.retrieve_ae_from_solr()
        self.page_ready(ae_list)
