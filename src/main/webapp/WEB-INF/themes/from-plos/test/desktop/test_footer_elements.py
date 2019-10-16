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
This test case validates the PLOS desktop site's home pages
for the six journals. It is a stub that will be expanded as the
old style pre-framework tests get migrated here.
"""

import pytest

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCollectionsHomePage import PlosCollectionsHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage

__author__ = 'jgray@plos.org'

@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("journal_home_page", [
    PlosBiologyHomePage,
    PlosMedicineHomePage,
    PlosCompBiolHomePage,
    PlosGeneticsHomePage,
    PlosPathogensHomePage,
    PlosNeglectedHomePage,
    PlosOneHomePage,
    PlosCollectionsHomePage,
])
class TestFooter:
    def test_footer_blocks(self, journal_home_page):
        plos_page = journal_home_page(self.driver)
        plos_page.validate_footer_logo()
        plos_page.validate_footer_nav_aux()
        plos_page.validate_footer_link_column_one()
        plos_page.validate_footer_link_column_two()
