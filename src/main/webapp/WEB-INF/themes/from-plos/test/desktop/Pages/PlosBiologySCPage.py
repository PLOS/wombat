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

from .SiteContentPage import SiteContentPage

__author__ = 'jgray@plos.org'

pages = ['/DesktopPlosBiology/s/accepted-manuscripts',
         '/DesktopPlosBiology/s/animal-research',
         '/DesktopPlosBiology/s/article-level-metrics',
         '/DesktopPlosBiology/s/best-practices-in-research-reporting',
         '/DesktopPlosBiology/s/comments',
         '/DesktopPlosBiology/s/competing-interests',
         '/DesktopPlosBiology/s/contact',
         '/DesktopPlosBiology/s/licenses-and-copyright',
         '/DesktopPlosBiology/s/corrections-and-retractions',
         '/DesktopPlosBiology/s/data-availability',
         '/DesktopPlosBiology/s/disclosure-of-funding-sources',
         '/DesktopPlosBiology/s/editorial-and-peer-review-process',
         '/DesktopPlosBiology/s/editorial-and-publishing-policies',
         '/DesktopPlosBiology/s/editorial-board',
         '/DesktopPlosBiology/s/ethical-publishing-practice',
         '/DesktopPlosBiology/s/figures',
         '/DesktopPlosBiology/s/help-using-this-site',
         '/DesktopPlosBiology/s/human-subjects-research',
         '/DesktopPlosBiology/s/journal-information',
         '/DesktopPlosBiology/s/latex',
         '/DesktopPlosBiology/s/list-of-filtered-robots-spiders',
         '/DesktopPlosBiology/s/materials-and-software-sharing',
         '/DesktopPlosBiology/s/other-article-types',
         '/DesktopPlosBiology/s/press-and-media',
         '/DesktopPlosBiology/s/publication-fees',
         '/DesktopPlosBiology/s/publishing-information',
         '/DesktopPlosBiology/s/reviewer-guidelines',
         '/DesktopPlosBiology/s/revising-your-manuscript',
         '/DesktopPlosBiology/s/search-tips',
         '/DesktopPlosBiology/s/submission-guidelines',
         '/DesktopPlosBiology/s/submit-now',
         '/DesktopPlosBiology/s/supporting-information',
         '/DesktopPlosBiology/s/tables',
         '/DesktopPlosBiology/s/usage-data-help'
         ]


class PlosBiologySCPage(SiteContentPage):
    """
    Model the PLoS Biology Site Content page.
    """

    PROD_URL = 'http://www.plosbiology.org/'

    def __init__(self, driver):
        selected_page = random.choice(pages)
        logging.info('Selected PLoS Biology Site Content Page: {0!r}'.format(selected_page))
        super(PlosBiologySCPage, self).__init__(driver, selected_page)

        # POM - Instance members

        # Locators - Instance members

        # POM Actions
