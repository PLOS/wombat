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
This test case validates advertisements in all Wombat pages
"""
import re

import pytest

from test.Base.Journal import Journal
from test.Base.JournalArticleSet import JournalArticleSet
from test.desktop.Pages.AdSpread import AdSpread


class AdSlot(object):
    def _get_dimensions_from_slot_name(self):
        dimension_tuple = str(self.ad_slot_name).split('_')[0]
        return re.split('[Xx]', dimension_tuple)

    def __init__(self, slot_name, index):
        self.ad_slot_name = slot_name
        self.index = index
        self.width, self.height = AdSlot._get_dimensions_from_slot_name(self)
        self.advert_div_id = ('div-gpt-ad-1458247671871-' + self.index)


standard_homepage_ad_slots = [AdSlot('728x90_ATF', '0'),
                              AdSlot('300x250_ITB1', '2'),
                              AdSlot('300x250_ITB2', '3'),
                              AdSlot('300x250_ITB3', '4')]

standard_article_ad_slots = [AdSlot('728x90_ATF', '0'),
                             AdSlot('160x600_BTF', '1')]


salp_ad_slots = [AdSlot('728x90_ATF', '0'),
                 AdSlot('300x250_ATF', '2')]

journals_with_ads = {'PLoSBiology', 'PLoSCompBiol', 'PLoSMedicine', 'PLoSNTD', 'PLoSGenetics',
                     'PLoSPathogens', 'PLoSONE'}

class AdContext(object):
    def __init__(self, slots, page_function):
        self.slots = slots
        self.get_path_for = page_function


homepage_context = AdContext(standard_homepage_ad_slots, Journal.build_homepage_path)
article_context = AdContext(standard_article_ad_slots, JournalArticleSet.build_article_path)

ad_contexts = [homepage_context, article_context]


# an ad case consists of a journal key, page, and a set of ad slots
def make_ad_cases():
    for journal_key in journals_with_ads:
        for ad_context in ad_contexts:
            path = ad_context.get_path_for(journal_key)
            yield journal_key, path, ad_context.slots
    yield 'PLoSONE', Journal.build_homepage_path('PLoSONE') + 'browse/', salp_ad_slots


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("ad_case", make_ad_cases())
class TestAd:
    def test_ad(self, ad_case):
        journal_key, page_path, ad_slots = ad_case
        driver = self.driver
        ad_spread = AdSpread(driver, journal_key, page_path, ad_slots)
        ad_spread.assert_advert_div_container_presence()
        ad_spread.assert_advert_div_container_dimensions()
        ad_spread.assert_all_ads_defined_in_javascript()
