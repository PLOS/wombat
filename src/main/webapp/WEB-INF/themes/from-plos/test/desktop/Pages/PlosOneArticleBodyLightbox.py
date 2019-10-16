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

from ..Pages.PlosOneArticleBody import PlosOneArticleBody
from ..Pages.Lightbox import Lightbox

dois = [
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0002554',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0005723',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0008519',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0010685',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0016329',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0016976',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0026358',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0027062',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0028031',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0036880',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0040259',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0040740',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0042593',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0046041',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0047391',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0050698',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0052690',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0055490',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0057943',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0058242',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0066742',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0067179',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0067227',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0067380',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0068090',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0081648',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0087236',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0093414',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0100977',
    '/DesktopPlosOne/article?id=10.1371/journal.pone.0105948',
]


class PlosOneArticleBodyLightbox(PlosOneArticleBody, Lightbox):
    """
    Model the PLoS One Article page, article tab.
    """
    PROD_URL = 'https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0002554'

    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi for PlosOneArticleBody: {0!r}'.format(selected_doi))
        super(PlosOneArticleBody, self).__init__(driver, selected_doi)

        # POM - Instance members

        # Locators - Instance members

    # POM Actions
