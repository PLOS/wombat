#! usr/bin/env python2

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

from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from .WombatPage import WombatPage
from math import ceil

__author__ = 'ivieira@plos.org'

class PaginationAjax(WombatPage):
  def __init__(self, driver, url_suffix=''):
    super(PaginationAjax, self).__init__(driver, url_suffix)
    self.has_pagination = False
    self._pagination_container = False

    try:
      self._load_pagination_container()
      self.has_pagination = True
    except:
      print('Page without pagination, skipping test')

  def _load_pagination_container(self):
    if self._pagination_container:
      self.set_timeout(10)
      self._wait.until(EC.staleness_of(self._pagination_container))
      self._wait.until(EC.presence_of_element_located((By.CLASS_NAME, 'nav-pagination')))
      self.restore_timeout()

    self._pagination_container = self._driver.find_element_by_id('article-pagination')

  def get_total_records(self):
    total_records = self._pagination_container.get_attribute('data-pagination-total-records')
    return int(total_records)

  def get_current_page(self):
    current_page = self._pagination_container.get_attribute('data-pagination-current-page')
    return int(current_page)

  def get_items_per_page(self):
    items_per_page = self._pagination_container.get_attribute('data-pagination-items-per-page')
    return int(items_per_page)

  def get_total_pages(self):
    total_records = float(self.get_total_records())
    items_per_page = float(self.get_items_per_page())
    total_pages = ceil(total_records / items_per_page)
    return int(total_pages)

  def click_on_page(self, page_number):
    button = self._pagination_container.find_element_by_css_selector('a[data-page="%s"]' % (str(page_number)))
    button.click()
    self._load_pagination_container()

    return self

  def click_first_page(self):
    self.click_on_page(1)
    return self

  def click_last_page(self):
    total_pages = self.get_total_pages()
    self.click_on_page(total_pages)
    return self

  def click_next_page(self):
    button = self._pagination_container.find_element_by_id('nextPageLink')
    button.click()
    self._load_pagination_container()

  def click_prev_page(self):
    button = self._pagination_container.find_element_by_id('prevPageLink')
    button.click()
    self._load_pagination_container()

  def validate_pagination_current_page(self):
    current_page_button = self._pagination_container.find_element_by_css_selector('a.number.active')

    assert current_page_button, 'No current page button found'

    expected_current_page = str(self.get_current_page())
    current_page_button_data_value = current_page_button.get_attribute('data-page')
    current_page_button_text = current_page_button.text

    assert current_page_button_data_value == expected_current_page, 'The current page button data value: %s is not the expected: %s' % (
    current_page_button_data_value, expected_current_page)
    assert current_page_button_text == expected_current_page, 'The current page button text: %s is not the expected: %s' % (
    current_page_button_text, expected_current_page)

    return self

  def validate_pagination_prev_button(self):
    prev_button = self._pagination_container.find_element_by_id('prevPageLink')
    button_classes = prev_button.get_attribute('class').split(' ')
    button_tag_name = prev_button.tag_name
    current_page = self.get_current_page()

    if current_page == 1:
      assert 'disabled' in button_classes, 'The previous page button is not disabled'
      assert button_tag_name == 'span', 'The previous page button is not a span'

    else:
      assert 'disabled' not in button_classes, 'The previous page button is disabled'
      assert button_tag_name == 'a', 'The previous page button is not a link'

    return self

  def validate_pagination_next_button(self):
    prev_button = self._pagination_container.find_element_by_id('nextPageLink')
    button_classes = prev_button.get_attribute('class').split(' ')
    button_tag_name = prev_button.tag_name
    current_page = self.get_current_page()
    total_pages = self.get_total_pages()

    if current_page == total_pages:
      assert 'disabled' in button_classes, 'The next page button is not disabled'
      assert button_tag_name == 'span', 'The next page button is not a span'

    else:
      assert 'disabled' not in button_classes, 'The next page button is disabled'
      assert button_tag_name == 'a', 'The next page button is not a link'

    return self

  def validate_pagination_total_pages(self):
    numbers_buttons = self._pagination_container.find_elements_by_css_selector('a.number')
    last_number_button = numbers_buttons[len(numbers_buttons) - 1]
    last_number_button_text = last_number_button.text
    last_number_button_data = last_number_button.get_attribute('data-page')
    total_pages = str(self.get_total_pages())

    assert last_number_button_text == total_pages, 'The last page button text: %s is not the expected: %s' % (
    last_number_button_text, total_pages)
    assert last_number_button_data == total_pages, 'The last page button data attribute: %s is not the expected: %s' % (
    last_number_button_data, total_pages)

    return self

  def _assert_pagination_element(self, value, expected_value):
    assert value == expected_value, 'The pagination item: %s is not the expected %s' % (value, expected_value)

  def validate_pagination_pages(self):
    pagination_elements = self._pagination_container.find_elements_by_xpath("./*")

    pages = []

    # Map the elements to create an array with the pages order
    for el in pagination_elements:
      el_class = el.get_attribute('class').split(' ')
      if 'number' in el_class:
        pages.append(int(el.get_attribute('data-page')))
      elif 'skip' in el_class:
        pages.append('divider')

    current_index = 0
    last_page_validated = 0
    current_page = self.get_current_page()
    total_pages = self.get_total_pages()

    # Validating start section
    if current_page < 5:
      print ('Validating full start section')
      i = 1
      limit = 5
      if total_pages < 5:
        limit = total_pages

      while i <= limit:
        self._assert_pagination_element(pages[current_index], i)
        last_page_validated = i
        current_index += 1
        i += 1

    else:
      print ('Validating compressed start section')
      self._assert_pagination_element(pages[current_index], 1)
      current_index += 1
      last_page_validated = 1

    # Validating middle section
    if current_page >= 5 and current_page <= (total_pages-4):
      print ('Validating middle section')
      print ('Validating middle section divider')
      self._assert_pagination_element(pages[current_index], 'divider')
      current_index += 1

      i = current_page - 1
      limit = current_page + 1

      while i <= limit:
        self._assert_pagination_element(pages[current_index], i)
        last_page_validated = i
        current_index += 1
        i += 1

    # Validating final section
    if total_pages > 5 and current_page > (total_pages - 4):
      print ('Validating full final section')
      if pages[current_index] == 'divider':
        print ('Validating final section divider')
        self._assert_pagination_element(pages[current_index], 'divider')
        current_index += 1
      if total_pages - 4 <= last_page_validated:
        i = last_page_validated + 1
      else:
        i = total_pages - 4

      limit = total_pages

      while i <= limit:
        print ('Validating compressed final section')
        self._assert_pagination_element(pages[current_index], i)
        last_page_validated = i
        current_index += 1
        i += 1

    return self

  def run_all_validations(self):
    if self.has_pagination:
      self.validate_pagination_prev_button()
      self.validate_pagination_current_page()
      self.validate_pagination_next_button()
      self.validate_pagination_total_pages()
      self.validate_pagination_pages()

  def validate_pagination(self):
    if self.has_pagination:
      # Validate with the first page
      self.run_all_validations()

      # Validate with the last page
      self.click_last_page()
      self.run_all_validations()

      # Validate with middle section
      total_pages = self.get_total_pages()
      if total_pages-4 > 0:
        self.click_on_page(total_pages-4)
        self.run_all_validations()

    return self
