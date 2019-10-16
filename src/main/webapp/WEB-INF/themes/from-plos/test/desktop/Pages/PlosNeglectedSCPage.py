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


pages = ['/DesktopPlosNtds/s/accepted-manuscripts',
         '/DesktopPlosNtds/s/contact',
         '/DesktopPlosNtds/s/editorial-and-peer-review-process',
         '/DesktopPlosNtds/s/editorial-board',
         '/DesktopPlosNtds/s/editors-in-chief',
         '/DesktopPlosNtds/s/getting-started',
         '/DesktopPlosNtds/s/human-subjects-research',
         '/DesktopPlosNtds/s/journal-information',
         '/DesktopPlosNtds/s/materials-and-software-sharing',
         '/DesktopPlosNtds/s/other-article-types',
         '/DesktopPlosNtds/s/press-and-media',
         '/DesktopPlosNtds/s/presubmission-inquiries',
         '/DesktopPlosNtds/s/reviewer-guidelines',
         '/DesktopPlosNtds/s/revising-your-manuscript',
         '/DesktopPlosNtds/s/submission-guidelines',
         '/DesktopPlosNtds/s/submit-now'
         ]


class PlosNeglectedSCPage(SiteContentPage):

  """
  Model the PLoS Neglected Tropical Diseases Site Content page.
  """

  PROD_URL = 'http://www.plosntds.org/'

  def __init__(self, driver):
    super(PlosNeglectedSCPage, self).__init__(driver, random.choice(pages))

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
