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

# utilities for manual testing code

import sys
import os
import re

from ..Base import PROD_URL_TABLE


def call_while_suppressing_stdout(func, args):
    # execute given function while suppressing output
    # accepts a function and a tuple of values as arguments
    dev_null = open(os.devnull, 'w')
    std_out = sys.stdout
    sys.stdout = dev_null
    return_obj = func(*args)
    sys.stdout = std_out
    dev_null.close()
    return return_obj


def translate_to_prod_url(dev_path):
    """Translate a dev-box path to a production URL.

    Example:
      '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001199'
    to
      'https://journals.plos.org/plosbiology/article?id=10.1371/journal.pbio.1001199'

    Returns None if the path does not start with a token that matches a
    site key.
    """
    match = re.match(r'/(.*?)/(.*)', dev_path)
    if match:
        site_key, url_suffix = match.groups()
        if site_key in PROD_URL_TABLE:
            base_url = PROD_URL_TABLE[site_key]
            return base_url + url_suffix
    return None
