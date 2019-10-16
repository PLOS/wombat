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
import re

import requests
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.by import By

from .. import resources
from ..Pages.WombatPage import WombatPage
from ...Base import Config
from ...Base import Utils
from ...Base.CustomException import ElementDoesNotExistAssertionError
from ...Base.styles import (
    WHITE, ARTICLE_TAB_BACKGROUND, BLACK,
    StyledPage as Styles
    )

__author__ = 'jgray@plos.org'


# Variable definitions


class Article(WombatPage):
    """
    Model an abstract base Article page, Article Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(Article, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._article_doi = (By.ID, 'artDoi')
        self._article_type = (By.ID, 'artType')
        self._title_top_closer = (By.ID, 'titleTopCloser')
        self._title_top_closer_button = (By.CSS_SELECTOR, "div.close-floater")
        self._comment_container = (By.ID, 'respond')
        self._article_container = (By.CSS_SELECTOR, 'div#thread')
        self._link_to_post_comment = (By.CSS_SELECTOR, 'p.post_comment')
        self._comments_thread = (By.CSS_SELECTOR, 'ul#threads')
        self._journal_title = (By.CSS_SELECTOR, "#pagehdr .logo a")
        self._article_tabs = (By.CLASS_NAME, 'article-tabs')
        self._article_tabs_list = (By.CSS_SELECTOR, '.article-tabs > li > a')
        self._article_tab_title_list = (By.CLASS_NAME, 'tab-title')

        # instance variables
        self._alm_data = None  # used to store associated ALM data
        # in order to prevent redundant
        # API calls
        self._counter_data = None  # used to store associated Counter data

    # POM Actions

    def close_floating_title_top(self):
        try:
            floating_close_button = self._get(
                self._title_top_closer).find_element(
                *self._title_top_closer_button)
            if floating_close_button.is_displayed() and \
                    floating_close_button.is_enabled():
                floating_close_button.click()
        except ElementDoesNotExistAssertionError:
            return self
        return self

    def extract_page_doi(self):
        full_doi = self._get(self._article_doi).text
        doi_idx = full_doi.find('10.1371')
        return full_doi[doi_idx:].strip()

    def extract_page_escaped_doi(self):
        return Utils.escape_doi(self.extract_page_doi())

    def get_article_current_revision(self):
        article_revisions = requests.get(
            resources.rhino_url + '/articles/' +
            self.extract_page_escaped_doi() + '/revisions')
        article_revisions_list = json.loads(article_revisions.text)
        current_revision = article_revisions_list[
            len(article_revisions_list) - 1]
        return current_revision

    def get_article_current_ingestion(self):
        current_revision = self.get_article_current_revision()
        return str(current_revision['ingestion']['ingestionNumber'])

    def return_article_type(self):
        """
        Extract the Article Type from the displayed page
        :return: String
        """
        article_type_text = self._get(self._article_type).text
        return article_type_text

    def return_alm_data(self):
        """
        Call out to the ALM (Article Level Metrics) API and retrieve ALM
        data if not already
        part of page object
        Returns: dict containing ALM data (empty when no data is present)
        """
        if self._alm_data is None:
            # the first time ALM data is requested, store in local var
            response = requests.get(Config.alm_url, params={
                'api_key': Config.alm_api_key,
                'ids': self.extract_page_doi()
                })
            try:
                json_response = json.loads(response.text)
            except ValueError:
                response = requests.get(Config.alm_url, params={
                    'api_key': Config.alm_api_key,
                    'ids': self.extract_page_doi()
                    })
                json_response = json.loads(response.text)
            finally:
                logging.info('doi: {0!s}, alm response status code: {1!s}'
                             .format(self.extract_page_doi(),
                                     response.status_code))
            self._alm_data = json_response['data'][0] if json_response[
                'data'] else {}
        return self._alm_data

    def return_counter_data(self):
        """
        Call out to the Counter API and retrieve counter data if not already
        part of page object
        Returns: dict containing Counter data (empty when no data is present)
        """
        if self._counter_data is None:
            # the first time Counter data is requested, store in local var
            response = requests.get(
                Config.counter_url + self.extract_page_doi(), params=None)
            json_response = json.loads(response.text)
            self._counter_data = json_response['totals'] if json_response[
                'totals'] else {}
        return self._counter_data

    def return_alm_data_source(self, source_name):
        """
        Iterate over the ALM data sources to find the requested data source
        Returns: the given data source or None if not present
        """
        alm_sources = self.return_alm_data().get('sources', [])
        return Utils.get_first_dict_by_value(alm_sources, 'name', source_name)

    def return_alm_data_sources(self, source_group_name=None):
        """
        Iterate over the ALM data sources to find the data sources belonging
        to the given group
        Returns: a list of all the data sources for the given group (or all
        sources if no
        group specified) or an empty list if no data is present
        """
        alm_sources = self.return_alm_data().get('sources', [])
        return Utils.filter_dicts_by_value(alm_sources, 'group_name',
                                           source_group_name) if \
            source_group_name else alm_sources

    def return_alm_metrics_total(self, source_name):
        source_data = self.return_alm_data_source(source_name)
        try:
            return source_data['metrics']['total']
        except:
            return 0

    def _check_comments_availability(self, section_to_check):
        """
        Check if Comments are available.
        :param section_to_check: locator to check for presence, possible
        values:
        self._comments_thread for article comments tab,
        self._response_section for single
        comment page
        :return: void function
        """
        try:
            self._get(section_to_check)
        except ElementDoesNotExistAssertionError as e:
            if self.is_element_present("comments_temp_unavailable"):
                logging.error('Comments unavailable, aborting test.')
            raise NoSuchElementException

    def page_ready(self, sign_status='sign in'):
        """
        A function to validate that the article page is loaded before
        interacting with it
        :param sign_status: string to validate signed status, possible values:
        'sign it', 'sign out'
        :return:
        """
        self._wait_for_element(self._get(self._sign_in_link))
        sign_button = self._get(self._sign_in_link)
        logging.info('The text of {0!r} button is {1!r}'
                     .format(sign_status, sign_button.text))
        self._wait_on_lambda(
            lambda: self._get(self._sign_in_link).text == sign_status,
            max_wait=60)

    def get_journal_info(self):
        """
        The method to get journal information from resourses, using journal
        title from the page
        :return: journal: dictionary, journal_name: string
        """
        journal_logo = self._get(self._journal_title)
        journal_title = journal_logo.text
        journal_logo_href_name = \
            journal_logo.get_attribute('href').split('/')[-2]
        if journal_logo_href_name == 'plosclinicaltrials':
            journal = resources.plosclinicaltrials_journal
        else:
            journal = Utils.get_first_dict_by_value(
                resources.sevenjournals, 'journalTitle',
                journal_title[5:].title())
        journal_name = 'PLOS {0!s}'.format(journal['journalTitle'])

        return journal, journal_name

    def get_article_xml(self):
        """Returns the url of the current article xml"""
        current_url = self.get_current_url()
        xml_url = '{}&type=manuscript' \
            .format(re.sub('article.*\?',
                           'article/file?', current_url).strip())
        return xml_url

    def get_peer_review_path(self):
        """Returns the url of the current article peer review tab"""
        current_url = self.get_current_url()
        peer_review_url = '{}' \
            .format(re.sub('article.*\?.*', 'article/peerReview?',
                           current_url).strip())
        return peer_review_url

    def get_tab_list(self):
        """ returns the list of article tabs """
        tabs_list = self._gets(self._article_tabs_list)
        return tabs_list

    def get_tab_title_list(self):
        """ returns the list of article tab titles """
        tab_title_list = self._gets(self._article_tab_title_list)
        return tab_title_list

    def validate_tabs_style(self, journal_brand_color):
        """
        Method to validate colors on article tabs
        :param journal_brand_color: journal brand color
        :return:
        """
        tab_titles = self.get_tab_title_list()
        tabs = self.get_tab_list()
        for i, tab in enumerate(tabs):
            if 'active' in tab_titles[i].get_attribute('class'):
                Styles.validate_element_background_color(
                    tab, journal_brand_color, 'hex')
                Styles.validate_element_color(tab, BLACK, 'hex')
            else:
                Styles.validate_element_background_color(
                    tab, ARTICLE_TAB_BACKGROUND[i], 'hex')
                Styles.validate_element_color(tab, WHITE, 'hex')
