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

import random

from test.Base.Journal import Journal


class JournalArticleSet(object):
    def __init__(self, journal_key, dois):
        self.journal_key = journal_key
        self.dois = dois

    def _get_paths(self, device_dict):
        for doi in self.dois:
            yield '/{site_key}/article?id={doi}'.format(
                site_key=device_dict[self.journal_key], doi=doi)

    def get_desktop_paths(self):
        return self._get_paths(Journal.desktop_journals)

    def get_mobile_paths(self):
        return self._get_paths(Journal.mobile_journals)

    @staticmethod
    def build_article_paths(journal_key):
        article_set = _journal_article_sets[journal_key]
        paths = article_set.get_desktop_paths()
        return list(paths)

    @staticmethod
    def build_article_path(journal_key):
        article_set = _journal_article_sets[journal_key]
        paths = article_set.get_desktop_paths()
        return random.choice(list(paths))


_journal_doi_table = {
    'PLoSONE': [
        "10.1371/journal.pone.0002554",
        "10.1371/journal.pone.0005723",
        "10.1371/journal.pone.0008519",
        "10.1371/journal.pone.0008915",
        "10.1371/journal.pone.0010685",
        "10.1371/journal.pone.0016329",
        "10.1371/journal.pone.0016976",
        "10.1371/journal.pone.0026358",
        "10.1371/journal.pone.0027062",
        "10.1371/journal.pone.0028031",
        "10.1371/journal.pone.0036880",
        "10.1371/journal.pone.0039314",
        "10.1371/journal.pone.0040259",
        "10.1371/journal.pone.0040740",
        "10.1371/journal.pone.0042593",
        "10.1371/journal.pone.0046041",
        "10.1371/journal.pone.0047391",
        "10.1371/journal.pone.0050698",
        "10.1371/journal.pone.0052690",
        "10.1371/journal.pone.0055490",
        "10.1371/journal.pone.0057943",
        "10.1371/journal.pone.0058242",
        "10.1371/journal.pone.0066742",
        "10.1371/journal.pone.0067179",
        "10.1371/journal.pone.0067227",
        "10.1371/journal.pone.0067380",
        "10.1371/journal.pone.0068090",
        "10.1371/journal.pone.0069640",
        "10.1371/journal.pone.0081648",
        "10.1371/journal.pone.0087236",
        "10.1371/journal.pone.0093414",
        "10.1371/journal.pone.0095513",
        "10.1371/journal.pone.0100977",
        "10.1371/journal.pone.0105948",
        "10.1371/journal.pone.0108198"
    ],
    'PLoSBiology': [
        "10.1371/journal.pbio.0030408",
        "10.1371/journal.pbio.0040088",
        "10.1371/journal.pbio.1001199",
        "10.1371/journal.pbio.1001291",
        "10.1371/journal.pbio.1001315",
        "10.1371/journal.pbio.1001569"
    ],
    'PLoSMedicine': [
        '10.1371/journal.pmed.0020007',
        '10.1371/journal.pmed.0020124',
        '10.1371/journal.pmed.0020171',
        '10.1371/journal.pmed.0020402',
        '10.1371/journal.pmed.0030132',
        '10.1371/journal.pmed.0030205',
        '10.1371/journal.pmed.0030303',
        '10.1371/journal.pmed.0030445',
        '10.1371/journal.pmed.0030520',
        '10.1371/journal.pmed.0040303',
        '10.1371/journal.pmed.1000097',
        '10.1371/journal.pmed.1000431',
        '10.1371/journal.pmed.1001080',
        '10.1371/journal.pmed.1001188',
        '10.1371/journal.pmed.1001202',
        '10.1371/journal.pmed.1001200',
        '10.1371/journal.pmed.1001210',
        '10.1371/journal.pmed.1001300',
        '10.1371/journal.pmed.1001418',
        '10.1371/journal.pmed.1001473',
        '10.1371/journal.pmed.1001518',
        '10.1371/journal.pmed.1001644',
        '10.1371/journal.pmed.1001682',
        '10.1371/journal.pmed.1001743',
        '10.1371/journal.pmed.1000431'
    ],
    'PLoSGenetics': [
        '10.1371/journal.pgen.1000052',
        '10.1371/journal.pgen.1002644',
        '10.1371/journal.pgen.1002912',
        '10.1371/journal.pgen.1003316',
        '10.1371/journal.pgen.1003500',
        '10.1371/journal.pgen.1004451',
        '10.1371/journal.pgen.1004643'
    ],
    'PLoSCompBiol': [
        '10.1371/journal.pcbi.0020120',
        '10.1371/journal.pcbi.0030134',
        '10.1371/journal.pcbi.0030158',
        '10.1371/journal.pcbi.1000112',
        '10.1371/journal.pcbi.1000589',
        '10.1371/journal.pcbi.1000974',
        '10.1371/journal.pcbi.1001051',
        '10.1371/journal.pcbi.1001083',
        '10.1371/journal.pcbi.1002484',
        '10.1371/journal.pcbi.1003447',
        '10.1371/journal.pcbi.1003842',
        '10.1371/journal.pcbi.1003849'
    ],
    'PLoSPathogens': [
        '10.1371/journal.ppat.0040045',
        '10.1371/journal.ppat.1000105',
        '10.1371/journal.ppat.1000166',
        '10.1371/journal.ppat.1001009',
        '10.1371/journal.ppat.1002247',
        '10.1371/journal.ppat.1002735',
        '10.1371/journal.ppat.1002769',
        '10.1371/journal.ppat.1003133',
        '10.1371/journal.ppat.1003762',
        '10.1371/journal.ppat.1004200',
        '10.1371/journal.ppat.1004377',
        '10.1371/journal.ppat.1004389',
        '10.1371/journal.ppat.1004411'
    ],
    'PLoSNTD': [
        '10.1371/journal.pntd.0000149',
        '10.1371/journal.pntd.0001041',
        '10.1371/journal.pntd.0001446',
        '10.1371/journal.pntd.0001969',
        '10.1371/journal.pntd.0002570',
        '10.1371/journal.pntd.0002958',
        '10.1371/journal.pntd.0003188',
        '10.1371/journal.pntd.0003205'
    ],
    'PLoSCollections': [

    ],
    'PLoSClinicalTrials': [
        '10.1371/journal.pctr.0020028'
    ]
}

_journal_article_sets = dict((journal_key, JournalArticleSet(journal_key, dois))
                             for (journal_key, dois) in _journal_doi_table.items())
