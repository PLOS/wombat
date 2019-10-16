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
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USexpected_ids = [unicode(ad_slot.advert_div_id) for ad_slot in self._ad_slots]E OR OTHER
# DEALINGS IN THE SOFTWARE.

import re

from selenium.webdriver.common.by import By

from ...Base.PlosPage import PlosPage

# This class makes use of ad unit names, which are statically defined in DoubleClick
# Example: PONE_728x90_ATF_0
# The list of ad unit names that we use can be found in Confluence:
# https://developer.plos.org/confluence/display/WEBDLVRY/DoubleClick+for+Publishers
# +%28DFP%29+implementation+notes


_ad_unit_journal_prefixes = {
    'PLoSONE': 'PONE',
    'PLoSMedicine': 'PMED',
    'PLoSGenetics': 'PGEN',
    'PLoSCompBiol': 'PCOMPBIO',
    'PLoSCollections': 'PCOL',
    'PLoSClinicalTrials': 'PCOL',
    'PLoSNTD': 'PNTD',
    'PLoSBiology': 'PBIO',
    'PLoSPathogens': 'PPATH'
}


class AdSpread(PlosPage):
    def __init__(self, driver, journal_key, url_suffix, ad_slots):
        super(AdSpread, self).__init__(driver, url_suffix)
        self._ad_slots = ad_slots
        self._journal_key = journal_key

    def assert_advert_div_container_dimensions(self):
        for ad_slot in self._ad_slots:
            ad_div_container = self._get_advert_div_container(ad_slot)
            assert ad_div_container.value_of_css_property('width') == ad_slot.width + 'px'
            assert ad_div_container.value_of_css_property('height') == ad_slot.height + 'px'
        return self

    def assert_advert_div_container_presence(self):
        for ad_slot in self._ad_slots:
            self._get_advert_div_container(ad_slot)
        return self

    def _get_advert_div_container(self, ad_slot):
        return self._get((By.ID, ad_slot.advert_div_id))

    def assert_all_ads_defined_in_javascript(self):

        expected_ids = [ad_slot.advert_div_id for ad_slot in self._ad_slots]

        expected_names = [ad_slot.ad_slot_name for ad_slot in self._ad_slots]

        script = self._check_for_invisible_element((By.ID, 'doubleClickSetupScript'))
        ids_in_head = re.findall(r'div-gpt-ad-1458247671871-\d+', script.get_attribute("innerHTML"))

        regex_name = r'\d{2,3}x\d{2,3}_[ABFIT]{3}\d?'
        names_in_head = re.findall(regex_name, script.get_attribute(
                "innerHTML"))

        ads_in_body = self._gets((By.CLASS_NAME, 'advertisement'))
        ids_in_body = [ad.get_attribute('id') for ad in ads_in_body]

        print("EXPECTED: " + str(sorted(expected_ids)) + "\n\nHEAD: " + str(
                sorted(ids_in_head)) + "\n\nBODY: " + str(sorted(ids_in_body)))
        assert set(expected_ids).issubset(set(set(ids_in_head)))
        assert set(expected_names).issubset(set(set(names_in_head)))
        assert sorted(expected_ids) == sorted(ids_in_body)
        return self
