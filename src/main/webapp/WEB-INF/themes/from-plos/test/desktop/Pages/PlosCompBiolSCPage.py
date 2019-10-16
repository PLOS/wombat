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


pages = ['/DesktopPlosCompBiol/s/accepted-manuscripts',
         '/DesktopPlosCompBiol/s/contact',
         '/DesktopPlosCompBiol/s/editorial-and-peer-review-process',
         '/DesktopPlosCompBiol/s/editorial-board',
         '/DesktopPlosCompBiol/s/editors-in-chief',
         '/DesktopPlosCompBiol/s/getting-started',
         '/DesktopPlosCompBiol/s/human-subjects-research',
         '/DesktopPlosCompBiol/s/journal-information',
         '/DesktopPlosCompBiol/s/materials-and-software-sharing',
         '/DesktopPlosCompBiol/s/other-article-types',
         '/DesktopPlosCompBiol/s/press-and-media',
         '/DesktopPlosCompBiol/s/presubmission-inquiries',
         '/DesktopPlosCompBiol/s/reviewer-guidelines',
         '/DesktopPlosCompBiol/s/revising-your-manuscript',
         '/DesktopPlosCompBiol/s/submission-guidelines',
         '/DesktopPlosCompBiol/s/submit-now'
         ]


class PlosCompBiolSCPage(SiteContentPage):

  """
  Model the PLoS Computational Biology Site Content page.
  """

  PROD_URL = 'http://www.ploscompbiol.org/'

  def __init__(self, driver):
    super(PlosCompBiolSCPage, self).__init__(driver, random.choice(pages))

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
