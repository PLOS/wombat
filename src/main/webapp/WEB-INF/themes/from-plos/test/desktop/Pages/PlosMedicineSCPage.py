#!/usr/bin/env python2

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

from .SiteContentPage import SiteContentPage
import random


pages = ['/DesktopPlosMedicine/s/accepted-manuscripts',
         '/DesktopPlosMedicine/s/competing-interests',
         '/DesktopPlosMedicine/s/contact',
         '/DesktopPlosMedicine/s/editorial-and-peer-review-process',
         '/DesktopPlosMedicine/s/editorial-board',
         '/DesktopPlosMedicine/s/getting-started',
         '/DesktopPlosMedicine/s/human-subjects-research',
         '/DesktopPlosMedicine/s/journal-information',
         '/DesktopPlosMedicine/s/materials-and-software-sharing',
         '/DesktopPlosMedicine/s/other-article-types',
         '/DesktopPlosMedicine/s/press-and-media',
         '/DesktopPlosMedicine/s/presubmission-inquiries',
         '/DesktopPlosMedicine/s/reviewer-guidelines',
         '/DesktopPlosMedicine/s/revising-your-manuscript',
         '/DesktopPlosMedicine/s/submission-guidelines',
         '/DesktopPlosMedicine/s/submit-now',
         '/DesktopPlosMedicine/s/why-publish-with-plos-medicine'
         ]


class PlosMedicineSCPage(SiteContentPage):

  """
  Model the PLoS Medicine Site Content page.
  """

  PROD_URL = 'http://www.plosmedicine.org/'

  def __init__(self, driver):
    super(PlosMedicineSCPage, self).__init__(driver, random.choice(pages))

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
