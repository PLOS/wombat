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

import re, cgi

"""
Utilities for common functionality

"""

__author__ = 'jfesenko@plos.org'

def to_int(element):
  return int(element.text.strip().replace(',', ''))

def get_first_dict_by_value(dict_list, key, value):
  """
  Get the first dict from a list of dicts that matches a given condition

  Args:
    dict_list: a list of dicts to choose from
    key: the dict key to match against
    value: the value to match

  Returns:
    The first dict that contains a key matching the given value, or None if no dict matches

  """
  return next((item for item in dict_list if item[key] == value), None)


def filter_dicts_by_value(dict_list, key, value):
  """
  Filter a list of dicts using the given condition
  Args:
    dict_list: a list of dicts to filter
    key: the dict key to match against
    value: the value to match

  Returns:
    A filtered list of dicts that contains a key matching the given value, or an empty list if no dict matches

  """
  return [item for item in dict_list if item[key] == value]

def escape_doi(doi):
  return doi.replace('+', '+-').replace('/', '++')

def strip_html(text):
  tag_re = re.compile(r'(<!--.*?-->|<[^>]*>)')
  no_tags = tag_re.sub('', text)
  ready_for_web = cgi.escape(no_tags)
  return ready_for_web
