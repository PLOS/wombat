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
This test case validates the PLOS mobile site article
figure viewer and it's captions.
"""

import logging
import pytest
import random

from test.Base.Journal import Journal
from .Pages.ArticlePage import ArticlePage

articles = {
    'PLoSONE': {
        'pone.0071019': 'Materials and Methods',
        'pone.0085292': 'Methods',
        'pone.0154836': 'Results',
    },
    'PLoSBiology': {
        'pbio.1001199': 'Results',
        'pbio.1001315': 'Results',
        'pbio.2004734': 'Discussion',
    },
    'PLoSCompBiol': {
        'pcbi.1002484': 'Results',
        'pcbi.1003842': 'Design and Implementation',
        'pcbi.1002377': 'Introduction',
    },
    'PLoSGenetics': {
        'pgen.1006973': 'Introduction',
        'pgen.1006754': 'Results',
        'pgen.1006022': 'Results',
    },
    'PLoSMedicine': {
        'pmed.1002459': 'Introduction',
        'pmed.1002012': 'Results',
        'pmed.1001473': 'Methods',
    },
    'PLoSNTD': {
        'pntd.0006451': 'Methods',
        'pntd.0001926': 'Methods',
        'pntd.0006498': 'Results',
    },
    'PLoSPathogens': {
        'ppat.1005590': 'Introduction',
        'ppat.1005623': 'Results',
        'ppat.1007086': 'Discussion',
    },
}


def make_cases(journal_key):
    """
    Function to get randomized doi and article section to use in test from 'articles' dictionary
    :param journal_key: specific journal key, string
    :return: randomly selected doi, article_section (strings)
    """
    item = articles[journal_key]
    doi = random.choice(list(item.keys()))
    article_section = item[doi]
    return doi, article_section


# TODO: remove next line (pytest.mark.skip_iphone) when AMBR-584 gets resolved
@pytest.mark.skip_iphone
@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("journal_key", articles.keys())
class TestArticleFigureViewerCaptionsNavigation:
    def test_article_test_figure_viewer_captions_navigation(self, journal_key):
        doi, article_section = make_cases(journal_key)
        doi = 'info:doi/10.1371/journal.{0}'.format(doi)
        article_path = Journal.build_article_path(journal_key, doi)
        logging.info('Validating figure viewer on {0!r}, article section: {1}'
                     .format(article_path, article_section))
        skip_iphone = self.skip_specific_validation
        plos_page = ArticlePage(self.driver, article_path)

        # click on article section
        plos_page.click_on_section_name(self.driver, article_section)
        plos_page.validate_figure_viewer_links(skip_iphone)
        plos_page.move_back_to_article()

        plos_page.click_on_journal_logo()
