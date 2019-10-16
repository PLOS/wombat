#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
A class to to check style consistency across the application.
Sections:
    - Typography
    - Icons
    - Form Elements
    - Navigation
    - Layout
    - Colors
    - Tables
    - Components
"""

import pytest
from selenium.webdriver.support.color import Color

__author__ = 'jgray@plos.org'
# Variable definitions

# typography
FONT_FACE_PLAIN = 'Helvetica, Arial, sans-serif'  # $font-face-plain

TXT_SIZE_LARGE = '18px'  # $txt-size-large: 18px;
TXT_SIZE_MEDIUMISH = '15px'  # $txt-size-mediumish: 15px;
TXT_SIZE_MEDIUM = '14px'  # txt-size-medium: 14px
TXT_SIZE_SMALL = '13px'  # $txt-size-small: 13px;
TXT_SIZE_XSMALL = '12px'  # $txt-size-xsmall: 12px;

LINE_HEIGHT = '18px'  # $line-height: 18px; // applies to paragraphs and lists by default

# colors
TABS_BASE_DARK = '#202020'  # 'rgb(32, 32, 32) - approx. black, $tabs-base-color
GREY_DARK = '#303030'  # rgb(48, 48, 48) $grey-dark:
BLACK = '#202020'  # rgb(32, 32, 32) $black: #202020;
WHITE = '#ffffff'  # rgb(255, 255, 255) $white: #fff;  #ffffff

# colors: journal_specific
# plos-one
LIME = '#d7df23'  # rgb(215, 223, 35)
PURPLE = '#3e0577'  # rgb(62, 5, 119) $plos-one-purple: #3e0577;
PINK = '#cc00a6'  # rgb(204, 0, 166) $plos-one-pink: #cc00a6;
TEAL = '#3daca0'  # rgb(61, 172, 160) $plos-one-teal: #3daca0;

# journals
BIO_GREEN = '#16a127'  # $plos-bio: #16a127; //green: biology, comp-bio, genetics
MED_PURPLE = '#891fb1'  # $plos-med: #891fb1; // purple: medicine, pathogens, ntds
LIGHT_BLUE = '#1dbfe0'  # $plos-currents: #1dbfe0; // light blue
PLOS_BLUE = '#3c63af'  # $plos-blue: #3c63af;

# article tabs
ARTICLE_TAB_BACKGROUND = ['#303030', '#424242', '#545454', '#666666', '#777777', '#898989']


class StyledPage:
    """
    Model the common styles of elements of the pages
    """

    # ===================================================
    # Typography ========================================
    # Note typefaces are defined as global variables at the top of the file
    @staticmethod
    def validate_application_font_family(element):
        assert FONT_FACE_PLAIN in element.value_of_css_property('font-family'), \
            'Element is not of the expected font-family: ' \
            '{0}'.format(element.value_of_css_property('font-family'))

    @staticmethod
    def validate_font_size(element, size):
        assert element.value_of_css_property('font-size') == size, \
            'Element font-size is not of the expected value: ' \
            '{0}'.format(element.value_of_css_property('font-size'))

    @staticmethod
    def validate_font_size_rounded(element, size):
        font_size_float = float(element.value_of_css_property('font-size').replace('px', ''))
        assert size == pytest.approx(font_size_float, 0.01), \
            'Element font-size is not of the expected value: ' \
            '{0}'.format(element.value_of_css_property('font-size'))

    @staticmethod
    def validate_text_align(element, alignment):
        assert alignment in element.value_of_css_property('text-align'), \
            'Element alignment is not of the expected type: ' \
            '{0}'.format(element.value_of_css_property('text-align'))

    @staticmethod
    def validate_text_weight(element, weight):
        assert element.value_of_css_property('font-weight') == weight, \
            'Element font-weight is not of the expected value: ' \
            '{0}'.format(element.value_of_css_property('font-weight'))

    # ===================================================
    # Icons =============================================

    # ===================================================
    # Form Elements =====================================

    # ===================================================
    # Navigation ========================================

    # ===================================================
    # Layout ============================================

    @staticmethod
    def validate_element_line_height(element, height):
        assert element.value_of_css_property('line-height') == height, \
            'Element is not of the expected line height: ' \
            '{0}'.format(element.value_of_css_property('line-height'))

    # ===================================================
    # Colors ============================================
    # Note Colors are defined as global variables at the top of the file
    @staticmethod
    def validate_element_color(element, expected_color, check_color='hex'):
        css_color = element.value_of_css_property('color')
        if check_color == 'rgb':
            actual_color = Color.from_string(css_color).rgb
        else:
            actual_color = Color.from_string(css_color).hex
        assert actual_color == expected_color, \
            'Element is not of the expected color: ' \
            '{0}'.format(element.value_of_css_property('color'))

    @staticmethod
    def validate_element_background_color(element, expected_color, check_color='hex'):
        css_color = element.value_of_css_property('background-color')
        if check_color == 'rgb':
            actual_color = Color.from_string(css_color).rgb
        else:
            actual_color = Color.from_string(css_color).hex
        assert actual_color == expected_color, \
            'Element is not of the expected background color: ' \
            '{0}'.format(element.value_of_css_property('background-color'))
