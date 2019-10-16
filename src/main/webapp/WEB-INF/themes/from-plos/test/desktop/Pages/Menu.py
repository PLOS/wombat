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

import logging

from selenium.webdriver.common.by import By

from .WombatPage import WombatPage

__author__ = 'jgray@plos.org'


class Menu(WombatPage):
    """
    Model the desktop PLos site menu.
    """

    def __init__(self, driver):
        super(Menu, self).__init__(driver)

        # Locators - Instance members
        self._nav_elements = (By.ID, 'pagehdr')
        self._browse_sublinks = (By.CSS_SELECTOR, 'ul#browse-dropdown-list li a')
        self._publish_submissions_sublinks = (
            By.CSS_SELECTOR, 'ul#submissions-dropdown-list.menu-section li a')
        self._publish_policies_sublinks = (
            By.CSS_SELECTOR, 'ul#policies-dropdown-list.menu-section li a')
        self._publish_manuscript_review_sublinks = (
            By.CSS_SELECTOR, 'ul#manuscript-review-and-publication-dropdown-list.menu-section li a')

        self._about_sublinks = (By.CSS_SELECTOR, 'ul#about-dropdown-list li a')
        self._about_sublinks_ambra = (
            By.CSS_SELECTOR, 'div[class="submenu"] > div[class="menu"] > ul > li > a')

    # POM Actions
    def _retrieve_browse_links(self):
        return self._retrieve_sub_links(*self._browse_sublinks)

    def _retrieve_sub_links(self, by, locator):
        links = self._get(self._nav_elements).find_elements(by, locator)
        assert len(links) > 1
        return links

    def _retrieve_publish_submissions_links(self):
        return self._retrieve_sub_links(*self._publish_submissions_sublinks)

    def _retrieve_publish_policies_links(self):
        return self._retrieve_sub_links(*self._publish_policies_sublinks)

    def _retrieve_publish_manuscript_review_links(self):
        return self._retrieve_sub_links(*self._publish_manuscript_review_sublinks)

    def _retrieve_about_links(self):
        return self._retrieve_sub_links(*self._about_sublinks)

    def _retrieve_about_links_ambra(self):
        return self._retrieve_sub_links(*self._about_sublinks_ambra)

    def validate_browse_links(self, expected_links):
        logging.info('Starting validation of "Browse" links...')
        actual_links = self._retrieve_browse_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_publish_submissions_links(self, expected_links):
        logging.info('Starting validation of "Publish:: Submission" links...')
        actual_links = self._retrieve_publish_submissions_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_publish_policies_links(self, expected_links):
        logging.info('Starting validation of "Publish::Policies" links...')
        actual_links = self._retrieve_publish_policies_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_publish_manuscript_review_links(self, expected_links):
        logging.info('Starting validation of "Publish::Manuscript Review and Publication" links...')
        actual_links = self._retrieve_publish_manuscript_review_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_about_links(self, expected_links):
        logging.info('Starting validation of "About" links...')
        actual_links = self._retrieve_about_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_about_links_ambra(self, expected_links):
        logging.info('Starting validation of "About" links...')
        actual_links = self._retrieve_about_links_ambra()
        self._validate_individual_links(actual_links, expected_links)

    def _validate_individual_links(self, actual_links, expected_links):
        # Validations
        link_count1 = len(actual_links)
        link_count2 = len(expected_links)
        logging.info("Actual links count is {0} (Expected {1})".format(link_count1, link_count2))
        assert link_count1 == link_count2

        for link in actual_links:
            self._wait_for_element(link)
            link_text = link.text.strip()
            logging.info('Verifying link "{0}"'.format(link_text))
            assert link_text in expected_links, \
                "'{0}' was not among '{1}'".format(link_text, expected_links)
            logging.info("PRESENT /", )

            assert link.get_attribute('href') == expected_links[link_text], \
                "'{0}' != '{1}'".format(link.get_attribute('href'), expected_links[link_text])
            logging.info("HREF OK /", )

            # calls-for-papers link is not environment specific, so skip this assertion
            if 'calls-for-papers' not in expected_links[link_text]:
                assert self._is_link_valid(link) is True
