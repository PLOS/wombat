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
import re
import time

import requests
from selenium.webdriver.common import keys
from selenium.webdriver.common.by import By

from ..Pages.Article import Article
from ...Base import ParseXML
from ...Base.Config import rhino_url
from ...Base.CustomException import ElementDoesNotExistAssertionError

__author__ = 'jgray@plos.org'


# Variable definitions

class ArticleBody(Article):
    """
    Model an abstract base Article page, Article Tab.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticleBody, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._article_doi = (By.ID, 'artDoi')
        self._tab_article = (By.ID, 'tabArticle')
        self._article_body_container = (By.CLASS_NAME, 'article-container')
        self._article_navigation_div = (By.ID, 'nav-article')
        self._article_dynamic_nav = (By.CLASS_NAME, 'nav-page')
        self._article_nav_amendments = (By.CLASS_NAME, 'amendment-correction')
        self._article_dynamic_headings = (By.CLASS_NAME, 'scroll')
        self._article_fixed_nav = (By.CLASS_NAME, 'nav-secondary')
        self._article_nav_comments = (By.ID, 'nav-comments')
        self._article_nav_media = (By.ID, 'nav-media')
        self._article_nav_figures = (By.ID, 'nav-figures')
        self._article_metadata = (By.CLASS_NAME, 'articleinfo')
        self._article_body_headings = (By.TAG_NAME, 'h2')
        self._article_body_amendment_notice_div = (By.CLASS_NAME, 'amendment')
        self._article_body_amendment_notice_corrections = \
            (By.XPATH, '//div[@class="amendment amendment-correction toc-section"]/h2')
        self._article_body_amendment_notice_eoc = (
            By.XPATH, '//div[@class="amendment amendment-eoc toc-section"]/h2')
        self._article_body_amendment_notice_retraction = \
            (By.XPATH, '//div[@class="amendment amendment-retraction toc-section"]/h2')
        self._article_body_amendment_notice_text = (By.CLASS_NAME, 'amendment-body')
        self._article_body_amendment_notice_citation = (By.CLASS_NAME, 'amendment-citation')
        self._article_body_amendment_notice_date = (By.CLASS_NAME, 'amendment-date')
        self._article_body_amendment_notice_link = (By.CLASS_NAME, 'amendment-link')
        self._article_body_first_figure_inline = (
            By.XPATH, '//div[@class="figure"]/div[@class="img-box"]/a/img')
        self._article_body_figure_navbar = (By.XPATH, '//*[@id="nav-figures"]/a')
        self._article_body_figure_carousel_item = \
            (By.XPATH, '//*[@id="figure-carousel"]/div[@class="carousel-wrapper"]'
                       '/div[@class="slider"]/div[@class="carousel-item lightbox-figure"]/img')

    # POM Actions

    def assert_article_tab_active(self):
        assert self._get(self._tab_article).get_attribute('class')
        return self

    def validate_article_body_nav_headings(self):
        doi = ArticleBody.extract_page_doi(self)
        escaped_doi = self.extract_page_escaped_doi()
        expression_of_concern = False
        retraction = False
        correction_count = 0
        complete_article_nav_headings = []
        # make a call to rhino to get the related article of types [correction-forward,
        # expressed-concern, retraction ]
        # information
        logging.info("Calling Rhino api v2 to get article amendment information...")
        response = requests.get(rhino_url + '/articles/' + escaped_doi + '/relationships')
        json_response = json.loads(response.text)
        related_articles = json_response['inbound']
        x = len(related_articles) - 1
        while x > -1:
            type_ra = json_response['inbound'][x]['type']
            if type_ra in 'object-of-concern':
                expression_of_concern = True
                break
            if type_ra in 'retracted-article':
                retraction = True
                break
            if type_ra in 'corrected-article':
                correction_count += 1
            x -= 1

        if expression_of_concern:
            complete_article_nav_headings.append('Expression of Concern')
        if retraction:
            complete_article_nav_headings.append('Retraction')
        if correction_count > 0:
            if correction_count == 1:
                complete_article_nav_headings.append('Correction')
            else:
                complete_article_nav_headings.append('Corrections (' + str(correction_count) + ')')

        xml_path = self.get_article_xml()
        article_nav_headings = ParseXML.ParseXML().get_article_sections(xml_path)

        for heading in article_nav_headings:
            complete_article_nav_headings.append(heading)
        self.set_timeout(1)
        try:
            article_presented_nav_headings = self._gets(self._article_dynamic_headings)
        except:
            logging.info("No article headings found for " + doi)
            self.restore_timeout()
            return self
        self.restore_timeout()
        presented_nav_headings_text = []
        for element in article_presented_nav_headings:
            presented_nav_headings_text.append(element.text)
        assert presented_nav_headings_text == complete_article_nav_headings, \
            "Presented nav headings list {0} does not match Complete article nav headings list {1}"\
            .format(presented_nav_headings_text, complete_article_nav_headings)
        return self

    def get_page_comments_count(self):
        # get the number of comments from the page
        comments_from_page = self._get(self._article_nav_comments)
        number_of_comments_from_page = re.search('Reader Comments \((\d+)\)',
                                                 comments_from_page.text)
        number_of_comments_from_page = number_of_comments_from_page.group(1)
        return int(number_of_comments_from_page)

    def validate_media_curation_link_number(self):
        # make a call to ALM to get the articlecoveragecurated information
        alm_acc_data = self.return_alm_metrics_total('articlecoveragecurated')

        # get the number of comments from the page
        media_from_page = self._get(self._article_nav_media)
        assert re.search(r'(\d+)', media_from_page.text)
        number_of_media_from_page = re.search(r'(\d+)', media_from_page.text).group()
        # check to make sure that the number of media in the alm response matches up with the
        # number indicated in the left hand nav
        assert int(number_of_media_from_page) == alm_acc_data, \
            "Media Coverage count inconsistent with ALM. Expected ({0!r}).".format(alm_acc_data)
        return self

    def validate_figures_link(self):
        assert self._get(self._article_nav_figures)
        return self

    def validate_article_body_headings(self):
        complete_article_headings = []
        presented_article_headings = []
        doi = ArticleBody.extract_page_doi(self)
        xml_path = self.get_article_xml()
        article_headings = ParseXML.ParseXML().get_article_sections(xml_path)
        self.set_timeout(1)
        try:
            article_presented_headings = self._gets(self._article_body_headings)
        except:
            logging.info("No headings found in article " + doi)
            self.restore_timeout()
            return self
        self.restore_timeout()
        for aheading in article_presented_headings:
            presented_article_headings.append(aheading.text)
        for heading in article_headings:
            complete_article_headings.append(heading)
            assert heading in presented_article_headings
        return self

    def validate_article_metadata_headings(self):
        presented_metadata_headings = []
        doi = ArticleBody.extract_page_doi(self)
        xml_path = self.get_article_xml()
        metadata_headings = ParseXML.ParseXML().get_metadata_sections(xml_path)
        self.set_timeout(5)
        try:
            metadata_content = self._get(self._article_metadata).text
        except:
            logging.info("No metadata information present in article " + doi)
            self.restore_timeout()
            return self
        self.restore_timeout()
        for meta_term in metadata_headings:
            logging.info('Testing meta_term: ' + str(meta_term))
            free_copyright = re.search('\.*' + 'public domain' + '\.*', metadata_content)
            if meta_term == 'Copyright:' and free_copyright is not None:
                term = 'Copyright:'
                presented_metadata_headings.append(str(term))
            else:
                term = re.search('\.*' + meta_term + '\.*', metadata_content).group()
                presented_metadata_headings.append(str(term))
        for heading in metadata_headings:
            assert heading in presented_metadata_headings, \
                'Heading is not present in metadata headings : {0}'.format(heading)
        return self

    def validate_article_amendment_notice(self):
        expressed_concern_doi_dirty = ''
        retraction_doi_dirty = ''
        correction_doi_list = []
        eoc_retr_background_color = 'rgb(252, 226, 229)'
        eoc_retr_heading_color = 'rgb(229, 51, 80)'
        corr_background_color = 'rgb(239, 239, 239)'
        corr_heading_color = 'rgb(32, 32, 32)'
        doi = self.extract_page_escaped_doi()
        expression_of_concern = False
        retraction = False
        correction_count = 0
        # make a call to rhino to get the related article of types [correction-forward,
        # expressed-concern, retraction ]
        # information
        logging.info("Calling Rhino api v2 to get article amendment information...")
        response = requests.get(rhino_url + '/articles/' + doi + '/relationships')
        json_response = json.loads(response.text)
        related_articles = json_response['inbound']
        x = len(related_articles) - 1
        while x > -1:
            type_ra = json_response['inbound'][x]['type']
            if type_ra in 'object-of-concern':
                expression_of_concern = True
                expressed_concern_doi_dirty = json_response['inbound'][x]['doi']
                break
            if type_ra in 'retracted-article':
                retraction = True
                retraction_doi_dirty = json_response['inbound'][x]['doi']
                break
            if type_ra in 'corrected-article':
                correction_count += 1
                correction_doi_list.append(json_response['inbound'][x]['doi'])
            x -= 1

        if expression_of_concern:
            amendment_div = self._get(self._article_body_amendment_notice_div)
            eoc_notice = self._get(self._article_body_amendment_notice_eoc)
            assert str(amendment_div.value_of_css_property(
                "background-color")) == eoc_retr_background_color
            assert str(eoc_notice.value_of_css_property("color")) == eoc_retr_heading_color
            # For all but the doi here, I am not validating the content
            assert self._get(self._article_body_amendment_notice_text)
            assert self._get(self._article_body_amendment_notice_date)
            assert self._get(self._article_body_amendment_notice_citation)
            amendment_citation = self._get(self._article_body_amendment_notice_citation).text
            expressed_concern_doi = expressed_concern_doi_dirty[9:]
            assert expressed_concern_doi in amendment_citation
            assert self._get(self._article_body_amendment_notice_link)

        if retraction:
            # Validate that block is present
            amendment_div = self._get(self._article_body_amendment_notice_div)
            retraction_notice = self._get(self._article_body_amendment_notice_retraction)
            assert str(amendment_div.value_of_css_property(
                "background-color")) == eoc_retr_background_color
            assert str(retraction_notice.value_of_css_property("color")) == eoc_retr_heading_color
            # For all but the doi here, I am not validating the content
            assert self._get(self._article_body_amendment_notice_text)
            assert self._get(self._article_body_amendment_notice_date)
            assert self._get(self._article_body_amendment_notice_citation)
            amendment_citation = self._get(self._article_body_amendment_notice_citation).text
            retraction_doi = retraction_doi_dirty[9:]
            assert retraction_doi in amendment_citation
            assert self._get(self._article_body_amendment_notice_link)

        if correction_count > 0:
            # Validate that block is present
            amendment_div = self._get(self._article_body_amendment_notice_div)
            correction_notice = self._get(self._article_body_amendment_notice_corrections)
            assert str(
                amendment_div.value_of_css_property("background-color")) == corr_background_color
            assert str(correction_notice.value_of_css_property("color")) == corr_heading_color
            corrections_from_page = self._gets(self._article_body_amendment_notice_citation)
            count = 1
            for correction in corrections_from_page:
                self._article_body_amendment_citation = (
                    By.XPATH, '//div[@class="amendment-citation"][' + str(count) + ']')
                citation = self._get(self._article_body_amendment_citation).text
                dirty_doi_from_correction = re.sub(r'^.*doi.org\/', '', citation)
                doi_from_correction = re.sub(r' View correction.*$', '', dirty_doi_from_correction)
                self._article_body_amendment_correction_date = \
                    (By.XPATH, '//div[@class="amendment-citation"]'
                               '[' + str(count) + ']/p/span[@class="amendment-date"]')
                self._article_body_amendment_correction_link = \
                    (By.XPATH, '//div[@class="amendment-citation"]'
                               '[' + str(count) + ']/p/a[@class="amendment-link"]')
                assert self._get(self._article_body_amendment_correction_date)
                assert self._get(self._article_body_amendment_correction_link)
                assert doi_from_correction in str(correction_doi_list)
                count += 1
        return self

    def _validate_article_lightbox_call_methods(self, call_method):
        """
        Given an element of the body page, it will click on it
        and check if the modal with the image is displayed.
        After this check, will click on the close icon.
        """
        self._get(call_method).click()
        time.sleep(1)
        page_body = self._get(self._page_body)
        assert 'overflow: hidden;' in page_body.get_attribute('style')
        page_body.send_keys(keys.Keys.ESCAPE)
        return self

    def validate_article_lightbox_image(self):
        """
        Check if the image loads in a lightbox when is clicked in
        one of these events: Click on inline image, click on carousel
        and clink on nave bar image link.
        """
        try:
            self._validate_article_lightbox_call_methods(self._article_body_first_figure_inline)
            self._validate_article_lightbox_call_methods(self._article_body_figure_navbar)
            self._validate_article_lightbox_call_methods(self._article_body_figure_carousel_item)
        except ElementDoesNotExistAssertionError:
            logging.info('This test can\'t be done without images')
        return self
