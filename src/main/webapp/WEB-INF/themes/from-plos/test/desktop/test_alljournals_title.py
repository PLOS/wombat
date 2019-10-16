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
This test validates the Journal Title across the all PLOS journal properties.
"""

import logging
import pytest


from .common_test import CommonTest
from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestHeaderSearchPerJournal(CommonTest):
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
    def test_desktop_test_header_search_per_journal(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating page title: {0}'.format(journal_name))

        if journal['journalTitle'] == 'One':
            expected_title = '{0}: accelerating the publication of peer-reviewed science' \
                .format(journal_name.upper())
        else:
            expected_title = '{0}: A Peer-Reviewed Open-Access Journal'.format(journal_name)

        self.validate_text_exact(actual_text=wombat_page._driver.title,
                                 expected_text=expected_title, message='Incorrect homepage title')
