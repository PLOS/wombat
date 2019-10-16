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
import re

import requests

from ..Pages.WombatPage import WombatPage
from ...Base import Config
from ...Base import Utils

__author__ = 'gtimonina@plos.org'


# Variable definitions

class Article(WombatPage):
    """
    Model an abstract base Article page, Article Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(Article, self).__init__(driver, url_suffix)

        # Locators - Instance members

        # instance variables
        self._alm_data = None  # used to store associated ALM data in order to prevent redundant
        # API calls
        self._counter_data = None  # used to store associated Counter data

    # POM Actions

    def extract_page_doi(self):
        full_doi = self.get_current_url()
        doi_idx = full_doi.find('10.1371')
        return full_doi[doi_idx:].strip()

    def extract_page_escaped_doi(self):
        return Utils.escape_doi(self.extract_page_doi())

    def return_alm_data(self):
        """
        Call out to the ALM (Article Level Metrics) API and retrieve ALM data if not already
        part of page object
        Returns: dict containing ALM data (empty when no data is present)
        """
        if self._alm_data is None:
            # the first time ALM data is requested, store in local var
            response = requests.get(Config.alm_url, params={'api_key': Config.alm_api_key,
                                                            'ids': self.extract_page_doi()})
            json_response = json.loads(response.text)
            self._alm_data = json_response['data'][0] if json_response['data'] else {}
        return self._alm_data

    def return_counter_data(self):
        """
        Call out to the Counter API and retrieve counter data if not already part of page object
        Returns: dict containing Counter data (empty when no data is present)
        """
        if self._counter_data is None:
            # the first time Counter data is requested, store in local var
            response = requests.get(Config.counter_url + self.extract_page_doi(), params=None)
            json_response = json.loads(response.text)
            self._counter_data = json_response['totals'] if json_response['totals'] else {}
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
        Iterate over the ALM data sources to find the data sources belonging to the given group
        Returns: a list of all the data sources for the given group (or all sources if no
        group specified) or an empty list if no data is present
        """
        alm_sources = self.return_alm_data().get('sources', [])
        return Utils.filter_dicts_by_value(alm_sources, 'group_name',
                                           source_group_name) if source_group_name else alm_sources

    def return_alm_metrics(self, source_name, data_key='total'):
        """
        The method to find the value in the requested data source, it is total value by default,
        or another key in metrics, if needed
        :param source_name: requested data source name, string
        :param data_key: name of the specific key, optional, default value is 'total'
        :return: data value if found the key, or 0 if key is not found
        """

        source_data = self.return_alm_data_source(source_name)
        try:
            return source_data['metrics'][data_key]
        except:
            return 0

    def _article_section_ready(self, locator):
        self._wait_for_element(self._get(locator))

    def get_article_xml(self):
        """Returns the url of the current article xml"""
        current_url = self.get_current_url()
        xml_url = '{}&type=manuscript'\
            .format(re.sub('article.*\?','article/file?', current_url).strip())
        return xml_url
