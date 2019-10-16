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

from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException

from .Menu import Menu
from ...Base import Config


class MenuManuscript(Menu):

  """
  Model the desktop PLos site menu.
  """

  def __init__(self, driver):
    super(MenuManuscript, self).__init__(driver)

    # Locators - Instance members
    self._manuscript_panel = (By.CLASS_NAME, "calloutcontainer")
    self._get_started_link = (By.CSS_SELECTOR, "a.btn")
    self._submit_your_manuscript_title = (By.CLASS_NAME, "callout-headline")
    self._text_paragraph = (By.CLASS_NAME, "callout-content")
    self._sub_link_in_paragraph = (By.XPATH, './a')

  # POM Actions
  def click_get_started_link(self):
    print ('Click Get Started button')
    get_started = self._get(self._manuscript_panel).find_element(*self._get_started_link)
    get_started.click()
    return self

  def _validate_section_title(self):
    print ('Starting validation of Submit Your Manuscript section...')
    print ('Validating title...')
    title = self._get(self._manuscript_panel).find_element(*self._submit_your_manuscript_title)
    assert title.text == 'SUBMIT YOUR MANUSCRIPT', title.text

  def _validate_paragraph_contains_link(self, paragraph, paragraph_link):
    try:
      print ('Checking for links in manuscript paragraph...')
      self._driver.implicitly_wait(1)
      sub_link = paragraph.find_element(By.XPATH, './a')
      if sub_link:
        print ('Bullet has a link, validating it...',)
        assert sub_link.get_attribute('href') == paragraph_link
        print ('HREF OK /',)
        self._is_link_valid(sub_link)
    except NoSuchElementException:
      pass
    self._driver.implicitly_wait(Config.wait_timeout)

  def _validate_section_paragraph(self, expected_paragraph):
    print ('Starting validation of paragraph...')
    actual_paragraph = self._get(self._manuscript_panel).find_elements(*self._text_paragraph)
    paragraph_count1 = len(actual_paragraph)
    paragraph_count2 = len(expected_paragraph)
    print ("Actual paragraph count is %s (Expected %s)" % (paragraph_count1, paragraph_count2))
    assert paragraph_count1 == paragraph_count2

    for paragraph in actual_paragraph:
      self._wait_for_element(paragraph)
      assert paragraph.is_displayed() is True
      paragraph_text = paragraph.text.strip()
      print ('Verifying paragraph "%s":' % paragraph_text,)
      assert paragraph_text in expected_paragraph
      print ('PRESENT')
      expected_link = expected_paragraph[paragraph_text]
      if expected_link is not None and expected_link is not '':
        self._validate_paragraph_contains_link(paragraph, expected_link)

  def validate_submit_your_manuscript_section(self, expected_paragraph):
    self._validate_section_title()
    self._validate_section_paragraph(expected_paragraph)
