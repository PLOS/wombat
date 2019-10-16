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
This test validates the Twitter widget div block for all PLOS journals.

For each journal, we validate the presence of the Twitter block and the Twitter title link
"""

import logging
import pytest
from selenium.webdriver.common.by import By

from .common_test import CommonTest
from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestAllJournalsTwitterWidget(CommonTest):
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
    def test_all_journals_twitter_widget(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()

        logging.info('Validating Twitter widget for {0} homepage'.format(journal_name))
        iframe = wombat_page._get(wombat_page._twitter)
        wombat_page.traverse_to_frame(iframe)
        iframe_body = (By.TAG_NAME, 'body')
        body = wombat_page._get(iframe_body)
        # Validate presence of div
        twitter_widget_header = body.find_element(*wombat_page._twitter_header)
        self.validate_text_contains(twitter_widget_header.text, 'Tweets by @PLOS')
        twitter_list = body.find_elements(*wombat_page._twitter_list)
        assert len(twitter_list) > 0, 'Twitter block should not be empty.'

        # validate link targets,
        # TODO: uncomment once DPRO-2692 is resolved
        # try: self.assertEqual(str(journal['journalTweetQuery']),
        # driver.find_element_by_xpath('//a[@class=\'customisable-highlight\']')
        # .get_attribute('href'))
        # except AssertionError as e:
        #   print('Twitter link as implemented:\n' +  driver.find_element_by_xpath(
        # '//a[@class=\'customisable-highlight\']').get_attribute('href') + '\nis not equal
        # to canonical form:\n' + str(journal['journalTweetQuery']))
        #   self.verificationErrors.append(str(e))
