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
This test case validates the PLOS mobile site's home pages
for all PLOS journals and Plos Collections.
"""
__author__ = 'jgray@plos.org'

import pytest

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosComputationalHomePage import PlosComputationalHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestHomePage:
    @pytest.mark.parametrize("page_under_test", [
        PlosBiologyHomePage,
        PlosComputationalHomePage,
        PlosGeneticsHomePage,
        PlosMedicineHomePage,
        PlosNeglectedHomePage,
        PlosPathogensHomePage,
        PlosOneHomePage,
    ])
    def test_homepage_buttons(self, page_under_test):
        """
        Validate Recent and Popular buttons in mobile home pages
        """
        plos_page = page_under_test(self.driver)
        plos_page.click_recent_button()
        plos_page.click_popular_button()
