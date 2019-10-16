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

"""
This test case validates the PLOS desktop site's Volumes pages.

"""

import pytest

from ..Base.Journal import Journal
from .Pages.Volumes import Volumes

__author__ = 'ivieira@plos.org'

journals_with_volumes = {'PLoSBiology',
                         'PLoSCompBiol',
                         'PLoSMedicine',
                         'PLoSNTD',
                         'PLoSGenetics',
                         'PLoSPathogens',
                         }


def make_volume_cases():
    for journal_key in journals_with_volumes:
        page_path = Journal.build_volumes_path(journal_key)
        yield journal_key, page_path


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("journal_key, page_path", make_volume_cases())
class TestVolumes:
    def test_volumes(self, journal_key, page_path):
        plos_page = Volumes(self.driver, journal_key, page_path)
        plos_page.assert_title_text()
        plos_page.test_volume_years()
