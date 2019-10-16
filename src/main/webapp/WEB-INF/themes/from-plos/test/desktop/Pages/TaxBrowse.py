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


import json
import logging
import random
import re
import requests

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond

from .WombatPage import WombatPage
from ...Base.Config import solr_url, base_url
from ...Base.styles import GREY_DARK, WHITE, LIME, TXT_SIZE_MEDIUM, TXT_SIZE_LARGE, \
    LINE_HEIGHT, TXT_SIZE_XSMALL, StyledPage as styles

__author__ = 'jgray@plos.org'


class TaxBrowser(WombatPage):
    """
    Model the PlosOne desktop Taxonomy Browser. This will be accessible from any Wombat-served
    PlosONE page.
    """

    def __init__(self, driver, url_suffix='/DesktopPlosOne'):
        super(TaxBrowser, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._nav_elements = (By.ID, 'pagehdr')
        self._taxbrowse_menu = (By.CLASS_NAME, 'subject-area')
        self._taxbrowse_container = (By.ID, 'taxonomyContainer')
        self._taxbrowse_block = (By.CSS_SELECTOR, '#taxonomy-browser[style *=block]')
        self._taxbrowse_container_header = (By.CLASS_NAME, 'taxonomy-header')
        self._taxbrowse_container_helpicon = (By.ID, 'subjInfo')
        self._taxbrowse_container_helptext = (By.CSS_SELECTOR, 'div#subjInfoText p')
        self._taxbrowse_container_helptext2 = (By.XPATH, '//div[@id="subjInfoText"]/p[2]')
        self._taxbrowse_container_helplink = (By.CSS_SELECTOR, 'div#subjInfoText p a')

        self._taxbrowse_container_levels_div = (By.CLASS_NAME, 'levels')
        self._taxbrowse_container_levels_level_title = (By.CLASS_NAME, 'level-title')
        self._taxbrowse_container_levels_level_top = (
            By.CSS_SELECTOR, 'div.level-active div.level-top')
        self._taxbrowse_container_levels_level_top_link = (
            By.CSS_SELECTOR, 'div.level-active div.level-top a')
        self._taxbrowse_container_levels_level_scrollarea = (By.CLASS_NAME, 'level-scroll')
        self._taxbrowse_container_levels_level_up_arrow = (By.CLASS_NAME, 'up')
        self._taxbrowse_container_levels_level_down_arrow = (By.CLASS_NAME, 'down')
        self._taxbrowse_container_levels_level_title_datalevel = (
            By.CSS_SELECTOR, 'div.level-active div.level-title')
        self._taxbrowse_active_level_top = (By.CSS_SELECTOR, 'div.level-active div.level-top a')
        self._taxbrowse_prevarrow = (By.CLASS_NAME, 'prev')
        self._taxbrowse_nextarrow = (By.CLASS_NAME, 'next')

        self._browse_page_subject_bar = (By.CSS_SELECTOR, 'div.subject h1')
        self._browse_page_article_count = (By.CLASS_NAME, 'count')

    # POM Actions
    def validate_taxbrowse_menu(self):
        self._get(self._taxbrowse_menu)
        return self

    def reopen_taxbrowse_menu(self):
        self._wait_for_element(self._get(self._taxbrowse_menu))
        taxbrowse_button = self._get(self._taxbrowse_menu)
        taxbrowse_button.click()
        self._wait_for_element(self._get(self._taxbrowse_block))
        return self

    def open_taxbrowser_validate_heading(self):
        tb_header_expected_text = 'Browse Subject Areas'
        tb_header_expected_helpicon = '?'
        tb_header_helpicon_expected_text = \
            'Click through the PLOS taxonomy to find articles in your field.'
        tb_header_helpicon_expected_text2 = \
            'For more information about PLOS Subject Areas, click here.'
        tb_header_help_expected_relative_link = ('https://github.com/PLOS/plos-thesaurus/blob/'
                                                 'develop/README.md')
        self._get(self._taxbrowse_menu).click()
        self._get(self._taxbrowse_container)
        tb_header = self._get(self._taxbrowse_container_header)
        tb_header_text = self._get(self._taxbrowse_container_header).text
        self.validate_text_contains(tb_header_text, tb_header_expected_text)
        styles.validate_application_font_family(tb_header)

        styles.validate_font_size(tb_header, TXT_SIZE_LARGE)
        styles.validate_element_color(tb_header, WHITE, 'hex')
        styles.validate_element_line_height(tb_header, '30px')
        tb_header_helpicon = self._get(self._taxbrowse_container_helpicon)
        tb_header_helpicon_text = self._get(self._taxbrowse_container_helpicon).text
        assert tb_header_helpicon_text == tb_header_expected_helpicon
        styles.validate_application_font_family(tb_header_helpicon)
        styles.validate_font_size(tb_header_helpicon, TXT_SIZE_XSMALL)
        styles.validate_element_color(tb_header_helpicon, WHITE, 'hex')
        styles.validate_element_background_color(tb_header_helpicon, GREY_DARK, 'hex')
        self._actions.move_to_element(tb_header_helpicon).perform()
        tb_header_helpicon_text = self._get(self._taxbrowse_container_helptext).text
        tb_header_helpicon_text2 = self._get(self._taxbrowse_container_helptext2).text
        self.validate_text_exact(tb_header_helpicon_text, tb_header_helpicon_expected_text)
        self.validate_text_exact(tb_header_helpicon_text2, tb_header_helpicon_expected_text2)
        self._wait_for_element(self._get(self._taxbrowse_container_helplink))
        link_to_help = self._get(self._taxbrowse_container_helplink)
        link_to_help.click()

        actual_url = self.open_page_in_new_window(
                page_title='plos-thesaurus', original_url=base_url)

        self.validate_text_contains(actual_url, tb_header_help_expected_relative_link,
                                    "Incorrect help link")
        return self

    def validate_level_display(self):
        expected_view_all_arts_link_text = 'View All Articles'
        expected_level_title_text = 'All Subject Areas'
        self._get(self._taxbrowse_container_levels_level_up_arrow)
        self._get(self._taxbrowse_container_levels_level_down_arrow)
        level_title = self._get(self._taxbrowse_container_levels_level_title)
        level_title_text = self._get(self._taxbrowse_container_levels_level_title).text
        self.validate_text_exact(level_title_text, expected_level_title_text)
        styles.validate_application_font_family(level_title)
        styles.validate_font_size(level_title, TXT_SIZE_MEDIUM)

        styles.validate_element_color(level_title, LIME, 'hex')
        view_all_arts_link = self._get(self._taxbrowse_container_levels_level_top_link)
        view_all_arts_link_text = self._get(self._taxbrowse_container_levels_level_top_link).text
        print(view_all_arts_link_text)
        self.validate_text_contains(view_all_arts_link_text, expected_view_all_arts_link_text)

        styles.validate_application_font_family(view_all_arts_link)
        styles.validate_font_size(view_all_arts_link, TXT_SIZE_MEDIUM)
        styles.validate_element_color(view_all_arts_link, GREY_DARK, 'hex')
        level_top = self._get(self._taxbrowse_container_levels_level_top)
        styles.validate_element_background_color(level_top, LIME, 'hex')
        view_all_arts_link.click()
        browse_page_subject = self._get(self._browse_page_subject_bar).text
        # capitalization mismatch between browser and target pages
        self.validate_text_exact(browse_page_subject.lower(), expected_level_title_text.lower())
        self._driver.back()
        return self

    def validate_level_navigation(self):
        self.reopen_taxbrowse_menu()

        expected_view_all_arts_link_text = 'View All Articles'
        view_all_arts_link_text = self._get(self._taxbrowse_container_levels_level_top_link).text
        self.validate_text_contains(view_all_arts_link_text, expected_view_all_arts_link_text)

        subject_hierarchy = self.get_subject_area_hierarchy()
        term_array = list(self.get_random_term_path_array(subject_hierarchy))
        print('path: ' + '/'.join(term_array))
        # iterate through the hierarchy and ensure you can click on each term in sequence
        final_term = None
        final_article_count = 0
        for index, term in enumerate(term_array):
            level = index + 1
            final_term = term
            final_article_count = self.click_term_link(level, term) or final_article_count
            if term == term_array[-1] and final_article_count == 0:
                # if last term in the path is a group, one more level is displayed, and we
                # checking 'view all articles' on that additional (level+1)
                # which counts our last term
                logging.info(str(term_array))
                logging.info('index: {0!r}, level: {1!r}, term: {2!r}, final_article_count: {3!r}'
                             .format(index, level, term, final_article_count))
                view_all_articles_link_css = \
                    (By.CSS_SELECTOR, 'div.level[data-level={0!r}] div.level-top > a'
                     .format(str(level+1)))
                view_all_articles_link = self._get(view_all_articles_link_css)
                article_count = view_all_articles_link.text.replace(term, '')
                final_article_count = article_count[:-1].replace('View All Articles (', '')
                logging.info('final_article_count: {0!r}'.format(final_article_count))
                self.click_covered_element(view_all_articles_link)

        self._wait.until(exp_cond.url_contains('browse'))
        actual_text = self._get(self._browse_page_subject_bar).text
        logging.info('Final_article_count: {0!r}, subject filter text: {1!r}, final term: {2!r}'
                     .format(final_article_count, actual_text, final_term))
        self.validate_text_contains(actual_text, final_term)
        count_text = self._get(self._browse_page_article_count).text
        assert str(final_article_count) == count_text.split(' ')[-1].replace(',', ''), (
            str(final_article_count), count_text.split(' ')[-1].replace(',', ''))
        return self

    def validate_navigation_controls(self):
        self.reopen_taxbrowse_menu()

        subject_hierarchy = self.get_subject_area_hierarchy()
        # remove paths that have less than three slashes - navigation controls appear at level 3
        filtered_hierarchy = filter(lambda s: s.count('/') > 3, subject_hierarchy)

        term_array = self.get_random_term_path_array(filtered_hierarchy)

        self._get(self._taxbrowse_container_levels_level_down_arrow).click()
        self._get(self._taxbrowse_container_levels_level_up_arrow).click()
        self._get(self._taxbrowse_container_levels_level_down_arrow).click()
        self._get(self._taxbrowse_container_levels_level_up_arrow).click()

        for index, term in enumerate(term_array):
            level = index + 1
            self.wait_for_animation('.level-scroll:last')
            self.click_term_link(level, term)
            if index > 1:
                self._get(self._taxbrowse_prevarrow).click()
                self._get(self._taxbrowse_nextarrow).click()
                self._get(self._taxbrowse_prevarrow).click()
                self._get(self._taxbrowse_nextarrow).click()
                break

        return self

    def click_term_link(self, level, term):
        logging.info('Checking level {0!r}, subject area: {1!r}'.format(level, term))
        # Setting current level container variable
        self._taxbrowse_container_levels_current_level = (
             By.CSS_SELECTOR, 'div[data-level=\"' + str(level) + '\"]')
        term_item = (By.XPATH,
                     '//a[@data-level={0!r}][contains(.,{1!r})]'.format(str(level), term))
        subject_area = self._driver.find_element(*term_item)
        sa = subject_area.wrapped_element
        logging.info('subject_area.wrapped_element text {0!r}'.format(sa.text))
        subject_area_covered = self._iget(term_item)
        text_term = self._driver.execute_script("javascript:return arguments[0].innerText",
                                                subject_area_covered)
        logging.info('subject_area.text {0!r}'.format(text_term))
        result = re.search(r'\(\d+\)', text_term)
        logging.info('regex result count result: {0!r}'.format(result))

        self.click_covered_element(subject_area)

        if not result:
            return None

        # return the resultant number of articles
        article_count = str(result.string.replace(term, '')).replace(' ', '').replace('(', ''). \
            replace(')', '')
        logging.info('count result after normalizing: {0!r}'.format(article_count))
        return article_count

    @staticmethod
    def get_random_term_path_array(subject_hierarchy):
        # Select a random taxonomy path from json response
        subject_hierarchy_string = random.Random().choice(list(subject_hierarchy))
        term_array = filter(None, subject_hierarchy_string.split('/'))
        return term_array

    @staticmethod
    def get_subject_area_hierarchy():
        subject_hierarchy_query = '?q=*%3A*&rows=0&wt=json&indent=true&facet=true' \
                                  '&facet.field=subject_hierarchy&facet.limit=-1'
        taxonomy_response = requests.get(solr_url + subject_hierarchy_query)
        json_response = json.loads(taxonomy_response.text)
        hierarchy = json_response['facet_counts']['facet_fields']['subject_hierarchy']
        # hierarchy contains term weights and paths. We only care about paths - remove weights
        return filter(lambda s: isinstance(s, str), hierarchy)
