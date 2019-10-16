#!/usr/bin/env python3
# -*- coding: utf-8 -*-
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
This test validates the Publish With PLOS div block for all
PLOS journals.

For each journal, we validate the presence of the div block,
The div block title, the presence and targets of both links
"""

import logging
import pytest
from selenium.webdriver.support import expected_conditions as exp_cond

from .common_test import CommonTest
from ..Base.Config import base_url
from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestAlljournalsPublishwPlos(CommonTest):
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
    def test_all_journals_publish_w_plos(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating \'Publish with PLOS\' section: {0}'.format(journal_name))
        # Validate presence of div
        wombat_page._get(wombat_page._submission_links)

        if journal['journalTitle'] != 'Biology':
            # validate div title, link text and targets
            expected_text_publish = 'PUBLISH WITH PLOS ONE' if journal['journalTitle'] == 'One' \
                else 'PUBLISH WITH PLOS'
            publish_with_plos = wombat_page._get(wombat_page._publish_with_plos)
            self.validate_text_exact(actual_text=publish_with_plos.text,
                                     expected_text=expected_text_publish)
            submission_instructions = wombat_page._get(wombat_page._submission_instructions)
            self.validate_text_exact(actual_text=submission_instructions.text,
                                     expected_text='SUBMISSION INSTRUCTIONS')

            submission_instructions.click()
            wombat_page._wait_for_element(wombat_page._get(wombat_page._submit_now))
            submit_now = wombat_page._get(wombat_page._submit_now)
            self.validate_text_exact(actual_text=submit_now.text, expected_text='Submit Now')
            wombat_page._driver.back()

        submit_your_manuscript = wombat_page._get(wombat_page._submit_your_manuscript)
        self.validate_text_exact(actual_text=submit_your_manuscript.text,
                                 expected_text='SUBMIT YOUR MANUSCRIPT')
        submit_your_manuscript.click()
        page_title = 'Editorial Manager'

        if str(journal['journalTitle']) == 'One':
            # new window, Editorial Manager
            submission_page_url = wombat_page.open_page_in_new_window(
                    page_title=page_title, original_url=base_url)
            expected_url = 'www.editorialmanager.com/pone/default.aspx'
            self.validate_text_contains(actual_text=submission_page_url,
                                        expected_part_text=expected_url)

        elif str(journal['journalTitle']) == 'Biology':

            # same window, but not Editorial Manager page
            wombat_page._wait_for_element(wombat_page._get(wombat_page._submit_now))
            submission_page_url = wombat_page.get_current_url()
            wombat_page._driver.back()
            wombat_page._wait_for_element(wombat_page._get(wombat_page._submit_your_manuscript))
            self.validate_text_exact(submission_page_url,
                                     '{0!s}/plosbiology/s/submit-now'.format(base_url.rstrip('/')))

        else:
            # same window, Editorial Manager page
            wombat_page._wait.until(exp_cond.title_contains(page_title))
            submission_page_url = wombat_page.get_current_url()
            expected_url = 'www.editorialmanager.com/p{0}/default.aspx' \
                .format(journal['journalKey'].lower())
            if str(journal['journalKey']) == 'Ntds':
                expected_url = 'www.editorialmanager.com/pntd/default.aspx'
            self.validate_text_contains(actual_text=submission_page_url,
                                        expected_part_text=expected_url)

            # TODO: check back from EM in FireFox
            try:
                wombat_page._driver.back()
                wombat_page._wait.until(exp_cond.url_contains(base_url))
            except TimeoutError:
                wombat_page._driver.back()
                wombat_page._wait.until(exp_cond.url_contains(base_url))

                # wombat_page._driver.get(journal_url))
                # TODO: Fix the verification of EM website in new window
