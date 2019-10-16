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

"""
This test validates the Connect with Us div block
across all PLOS journals.
"""

import logging
import pytest

from ..Base.Config import blogs_url
from .common_test import CommonTest
from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestAlljournalsConnectwus(CommonTest):
    @pytest.mark.homepage
    @pytest.mark.parametrize("journal_home_page", [
        PlosBiologyHomePage,
        PlosCompBiolHomePage,
        PlosGeneticsHomePage,
        PlosMedicineHomePage,
        PlosNeglectedHomePage,
        PlosOneHomePage,
        PlosPathogensHomePage,
    ])
    def test_alljournals_connectwus(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating \'CONNECT WITH US\' section: {0}'.format(journal_name))
        journal_url = wombat_page.get_current_url()

        connect_with_us_section = wombat_page._get(wombat_page._connect_with_us)
        expected_text = 'CONNECT WITH US'

        self.validate_text_exact(connect_with_us_section.text, expected_text,
                                 'Incorrect section title')

        text_twitter = '{0} on Twitter'.format(journal_name.upper()) \
            if journal_home_page == PlosOneHomePage \
            else '{0} on Twitter'.format(journal_name)
        expected_social_links = {
            'Contact Us': '{0}s/contact'.format(journal_url),
            'RSS': '{0}feed/atom'.format(journal_url),
            '{0}'.format(text_twitter):
                '{0}'.format(str(journal['journalTweetTarget']).lower()),
            'PLOS on Facebook': 'https://www.facebook.com/plos.org',
            'PLOS Blogs': '{}'.format(blogs_url)
        }
        actual_social_links = wombat_page.get_social_links()
        assert len(actual_social_links) == len(expected_social_links), \
            '{0!s} links expected, {1!s} found'. \
            format(len(expected_social_links), len(actual_social_links))

        self._validate_social_links(wombat_page, actual_social_links, expected_social_links)

        # TODO: check this block: why it was commented (3/4/16), do we need this
        # try:
        #   self.assertTrue(wombat_page.is_element_present('social-link-email'))
        # except AssertionError as e:
        #   self.verificationErrors.append(str(e))
        # driver.find_element_by_css_selector("span.icon-email").click()
        # if driver.title == 'PLOS':
        #   driver.find_element_by_id('username').clear()
        # driver.find_element_by_id('username').send_keys(resources.existing_user_email)
        # driver.find_element_by_id('password').clear()
        # driver.find_element_by_id('password').send_keys(resources.existing_user_pw)
        # driver.find_element_by_name('submit').click()
        # else:
        #   self.assertEqual('PLOS Journals: A Peer-Reviewed, Open-Access Journal', driver.title)
        # try:
        #   self.assertEqual('Manage your journal alert emails',
        #                    driver.find_element_by_css_selector('#alert-form > legend').text)
        # except AssertionError as e:
        #   self.verificationErrors.append(str(e))
        # try:
        #   self.assertTrue(self.is_element_present(By.LINK_TEXT, 'sign out'))
        # except AssertionError as e:
        #   self.verificationErrors.append(str(e))
        # driver.find_element_by_link_text('sign out').click()
        # driver.get(journal_url)

        # TODO: consider another way to validate RSS link,
        # commenting next block for now as Firefox removed built-in feed reader
        # validate RSS link
        # target_title = journal_name.upper() if journal_home_page == PlosOneHomePage \
        #     else journal_name
        # rss_page_url, link_text = wombat_page.click_on_social_link_same_window(1, target_title)
        # self.validate_text_exact(rss_page_url, expected_social_links[link_text],
        #                          'Incorrect link to RSS page')

        # validate Twitter link
        twitter_page_url, link_text = wombat_page \
            .click_on_social_link_new_window(idx=2, page_title='Twitter')
        self.validate_text_exact(twitter_page_url, expected_social_links[link_text]
                                 .replace('http:', 'https:'), 'Incorrect link to Twitter page')

        # validate Facebook link
        facebook_page_url, link_text = wombat_page \
            .click_on_social_link_new_window(idx=3, page_title='PLOS - Home | Facebook')
        self.validate_text_exact(facebook_page_url.lower().rstrip('/'),
                                 expected_social_links[link_text].rstrip('/')
                                 .replace('http:', 'https:').lower(),
                                 'Incorrect link to Facebook page')

        # validate PLOS Blogs links
        if 'blogs.plos.org' in blogs_url:
            blogs_page_url, link_text = wombat_page.click_on_social_link_same_window(
                    4, 'PLOS Blogs Network | Diverse perspectives on science and medicine')
            self.validate_text_exact(blogs_page_url.rstrip('/'), expected_social_links[
                link_text].rstrip('/'), 'Incorrect link to Blogs page')

    @staticmethod
    def _validate_social_links(journal_homepage, actual_links, expected_links):
        """
        The method to validate actual links against expected, checking text, href attributes
        :param journal_homepage: page object instance to communicate with home page
        :param actual_links: list of web elements, actual links
        :param expected_links: dictionary with expected text and links
        :return: void function
        """
        for link in actual_links:
            journal_homepage._wait_for_element(link)
            link_text = link.get_attribute('title').strip().rstrip('/')
            logging.info('Verifying link "{0}"'.format(link_text))
            assert link_text in expected_links, \
                '{0!r} was not among {1!r}'.format(link_text, expected_links)
            if 'blogs.plos.org' in blogs_url:
                assert link.get_attribute('href').rstrip('/') == \
                       expected_links[link_text].rstrip('/'), '{0!r} != {1!r}'\
                       .format(link.get_attribute('href'), expected_links[link_text])
                logging.info("HREF OK ")
                assert journal_homepage._is_link_valid(link) is True
