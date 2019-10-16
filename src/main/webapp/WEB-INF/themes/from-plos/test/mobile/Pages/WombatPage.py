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


from selenium.webdriver.common.by import By

from ...Base.PlosPage import PlosPage

__author__ = 'jgray@plos.org'


# Variable definitions

class WombatPage(PlosPage):
    """
    Model an abstract base Wombat page for desktop
    """
    expected_about_us_link = '/s/journal-information'
    expected_full_site_link = '/#'
    expected_feedback_link = 'mailto:webmaster@plos.org'
    expected_help_link = 'https://www.plos.org/contact'
    expected_privacy_link = 'https://www.plos.org/privacy-policy'
    expected_terms_link = 'https://www.plos.org/terms-of-use'
    expected_media_link = 'https://www.plos.org/media-inquiries'

    def __init__(self, driver, url_suffix=''):
        super(WombatPage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._footer_about_us_link = (By.XPATH, "//nav[@class='footer-menu']/ul/li/a")
        self._footer_full_site_link = (By.ID, 'full-site-link')
        self._footer_feedback_link = (By.XPATH, "//nav[@class='footer-menu']/ul/li[3]/a")
        self._footer_help_link = (By.XPATH, '//nav[@class="footer-secondary-menu"]/ul/li/a')
        self._footer_privacy_policy_link = (
            By.XPATH, '//nav[@class="footer-secondary-menu"]/ul/li[2]/a')
        self._footer_terms_of_use_link = (
            By.XPATH, '//nav[@class="footer-secondary-menu"]/ul/li[3]/a')
        self._footer_media_inquiries_link = (
            By.XPATH, '//nav[@class="footer-secondary-menu"]/ul/li[4]/a')
        self._site_logo = (By.ID, 'site-logo')
        self._article_list_block = (By.ID, 'article-results')
        self._popular_button = (By.CSS_SELECTOR, "li[data-method='popular']")


    # POM Actions

    def validate_footer_menu(self):
        """
        The footer implementation is split into four different sections. footer-menu contains the top level links.
        :return: success/assertion exception
        """
        about_us_link = self._get(self._footer_about_us_link).get_attribute('href')
        local_about_us_link = '/' + '/'.join(about_us_link.rsplit('/')[-2:])
        self.validate_text_exact(local_about_us_link, self.expected_about_us_link,
                                 'Incorrect \'about us\' link')

        full_site_link = self._get(self._footer_full_site_link).get_attribute('href')
        local_full_site_link = '/' + '/'.join(full_site_link.rsplit('/')[-1:])
        self.validate_text_exact(local_full_site_link, self.expected_full_site_link,
                                 'Incorrect \'full site\' link')

        feedback_link = self._get(self._footer_feedback_link).get_attribute('href')
        self.validate_text_exact(feedback_link, self.expected_feedback_link,
                                 'Incorrect feedback link')

        return self

    def validate_footer_secondary_menu(self):
        """
        The footer implementation is split into four different sections. footer secondary menu contains the lower set
        of horizontal links.
        Validate the links point to the correct place.
        :return: success/assertion exception
        """
        help_link = self._get(self._footer_help_link).get_attribute('href')
        self.validate_text_exact(help_link, self.expected_help_link, 'Incorrect help link')

        privacy_link = self._get(self._footer_privacy_policy_link).get_attribute('href')
        self.validate_text_exact(privacy_link, self.expected_privacy_link, 'Incorrect privacy link')

        terms_link = self._get(self._footer_terms_of_use_link).get_attribute('href')
        self.validate_text_exact(terms_link, self.expected_terms_link, 'Incorrect terms link')

        media_link = self._get(self._footer_media_inquiries_link).get_attribute('href')
        self.validate_text_exact(media_link, self.expected_media_link, 'Incorrect media link')

        return self

    def click_on_journal_logo(self):
        self._get(self._site_logo).click()
        self._wait_for_element(self._get(self._popular_button))
