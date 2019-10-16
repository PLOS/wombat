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
This test case validates the Wombat Desktop Article Page article tab body elements
"""

__author__ = 'ivieira@plos.org'

import logging
import pytest

from .Pages.PlosBiologyArticleBodyLightbox import PlosBiologyArticleBodyLightbox
from .Pages.PlosMedicineArticleBodyLightbox import PlosMedicineArticleBodyLightbox
from .Pages.PlosCompBiolArticleBodyLightbox import PlosCompBiolArticleBodyLightbox
from .Pages.PlosGeneticsArticleBodyLightbox import PlosGeneticsArticleBodyLightbox
from .Pages.PlosPathogensArticleBodyLightbox import PlosPathogensArticleBodyLightbox
from .Pages.PlosNeglectedArticleBodyLightbox import PlosNeglectedArticleBodyLightbox
from .Pages.PlosOneArticleBodyLightbox import PlosOneArticleBodyLightbox
from .Pages.PlosClinicalTrialsArticleBodyLightbox import PlosClinicalTrialsArticleBodyLightbox


@pytest.mark.usefixtures("driver_get")
class TestArticleBody:
    @pytest.mark.parametrize("plos_page", [
        PlosBiologyArticleBodyLightbox,
        PlosMedicineArticleBodyLightbox,
        PlosCompBiolArticleBodyLightbox,
        PlosGeneticsArticleBodyLightbox,
        PlosPathogensArticleBodyLightbox,
        PlosNeglectedArticleBodyLightbox,
        PlosOneArticleBodyLightbox,
        PlosClinicalTrialsArticleBodyLightbox,
    ])
    def test_plos_article_lightbox(self, plos_page):
        plos_page_lightbox = plos_page(self.driver)
        try:
            plos_page_lightbox.validate_article_first_figure_inline()
        except AssertionError:
            logging.info('This test can\'t be done without images')
            return self

        plos_page_lightbox.validate_article_lightbox_close_button()
        plos_page_lightbox.validate_article_lightbox_title()
        plos_page_lightbox.validate_article_lightbox_authors()
        plos_page_lightbox.validate_article_lightbox_zoom_reset()
        plos_page_lightbox.validate_article_lightbox_caption()
        plos_page_lightbox.validate_article_lightbox_show_in_context_button()
        plos_page_lightbox.validate_article_lightbox_prev_next_buttons()
        plos_page_lightbox.validate_article_lightbox_drawer()
        plos_page_lightbox.validate_article_lightbox_download_buttons()
