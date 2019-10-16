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

__author__ = 'jkrzemien@plos.org'

import logging
from datetime import datetime
from inspect import getfile
from os.path import abspath, dirname
from time import time

from selenium.common.exceptions import (
    NoSuchElementException,
    WebDriverException
    )
from selenium.webdriver.support.events import AbstractEventListener

from .CustomException import (
    ElementDoesNotExistAssertionError,
    ErrorAlertThrownException,
    ErrorTimeoutException
    )

LOG_HEADER = '\t[WebDriver %s] '


class WebDriverListener(AbstractEventListener):
    """
    WebDriver's listener for printing out information to STDOUT whenever the
    driver is about to/done
    processing an event.

    These events are triggered from the EventFiringWebDriver instance before
    and after each
    available action on the driver's instance.
    """

    # Just a mapping between HTML tag names and their 'human readable' form,
    #  for the logger
    _pretty_names = {
        'a': 'link',
        'img': 'image',
        'button': 'button',
        'input': 'text box',
        'textarea': 'text area',
        'submit': 'button',
        'cancel': 'button',
        'select': 'drop down box',
        'option': 'drop down option',
        'radio': 'radio button',
        'li': 'list item'
        }

    # Constructor. Initializes parent class (`AbstractEventListener`)
    def __init__(self):
        super(WebDriverListener, self).__init__()
        self._driver = None

    def after_click(self, element, driver):
        self._log('Click on "%s" %s successful' % self.lastElement)

    def after_find(self, by, value, driver):
        self._log('Element "%s" identified successfully' % value)

    def before_click(self, element, driver):
        friendly_name = self._friendly_tag_name(element)
        self.lastElement = (self._tidy_text(element.text), friendly_name)
        self._log('Clicking on "%s" %s...' % self.lastElement)

    def before_find(self, by, value, driver):
        if self._driver is None:
            self._driver = driver
        message = 'Identifing element using {0} as locator ({1} ' \
                  'strategy)...'.format(value, str(by))
        self._log(message)

    def before_navigate_back(self, driver):
        self._log('Navigating back to previous page...')

    def before_navigate_to(self, url, driver):
        if self._driver is None:
            self._driver = driver
        print('=' * 80)
        self._log('Navigating to %s...' % url)

    def on_exception(self, exception, driver):
        if type(exception) in [NoSuchElementException,
                               ElementDoesNotExistAssertionError,
                               WebDriverException,
                               ErrorAlertThrownException,
                               ErrorTimeoutException,
                               ]:
            self._log(
                'The locator provided did not match any element in the page.'
                ' {0}'.format(exception.msg))
        if '\'WebDriver\' object has no attribute' not in str(exception):
            driver.save_screenshot(self._generate_png_filename(exception))

    def _generate_png_filename(self, exception):
        """
        Helper *internal* method to generate a file name for the captured
        screenshots
        """
        ts = time()
        timestamp = datetime.fromtimestamp(ts).strftime('%Y%m%d-%H%M%S')
        path = dirname(abspath(getfile(WebDriverListener)))
        logging.warning('Saving screenshot: ')
        logging.warning(
            exception.__class__.__name__ + '-' + timestamp + '.png')
        return '{0}/../Output/{1}-{2}.png'\
            .format(path, exception.__class__.__name__, timestamp)

    def _friendly_tag_name(self, element):
        """
        Helper *internal* method to "translate" some keywords to human
        readable ones before
        printing out messages
        """
        try:
            name = WebDriverListener._pretty_names[element.tag_name]
        except KeyError:
            try:
                name = WebDriverListener._pretty_names[
                    element.get_attribute("type")]
            except KeyError:
                name = ""
        return name

    def _log(self, msg):
        """
        Helper *internal* method to print out messages from this listener
        """
        d = dict(self._driver.capabilities)
        print('')
        print(LOG_HEADER % d['browserName'], )
        print(msg)

    def _tidy_text(self, text):
        """
        Helper *internal* method to remove some annoying characters before
        printing out messages
        """
        if text is not None:
            text = text.strip()
            text = text.replace('\t', ' ')
            text = text.replace('\n', ' ')
            text = text.replace('\v', ' ')
            while text.count('  ') > 0:
                text = text.replace('  ', ' ')
            return text
        return ''

    # Not implemented
    def after_change_value_of(self, element, driver):
        pass

    # Not implemented
    def after_close(self, driver):
        pass

    # Not implemented
    def after_execute_script(self, script, driver):
        pass

    # Not implemented
    def after_navigate_back(self, driver):
        pass

    # Not implemented
    def after_navigate_forward(self, driver):
        pass

    # Not implemented
    def after_navigate_to(self, url, driver):
        pass

    # Not implemented
    def after_quit(self, driver):
        pass

    # Not implemented
    def before_change_value_of(self, element, driver):
        pass

    # Not implemented
    def before_close(self, driver):
        pass

    # Not implemented
    def before_execute_script(self, script, driver):
        pass

    # Not implemented
    def before_navigate_forward(self, driver):
        pass

    # Not implemented
    def before_quit(self, driver):
        pass
