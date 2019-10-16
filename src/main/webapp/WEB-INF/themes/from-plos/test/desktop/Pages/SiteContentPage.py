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


from selenium.webdriver.common.by import By

from .WombatPage import WombatPage
from ...Base.styles import TABS_BASE_DARK, StyledPage as styles

__author__ = 'jgray@plos.org'


class SiteContentPage(WombatPage):
    """
    Model an abstract base Site Content page.
    """

    def __init__(self, driver, url_suffix=''):
        super(SiteContentPage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._body = (By.CLASS_NAME, 'static')
        self._cms_content_div = (By.CLASS_NAME, 'lemur-content')
        self._cms_layout_type = (By.CLASS_NAME, 'two-column')
        self._cms_layout_type_fw = (By.CLASS_NAME, 'full-width')
        self._left_hand_nav = (By.CLASS_NAME, 'site-content-nav')
        self._nav_list = (By.CLASS_NAME, 'nav-page')
        self._page_main_heading = (By.TAG_NAME, 'h1')

    # POM Actions

    def validate_cms_body(self):
        self.set_timeout(1)
        self._get(self._body)
        self.restore_timeout()
        return self

    def validate_cms_main_div(self):
        self.set_timeout(1)
        self._get(self._cms_content_div)
        self.restore_timeout()
        return self

    def validate_cms_two_col_layout(self):
        self.set_timeout(1)
        self._get(self._cms_layout_type)
        self.restore_timeout()
        return self

    # Only present in two column layout
    def validate_lh_nav(self):
        self.set_timeout(1)
        self._get(self._left_hand_nav)
        self.restore_timeout()
        return self

    # Only present in two column layout
    def validate_nav_list(self):
        self.set_timeout(5)
        nav = self._get(self._nav_list)
        # We should never have an empty nav list
        assert len(nav.text) > 0, 'Empty Nav List found for article'
        self.restore_timeout()
        return self

    def validate_cms_full_width_layout(self):
        self.set_timeout(1)
        self._get(self._cms_layout_type_fw)
        self.restore_timeout()
        return self

    def validate_main_page_heading(self):
        self.set_timeout(1)
        main_head = self._get(self._page_main_heading)
        self.restore_timeout()
        styles.validate_application_font_family(main_head)
        styles.validate_text_weight(main_head, '400')
        styles.validate_element_color(main_head, TABS_BASE_DARK, 'hex')
        return self
