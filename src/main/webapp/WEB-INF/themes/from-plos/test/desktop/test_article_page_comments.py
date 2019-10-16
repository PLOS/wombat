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
This test case validates the PLOS desktop site's Article page comments section.

"""

import pytest

from .Pages.ArticleComments import ArticleComments
from ..Base.Journal import Journal

__author__ = 'ivieira@plos.org'

article_comments_list = {
    'pone.0008519': 'PLoSONE',
    'pone.0162114': 'PLoSONE',
    'pbio.1000501': 'PLoSBiology',
    'pbio.2000958': 'PLoSBiology',
    'pcbi.1000433': 'PLoSCompBiol',
    'pcbi.1005112': 'PLoSCompBiol',
    'pgen.1005902': 'PLoSGenetics',
    'pgen.1006316': 'PLoSGenetics',
    'ppat.1003788': 'PLoSPathogens',
    'ppat.1005827': 'PLoSPathogens',
    'pmed.1000156': 'PLoSMedicine',
    'pmed.1002134': 'PLoSMedicine',
    'pntd.0001379': 'PLoSNTD',
    'pntd.0004962': 'PLoSNTD',
    }


def make_article_page_comments_cases():
    for (doi, journalKey) in article_comments_list.items():
        doi = 'info:doi/10.1371/journal.{0}'.format(doi)
        yield journalKey, Journal.build_article_comments_path(journalKey, doi)


def idfn(fixture_value):
    """
    The function to return simple parameter id instead of full article comment info, for example:
    '/DesktopPlosOne/article/comments?id=info:doi/10.1371/journal.pone.0162114' ->
    'journal.pone.0162114'
    """
    return fixture_value.split('/')[-1]


@pytest.mark.usefixtures("driver_get")
class TestArticlePageComments:
    @pytest.mark.parametrize("journal_key, page_path", make_article_page_comments_cases(), ids=idfn)
    def test_article_comments(self, journal_key, page_path, nav_article):
        plos_page = nav_article(ArticleComments, page_path, "", ingest=True)
        actual_title = plos_page.get_comments_title()
        expected_title = "Reader Comments"
        assert actual_title == expected_title, 'The title: {0!r} is not the expected: {1!r}'\
            .format(actual_title, expected_title)

        self.validate_comments_count(plos_page)

        plos_page.validate_comments()
        plos_page.validate_new_comment(page_path)

    @staticmethod
    def validate_comments_count(plos_page):
        expected_comments_count = \
            plos_page.get_rhino_comments_count(plos_page.extract_page_escaped_doi())
        actual_page_comments_count = plos_page.get_page_comments_count()

        assert actual_page_comments_count == expected_comments_count, \
            'Title comment count: {0!s} is not the expected: ' \
            '{1!s}'.format(actual_page_comments_count, expected_comments_count)
