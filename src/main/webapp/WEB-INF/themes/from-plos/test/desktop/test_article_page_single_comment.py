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
This test case validates the PLOS desktop site's Article page single comment page.

"""

__author__ = 'ivieira@plos.org'

import pytest

from ..Base.Journal import Journal
from .Pages.ArticleSingleComment import ArticleSingleComment

article_comments_list = {
    '13ea20d1-91e6-49c3-bc4b-8fd1ca18f150': 'PLoSONE',
    '1689c06d-9e47-4459-ae40-ae29a325b2e5': 'PLoSONE',
    'bcf5eb7a-8bf2-437e-ae27-d43b5cc749f6': 'PLoSBiology',
    '30604e2f-f925-4e0f-8896-b69e25b720ee': 'PLoSBiology',
    '7210a93e-0455-4c11-9c9a-240d4b13410a': 'PLoSCompBiol',
    'a2030054-c78e-493c-bef7-e08d3fd9b909': 'PLoSCompBiol',
    '040af32a-5155-4b5f-a5b5-96cc167305c3': 'PLoSGenetics',
    'c1af84c8-4270-42a5-ba0d-98ecce31f040': 'PLoSPathogens',
    '2b649a20-5845-433c-93f2-e75b3701e5ec': 'PLoSPathogens',
    'a19124d7-1387-489a-a965-8e58dd285e2d': 'PLoSMedicine',
    'd14fae02-c6b8-4222-86bc-0ca812fa442b': 'PLoSMedicine',
    'fcfeee13-1348-4adf-9995-2b99fee50088': 'PLoSNTD',
    'fdac9146-906d-42da-8889-e9ab0d1cc379': 'PLoSNTD',
}


def make_article_page_single_comment_cases():
    for (uri, journalKey) in article_comments_list.items():
        uri = '10.1371/annotation/{0}'.format(uri)
        yield journalKey, Journal.build_article_single_comment_path(journalKey, uri), uri


def idfn(fixture_value):
    """
    The function to return simple parameter id instead of full article comment info
    """
    return '{0}_{1}'.format(fixture_value[0],fixture_value[2].split('/')[-1])


@pytest.mark.usefixtures("driver_get")
class TestArticlePageSingleComment:
    @pytest.mark.parametrize("article_comments_case",
                             make_article_page_single_comment_cases(), ids=idfn)
    def test_article_comments(self, article_comments_case, nav_annotation):
        journal_key, page_path, uri = article_comments_case

        plos_page = nav_annotation(ArticleSingleComment, page_path, uri)

        plos_page.validate_comment_content()
        plos_page.validate_replies()
