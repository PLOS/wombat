# !/usr/bin/env python3
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
This test case validates the Wombat Desktop Article Page article tab body elements
"""

import logging
import random

import pytest

from .Pages.ArticlePeerReview import ArticlePeerReview
from .resources import sevenjournals
from ..Base import Utils
from ..Base.Journal import Journal

__author__ = 'gtimonina@plos.org'

articles_tpr = {
    'PLoSONE': [
        '10.1371/journal.pone.0219832',
        '10.1371/journal.pone.0219719',
        '10.1371/journal.pone.0218301',
        ],
    }

articles_no_tpr = {
    'PLoSONE': [
        '10.1371/journal.pone.0162114',
        '10.1371/journal.pone.0204878',
        "10.1371/journal.pone.0066742",
        "10.1371/journal.pone.0067179",
        ],
    'PLoSBiology': [
        '10.1371/journal.pbio.0030408',
        '10.1371/journal.pbio.1001569',
        '10.1371/journal.pbio.0040088',
        '10.1371/journal.pbio.1001199',
        ],
    'PLoSMedicine': [
        '10.1371/journal.pmed.0020007',
        '10.1371/journal.pmed.0020124',
        '10.1371/journal.pmed.1001473',
        '10.1371/journal.pmed.1001518',
        ],
    'PLoSGenetics': [
        '10.1371/journal.pgen.1002912',
        '10.1371/journal.pgen.1003316',
        '10.1371/journal.pgen.1003500',
        '10.1371/journal.pgen.1004451',
        ],
    'PLoSCompBiol': [
        '10.1371/journal.pcbi.0030158',
        '10.1371/journal.pcbi.1000112',
        '10.1371/journal.pcbi.1003447',
        '10.1371/journal.pcbi.1003842'
        ],
    'PLoSPathogens': [
        '10.1371/journal.ppat.0040045',
        '10.1371/journal.ppat.1000105',
        '10.1371/journal.ppat.1004377',
        '10.1371/journal.ppat.1004389',
        ],
    'PLoSNTD': [
        '10.1371/journal.pntd.0000149',
        '10.1371/journal.pntd.0001041',
        '10.1371/journal.pntd.0002958',
        '10.1371/journal.pntd.0003188',
        ],
    }


def make_cases(journal_key, articles, tab=''):
    """
    Function to get randomized doi and article section to use in test from 'articles' dictionary
    :param journal_key: specific journal key, string
    :param articles: dictionary with test data
    :param tab: specific tab to navigate, default is '', means navigating to the default
        'Article' tab
    :return: randomly selected doi, article_section (strings)
    """

    item = articles[journal_key]
    doi = random.choice(item)
    if tab == 'Peer Review':
        article_path = Journal.build_article_peer_review_path(journal_key, doi)
    else:
        article_path = Journal.build_article_path(journal_key, doi)
    return doi, article_path


@pytest.mark.usefixtures("driver_get")
class TestArticlePeerReview:
    @pytest.mark.parametrize("journal_key", articles_tpr.keys())
    def test_smoke_peer_review_tab(self, journal_key, nav_article):
        """
        Test to validate :
        - xml has 'sub-article' nodes (that means the article has Peer Review content)
        - tab 'Peer Review' should be visible from main and other pages
        - 6 tabs for article with TPR content
        - when the reader clicks on the Peer Review tab, content is displaying
        :param journal_key: parameterized Journal Key value (articles_tpr)
        :param nav_article: pytest fixture to navigate the article using article path
        :return: void function
        """
        doi, article_path = make_cases(journal_key, articles_tpr)
        logging.info('Validating peer review tab on {0!r}'.format(article_path))
        plos_page = nav_article(ArticlePeerReview, article_path, doi, ingest=True)
        sub_articles = plos_page.get_article_peer_review_sections()
        logging.info('')
        assert sub_articles, 'No sub-article nodes in xml, article: {}'.format(article_path)
        logging.info('sub-articles list from xml (titles): {0!r}'.format(sub_articles))

        assert plos_page.peer_review_tab_is_visible(), \
            'Peer Review tab is expected to be visible, article: {}'.format(article_path)

        tabs_list = plos_page.get_tab_names_list()
        assert len(tabs_list) == 6, "6 tabs is expected on the article page, found: {0!s}" \
            .format(len(tabs_list))

        plos_page.click_on_article_tab(plos_page._tab_peer_review)
        assert plos_page.peer_review_tab_is_visible(), \
            'Peer Review tab is expected to be visible from main page, article: {}' \
                .format(article_path)

        assert tabs_list[5] == "Peer Review", 'Expected tab title: "Peer Review", found: {0!r}' \
            .format(tabs_list[5])

        peer_review_letters = plos_page.get_peer_review_letters()

        logging.info('peer_review_letters list from Peer Review tab page (class names): {0!r}'
                     .format(peer_review_letters))
        assert len(peer_review_letters) == len(sub_articles)

        plos_page.click_on_article_tab(plos_page._tab_authors)
        assert plos_page.peer_review_tab_is_visible(), \
            'Peer Review tab is expected to be visible from the "Authors" page, article: {}' \
                .format(article_path)

    @pytest.mark.parametrize("journal_key", articles_no_tpr.keys())
    def test_smoke_404_response_w_no_pr(self, journal_key, nav_article):
        """
        Test to validate :
        - Returns Error page "Page not Found" for the peer review tab for an
        article without peer review
        :param journal_key: parameterized Journal Key value (articles_tpr)
        :param nav_article: pytest fixture to navigate the article using specific path
        :return: void function
        """
        doi, article_path = make_cases(journal_key, articles_no_tpr, 'Peer Review')
        logging.info('Validating 404 response for articles with no peer review '
                     'using direct peer review tab link : {0!r}'.format(article_path))
        # Responds with a 404 to a request for the peer review tab for an article
        # without peer review:  /article/peerReview?id=... should return 'Page not found'
        plos_page = nav_article(ArticlePeerReview, article_path, doi)
        page_title = plos_page.get_page_title().lower()
        assert 'page not found' in page_title, 'Error "Page not Found" page is expected'

    @pytest.mark.parametrize("journal_key", articles_no_tpr.keys())
    def test_smoke_no_peer_review_tab(self, journal_key, nav_article):
        """
        Test to validate :
        - No 'sub-article' nodes in the xml (no Peer Review content)
        - tab 'Peer Review' should not be visible
        - <=5 tabs for article with no TPR content
        :param journal_key: parameterized Journal Key value (articles_tpr)
        :param nav_article: pytest fixture to navigate the article using article path
        :return: void function
        """
        doi, article_path = make_cases(journal_key, articles_no_tpr)
        logging.info('Validating article with no peer review tab: {0!r}'.format(article_path))
        plos_page = nav_article(ArticlePeerReview, article_path, doi, ingest=True)
        sub_articles = plos_page.get_article_peer_review_sections()
        assert not sub_articles, 'Sub-article nodes not expected in xml, article: {}' \
            .format(article_path)
        assert plos_page.no_peer_review_tab(), '"Peer Review" tab should not be displayed'
        tabs_list = plos_page.get_tab_names_list()
        assert len(tabs_list) <= 5, "Incorrect number of tabs. Expected: {0!s}, found: {1!s}" \
            .format("<=5", len(tabs_list))
        assert tabs_list.count("Peer Review") == 0, 'No tab with title "Peer Review" expected'

    @pytest.mark.parametrize("journal_key", articles_tpr.keys())
    def test_smoke_peer_review_tab_style(self, journal_key, nav_article):
        """
        Test to validate Peer Review tab style:
        - tabs retains the shading of grey based on percentage
        - when the reader clicks on the Peer Review tab, the tab has journal brand color as a
        background color
        :param journal_key: parameterized Journal Key value (articles_tpr)
        :param nav_article: pytest fixture to navigate the article using article path
        :return: void function
        """
        doi, article_path = make_cases(journal_key, articles_tpr)
        logging.info('Validating peer review tab on {0!r}'.format(article_path))
        plos_page = nav_article(ArticlePeerReview, article_path, doi, ingest=True)
        j_info = Utils.get_first_dict_by_value(sevenjournals, 'rhinoJournalKey', journal_key)
        journal_brand_color = j_info["journal_brand_color"]
        plos_page.validate_peer_review_tab_style(journal_brand_color)
