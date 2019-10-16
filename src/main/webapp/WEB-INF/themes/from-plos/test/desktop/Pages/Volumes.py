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

from selenium.webdriver.common.by import By

from ...Base.PlosPage import PlosPage


class Volumes(PlosPage):
    def __init__(self, driver, journal_key, url_suffix):
        super(Volumes, self).__init__(driver, url_suffix)
        self._journal_key = journal_key
        self._last_year = 0
        self._title = (By.CSS_SELECTOR, '.journal_issues h3')
        self._slides = (By.CSS_SELECTOR, '.journal_issues #journal_slides .slide')
        self._items = (By.CSS_SELECTOR, 'ul li')
        self._years_link = (By.CSS_SELECTOR, '.journal_issues #journal_years .btn a')

    def assert_title_text(self):
        title = self._get(self._title)
        expected_text = 'All Issues'

        assert title.text == expected_text, 'The title text: {0!r} is not the expected: {1!r}'\
            .format(title.text, expected_text)

    def _assert_year_text(self, year_link):
        if self._last_year:
            current_year = int(year_link.text)
            expected_year = int(self._last_year) + 1
            assert current_year == expected_year, 'The year link:{0!r} is not the expected: {1!r}'\
                .format(current_year, expected_year)
            self._last_year = current_year

    def _assert_is_active_year(self, year_link):
        parent = year_link.find_element_by_xpath('..')
        parent_classes = parent.get_attribute('class').split(' ')
        expected_class = 'selected'

        assert expected_class in parent_classes, 'The year {0!r} is not active as ' \
                                                 'expected'.format(year_link.text)

    def _assert_active_year_slider(self, year_link, current_index):
        slides = self._gets(self._slides)
        slide = slides[current_index]
        slide_classes = slide.get_attribute('class').split(' ')
        expected_class = 'selected'

        assert expected_class in slide_classes, 'The year slide {0!r} is not active as ' \
                                                'expected'.format(year_link.text)

        items = slide.find_elements(*self._items)
        for item in items:
            link = item.find_element_by_css_selector('a')
            month_title = link.find_element_by_css_selector('span').text

            logging.info('Testing link for {0!r}/{1!r}'.format(month_title, year_link.text))
            assert self._is_link_valid(link)

            image = link.find_element_by_css_selector('img')

            logging.info('Testing image for {0!r}/{1!r}'.format(month_title, year_link.text))
            assert self._is_image_valid(image)

    def test_volume_years(self):
        years_link = self._gets(self._years_link)

        for i, year_link in enumerate(years_link):
            if year_link.text:
                self._assert_year_text(year_link)
                year_link.click()
                self._assert_is_active_year(year_link)
                self._assert_active_year_slider(year_link, i)
