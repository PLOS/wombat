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

__author__ = 'sbassi@plos.org'


import logging
import random

from ..Pages.PlosMedicineArticleBody import PlosMedicineArticleBody
from ..Pages.Lightbox import Lightbox

dois = [
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0020007',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0020124',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0020171',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0020402',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0030132',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0030445',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.0030520',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1000097',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1000100',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1000431',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001080',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001200',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001300',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001418',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001473',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001518',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001644',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1001743',
    '/DesktopPlosMedicine/article?id=10.1371/journal.pmed.1000431',
]


class PlosMedicineArticleBodyLightbox(PlosMedicineArticleBody, Lightbox):
    """
    Model the PLoS Medicine Article page, article tab.
    """

    PROD_URL = 'https://journals.plos.org/plosmedicine/article?id=10.1371/journal.pmed.1001644'

    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi for PlosMedicineArticleBody: {0!r}'.format(selected_doi))
        super(PlosMedicineArticleBody, self).__init__(driver, selected_doi)

        # POM - Instance members

        # POM - Locators section

    # POM Actions
