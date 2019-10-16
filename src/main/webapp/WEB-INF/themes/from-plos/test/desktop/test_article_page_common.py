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
This test case validates the Wombat Desktop Article Page Common Header Elements for all journals
"""

import logging
import pytest
import time

from ..Base.CustomException import ElementDoesNotExistAssertionError
from ..desktop.Pages.PlosBiologyArticlePage import PlosBiologyArticlePage
from ..desktop.Pages.PlosMedicineArticlePage import PlosMedicineArticlePage
from ..desktop.Pages.PlosCompBiolArticlePage import PlosCompBiolArticlePage
from ..desktop.Pages.PlosGeneticsArticlePage import PlosGeneticsArticlePage
from ..desktop.Pages.PlosPathogensArticlePage import PlosPathogensArticlePage
from ..desktop.Pages.PlosNeglectedArticlePage import PlosNeglectedArticlePage
from ..desktop.Pages.PlosOneArticlePage import PlosOneArticlePage
from ..desktop.Pages.PlosClinicalTrialsArticlePage import PlosClinicalTrialsArticlePage

__author__ = 'jgray@plos.org'


@pytest.mark.usefixtures("driver_get")
class TestArticlePageCommon:
    @pytest.mark.parametrize("plos_page", [
        PlosBiologyArticlePage,
        PlosMedicineArticlePage,
        PlosCompBiolArticlePage,
        PlosGeneticsArticlePage,
        PlosPathogensArticlePage,
        PlosNeglectedArticlePage,
        PlosOneArticlePage,
        PlosClinicalTrialsArticlePage,
    ])
    def test_plos_article_header(self, plos_page):
        test_page = plos_page(self.driver)
        # inserting a sleep here as occasionally in practice the page objects would not be
        # recognized as available on instantiating the driver and the assert_license call would
        # fail.
        time.sleep(3)
        test_page.assert_license()
        test_page.assert_article_type()
        article_type_text = test_page.return_article_type()
        # if article type is Research Article, then display peer reviewed bug
        if article_type_text == 'RESEARCH ARTICLE':
            logging.info("Article type is Research Article, checking for Peer Reviewed logo")
            test_page.assert_peer_reviewed()
        else:
            test_page.set_timeout(1)
            logging.info('Article Type is: {0!r}. Peer-Reviewed logo should not be present.'
                         .format(article_type_text))
            try:
                test_page.assert_peer_reviewed()
            except ElementDoesNotExistAssertionError:
                pass
            finally:
                test_page.restore_timeout()
        test_page.assert_article_title()
        test_page.return_article_title()
        # validate author list present
        has_auth_list = test_page.validate_article_author_list()
        if has_auth_list:
            test_page.assert_article_author_list()
            # if author list is > 14 authors ensure truncation of list by first 13,
            # last one pattern
            test_page.validate_article_author_list_truncation()
            test_page.click_article_author_list_expander_contractor()
            """
            DPRO-319
            for author in author_list: do
              1. validate hover underline
              2. validate on click:
                a. list item style change (blue pill, white text)
                b. expanding pill opens with following elements:
                  i. Close X
                  ii. Affiliation list
                  iii. if corresponding author, email icon, email addr wrapped in mailto anchor
                  iv. if co-contributing author, co-contributor icon, explanation (multiple layers 
                      of co-contributing possible)
                  v. if supervisory author, supervisor icon, explanation
            """
            test_page.validate_article_author_list_click_tooltip()
            test_page.validate_special_auth('corresp')
            test_page.validate_special_auth('cocontrib')
            test_page.validate_special_auth('customfootnote')
        # TODO: Consider looking in mysql based on articleDoi return to determine if article.title
        # contains text within
        # TODO:  <italic> tags that should then have that text rendered in <i> tags in the html -
        # Could be slow and expensive
        test_page.assert_article_publication_date()
        # TODO: Extract Publish date from mysql based on articleDoi return
        test_page.assert_article_doi()
        # TODO: This is the primary canonical key for an article - do we need to validate
        # this independently?
        # If Published In is present for article, validate that Journals are rendered in italics
        # and Plos Collections is not
        test_page.set_timeout(1)
        try:
            xpub_true = test_page.assert_article_published_in()
        except ElementDoesNotExistAssertionError:
            xpub_true = ''
        finally:
            test_page.restore_timeout()
        if xpub_true:
            test_page.set_timeout(1)
            try:
                test_page.assert_article_published_in_journal()
                xpubbedjournal = test_page.return_article_published_in()
                self.assertNotEqual(xpubbedjournal, 'PLOS Collections',
                                    msg="Collections is italicized")
            except AssertionError:
                xpubbedjournal = test_page.return_article_published_in()
                self.assertEqual(xpubbedjournal, 'PLOS Collections',
                                 msg="Collections is not italicized")
            finally:
                test_page.restore_timeout()
        test_page.assert_tab_article()
        test_page.assert_tab_article_active()
        # if article_type NOT in (CORRECTION, RETRACTION, EXPRESSION OF CONCERN)
        no_author_tab = {'CORRECTION', 'RETRACTION', 'EXPRESSION OF CONCERN'}
        author_list = test_page.validate_article_author_list()
        if article_type_text in no_author_tab:
            logging.info("Article type: {0!r} precludes display of Author tab."
                         .format(article_type_text))
            test_page.set_timeout(1)
            try:
                test_page.assert_tab_authors()
            except ElementDoesNotExistAssertionError:
                pass
            finally:
                test_page.restore_timeout()
        elif not author_list:
            logging.info("Article lacks any authors: precludes display of Author tab.")
            test_page.set_timeout(1)
            try:
                test_page.assert_tab_authors()
            except AssertionError:
                pass
            finally:
                test_page.restore_timeout()
        else:
            test_page.assert_tab_authors()
        test_page.assert_tab_metrics()
        test_page.assert_tab_comments()
        test_page.assert_tab_related()
        # On initial page load the floating header should not be present
        test_page.set_timeout(1)
        try:
            test_page.assert_floating_header()
        except AssertionError:
            pass
        finally:
            test_page.restore_timeout()

        test_page = plos_page(self.driver)
        test_page.refresh()
        test_page.moveto_top()
        test_page.moveto_footer()
        test_page.assert_floating_header()
        test_page.assert_floating_header_title()
        test_page.assert_floating_header_author_list()
        test_page.assert_floating_header_logo()
        test_page.click_floating_header_closer()
        test_page.moveto_top()
        test_page.set_timeout(1)
        try:
            test_page.assert_floating_header()
        except AssertionError:
            pass
        finally:
            test_page.restore_timeout()
