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

import logging
import random

from .ArticleMetrics import ArticleMetrics

__author__ = 'jfesenko@plos.org'

dois = [
    # '/DesktopPlosBiology/article/metrics?id=10.1371/journal.pbio.0030408',
    # '/DesktopPlosBiology/article/metrics?id=10.1371/journal.pbio.0040088',
    '/DesktopPlosBiology/article/metrics?id=10.1371/journal.pbio.1001199',
    '/DesktopPlosBiology/article/metrics?id=10.1371/journal.pbio.1001291',
    '/DesktopPlosOne/article/metrics?id=10.1371/journal.pone.0088458',
    '/DesktopPlosOne/article/metrics?id=10.1371/journal.pone.0085292',
    # '/DesktopPlosOne/article/metrics?id=10.1371/journal.pone.0142893',
    # '/DesktopPlosOne/article/metrics?id=10.1371/journal.pone.0141595',
    # '/DesktopPlosMedicine/article/metrics?id=10.1371/journal.pmed.0020402',
    # '/DesktopPlosMedicine/article/metrics?id=10.1371/journal.pmed.0030132',
    # '/DesktopPlosMedicine/article/metrics?id=10.1371/journal.pmed.0030205',
    # '/DesktopPlosMedicine/article/metrics?id=10.1371/journal.pmed.0030303',
    # '/DesktopPlosNtds/article/metrics?id=10.1371/journal.pntd.0001446',
    # '/DesktopPlosNtds/article/metrics?id=10.1371/journal.pntd.0001969',
    # '/DesktopPlosNtds/article/metrics?id=10.1371/journal.pntd.0002570',
    # '/DesktopPlosNtds/article/metrics?id=10.1371/journal.pntd.0002958',
    # '/DesktopPlosPathogens/article/metrics?id=10.1371/journal.ppat.1002769',
    # '/DesktopPlosPathogens/article/metrics?id=10.1371/journal.ppat.1003133',
    '/DesktopPlosPathogens/article/metrics?id=10.1371/journal.ppat.1003762',
    # '/DesktopPlosPathogens/article/metrics?id=10.1371/journal.ppat.1004200',
    # '/DesktopPlosGenetics/article/metrics?id=10.1371/journal.pgen.1002912',
    # '/DesktopPlosGenetics/article/metrics?id=10.1371/journal.pgen.1003316',
    # '/DesktopPlosGenetics/article/metrics?id=10.1371/journal.pgen.1003500',
    # '/DesktopPlosGenetics/article/metrics?id=10.1371/journal.pgen.1004451',
    # '/DesktopPlosCompBiol/article/metrics?id=10.1371/journal.pcbi.1000974',
    # '/DesktopPlosCompBiol/article/metrics?id=10.1371/journal.pcbi.1001051',
    # '/DesktopPlosCompBiol/article/metrics?id=10.1371/journal.pcbi.1001083',
    '/DesktopPlosCompBiol/article/metrics?id=10.1371/journal.pcbi.1002484'
]


class PlosArticleMetrics(ArticleMetrics):
    """
    Model the PLOS Article page, metrics tab.
    """
    PROD_URL = 'https://journals.plos.org/plosbiology/article/metrics?id=10.1371/journal.pbio.1001569'

    def __init__(self, driver):
        selected_doi = random.choice(dois)
        logging.info('Selected doi: {0}'.format(selected_doi))
        super(PlosArticleMetrics, self).__init__(driver, selected_doi)


        # POM - Instance members

        # Locators - Instance members

        # POM Actions
