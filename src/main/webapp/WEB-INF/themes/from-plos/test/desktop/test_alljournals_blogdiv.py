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
This test validates the Blogs/RSS div block for all
PLOS journals.

For each journal, we validate the presence of the div block,
the div block title link, the number of items presented, their 
images and links.
"""
import logging
import pytest

from selenium.webdriver.support import expected_conditions as exp_cond

from ..Base.Config import base_url
from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage


@pytest.mark.usefixtures("driver_get")
class TestAllJournalsBlogdiv:
    @pytest.mark.homepage
    @pytest.mark.parametrize("journal_home_page", [
        PlosBiologyHomePage,
        PlosCompBiolHomePage,
        PlosGeneticsHomePage,
        PlosMedicineHomePage,
        PlosNeglectedHomePage,
        PlosOneHomePage,
        PlosPathogensHomePage,
    ])
    def test_all_journals_blogs_rss(self, journal_home_page):
        driver = self.driver
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating Blogs for {0}'.format(journal_name))
        wombat_page._get(wombat_page._blogs)
        # If div present, validate div title, link text and targets
        blog_header = wombat_page._get(wombat_page._blogs_header)
        # Validate Title and Title Link of div
        assert blog_header.text == str(journal['journalBlogTitle']), \
            'Incorrect Blog title for {0}, expected: {1!r}, found: {2!r}' \
            .format(journal_name, blog_header.text, journal['journalBlogTitle'])

        blog_header.click()
        wombat_page._wait.until(exp_cond.url_contains('blogs'))
        current_url = wombat_page.get_current_url()
        expected_url = journal['journalBlogTarget'].rstrip('/')
        assert expected_url in current_url, \
            'Incorrect Blog target for {0} journal, expected: {1!r}, found: {2!r}' \
            .format(journal_name, expected_url, current_url)

        driver.back()
        wombat_page._wait.until(exp_cond.url_contains(base_url))
        # TODO: Validate two items returned, with images (and image credits), and publish date.
        # Items should be "latest" blog posts for relevant blogs
        # Validate presence and target of "See All Blogs (>)" link
        wombat_page._get(wombat_page._see_all_blogs_link)
