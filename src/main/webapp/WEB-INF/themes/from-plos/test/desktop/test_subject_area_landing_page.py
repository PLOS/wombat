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
This test case validates the PLOS ONE desktop site's subject area landing page
"""

import pytest

from .Pages.PlosOneSubjectAreaLandingPage import PlosOneSubjectAreaLandingPage

__author__ = 'stower@plos.org'


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("plos_page", [
    PlosOneSubjectAreaLandingPage,
])
class TestSubjectAreaLandingPage:
    def test_view_by_buttons(self, plos_page):
        plos_one_page = plos_page(self.driver)
        plos_one_page.validate_related_content_dropdown()
        plos_one_page.validate_rss_link()
        plos_one_page.validate_article_count()
        plos_one_page.validate_view_by_buttons()
        plos_one_page.validate_listview_items()
