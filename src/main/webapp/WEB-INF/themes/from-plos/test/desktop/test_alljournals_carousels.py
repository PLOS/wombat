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
This test validates the carousel functionality for all PLOS Journals. 

For each journal we need to validate the carousel heading colors, rotation 
through articles, and article linking 
"""
import logging
import pytest
import random

from .Pages.PlosBiologyHomePage import PlosBiologyHomePage
from .Pages.PlosCompBiolHomePage import PlosCompBiolHomePage
from .Pages.PlosGeneticsHomePage import PlosGeneticsHomePage
from .Pages.PlosMedicineHomePage import PlosMedicineHomePage
from .Pages.PlosNeglectedHomePage import PlosNeglectedHomePage
from .Pages.PlosOneHomePage import PlosOneHomePage
from .Pages.PlosPathogensHomePage import PlosPathogensHomePage

num_carousels = 2


@pytest.mark.usefixtures("driver_get")
class TestAlljournalsCarousels:
    @pytest.mark.homepage
    @pytest.mark.parametrize("journal_home_page", [
        PlosBiologyHomePage,
        PlosCompBiolHomePage,
        PlosGeneticsHomePage,
        PlosMedicineHomePage,
        PlosNeglectedHomePage,
        PlosOneHomePage,
        PlosPathogensHomePage,
    ])
    def test_alljournals_carousels(self, journal_home_page):
        wombat_page = journal_home_page(self.driver)
        journal, journal_name = wombat_page.get_journal_info()
        logging.info('Validating Carousels for {0}'.format(journal_name))

        carousels = wombat_page._gets(wombat_page._carousels)
        assert len(carousels) >= num_carousels, \
            'The number of Carousels = {0!s}, expected: {1!s}' \
            .format(len(carousels), num_carousels)

        for carousel_index, carousel in enumerate(carousels):
            # carousel wrapper present
            block_header = carousel.find_element(*wombat_page._block_header)
            # header dif text white against non-white background
            # AMBR-307: for PlosOne carousel title should not be white
            # for other journals it is expected to be white
            if not journal_home_page == PlosOneHomePage:
                assert block_header.value_of_css_property('color') in \
                       ('rgb(255, 255, 255)', 'rgba(255, 255, 255, 1)'), \
                       '{0} Carousel # {1!s}: block header color should be white.' \
                       .format(journal_name, carousel_index + 1)

            assert block_header.value_of_css_property('background') not in \
                ('rgb(255, 255, 255)', 'rgba(255, 255, 255, 1)'), \
                '{0} Carousel # {1!s}: block header background color should not be white.' \
                .format(journal_name, carousel_index + 1)

            items, total_index, carousel_control = wombat_page.get_carousel_items(carousel)
            assert len(items) == total_index
            # TODO: add a check for link validity when full content becomes available
            # in test environment

            # carousel buttons work
            direction = random.choice(['previous', 'next'])
            logging.info('testing {0} carousel buttons'.format(direction))
            for item_idx, item in enumerate(items):
                cur_idx = wombat_page.get_carousel_next_item(carousel_control, direction)
                if direction == 'next':
                    expected_idx = item_idx + 2 if item_idx + 1 < total_index else 1
                else:
                    expected_idx = total_index - item_idx if total_index > 0 else total_index
                assert cur_idx == expected_idx, \
                    '{0}, Carousel #{1}: item indicator (1 of x) value invalid during navigation,' \
                    ' expected: {1!s}, found: {2!s}'.format(journal_name, expected_idx, cur_idx)
