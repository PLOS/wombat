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

__author__ = 'jgray@plos.org'

from .HomePage import HomePage
from .Menu import Menu
from ...Base import Config


class PlosCollectionsHomePage(HomePage):
    """
    Model the PLoS Collections Journal page.
    """

    if Config.collections_url == Config.base_url:
        url_suffix = '/DesktopPlosCollections/'
    else:
        url_suffix = '/'

    base_url = Config.base_url
    LINK_URL = (Config.collections_url + url_suffix)
    PROD_URL = 'http://www.ploscollections.org/'

    def __init__(self, driver, url_suffix=url_suffix):
        super(PlosCollectionsHomePage, self).__init__(driver, url_suffix)

        # POM - Instance members
        self._menu = Menu(driver)

        # Locators - Instance members

    # POM Actions
    def validate_browse_links(self):
        """
        Check for agreement between number, and list of menu items of the Browse menu,
        then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was received
        """
        expected_links = {
            'Biology & Life Sciences': '{}s/biology-life-sciences'.format(self.LINK_URL),
            'Computer & Information Sciences':
                '{}s/computer-information-sciences'.format(self.LINK_URL),
            'Earth & Environmental Sciences': '{}s/earth-environmental-sciences'
                .format(self.LINK_URL),
            'Medicine & Health Sciences': '{}s/medicine-health-sciences'.format(self.LINK_URL),
            'Research Analysis & Science Policy': '{}s/research-analysis-science-policy'
                .format(self.LINK_URL),
        }
        self._menu.validate_browse_links(expected_links)

    def validate_about_links(self):
        """
        Check for agreement between number, and list of menu items of the About menu, then do a get
        on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was received
        """
        expected_links = {
            'About PLOS Collections': '{}s/about'.format(self.LINK_URL),
            'Propose a Special Collection':
                '{}s/propose-a-special-collection'.format(self.LINK_URL),
            'Finances for Special Collections': '{}s/finances-for-special-collections'
                .format(self.LINK_URL),
        }
        self._menu.validate_about_links(expected_links)
