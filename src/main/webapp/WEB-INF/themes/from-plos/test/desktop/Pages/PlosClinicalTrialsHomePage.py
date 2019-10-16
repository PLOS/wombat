#!/usr/bin/env python2
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

__author__ = 'jgray@plos.org'

from .HomePage import HomePage
from .Menu import Menu
from os import getenv


class PlosClinicalTrialsHomePage(HomePage):
  """
  Model the PLoS Clinical Trials Journal page.
  """
  base_url = getenv('WEBDRIVER_TARGET_URL', 'http://one-dpro.plosjournals.org/wombat')
  LINK_URL = base_url + "/DesktopPlosClinicalTrials/"

  def __init__(self, driver):
    super(PlosClinicalTrialsHomePage, self).__init__(driver, '/DesktopPlosClinicalTrials/')

    # POM - Instance members
    self._menu = Menu(driver)

    # Locators - Instance members

  # POM Actions

  def validate_publish_submissions_links(self):
    """
    Check for agreement between number, and list of menu items of the Publish::Submissions sub-menu, then do a get
    on those targets to ensure they exist. (Return a 200)
    :return: Success on success or Error message indicating what non-200 error code was received.
    """
    expected_links = {# Submissions
                      'Getting Started': '%ss/getting-started' % self.LINK_URL,
                      'Submission Guidelines': '%ss/submission-guidelines' % self.LINK_URL,
                      'Figures': '%ss/figures' % self.LINK_URL,
                      'Tables': '%ss/tables' % self.LINK_URL,
                      'Supporting Information': '%ss/supporting-information' % self.LINK_URL,
                      'LaTeX': '%ss/latex' % self.LINK_URL,
                      'Other Article Types': '%ss/other-article-types' % self.LINK_URL,
                      'Revising Your Manuscript': '%ss/revising-your-manuscript' % self.LINK_URL,
                      'Submit Now': '%ss/submit-now' % self.LINK_URL
                      }
    self._menu.validate_publish_submissions_links(expected_links)

  def validate_publish_policies_links(self):
    """
    Check for agreement between number, and list of menu items of the Publish::Policies sub-menu, then do a get
    on those targets to ensure they exist. (Return a 200)
    :return: Success on success or Error message indicating what non-200 error code was received.
    """
    expected_links = {# Policies
                      'Best Practices in Research Reporting': '%ss/best-practices-in-research-reporting' % self.LINK_URL,
                      'Human Subjects Research': '%ss/human-subjects-research' % self.LINK_URL,
                      'Animal Research': '%ss/animal-research' % self.LINK_URL,
                      'Competing Interests': '%ss/competing-interests' % self.LINK_URL,
                      'Disclosure of Funding Sources': '%ss/disclosure-of-funding-sources' % self.LINK_URL,
                      'Licenses and Copyright': '%ss/licenses-and-copyright' % self.LINK_URL,
                      'Data Availability': '%ss/data-availability' % self.LINK_URL,
                      'Materials and Software Sharing': '%ss/materials-and-software-sharing' % self.LINK_URL,
                      'Ethical Publishing Practice': '%ss/ethical-publishing-practice' % self.LINK_URL,
                     }
    self._menu.validate_publish_policies_links(expected_links)

  def validate_publish_manuscript_review_links(self):
    """
    Check for agreement between number, and list of menu items of the Publish::Manuscript Review and Publication
    sub-menu, then do a get on those targets to ensure they exist. (Return a 200)
    :return: Success on success or Error message indicating what non-200 error code was received.
    """
    expected_links = {# Manuscript Review and Publication
                      'Criteria for Publication': '%ss/criteria-for-publication' % self.LINK_URL,
                      'Editorial and Peer Review Process': '%ss/editorial-and-peer-review-process' % self.LINK_URL,
                      'Guidelines for Reviewers': '%ss/reviewer-guidelines' % self.LINK_URL,
                      'Accepted Manuscripts': '%ss/accepted-manuscripts' % self.LINK_URL,
                      'Corrections and Retractions': '%ss/corrections-and-retractions' % self.LINK_URL,
                      'Comments': '%ss/comments' % self.LINK_URL,
                      'Article-Level Metrics': '%ss/article-level-metrics' % self.LINK_URL
                      }
    self._menu.validate_publish_manuscript_review_links(expected_links)

  def validate_about_links(self):
    """
    Check for agreement between number, and list of menu items of the About menu, then do a get on those targets to
    ensure they exist. (Return a 200)
    :return: Success on success or Error message indicating what non-200 error code was received.
    """
    expected_links = {'Journal Information': '%ss/journal-information' % self.LINK_URL,
                      'Editorial Board': '%ss/editorial-board' % self.LINK_URL,
                      'Section Editors': '%ss/section-editors' % self.LINK_URL,
                      'Advisory Groups': '%ss/advisory-groups' % self.LINK_URL,
                      'Publishing Information': '%ss/publishing-information' % self.LINK_URL,
                      'Publication Fees': '%ss/publication-fees' % self.LINK_URL,
                      'Press and Media': '%ss/press-and-media' % self.LINK_URL,
                      'Resources': '%ss/resources' % self.LINK_URL,
                      'Contact': '%ss/contact' % self.LINK_URL
                    }
    self._menu.validate_about_links(expected_links)
