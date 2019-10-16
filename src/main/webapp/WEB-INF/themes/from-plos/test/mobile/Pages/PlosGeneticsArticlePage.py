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

__author__ = 'gtimomnina@plos.org'

import logging
import random

from test.mobile.Pages.ArticlePage import ArticlePage

dois = [
    '/MobilePlosGenetics/article?id=10.1371/journal.pgen.1003500',
    '/MobilePlosGenetics/article?id=10.1371/journal.pgen.1000052',
    '/MobilePlosGenetics/article?id=10.1371/journal.pgen.1004643',
    '/MobilePlosGenetics/article?id=10.1371/journal.pgen.1003500',
]


class PlosGeneticsArticlePage(ArticlePage):
  """
  Model the PLoS One Article page.
  """
  PROD_URL = 'https://journals.plos.org/plosgenetics/article?id=10.1371/journal.pgen.1003500'

  def __init__(self, driver):
      selected_doi = random.choice(dois)
      logging.info('Selected doi: {0}'.format(selected_doi))
      super(PlosGeneticsArticlePage, self).__init__(driver, selected_doi)

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
