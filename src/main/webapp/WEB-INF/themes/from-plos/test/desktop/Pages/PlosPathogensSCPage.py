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


pages = ['/DesktopPlosPathogens/s/accepted-manuscripts',
         '/DesktopPlosPathogens/s/contact',
         '/DesktopPlosPathogens/s/editorial-and-peer-review-process',
         '/DesktopPlosPathogens/s/editorial-board',
         '/DesktopPlosPathogens/s/editors-in-chief',
         '/DesktopPlosPathogens/s/getting-started',
         '/DesktopPlosPathogens/s/human-subjects-research',
         '/DesktopPlosPathogens/s/journal-information',
         '/DesktopPlosPathogens/s/materials-and-software-sharing',
         '/DesktopPlosPathogens/s/other-article-types',
         '/DesktopPlosPathogens/s/press-and-media',
         '/DesktopPlosPathogens/s/presubmission-inquiries',
         '/DesktopPlosPathogens/s/reviewer-guidelines',
         '/DesktopPlosPathogens/s/revising-your-manuscript',
         '/DesktopPlosPathogens/s/submission-guidelines',
         '/DesktopPlosPathogens/s/submit-now'
         ]


class PlosPathogensSCPage(SiteContentPage):

  """
  Model the PLoS Pathogens Site Content page.
  """

  PROD_URL = 'http://www.plospathogens.org/'

  def __init__(self, driver):
    super(PlosPathogensSCPage, self).__init__(driver, random.choice(pages))

    # POM - Instance members

    # Locators - Instance members

  # POM Actions
