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

from selenium.common.exceptions import StaleElementReferenceException

__author__ = 'jkrzemien@plos.org'


# === Element To Be Clickable expectation definition ===
class ElementToBeClickable(object):
    """

    An expectation for checking that an element is **present on the DOM** of a
    page and **visible**.

    Arguments:

    1. element - an instance of a Web Element (**not** a locator)

    Returns:

    1. the same WebElement once it has been located and is visible

    *Visibility* means that the element is not only displayed
    but also that its *height* and *width* are *greater* than 0.

    """

    def __init__(self, element):
        self.element = element

    def __call__(self, driver):
        try:
            return self.element.is_displayed() and self.element.is_enabled()
        except StaleElementReferenceException:
            return False
