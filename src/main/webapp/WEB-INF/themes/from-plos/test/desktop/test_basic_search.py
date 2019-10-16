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

import pytest

from .Pages.PlosBiologyBasicSearch import PlosBiologyBasicSearch
from .Pages.PlosClinicalTrialsBasicSearch import PlosClinicalTrialsBasicSearch
from .Pages.PlosCompBiolBasicSearch import PlosCompBiolBasicSearch
from .Pages.PlosCollectionsBasicSearch import PlosCollectionsBasicSearch
from .Pages.PlosGeneticsBasicSearch import PlosGeneticsBasicSearch
from .Pages.PlosMedicineBasicSearch import PlosMedicineBasicSearch
from .Pages.PlosNeglectedBasicSearch import PlosNeglectedBasicSearch
from .Pages.PlosOneBasicSearch import PlosOneBasicSearch
from .Pages.PlosPathogensBasicSearch import PlosPathogensBasicSearch

__author__ = 'achoe@plos.org'


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("journal_page", [
            (PlosBiologyBasicSearch),
            (PlosCompBiolBasicSearch),
            (PlosGeneticsBasicSearch),
            (PlosPathogensBasicSearch),
            (PlosNeglectedBasicSearch),
            (PlosMedicineBasicSearch),
            (PlosOneBasicSearch),
            (PlosCollectionsBasicSearch),
            (PlosClinicalTrialsBasicSearch),
])
class TestSearch:
    def test_search(self, journal_page):
        plos_page = journal_page(self.driver)
        plos_page.validate_search_bar()
        plos_page.validate_search_placeholder_text()
        # TODO: create test for validating tooltip when no search term is entered.
        plos_page.positive_search()
        plos_page.validate_default_journal_filter(plos_page.journal_name)
        plos_page.negative_search()
