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
# set friendly_testhostname to 'prod' to run suite against production
# Two fields need to be changed to support running tests in your loacl development
# environment, first, set friendly_testhostname to localhost, then correct the 
# base_url value if you are using a port or key different than 8081 and wombat.

from test.Base.Config import environment
from test.Base.Config import rhino_url, base_url

friendly_testhostname = environment
if friendly_testhostname == 'prod':
    base_url = base_url #'https://journals.plos.org/'
    rhino_url = rhino_url
elif friendly_testhostname == 'localhost':
    base_url = 'http://localhost:8081/wombat'
    rhino_url = 'http://one-' + friendly_testhostname + '.plosjournals.org/v1/'
elif friendly_testhostname == 'sc01':
    base_url = 'http://one-' + friendly_testhostname + '.plosjournals.org:8046/'
    rhino_url = 'http://rhino-' + friendly_testhostname + '.plosjournals.org:8006/api/v1/'
else:
    base_url = 'http://one-' + friendly_testhostname + '.plosjournals.org/wombat'
    rhino_url = 'http://one-' + friendly_testhostname + '.plosjournals.org/v1/'

journal_key = 'plosone'

journals = [
    {'journalKey': 'plosone', 'journalTitle': 'One', 'journalAdID': 'a3ac9da4',
     'journalZoneID': '345'},
    {'journalKey': 'plosbiology', 'journalTitle': 'Biology', 'journalAdID': 'a035a937',
     'journalZoneID': '333'},
    {'journalKey': 'plosmedicine', 'journalTitle': 'Medicine', 'journalAdID': 'acb99019',
     'journalZoneID': '349'},
    {'journalKey': 'plospathogens', 'journalTitle': 'Pathogens', 'journalAdID': 'a9c6c347',
     'journalZoneID': '343'},
    {'journalKey': 'ploscompbiol', 'journalTitle': 'Computational Biology',
     'journalAdID': 'a7e3e24e', 'journalZoneID': '337'},
    {'journalKey': 'plosgenetics', 'journalTitle': 'Genetics', 'journalAdID': 'af816654',
     'journalZoneID': '339'},
    {'journalKey': 'plosntds', 'journalTitle': 'Neglected Tropical Diseases',
     'journalAdID': 'a0253914', 'journalZoneID': '347'}
]

sixjournals = [
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
     'journalZoneID': '347'}
]

# Header resources
search_term = 'Cell'
search_term_root = 'Cell'
search_terms_root = 'Med|Hospital|Patient|Disease|Patho|Multidrug|Drug|Cancer|Diabetes|' \
                    'Opisthorchiasis|Tumor|Virus|Cell|genes'
searchterms = ['MicroRNA', 'Small interfering RNA', 'Transgenic animals', 'Transgenic plants',
               'Trait loci', 'Flight mechanics (biology)', 'Hand, foot, and mouth disease',
               'Ontology and logic', 'Immunity to infections', 'Microbial drug resistance',
               'Stomates', 'Food habits', 'Cell']

# registration resources
non_existing_user_email = 'jgray1@plos.org'
existing_user_email = 'jgray@plos.org'
new_user_id_local_base = 'sealresq'
new_user_id_domain = '@gmail.com'
new_user_id_index = 250
