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

__author__ = 'jgray@plos.org'

import logging
import random

from ..Pages.ArticleBody import ArticleBody

dois = [
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.0020120',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.0030134',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.0030158',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1000112',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1000589',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1000974',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1001051',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1001083',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1002484',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1003447',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1003842',
    '/DesktopPlosCompBiol/article?id=10.1371/journal.pcbi.1003849',
]


class PlosCompBiolArticleBody(ArticleBody):
    """
    Model the PLoS Computational Biology article page, article tab.
    """


    PROD_URL = 'https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.0020120'


    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi for PlosCompBioArticleBody: {0!r}'.format(selected_doi))
        super(PlosCompBiolArticleBody, self).__init__(driver, selected_doi)

        # POM - Instance members

        # Locators - Instance members

    # POM Actions
