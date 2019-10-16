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

__author__ = 'gtimonina@plos.org'

import logging
import time

from selenium.webdriver.common.by import By

from ..Pages.Article import Article
from ...Base import ParseXML
from ...Base.Config import crepo_bucket
from ...Base.MySQL import MySQL

# Variable definitions
author_list_truncation_max = 14
author_list_ellipsis_index = 13
author_list_ellipsis = ' [ ... ]'


class ArticlePage(Article):
    """
    Model an abstract base Article page.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticlePage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._article_title = (By.CLASS_NAME, 'article-title')
        self._article_type = (By.CLASS_NAME, 'item-title')

        self._article_author_list = (By.CLASS_NAME, 'author-list')
        self._article_author_info = (By.CLASS_NAME, 'author-info')
        self._article_author_corresponding_author = (By.CLASS_NAME, 'email')
        self._author_info_list = (By.CSS_SELECTOR, '.modal-content p')
        self._close_info_modal = (By.CLASS_NAME, 'close')
        self._article_meta_data = (By.CLASS_NAME, 'articleinfo')
        self._section2 = (By.ID, 'section2')
        self._references_section = (By.ID, 'references')
        self._items = (By.CSS_SELECTOR, 'a.expander')
        self._references_section = (By.LINK_TEXT, 'References')
        self._abstract_section = (By.LINK_TEXT, 'Abstract')
        self._introduction_section = (By.LINK_TEXT, 'Introduction')
        self._section1_first_ref = (By.CSS_SELECTOR, '#section1 a.xref')
        self._ref_content = (By.ID, 'ref-panel-content')
        self._ref_label_text = (By.CSS_SELECTOR, '#ref-panel-content > span.label')
        self._close_ref_panel = (By.CSS_SELECTOR, '#reference-panel a.close')
        self._references_label = (By.CSS_SELECTOR, 'span.label')
        self._figures_more = (By.PARTIAL_LINK_TEXT, 'More »')
        self._materials_and_methods = (By.LINK_TEXT, 'Materials and Methods')
        self._more_detail = (By.CSS_SELECTOR, '#figure-info-window > .modal-tab')
        self._arrow = (By.CSS_SELECTOR, 'span.arrow')
        self._figure_description = (By.CSS_SELECTOR, 'div.figure-description')
        self._figure_modal_header = (By.CLASS_NAME, 'clearfix')
        self._v = (By.LINK_TEXT, 'v')
        self._back_to_article = (By.CLASS_NAME, 'back')
        self._popular_button = (By.CSS_SELECTOR, "li[data-method='popular']")

    # POM Actions

    def assert_article_type(self):
        """
        Ensure Article Type bug is displayed on page
        Valid for all article pages
        :return: True if found, else False
        """
        self._get(self._article_type)
        return self

    def assert_article_title(self):
        """
        Ensure Article Title ID appears on the displayed page
        :return: True if found, else False
        """
        self._get(self._article_title)
        return self

    def return_article_title(self):
        """
        Extracts text of item on displayed article page bearing the id for article title
        then compares this string to the article title as extracted from the ambra database
        :return: String and optional error string if displayed article title doesn't match
        that extracted from the ambra database.
        """
        article_title_text = self._get(self._article_title).text
        article_doi_stripped = self.extract_page_doi()
        article_title_db = str(MySQL().query(
            "SELECT title FROM articleIngestion WHERE articleId in (select articleId \
                   from article where doi = '" + str(article_doi_stripped) + "')"))
        if article_title_text != article_title_db:
            print(article_title_text + " is not equal to " + article_title_db)
        return article_title_text

    def validate_article_author_list(self):
        """
        Queries CREPO to determine whether an author list should be displayed or not
        :return: True if author list should exist, else False
        """
        doi = self.extract_page_doi()
        xml_path = self.get_article_xml()
        author_list = ParseXML.ParseXML().get_auths(xml_path)
        if len(author_list) > 0:
            return True
        else:
            print("Article: " + doi + " has an empty author list.")
            return False

    def assert_article_author_list(self):
        """
        Verify that an item with the ID of the author list is displayed on the page
        Presumed to be called only in the case where validate_article_author_list()
        returns True
        :return: True if present, else False
        """
        self._get(self._article_author_list)
        return self

    def validate_article_author_list_truncation(self):
        """
        Validate that if an author list is present and it has more than 14 authors
        that the item at index 13 of the list is presented as an ellipsis
        :return: True if list index 13 of author list is an ellipsis, else False OR Empty
        string if author list is empty
        """
        article_author_list_text = self._get(self._article_author_list).text
        if article_author_list_text == '':
            print('Article Author List is empty')
            return article_author_list_text
        else:
            author_list = article_author_list_text.split(', ')
            # /DesktopPlosGenetics/article?id=10.1371/journal.pgen.1003500 has 265 authors!
            # if article has more than 14 authors, item 13 in the list should be an ellipsis
            # in square brackets
            if (len(author_list)) > author_list_truncation_max:
                assert author_list[author_list_ellipsis_index] == author_list_ellipsis
        return self

    def click_article_author_list_expander_contractor(self):
        """
        Validate the function of the article author list expansion and contraction function
        In the case where author list has 14 or more members that the expander element and ellipsis
        are present, that selecting the expander makes the ellipsis disappear and shows the
        contractor element, and that selecting the contractor makes the ellipsis reappear.
        :return: True if article has less than 14 authors OR if the noted conditions all verify,
        else False
        """
        article_author_list_text = self._get(self._article_author_list).text
        author_list = article_author_list_text.split(',\n')
        if (len(author_list)) > author_list_truncation_max:
            print(author_list[author_list_ellipsis_index])
            assert author_list[author_list_ellipsis_index] == author_list_ellipsis
            self._get(self._article_list_expander).click()
            # for some unknown reason the expanded section of the author list has a different class
            expanded_article_author_list_text = self._get(self._article_author_list).text
            expanded_author_list = expanded_article_author_list_text.split(',\n')
            assert expanded_author_list[author_list_ellipsis_index] != author_list_ellipsis
            self._get(self._article_list_contractor).click()
            article_author_list_text = self._get(self._article_author_list).text
            author_list = article_author_list_text.split(',\n')
            assert author_list[author_list_ellipsis_index] == author_list_ellipsis
        return self

    def validate_article_author_list_click_tooltip(self):
        self.moveto_author_list()
        article_author_list_text = self._get(self._article_author_list).text
        if article_author_list_text == '':
            print('Article Author List is empty')
            return article_author_list_text
        else:
            author_list = article_author_list_text.split(',\n')
            if (len(author_list)) > author_list_truncation_max:
                self._get(self._article_list_expander).click()
                expanded_article_author_list_text = self._get(self._article_author_list).text
                author_list = expanded_article_author_list_text.split(',\n')
                author = author_list[0]
                auth_index = 0
                # LOCATOR
                self._article_author_list_item = (
                    By.XPATH, '//*[@data-author-id=' + str(auth_index) + ']')
                # LOCATOR
                # self._article_author_no_author_data = (By.XPATH, '//span[@data-author-id='
                #  + str(auth_index) + ']')
                if self._get(self._article_author_list_item):
                    self._get(self._article_author_list_item).click()
                    try:
                        article_author__info_list = self._gets(self._author_info_list)
                        assert article_author__info_list
                        # article_author_affiliation = (
                        # By.ID, 'authAffiliations-' + str(auth_index))
                        # self._get(article_author_affiliation)
                        # self._article_author_tooltip_close = (
                        #     By.ID, 'tooltipClose' + str(auth_index))
                    except:
                        print("Class is no-author-data")

        close_author_info_modal = self._get(self._close_info_modal)
        close_author_info_modal.click()
        # if (len(author_list)) > author_list_truncation_max:
        #     self._get(self._article_list_contractor).click()
        # return self

    def click_on_section(self, section_locator):
        article_section = self._get(section_locator)
        article_section.click()
        return self

    def click_on_section_name(self, driver, article_section_name):
        """
        click on section using section name
        :param article_section_name: string
        :return: void function
        """
        article_section = driver.find_element_by_link_text(article_section_name)
        article_section.click()

    def click_on_introduction_ref_link(self):
        first_reference = self._get(self._section1_first_ref)
        first_reference.click()
        self._get(self._ref_content)

    def get_introduction_ref_label_text(self):
        label_text = self._get(self._ref_label_text).text
        close_button = self._get(self._close_ref_panel)
        close_button.click()
        return label_text

    def get_references_list(self):
        references_list = self._gets(self._references_label)
        return references_list

    # TODO: Validate not present on load, Insert Page down keystroke, validate presence
    # TODO: Validate Title, validate author list, validate logo, validate close function
    def moveto_section_methods(self):
        section2 = self._get(self._section2)
        self._actions.move_to_element(section2).perform()
        return self

    def moveto_section_references(self):
        refs = self._get(self._references_section)
        self._actions.move_to_element(refs).perform()
        return self

    def validate_figure_viewer_links(self, i_phone=False):
        logging.info('Validating Figure Viewer link')
        figures_more_link = self._get(self._figures_more)
        figures_more_link.click()
        if not i_phone:
            self._get(self._more_detail)
            arrow = self._get(self._arrow)
            arrow.click()
            time.sleep(1)
            self._get(self._figure_description)
            self._get(self._figure_modal_header)
            self._wait_for_element(self._get(self._v))
            close_info_modal = self._get(self._v)
            close_info_modal.click()
        self._wait_for_element(self._get(self._back_to_article))

    def move_back_to_article(self):
        logging.info('Validating figure viewer link navigation back to article')
        back_to_article = self._get(self._back_to_article)
        back_to_article.click()
        self._wait_for_element(self._get(self._site_logo))
