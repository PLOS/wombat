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
This test case validates article cards in the PLOS ONE Homepage and the root SALP
"""

import logging
import pytest
import random

from ..Base.Journal import Journal
from .Pages.ArticleCard import ArticleCard
from .resources import subject_areas

browse_path = Journal.build_homepage_path('PLoSONE') + 'browse/'


@pytest.mark.usefixtures("driver_get")
class TestArticleCard:
    @pytest.mark.parametrize("test_path", ["browse_path", "browse_path_with_subject_area"])
    def test_plos_one_homepage(self, test_path):
        driver = self.driver
        if test_path == 'browse_path':
            path = browse_path
        else:
            path = browse_path + random.choice(subject_areas)
            logging.info('selected path: {0!r}'.format(path))
        ArticleCard(driver, path).validate_article_card()
