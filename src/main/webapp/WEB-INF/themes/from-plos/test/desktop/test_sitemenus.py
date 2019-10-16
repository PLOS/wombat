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
This test case validates the PLOS desktop site's menu link contents
It also validates the presence of the iSCB block on the CompBiol HP.
"""

import pytest

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosCollectionsHomePage import PlosCollectionsHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage

__author__ = 'jgray@plos.org'


@pytest.mark.usefixtures("driver_get")
class TestSiteMenus:
    """
    For each of the "six" journals, plus plosOONE and Collections, validate the content
    of the header menus.
    For Computational Biology, validate also the existence of the iSCB block.
    """

    def test_plos_biology_site_menus(self):
        plos_biology = PlosBiologyHomePage(self.driver)
        plos_biology.hover_browse()
        plos_biology.validate_browse_links()
        plos_biology.hover_publish()
        plos_biology.validate_publish_submissions_links()
        plos_biology.validate_publish_policies_links()
        plos_biology.validate_publish_manuscript_review_links()
        plos_biology.validate_submit_your_manuscript_section()
        plos_biology.hover_about()
        plos_biology.validate_about_links()

    def test_plos_collections_site_menus(self):
        plos_collections = PlosCollectionsHomePage(self.driver)
        plos_collections.hover_browse()
        plos_collections.validate_browse_links()
        plos_collections.validate_about_links()

    def test_plos_computational_site_menus(self):
        plos_computational = PlosCompBiolHomePage(self.driver)
        plos_computational.hover_browse()
        plos_computational.validate_browse_links()
        plos_computational.hover_publish()
        plos_computational.validate_publish_submissions_links()
        plos_computational.validate_publish_policies_links()
        plos_computational.validate_publish_manuscript_review_links()
        plos_computational.hover_about()
        plos_computational.validate_about_links()
        plos_computational.validate_iscb_div()

    def test_plos_genetics_site_menus(self):
        plos_genetics = PlosGeneticsHomePage(self.driver)
        plos_genetics.hover_browse()
        plos_genetics.validate_browse_links()
        plos_genetics.hover_publish()
        plos_genetics.validate_publish_submissions_links()
        plos_genetics.validate_publish_policies_links()
        plos_genetics.validate_publish_manuscript_review_links()
        plos_genetics.hover_about()
        plos_genetics.validate_about_links()

    def test_plos_medicine_site_menus(self):
        plos_medicine = PlosMedicineHomePage(self.driver)
        plos_medicine.hover_browse()
        plos_medicine.validate_browse_links()
        plos_medicine.hover_publish()
        plos_medicine.validate_publish_submissions_links()
        plos_medicine.validate_publish_policies_links()
        plos_medicine.validate_publish_manuscript_review_links()
        plos_medicine.validate_submit_your_manuscript_section()
        plos_medicine.hover_about()
        plos_medicine.validate_about_links()

    def test_plos_neglected_site_menus(self):
        plos_neglected = PlosNeglectedHomePage(self.driver)
        plos_neglected.hover_browse()
        plos_neglected.validate_browse_links()
        plos_neglected.hover_publish()
        plos_neglected.validate_publish_submissions_links()
        plos_neglected.validate_publish_policies_links()
        plos_neglected.validate_publish_manuscript_review_links()
        plos_neglected.validate_submit_your_manuscript_section()
        plos_neglected.hover_about()
        plos_neglected.validate_about_links()

    def test_plos_one_site_menus(self):
        plos_one = PlosOneHomePage(self.driver)
        plos_one.validate_subject_area()
        plos_one.hover_publish()
        plos_one.validate_publish_submissions_links()
        plos_one.validate_publish_policies_links()
        plos_one.validate_publish_manuscript_review_links()
        plos_one.validate_submit_your_manuscript_section()
        plos_one.hover_about()
        plos_one.validate_about_links()

    def test_plos_pathogens_site_menus(self):
        plos_pathogens = PlosPathogensHomePage(self.driver)
        plos_pathogens.hover_browse()
        plos_pathogens.validate_browse_links()
        plos_pathogens.hover_publish()
        plos_pathogens.validate_publish_submissions_links()
        plos_pathogens.validate_publish_policies_links()
        plos_pathogens.validate_publish_manuscript_review_links()
        plos_pathogens.validate_submit_your_manuscript_section()
        plos_pathogens.hover_about()
        plos_pathogens.validate_about_links()
