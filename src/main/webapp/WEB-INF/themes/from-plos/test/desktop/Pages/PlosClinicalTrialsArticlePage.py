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

from ..Pages.ArticlePage import ArticlePage

dois = ['/DesktopPlosClinicalTrials/article?id=10.1371/journal.pctr.0020005']


class PlosClinicalTrialsArticlePage(ArticlePage):
    """
    Model the PLoS Clinical Trials Article page.
    """
    PROD_URL = 'https://journals.plos.org/plosclinicaltrials/article?id=10.1371/' \
               'journal.pctr.0020005'

    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi: {0}'.format(selected_doi))
        super(PlosClinicalTrialsArticlePage, self).__init__(driver, selected_doi)

        # POM - Instance members

        # Locators - Instance members

    # POM Actions
