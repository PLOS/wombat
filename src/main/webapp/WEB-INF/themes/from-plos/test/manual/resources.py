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
This Resource File sets variables that are used in individual
test cases. It eventually should be replaced with more robust,
less static, variable definitions.
"""

# General resources

from ..Base import Config

base_url = Config.base_url
rhino_url = Config.rhino_url

alm_url = 'https://alm.plos.org/'
alm_key = 'xgCZJ8Fd5so2VD5Yqw94'

journals = [
    {'journalKey': 'One', 'journalTitle': 'One', 'journalAdID': 'a3ac9da4', 'journalZoneID': '345'},
    {'journalKey': 'Biology', 'journalTitle': 'Biology', 'journalAdID': 'a035a937',
     'journalZoneID': '333'},
    {'journalKey': 'Medicine', 'journalTitle': 'Medicine', 'journalAdID': 'acb99019',
     'journalZoneID': '349'},
    {'journalKey': 'Pathogens', 'journalTitle': 'Pathogens', 'journalAdID': 'a9c6c347',
     'journalZoneID': '343'},
    {'journalKey': 'CompBiol', 'journalTitle': 'Computational Biology', 'journalAdID': 'a7e3e24e',
     'journalZoneID': '337'},
    {'journalKey': 'Genetics', 'journalTitle': 'Genetics', 'journalAdID': 'af816654',
     'journalZoneID': '339'},
    {'journalKey': 'Ntds', 'journalTitle': 'Neglected Tropical Diseases', 'journalAdID': 'a0253914',
     'journalZoneID': '347'},
    {'journalKey': 'Collections', 'journalTitle': 'Collections', 'journalAdID': 'ae7a713f',
     'journalZoneID': '331'}
]

journal_sites = {
    'desktop': {
        'PLoSONE': 'DesktopPlosOne',
        'PLoSMedicine': 'DesktopPlosMedicine',
        'PLoSGenetics': 'DesktopPlosGenetics',
        'PLoSCompBiol': 'DesktopPlosCompBiol',
        'PLoSCollections': 'DesktopPlosCollections',
        'PLoSNTD': 'DesktopPlosNtds',
        'PLoSBiology': 'DesktopPlosBiology',
        'PLoSClinicalTrials': 'DesktopPlosClinicalTrials',
        'PLoSPathogens': 'DesktopPlosPathogens'
    },
    'mobile': {
        'PLoSONE': 'MobilePlosOne',
        'PLoSMedicine': 'MobilePlosMedicine',
        'PLoSGenetics': 'MobilePlosGenetics',
        'PLoSCompBiol': 'MobilePlosCompBiol',
        'PLoSCollections': 'MobilePlosCollections',
        'PLoSNTD': 'MobilePlosNtds',
        'PLoSBiology': 'MobilePlosBiology',
        'PLoSClinicalTrials': 'MobilePlosClinicalTrials',
        'PLoSPathogens': 'MobilePlosPathogens'
    }
}
