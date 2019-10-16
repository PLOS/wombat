#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Copyright (c) 2017 Public Library of Science
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated 2documentation files (the "Software"),
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

from ...Base import Config
from ...Base.CustomException import ElementDoesNotExistAssertionError
from .Menu import Menu

__author__ = 'jgray@plos.org'

class MenuManuscript(Menu):
    """
    Model the mobile PLoS site menu (with Submit Your Manuscript section).
    """

    def __init__(self, driver):
        super(MenuManuscript, self).__init__(driver)

        # Locators - Instance members
        self._manuscript_panel = (By.ID, 'submit-manuscript-container')
        self._submit_your_manuscript_title = (By.ID, 'callout-headline')
        self._text_paragraph = (By.ID, 'callout-description')
        self._get_started_link = (By.ID, 'callout-button')
        self._callout_link = (By.ID, 'callout-link')
        self._get_started_link_bio = (By.CSS_SELECTOR, 'p.button-contain.special > a')
        self._ed_board_link = (By.PARTIAL_LINK_TEXT, 'Editorial Board')

    # POM Actions
    def click_get_started_link(self, bio=False):
        logging.info('Click Get Started button')
        if bio:
            get_started = self._get(self._manuscript_panel).find_element(
                    *self._get_started_link_bio)
        else:
            get_started = self._get(self._manuscript_panel).find_element(*self._get_started_link)
        time.sleep(5)
        get_started.click()
        self._driver.back()
        return self

    def click_editorial_board_link(self):
        logging.info('Click on Editorial Board link')
        ed_board = self._get(self._ed_board_link)
        ed_board.click()

    def click_callout_link(self):
        logging.info('Click Submit Now link')
        get_started = self._get(self._manuscript_panel).find_element(*self._callout_link)
        get_started.click()
        self._driver.back()
        return self

    def _validate_section_title(self):
        logging.info('Starting validation of Submit Your Manuscript section...')
        logging.info('Validating title...')
        title = self._get(self._manuscript_panel).find_element(*self._submit_your_manuscript_title)
        self.validate_text_exact(title.text, 'SUBMIT YOUR MANUSCRIPT', 'Incorrect title')

    def _validate_paragraph_contains_link(self, paragraph, paragraphLink):
        try:
            logging.info('Checking for links in paragraph...')
            self._driver.implicitly_wait(1)
            subLink = paragraph.find_element(By.XPATH, './a')
            if subLink:
                logging.info('Paragraph has a link, validating it...', )
                assert subLink.get_attribute('href') == paragraphLink
                logging.info('HREF OK /', )
                self._is_link_valid(subLink)
        except ElementDoesNotExistAssertionError:
            pass
        self._driver.implicitly_wait(Config.wait_timeout)

    def _validate_section_paragraph(self, expectedParagraph):
        logging.info('Starting validation of paragraph...')
        actualParagraph = self._get(self._text_paragraph).text.encode('utf8')
        for key in expectedParagraph.keys():
            paragraphText = key.encode('utf8')
            assert actualParagraph == paragraphText
            logging.info('PRESENT')
            expectedLink = expectedParagraph[key]
            if expectedLink is not None and expectedLink is not '':
                self._validate_paragraph_contains_link(self._get(self._text_paragraph),
                                                       expectedLink)

    def validate_submit_your_manuscript_section(self, expectedParagraph):
        self._validate_section_title()
        self._validate_section_paragraph(expectedParagraph)
