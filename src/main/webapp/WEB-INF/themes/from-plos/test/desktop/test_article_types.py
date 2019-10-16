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
This test case validates the PLOS desktop site's Article page types.

"""

__author__ = 'ivieira@plos.org'

import pytest

from ..Base.Journal import Journal
from ..Base import Utils
from .Pages.ArticleType import ArticleType
from .resources import article_type_list, sevenjournals
from ..Base.Config import base_url


def make_article_type_cases():
    for (journalKey, item) in article_type_list.items():
        for (doi, article_type) in item.items():
            j_info = Utils.get_first_dict_by_value(sevenjournals, 'rhinoJournalKey', journalKey)
            journal_url_name = j_info["journal_url_name"]
            doi = '10.1371/journal.{0}'.format(doi)
            article_url = '{0}/{1}/article?id={2}'.format(base_url.rstrip('/'), journal_url_name,
                                                         doi)
            yield journalKey, article_url, article_type



def idfn(fixture_value):
    """
    The function to return simple parameter id instead of full article info, for example:
    '/DesktopPlosOne/article?id=info:doi/10.1371/journal.pone.0093695' -> 'journal.pone.0093695'
    """
    return fixture_value.split('/')[-1]


@pytest.mark.usefixtures("driver_get")
class ArticleTypeTest:
    @pytest.mark.homepage
    @pytest.mark.parametrize("journal_key, page_path, article_type", make_article_type_cases(),
                             ids=idfn)
    def test_article_type(self, journal_key, page_path, article_type):
        driver = self.driver
        plos_page = ArticleType(driver, journal_key, page_path, article_type)
        plos_page.assert_article_type_text()
        plos_page.assert_content_presence()
