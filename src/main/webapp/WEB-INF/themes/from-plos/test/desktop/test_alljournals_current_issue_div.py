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
This test validates the Current Issue div for all PLOS Journals. 

For each journal we need to validate the image, the link from 
the image, the link from 'Current Issue' and the Date and Year 
of issue display
"""

import logging

import pytest
from selenium.webdriver.support import expected_conditions as exp_cond

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestAlljournalsCurrentIssueDiv:
    @pytest.mark.homepage
    @pytest.mark.parametrize("journal_home_page", [
            PlosBiologyHomePage,
            PlosCompBiolHomePage,
            PlosGeneticsHomePage,
            PlosMedicineHomePage,
            PlosNeglectedHomePage,
            # PlosOneHomePage, # commented due to AMBR-22, we don't have 'current
            # issue' block for PLOS ONE homepage
            PlosPathogensHomePage,
    ])
    def test_alljournals_current_issue_div(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating \'Current issue\' section: {0}'.format(journal_name))
        wombat_page.get_current_url()

        wombat_page._get(wombat_page._current_issue)
        current_issue_img = wombat_page._get(wombat_page._current_issue_img)

        current_issue_img.click()
        wombat_page._wait.until(exp_cond.url_contains('issue'))

        # if we have problems with displaying issue, the error message is showing
        # instead of current issue (another way is to check class: 'main article.error-page')
        article_section = wombat_page._get(wombat_page._issue_article_section)
        assert not article_section.text.strip().startswith('Something\'s Broken!'), \
            'Issue is not displaying, the error message: {0!r}'.format('Something\'s Broken!')
        wombat_page._driver.back()

        # Validate Current Issue Link
        wombat_page._get(wombat_page._current_issue_link)

        # Validate Month and Year display of current issue against Rhino
        issue_subhead = wombat_page._get(wombat_page._current_issue_subhead)
        issue_text = issue_subhead.text

        key = wombat_page.get_current_issue_keys(journal)
        logging.info(key)

        assert '{0} {1}'.format(key[0], key[1]) == issue_text, \
            'Current Issue Month and Year not valid. Expected Value: \'{0} {1}\', ' \
            'Actual value: {2}'.format(key[0], key[1], issue_text)
