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

from .HomePage import HomePage
from .MenuManuscript import MenuManuscript
from test.Base.Config import base_url_mobile

__author__ = 'jgray@plos.org'


class PlosNeglectedHomePage(HomePage):
    """
    Model the PLoS Neglected Journal page.
    """
    PROD_URL = '{0!s}/plosntds/'.format(base_url_mobile.rstrip('/'))

    def __init__(self, driver):
        super(PlosNeglectedHomePage, self).__init__(driver, '/MobilePlosNtds/')

        # POM - Instance members
        self._menu = MenuManuscript(driver)

        # Locators - Instance members

    # POM Actions
    def click_menu_button(self):
        self._menu.click_menu_button()
        return self

    def click_browse(self):
        self._menu.click_browse()
        return self

    def click_submissions(self):
        self._menu.click_submissions()
        return self

    def click_policies(self):
        self._menu.click_policies()
        return self

    def click_manu_review(self):
        self._menu.click_manu_review()
        return self

    def click_about(self):
        self._menu.click_about()
        return self

    def click_get_started_link(self):
        self._menu.click_get_started_link()
        return self

    def validate_browse_links(self):
        """
        Check for agreement between number, and list of menu items of the Browse menu, then do a
        get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            'Current Issue': '{0}issue'.format(self.PROD_URL),
            'Journal Archive': '{0}volume'.format(self.PROD_URL),
            'Collections': '{0}s/collections'.format(self.PROD_URL),
            'Find and Read Articles': '{0}s/find-and-read-articles'.format(self.PROD_URL)
        }
        self._menu.validate_browse_links(expected_links)

    def validate_submissions_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Submissions
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Submissions
            'Getting Started': '{0}s/getting-started'.format(self.PROD_URL),
            'Presubmission Inquiries': '{0}s/presubmission-inquiries'.format(self.PROD_URL),
            'Submission Guidelines': '{0}s/submission-guidelines'.format(self.PROD_URL),
            'Figures': '{0}s/figures'.format(self.PROD_URL),
            'Tables': '{0}s/tables'.format(self.PROD_URL),
            'Supporting Information': '{0}s/supporting-information'.format(self.PROD_URL),
            'LaTeX': '{0}s/latex'.format(self.PROD_URL),
            'Other Article Types': '{0}s/other-article-types'.format(self.PROD_URL),
            'Preprints': '{0}s/preprints'.format(self.PROD_URL),
            'Revising Your Manuscript': '{0}s/revising-your-manuscript'.format(self.PROD_URL),
            'Submit Now': '{0}s/submit-now'.format(self.PROD_URL)
        }
        self._menu.validate_submissions_links(expected_links)

    def validate_policies_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Policies
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Policies
            'Best Practices in Research Reporting':
                '{0}s/best-practices-in-research-reporting'.format(self.PROD_URL),
            'Human Subjects Research': '{0}s/human-subjects-research'.format(self.PROD_URL),
            'Animal Research': '{0}s/animal-research'.format(self.PROD_URL),
            'Competing Interests': '{0}s/competing-interests'.format(self.PROD_URL),
            'Disclosure of Funding Sources':
                '{0}s/disclosure-of-funding-sources'.format(self.PROD_URL),
            'Licenses and Copyright':
                '{0}s/licenses-and-copyright'.format(self.PROD_URL),
            'Data Availability': '{0}s/data-availability'.format(self.PROD_URL),
            'Materials and Software Sharing':
                '{0}s/materials-and-software-sharing'.format(self.PROD_URL),
            'Ethical Publishing Practice': '{0}s/ethical-publishing-practice'.format(self.PROD_URL),
            'Authorship': '{0}s/authorship'.format(self.PROD_URL),
            'Downloads and Translations': '{0}s/downloads-and-translations'.format(self.PROD_URL),
        }
        self._menu.validate_policies_links(expected_links)

    def validate_manu_review_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Manuscript
        Review and Publication sub-menu, then do a get on those targets to ensure they exist.
        (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            # Manuscript Review and Publication
            'Editorial and Peer Review Process':
                '{0}s/editorial-and-peer-review-process'.format(self.PROD_URL),
            'Guidelines for Reviewers': '{0}s/reviewer-guidelines'.format(self.PROD_URL),
            'Guidelines for Editors': '{0}s/guidelines-for-editors'.format(self.PROD_URL),
            'Accepted Manuscripts': '{0}s/accepted-manuscripts'.format(self.PROD_URL),
            'Corrections and Retractions': '{0}s/corrections-and-retractions'.format(self.PROD_URL),
            'Comments': '{0}s/comments'.format(self.PROD_URL),
            'Article-Level Metrics': '{0}s/article-level-metrics'.format(self.PROD_URL)
        }
        self._menu.validate_manu_review_links(expected_links)

    def validate_about_links(self):
        """
        Check for agreement between number, and list of menu items of the About menu, then do a
        get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            'Journal Information': '{0}s/journal-information'.format(self.PROD_URL),
            'Editors-in-Chief': '{0}s/editors-in-chief'.format(self.PROD_URL),
            'Editorial Board': '{0}s/editorial-board'.format(self.PROD_URL),
            'Publishing Information': '{0}s/publishing-information'.format(self.PROD_URL),
            'Publication Fees': '{0}s/publication-fees'.format(self.PROD_URL),
            'Press and Media': '{0}s/press-and-media'.format(self.PROD_URL),
            'Commitment To Capacity': '{0}s/commitment-to-capacity'.format(self.PROD_URL),
            'Contact': '{0}s/contact'.format(self.PROD_URL)
        }
        self._menu.validate_about_links(expected_links)

    def validate_submit_your_manuscript_section(self):
        """
        Check for The correct text in the Manuscript Submit section of the Publish menu (Where
        present), then do a get on any link targets to ensure they exist, if present. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_bullets = {
            'PLOS Neglected Tropical Diseases is the top Open Access tropical medicine journal, '
            'featuring an International Editorial Board and increased support for '
            'developing country authors.':
                'https://www.plos.org/fee-assistance#loc-global-initiative'
        }
        self._menu.validate_submit_your_manuscript_section(expected_bullets)
