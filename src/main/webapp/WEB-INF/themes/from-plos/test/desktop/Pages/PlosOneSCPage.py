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


pages = ['/DesktopPlosOne/s/accepted-manuscripts',
         '/DesktopPlosOne/s/advisory-groups',
         '/DesktopPlosOne/s/animal-research',
         '/DesktopPlosOne/s/contact',
         '/DesktopPlosOne/s/criteria-for-publication',
         '/DesktopPlosOne/s/editorial-and-peer-review-process',
         '/DesktopPlosOne/s/figures',
         '/DesktopPlosOne/s/getting-started',
         '/DesktopPlosOne/s/human-subjects-research',
         '/DesktopPlosOne/s/journal-information',
         '/DesktopPlosOne/s/materials-and-software-sharing',
         '/DesktopPlosOne/s/press-and-media',
         '/DesktopPlosOne/s/reviewer-guidelines',
         '/DesktopPlosOne/s/revising-your-manuscript',
         '/DesktopPlosOne/s/section-editors',
         '/DesktopPlosOne/s/submission-guidelines',
         '/DesktopPlosOne/s/submit-now'
         ]


class PlosOneSCPage(SiteContentPage):

  """
  Model the PLoS One Site Content page.
  """

  PROD_URL = 'http://www.plosone.org/'

  def __init__(self, driver):
    super(PlosOneSCPage, self).__init__(driver, random.choice(pages))

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
