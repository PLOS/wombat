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

import random
import logging

from ..Pages.ArticleBody import ArticleBody

dois = ['/DesktopPlosBiology/article?id=10.1371/journal.pbio.0030408',
        '/DesktopPlosBiology/article?id=10.1371/journal.pbio.0040088',
        '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001199',
        '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001291',
        '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001315',
        '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001569'
        ]


class PlosBiologyArticleBody(ArticleBody):
    """
    Model the PLoS Biology Article page, article tab.
    """
    PROD_URL = 'https://journals.plos.org/plosbiology/article?id=10.1371/journal.pbio.1001569'

    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi for PlosBiologyArticleBody: {0!r}'.format(selected_doi))
        super(PlosBiologyArticleBody, self).__init__(driver, selected_doi)

    # POM - Instance members

    # Locators - Instance members

    # POM Actions
