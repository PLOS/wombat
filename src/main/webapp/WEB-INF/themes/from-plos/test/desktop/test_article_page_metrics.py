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
This test case validates the Wombat Desktop Article Page metrics tab body elements
"""
import pytest

from .Pages.PlosArticleMetrics import PlosArticleMetrics

__author__ = 'jfesenko@plos.org'


@pytest.mark.usefixtures("driver_get")
class TestArticleMetrics:
    @pytest.mark.parametrize("article_metrics_page", [
        PlosArticleMetrics,
    ])
    def test_plos_article_metrics(self, article_metrics_page):
        plos_page = article_metrics_page(self.driver)
        plos_page.assert_metrics_tab_active()
        plos_page.validate_viewed_section()
        plos_page.validate_cited_section()
        plos_page.validate_saved_section()
        plos_page.validate_discussed_section()
