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

from .HomePage import HomePage
from .Menu import Menu
from ...Base import Config

__author__ = 'jgray@plos.org'


class PlosGeneticsHomePage(HomePage):
    """
    Model the PLoS Genetics Journal page.
    """
    base_url = Config.base_url
    if Config.environment != 'prod':
        LINK_URL = base_url + "/DesktopPlosGenetics/"
    else:
        LINK_URL = '{0!s}/plosgenetics/'.format(base_url.rstrip('/'))

    LEGACY_LINK_URL = 'http://www.plosgenetics.org/'

    def __init__(self, driver):
        super(PlosGeneticsHomePage, self).__init__(driver, '/DesktopPlosGenetics/')

        # POM - Instance members
        self._menu = Menu(driver)

        # Locators - Instance members

        self._advert_bottom_slot_container = (By.ID, 'ad-slot-bottom')
        self._bottom_slot_1_ad_div = (By.ID, 'div-gpt-ad-1458247671871-2')
        self._bottom_slot_2_ad_div = (By.ID, 'div-gpt-ad-1458247671871-3')
        self._bottom_slot_3_ad_div = (By.ID, 'div-gpt-ad-1458247671871-4')
        self._google_iframe_div_1 = \
            (By.ID, 'google_ads_iframe_/75507958/PGEN_300x250_ITB1_0__container__')
        self._google_iframe_div_2 = \
            (By.ID, 'google_ads_iframe_/75507958/PGEN_300x250_ITB2_0__container__')
        self._google_iframe_div_3 = \
            (By.ID, 'google_ads_iframe_/75507958/PGEN_300x250_ITB3_0__container__')

    # POM Actions
    def assert_bottom_slot_advert_div_presence(self):
        self._get(self._advert_bottom_slot_container)
        return self

    def validate_pgen_bottom_slot_ads(self):
        self._get(self._bottom_slot_1_ad_div)
        self._get(self._bottom_slot_2_ad_div)
        self._get(self._bottom_slot_3_ad_div)
        return self

    def validate_pgen_bottom_slot_google_iframe_id(self):
        self._get(self._google_iframe_div_1)
        self._get(self._google_iframe_div_2)
        self._get(self._google_iframe_div_3)
        return self

    def validate_browse_links(self):
        """
        Check for agreement between number, and list of menu items of the Browse menu, then do
        a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            'Current Issue': '{0}issue'.format(self.LINK_URL),
            'Journal Archive': '{0}volume'.format(self.LINK_URL),
            'Collections': '{0}s/collections'.format(self.LINK_URL),
            'Find and Read Articles': '{0}s/find-and-read-articles'.format(self.LINK_URL)
        }
        self._menu.validate_browse_links(expected_links)

    def validate_publish_submissions_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Submissions
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Submissions
            'Getting Started': '{0}s/getting-started'.format(self.LINK_URL),
            'Presubmission Inquiries': '{0}s/presubmission-inquiries'.format(self.LINK_URL),
            'Submission Guidelines': '{0}s/submission-guidelines'.format(self.LINK_URL),
            'Figures': '{0}s/figures'.format(self.LINK_URL),
            'Tables': '{0}s/tables'.format(self.LINK_URL),
            'Supporting Information': '{0}s/supporting-information'.format(self.LINK_URL),
            'LaTeX': '{0}s/latex'.format(self.LINK_URL),
            'Other Article Types': '{0}s/other-article-types'.format(self.LINK_URL),
            'Preprints': '{0}s/preprints'.format(self.LINK_URL),
            'Revising Your Manuscript': '{0}s/revising-your-manuscript'.format(self.LINK_URL),
            'Submit Now': '{0}s/submit-now'.format(self.LINK_URL)
        }
        self._menu.validate_publish_submissions_links(expected_links)

    def validate_publish_policies_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Policies
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Policies
            'Best Practices in Research Reporting':
                '{0}s/best-practices-in-research-reporting'.format(self.LINK_URL),
            'Human Subjects Research': '{0}s/human-subjects-research'.format(self.LINK_URL),
            'Animal Research': '{0}s/animal-research'.format(self.LINK_URL),
            'Competing Interests': '{0}s/competing-interests'.format(self.LINK_URL),
            'Disclosure of Funding Sources':
                '{0}s/disclosure-of-funding-sources'.format(self.LINK_URL),
            'Licenses and Copyright': '{0}s/licenses-and-copyright'.format(self.LINK_URL),
            'Data Availability': '{0}s/data-availability'.format(self.LINK_URL),
            'Materials and Software Sharing':
                '{0}s/materials-and-software-sharing'.format(self.LINK_URL),
            'Ethical Publishing Practice': '{0}s/ethical-publishing-practice'.format(self.LINK_URL),
            'Authorship': '{0}s/authorship'.format(self.LINK_URL),
            'Downloads and Translations': '{0}s/downloads-and-translations'.format(self.LINK_URL),
        }
        self._menu.validate_publish_policies_links(expected_links)

    def validate_publish_manuscript_review_links(self):
        """
        Check for agreement between number, and list of menu items of the
        Publish::Manuscript Review and Publication sub-menu, then do a get on those targets to
        ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Manuscript Review and Publication
            'Editorial and Peer Review Process':
                '{0}s/editorial-and-peer-review-process'.format(self.LINK_URL),
            'Guidelines for Reviewers': '{0}s/reviewer-guidelines'.format(self.LINK_URL),
            'Guidelines for Editors': '{0}s/guidelines-for-editors'.format(self.LINK_URL),
            'Accepted Manuscripts': '{0}s/accepted-manuscripts'.format(self.LINK_URL),
            'Corrections and Retractions': '{0}s/corrections-and-retractions'.format(self.LINK_URL),
            'Comments': '{0}s/comments'.format(self.LINK_URL),
            'Article-Level Metrics': '{0}s/article-level-metrics'.format(self.LINK_URL)
        }
        self._menu.validate_publish_manuscript_review_links(expected_links)

    def validate_about_links(self):
        """
        Check for agreement between number, and list of menu items of the About menu, then do a
        get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            'Journal Information': '{0}s/journal-information'.format(self.LINK_URL),
            'Editors-in-Chief': '{0}s/editors-in-chief'.format(self.LINK_URL),
            'Editorial Board': '{0}s/editorial-board'.format(self.LINK_URL),
            'Publishing Information': '{0}s/publishing-information'.format(self.LINK_URL),
            'Publication Fees': '{0}s/publication-fees'.format(self.LINK_URL),
            'Press and Media': '{0}s/press-and-media'.format(self.LINK_URL),
            'Contact': '{0}s/contact'.format(self.LINK_URL)
        }
        self._menu.validate_about_links(expected_links)
