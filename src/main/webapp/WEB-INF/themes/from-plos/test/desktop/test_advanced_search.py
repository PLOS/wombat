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

__author__ = 'achoe@plos.org'

import pytest

from ..Base.Config import environment
from .Pages.PlosBiologyAdvancedSearch import PlosBiologyAdvancedSearch
from .Pages.PlosCompBiolAdvancedSearch import PlosCompBiolAdvancedSearch
from .Pages.PlosGeneticsAdvancedSearch import PlosGeneticsAdvancedSearch
from .Pages.PlosPathogensAdvancedSearch import PlosPathogensAdvancedSearch
from .Pages.PlosNeglectedAdvancedSearch import PlosNeglectedAdvancedSearch
from .Pages.PlosMedicineAdvancedSearch import PlosMedicineAdvancedSearch
from .Pages.PlosOneAdvancedSearch import PlosOneAdvancedSearch
from .Pages.PlosCollectionsAdvancedSearch import PlosCollectionsAdvancedSearch
from .Pages.PlosClinicalTrialsAdvancedSearch import PlosClinicalTrialsAdvancedSearch


@pytest.mark.usefixtures("driver_get")
class TestAdvancedSearch:
    @pytest.mark.homepage
    @pytest.mark.parametrize("plos_adv_search_page", [
        PlosBiologyAdvancedSearch,
        PlosCompBiolAdvancedSearch,
        PlosGeneticsAdvancedSearch,
        PlosPathogensAdvancedSearch,
        PlosNeglectedAdvancedSearch,
        PlosMedicineAdvancedSearch,
        PlosOneAdvancedSearch,
        PlosCollectionsAdvancedSearch,
        PlosClinicalTrialsAdvancedSearch,
    ])
    def test_advanced_search(self, plos_adv_search_page):
        """
        Advanced Search tests will be commented out, as part of DPRO-2207. This test will need to
        be updated to now validate the new query builder on the search results page.
        """
        if environment == 'prod':
            advanced_search_page = plos_adv_search_page(self.driver)
            advanced_search_page.enter_search_term()
            advanced_search_page.submit_search()
            advanced_search_page.validate_served_by_wombat(advanced_search_page.journal_name)
        else:
            print('Environment does not have prod urls')
