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

__author__ = 'jgray@plos.org'

import logging
import re
import time

from selenium.webdriver.common.by import By

from ..Pages.Article import Article
from ...Base import ParseXML
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
        self._article_top = (By.ID, 'topslot')
        self._license = (By.ID, 'licenseShort')
        self._peer_reviewed = (By.ID, 'peerReviewed')
        self._article_title = (By.ID, 'artTitle')
        self._article_author_list = (By.CLASS_NAME, 'author-list')
        self._article_author_info = (By.CLASS_NAME, 'author-info')
        self._article_author_corresponding_author = (By.CLASS_NAME, 'email')
        self._article_author_equal_contributing_author = (By.CLASS_NAME, 'contribute')
        self._article_author_customfootnote_author = (By.CLASS_NAME, 'rel-footnote')
        # inconsistent number in paired ID's - watch out
        self._article_list_expander = (By.ID, 'authors-show')
        self._article_list_contractor = (By.ID, 'author-hide')
        self._article_publication_date = (By.ID, 'artPubDate')
        self._article_doi = (By.ID, 'artDoi')
        self._article_published_in = (By.XPATH, "//ul[@class='date-doi']/li[3]/a")
        # We cross-publish to both sites (Collections) and journals (eg Medicine).
        # Only journals are italicized
        self._article_published_in_journal = (By.XPATH, "//ul[@class='date-doi']/li[3]/a/em")
        self._tab_article = (By.ID, 'tabArticle')
        self._tab_authors = (By.ID, 'tabAuthors')
        self._tab_metrics = (By.ID, 'tabMetrics')
        self._tab_comments = (By.ID, 'tabComments')
        self._tab_related = (By.ID, 'tabRelated')
        self._tab_peer_review = (By.ID, 'tabPeerReview')
        self._section1 = (By.ID, 'section1')
        # a down page target to bring up the floating header, visible
        self._article_meta_data = (By.CLASS_NAME, 'articleinfo')
        self._section2 = (By.ID, 'section2')
        self._references_section = (By.ID, 'references')
        self._article_floating_title_div = (By.CLASS_NAME, 'topVisible')
        self._article_floating_title = (By.XPATH, "//div[@class='float-title-inner']/h1")
        self._article_floating_title_author_list = (By.ID, 'floatAuthorList')
        self._article_floating_title_logo = (By.XPATH, "//div[@id='titleTopCloser']/img")
        self._article_floating_title_closer = (By.CLASS_NAME, 'close-floater')

    # POM Actions

    def assert_license(self):
        """
        This method validates the presence of the OpenAccess 'bug' on article pages of
        type Research Article.
        :return:
        """
        self._get(self._license)
        return self

    def assert_peer_reviewed(self):
        """
        Ensure Peer Review bug appears on page
        Should only be called for pages of type Research Article
        :return: True if found, else False
        """
        self._get(self._peer_reviewed)
        return self

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
        article_doi_stripped = ArticlePage.extract_page_doi(self)
        article_title_db = str(MySQL().query(
            "SELECT title FROM articleIngestion WHERE articleId in (select articleId \
            from article where doi = '" + str(article_doi_stripped) + "')"))
        # italic_title_element = re.sub(r'^.*<italic>(.*)</italic>.*$', '\\1', article_title_db)
        if article_title_text != article_title_db:
            logging.error(article_title_text + " is not equal to " + article_title_db)
        return article_title_text

    def validate_article_author_list(self):
        """
        Queries CREPO to determine whether an author list should be displayed or not
        :return: True if author list should exist, else False
        """
        doi = Article.extract_page_doi(self)
        xml_path = self.get_article_xml()
        author_list = ParseXML.ParseXML().get_auths(xml_path)
        if len(author_list) > 0:
            return True
        else:
            logging.warning("Article: {0!r} has an empty author list.".format(doi))
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
        :return: True if list index 13 of author list is an ellipsis, else False OR Empty string
        if author list is empty
        """
        article_author_list_text = self._get(self._article_author_list).text
        if article_author_list_text == '':
            logging.info('Article Author List is empty')
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
            logging.info(author_list[author_list_ellipsis_index])
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
            logging.info('Article Author List is empty')
            return article_author_list_text
        else:
            author_list = article_author_list_text.split(',\n')
            if (len(author_list)) > author_list_truncation_max:
                self._get(self._article_list_expander).click()
                expanded_article_author_list_text = self._get(self._article_author_list).text
                author_list = expanded_article_author_list_text.split(',\n')
            for author in author_list:
                auth_index = author_list.index(author)
                # LOCATOR
                self._article_author_list_item = (
                    By.XPATH, '//*[@data-author-id=' + str(auth_index) + ']')
                # LOCATOR
                # self._article_author_no_author_data = (By.XPATH, '//span[@data-author-id='
                # + str(auth_index) + ']')
                if self._get(self._article_author_list_item):
                    if auth_index == 0:
                        self.close_floating_title_top()
                        self._get(self._article_author_list_item).click()
                    else:
                        self._get(self._article_author_list_item).click()
                    # LOCATOR
                    try:
                        article_author_affiliation = (By.ID, 'authAffiliations-' + str(auth_index))
                        self._get(article_author_affiliation)
                        self._article_author_tooltip_close = (
                            By.ID, 'tooltipClose' + str(auth_index))
                    except:
                        logging.info("Class is no-author-data")
                        continue
        outside_link = self._get(self._license)
        outside_link.click()
        if (len(author_list)) > author_list_truncation_max:
            self._get(self._article_list_contractor).click()
        return self

    def validate_special_auth(self, special_author_type):
        self.moveto_author_list()
        clean_author_list = []
        xml_path = self.get_article_xml()
        if special_author_type == "corresp":
            special_author_case = ParseXML.ParseXML().get_corresp_auths(xml_path)
            special_author_locator = self._article_author_corresponding_author
        elif special_author_type == "cocontrib":
            special_author_case = ParseXML.ParseXML().get_cocontributing_auths(xml_path)
            special_author_locator = self._article_author_equal_contributing_author
        elif special_author_type == "customfootnote":
            special_author_case = ParseXML.ParseXML().get_customfootnote_auths(xml_path)
            special_author_locator = self._article_author_customfootnote_author
        if special_author_case:
            article_author_list_text = self._get(self._article_author_list).text
            author_list = article_author_list_text.split(',\n')
            if (len(author_list)) > author_list_truncation_max:
                self._get(self._article_list_expander).click()
                expanded_article_author_list_text = self._get(self._article_author_list).text
                author_list = expanded_article_author_list_text.split(',\n')
            for author in author_list:
                grey_author = author.rstrip(' ')
                clean_author = grey_author.replace('\n[ view less ]', '')
                clean_author = re.sub('(?i)(.*on behalf of )(the)?', '', clean_author).strip()
                clean_author_list.append(clean_author)
            for author in special_author_case:
                logging.info("Checking {0!r} for {1} author stanza in tooltip"
                             .format(str(author), special_author_type))
                auth_index = clean_author_list.index(author)
                # LOCATOR
                self._article_author_list_item = (
                    By.XPATH, '//a[@data-author-id=' + str(auth_index) + ']')
                self._get(self._article_author_list_item).click()
                # LOCATOR
                self._article_author_meta_div = (By.ID, 'author-meta-' + str(auth_index))
                self._tooltip_close = (By.ID, 'tooltipClose' + str(auth_index))
                self._get(self._article_author_list_item).find_element(*special_author_locator)
                close_tooltip = self._get(self._tooltip_close)
                close_tooltip.click()
            if (len(author_list)) > author_list_truncation_max:
                self._get(self._article_list_contractor).click()
        outside_link = self._get(self._license)
        outside_link.click()
        return self

    def assert_article_publication_date(self):
        self._get(self._article_publication_date)
        return self

    def return_article_publication_date(self):
        article_pub_date_text = self._get(self._article_publication_date).text
        return article_pub_date_text

    def assert_article_doi(self):
        self._get(self._article_doi)
        return self

    def return_article_doi(self):
        article_doi_text = self._get(self._article_doi).text
        return article_doi_text

    def assert_article_published_in(self):
        self._get(self._article_published_in)
        return self

    def return_article_published_in(self):
        xp_journal = self._get(self._article_published_in).text
        return xp_journal

    def assert_article_published_in_journal(self):
        self._get(self._article_published_in_journal)
        return self

    def assert_tab_article(self):
        self._get(self._tab_article)
        return self

    def assert_tab_article_active(self):
        tab_article = self._get(self._tab_article)
        assert tab_article.get_attribute('class') == 'tab-title active'
        logging.info('Article Tab is Active')
        return self

    def assert_tab_authors(self):
        self._get(self._tab_authors)
        return self

    def assert_tab_metrics(self):
        self._get(self._tab_metrics)
        return self

    def assert_tab_comments(self):
        self._get(self._tab_comments)
        return self

    def assert_tab_related(self):
        self._get(self._tab_related)
        return self

    def moveto_top(self):
        top = self._get(self._article_top)
        self._scroll_into_view(top)
        self._actions.move_to_element(top).perform()
        return self

    def moveto_authors(self):
        authors = self._get(self._article_author_list)
        self._scroll_into_view(authors)
        self._actions.move_to_element(authors).perform()
        return self

    def moveto_section_introduction(self):
        section1 = self._get(self._section1)
        self._actions.move_to_element(section1).perform()
        return self

    def moveto_author_list(self):
        self.moveto_footer()
        self.moveto_authors()
        return self

    def moveto_section_metadata(self):
        metadata = self._get(self._article_meta_data)
        self._actions.move_to_element(metadata).perform()
        return self

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

    def assert_floating_header(self):
        floating_header = self._get(self._article_floating_title_div)
        assert floating_header
        return self

    def assert_floating_header_title(self):
        assert self._get(self._article_floating_title).text
        return self

    def assert_floating_header_author_list(self):
        xml_path = self.get_article_xml()
        author_list = ParseXML.ParseXML().get_auths(xml_path)
        if len(author_list) > 0:
            assert self._get(self._article_floating_title_author_list).text
        return self

    def assert_floating_header_logo(self):
        floater_logo = self._get(self._article_floating_title_logo)
        assert floater_logo
        return self

    def click_floating_header_closer(self):
        floater_closer = self._get(self._article_floating_title_closer)
        time.sleep(3)
        floater_closer.click()
        return self
