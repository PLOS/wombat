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
from .MenuManuscript import MenuManuscript
from ...Base.Config import base_url, collections_url, environment

__author__ = 'jgray@plos.org'


class PlosOneHomePage(HomePage):
    """
    Model the PLoS One Journal page.
    """

    if environment != 'prod':
        CURRENT_URL = base_url + "/DesktopPlosOne/"
        print(CURRENT_URL)
    else:
        CURRENT_URL = '{0!s}/plosone/'.format(base_url.rstrip('/'))

    # Need variable since dpro has mix of prod and dev links
    JOURNAL_URL = 'http://www.plosone.org/'

    def __init__(self, driver):
        super(PlosOneHomePage, self).__init__(driver, '/DesktopPlosOne/')

        # POM - Instance members
        self._menu = MenuManuscript(driver)

        # Locators - Instance members
        self._subject_area_menu = (By.CLASS_NAME, 'subject-area')
        self._subject_area_menu_ambra = (By.CLASS_NAME, 'areas-link')
        self._in_the_news_link = (By.LINK_TEXT, 'In the News')
        self._in_the_news_active = (By.CSS_SELECTOR, 'a.news.active')
        self._recent_link = (By.LINK_TEXT, 'Recent')
        self._recent_active = (By.CSS_SELECTOR, 'a.recent.active')
        self._most_viewed_link = (By.LINK_TEXT, 'Most Viewed')
        self._most_viewed_active = (By.CSS_SELECTOR, 'a.popular.active')
        self._footer_bar = (By.ID, 'sticky-footer')
        self._footer_bar_close = (By.CSS_SELECTOR, '#sticky-footer a.close')

    # POM Actions

    def click_in_the_news_link(self):
        in_the_news_link = self._get(self._in_the_news_link)
        in_the_news_link.click()
        return self

    def click_recent_link(self):
        recent_link = self._get(self._recent_link)
        recent_link.click()
        return self

    def click_most_viewed_link(self):
        most_viewed_link = self._get(self._most_viewed_link)
        most_viewed_link.click()
        return self

    def validate_footer_bar(self):
        # TODO: fill out this stub with full validation.
        # Stub included here in order to ultimately close the footer bar
        # (see comment in validate_footer_bar_and_article_card() method)

        # activate footer bar by scrolling to bottom of page
        self.moveto_footer()
        footer_bar_close_button = self._get(self._footer_bar_close)
        footer_bar_close_button.click()
        self._check_for_invisible_element(self._footer_bar)

    def validate_in_the_news_section(self):
        assert self._get(self._in_the_news_active)
        return self

    def validate_recent_section(self):
        assert self._get(self._recent_active)
        return self

    def validate_most_viewed_section(self):
        assert self._get(self._most_viewed_active)
        return self

    def validate_subject_area(self):
        self._get(self._subject_area_menu)
        return self

    def validate_publish_submissions_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Submissions
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {  # Submissions
            'Getting Started': '{0}s/getting-started'.format(self.CURRENT_URL),
            'Submission Guidelines': '{0}s/submission-guidelines'.format(self.CURRENT_URL),
            'Figures': '{0}s/figures'.format(self.CURRENT_URL),
            'Tables': '{0}s/tables'.format(self.CURRENT_URL),
            'Supporting Information': '{0}s/supporting-information'.format(self.CURRENT_URL),
            'LaTeX': '{0}s/latex'.format(self.CURRENT_URL),
            'Preprints': '{0}s/preprints'.format(self.CURRENT_URL),
            'Revising Your Manuscript': '{0}s/revising-your-manuscript'.format(self.CURRENT_URL),
            'Submit Now': '{0}s/submit-now'.format(self.CURRENT_URL),
            'Calls for Papers': '{0}/s/calls-for-papers'
            .format(collections_url.rstrip('/').replace('-dev', ''))
            }

        self._menu.validate_publish_submissions_links(expected_links)

    def validate_publish_policies_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Policies
        sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {  # Policies
            'Best Practices in Research Reporting':
                '{0}s/best-practices-in-research-reporting'.format(self.CURRENT_URL),
            'Human Subjects Research': '{0}s/human-subjects-research'.format(self.CURRENT_URL),
            'Animal Research': '{0}s/animal-research'.format(self.CURRENT_URL),
            'Competing Interests': '{0}s/competing-interests'.format(self.CURRENT_URL),
            'Disclosure of Funding Sources': 
                '{0}s/disclosure-of-funding-sources'.format(self.CURRENT_URL),
            'Licenses and Copyright': '{0}s/licenses-and-copyright'.format(self.CURRENT_URL),
            'Data Availability': '{0}s/data-availability'.format(self.CURRENT_URL),
            'Materials and Software Sharing':
                '{0}s/materials-and-software-sharing'.format(self.CURRENT_URL),
            'Ethical Publishing Practice': 
                '{0}s/ethical-publishing-practice'.format(self.CURRENT_URL),
            'Authorship': '{0}s/authorship'.format(self.CURRENT_URL),
            'Downloads and Translations': 
                '{0}s/downloads-and-translations'.format(self.CURRENT_URL),
            }
        self._menu.validate_publish_policies_links(expected_links)

    def validate_publish_manuscript_review_links(self):
        """
        Check for agreement between number, and list of menu items of the Publish::Manuscript
        Review and Publication sub-menu, then do a get on those targets to ensure they exist.
        (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {  # Manuscript Review and Publication
            'Criteria for Publication': '{0}s/criteria-for-publication'.format(self.CURRENT_URL),
            'Editorial and Peer Review Process':
                '{0}s/editorial-and-peer-review-process'.format(self.CURRENT_URL),
            'Editor Center': '{0}s/editor-center'.format(self.CURRENT_URL),
            'Guidelines for Reviewers': '{0}s/reviewer-guidelines'.format(self.CURRENT_URL),
            'Accepted Manuscripts': '{0}s/accepted-manuscripts'.format(self.CURRENT_URL),
            'Corrections and Retractions': 
                '{0}s/corrections-and-retractions'.format(self.CURRENT_URL),
            'Comments': '{0}s/comments'.format(self.CURRENT_URL),
            'Article-Level Metrics': '{0}s/article-level-metrics'.format(self.CURRENT_URL)
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
            'Why Publish with PLOS ONE': '{0}static/publish'.format(self.CURRENT_URL),
            'Journal Information': '{0}s/journal-information'.format(self.CURRENT_URL),
            'Staff Editors': '{0}s/staff-editors'.format(self.CURRENT_URL),
            'Editorial Board': '{0}static/editorial-board'.format(self.CURRENT_URL),
            'Section Editors': '{0}s/section-editors'.format(self.CURRENT_URL),
            'Advisory Groups': '{0}s/advisory-groups'.format(self.CURRENT_URL),
            'Find and Read Articles': '{0}s/find-and-read-articles'.format(self.CURRENT_URL),
            'Publishing Information': '{0}s/publishing-information'.format(self.CURRENT_URL),
            'Publication Fees': '{0}s/publication-fees'.format(self.CURRENT_URL),
            'Press and Media': '{0}s/press-and-media'.format(self.CURRENT_URL),
            'Contact': '{0}s/contact'.format(self.CURRENT_URL)
            }
        self._menu.validate_about_links(expected_links)

    def validate_submit_your_manuscript_section(self):
        """
        Check for The correct text in the Manuscript Submit section of the Publish menu (Where
        present), then do a get on any link targets to ensure they exist, if present. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_paragraph = {
            'Discover a faster, simpler path to publishing in a high-quality journal. '
            'PLOS ONE promises ' +
            'fair, rigorous peer review, broad scope, and wide readership – a perfect fit for ' +
            'your research every time.': ''
            }
        print(expected_paragraph)
        self._menu.validate_submit_your_manuscript_section(expected_paragraph)
