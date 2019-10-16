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
import time

from selenium.webdriver.common.by import By

from .WombatPage import WombatPage

__author__ = 'jgray@plos.org'


class Menu(WombatPage):
    """
    Model the mobile PLoS site menu.
    """

    def __init__(self, driver):
        super(Menu, self).__init__(driver)

        # Locators - Instance members
        self._site_menu_button = (By.ID, 'site-menu-button')
        self._menu_panel = (By.ID, 'common-menu-container')
        self._browse_link = (By.PARTIAL_LINK_TEXT, 'Browse')
        self._submissions_link = (By.PARTIAL_LINK_TEXT, 'Submissions')
        self._policies_link = (By.PARTIAL_LINK_TEXT, 'Policies')
        self._manu_review_link = (By.PARTIAL_LINK_TEXT, 'Manuscript Review and Publication')
        self._about_link = (By.PARTIAL_LINK_TEXT, 'About')
        self._browse_sublinks = (By.CSS_SELECTOR, 'li.accordion-item.expanded > ul > li > a')
        self._submissions_sublinks = (By.CSS_SELECTOR, 'li.accordion-item.expanded > ul > li > a')
        self._policies_sublinks = (By.CSS_SELECTOR, 'li.accordion-item.expanded > ul > li > a')
        self._manu_review_sublinks = (By.CSS_SELECTOR, 'li.accordion-item.expanded > ul > li > a')
        self._about_sublinks = (By.CSS_SELECTOR, 'li.accordion-item.expanded > ul > li > a')

    # POM Actions

    def click_menu_button(self):
        logging.info('Click Menu button')
        self._get(self._site_menu_button).click()
        return self

    def click_browse(self):
        logging.info('Click Browse button')
        time.sleep(5)
        browse = self._get(self._menu_panel).find_element(*self._browse_link)
        browse.click()
        return self

    def click_submissions(self):
        logging.info('Click Submissions button')
        time.sleep(5)
        submissions = self._get(self._menu_panel).find_element(*self._submissions_link)
        submissions.click()
        return self

    def click_policies(self):
        logging.info('Click Policies button')
        time.sleep(5)
        policies = self._get(self._menu_panel).find_element(*self._policies_link)
        policies.click()
        return self

    def click_manu_review(self):
        logging.info('Click Manuscript Review and Publication button')
        time.sleep(5)
        manu_review = self._get(self._menu_panel).find_element(*self._manu_review_link)
        manu_review.click()
        return self

    def click_about(self):
        logging.info('Click About button')
        time.sleep(5)
        about = self._get(self._menu_panel).find_element(*self._about_link)
        about.click()
        return self

    def _retrieve_sub_links(self, by, locator):
        links = self._get(self._menu_panel).find_elements(by, locator)
        assert len(links) > 1
        return links

    def _retrieve_browse_links(self):
        return self._retrieve_sub_links(*self._browse_sublinks)

    def _retrieve_submissions_links(self):
        return self._retrieve_sub_links(*self._submissions_sublinks)

    def _retrieve_policies_links(self):
        return self._retrieve_sub_links(*self._policies_sublinks)

    def _retrieve_manu_review_links(self):
        return self._retrieve_sub_links(*self._manu_review_sublinks)

    def _retrieve_about_links(self):
        return self._retrieve_sub_links(*self._about_sublinks)

    def validate_browse_links(self, expected_links):
        logging.info('Starting validation of "Browse" links...')
        actual_links = self._retrieve_browse_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_submissions_links(self, expected_links):
        logging.info('Starting validation of "Submissions" links...')
        actual_links = self._retrieve_submissions_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_policies_links(self, expected_links):
        logging.info('Starting validation of "Policies" links...')
        actual_links = self._retrieve_policies_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_manu_review_links(self, expected_links):
        logging.info('Starting validation of "Manuscript Review and Publication" links...')
        actual_links = self._retrieve_manu_review_links()
        self._validate_individual_links(actual_links, expected_links)

    def validate_about_links(self, expected_links):
        logging.info('Starting validation of "About" links...')
        actual_links = self._retrieve_about_links()
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
            logging.info('Verifiying link {0}:'.format(link_text))
            assert link_text in expected_links.keys()
            logging.info("PRESENT /", )
            assert link.get_attribute('href') == expected_links[link_text], \
                '{0} is not equal to {1}'.format(link.get_attribute('href'),
                                                 expected_links[link_text])
            logging.info("HREF OK /", )

            # calls-for-papers link is not environment specific, so skip this assertion
            if 'calls-for-papers' not in expected_links[link_text]:
                assert self._is_link_valid(link) is True
