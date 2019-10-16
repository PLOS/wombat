#!/usr/bin/env python
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

from datetime import datetime
import json
import random
import requests

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond

from ...Base import Config
from .MenuManuscript import MenuManuscript
from .WombatPage import WombatPage

__author__ = 'stower@plos.org'

subject_area_terms = \
    [
        'biology_and_life_sciences',
        'women\'s_health',
        'neural_networks',
        'historical_linguistics',
        'medicine_and_health_sciences',
        'fur',
        'cell_biology',
        ''
    ]

url_suffix = '/DesktopPlosOne/browse/'


class PlosOneSubjectAreaLandingPage(WombatPage):
    """
    Model the PLoS One Subject Area Landing page.
    """

    PROD_URL = 'http://www.journals.plos.org/plosone'

    if Config.environment == 'prod':
        CURRENT_URL = PROD_URL + '/browse/'
    else:
        CURRENT_URL = Config.base_url + url_suffix

    solr_url = Config.solr_url

    def __init__(self, driver):
        super(PlosOneSubjectAreaLandingPage, self).__init__(driver, url_suffix + random.choice(
                subject_area_terms))

        # POM - Instance members
        self._menu = MenuManuscript(driver)

        # Locators - Instance members
        self._subject_area_menu = (By.CLASS_NAME, 'subject-area')
        self._subject_area_filter_bar = (By.CLASS_NAME, 'filter-bar')
        self._related_content_dropdown_arrow = (By.CLASS_NAME, 'first')
        self._dropdown_list = (By.CSS_SELECTOR, 'li.first.over')
        self._rss_link = (By.ID, 'browseRssFeedButton')
        self._cover_page_link = (By.ID, 'cover-page-link')
        self._list_articles_link = (By.ID, 'list-page-link')
        self._recent_popular_inactive = (By.CSS_SELECTOR, 'div.sidebar a')
        self._recent_link = (By.LINK_TEXT, 'Recent')
        self._popular_link = (By.CSS_SELECTOR, '.sidebar > .sort > a')
        self._subject_cover_view = (By.ID, 'subject-cover-view')
        self._subject_list_view = (By.ID, 'subject-list-view')
        self._article_link = (By.ID, 'articleURL')
        self._search_results_list = (By.ID, 'search-results')
        self._number_of_results = (By.CLASS_NAME, 'count')
        self._listview_button = (By.CSS_SELECTOR, '.hdr-results .sort #list-page-link')
        self._items = (By.CSS_SELECTOR, '#subject-list-view #search-results li')
        self._alm_data = (By.CLASS_NAME, 'search-results-alm')
        self._alm_loading = (By.CLASS_NAME, 'search-results-alm-loading')
        self._alm_wrapper = (By.CLASS_NAME, 'search-results-alm-container')
        self._date = (By.CSS_SELECTOR, 'p.date')
        self._authors = (By.CSS_SELECTOR, '.authors span.author')
        self._title = (By.CSS_SELECTOR, 'h2 a')
        self._a_tag = (By.TAG_NAME, 'a')
        self._span_tag = (By.TAG_NAME, 'span')
        self._li_tag = (By.TAG_NAME, 'li')
        self._h1_tag = (By.TAG_NAME, 'h1')

    # POM Actions
    def click_cover_page_link(self):
        cover_page_link = self._get(self._cover_page_link)
        cover_page_link.click()
        return self

    def click_list_articles_link(self):
        list_article_link = self._get(self._list_articles_link)
        list_article_link.click()
        return self

    def click_recent_link(self):
        recent_link = self._get(self._recent_popular_inactive)
        recent_link.click()
        return self

    def click_popular_link(self):
        popular_link = self._get(self._recent_popular_inactive)
        popular_link.click()
        return self

    def click_article_link(self):
        article_link = self._get(self._article_link)
        article_link.click()
        return self

    def click_search_results_list(self):
        search_results_list = self._get(self._search_results_list)
        search_results_list.click()
        return self

    def validate_article_count(self):
        self.validate_total_num_returned()
        count_text = self._get(self._number_of_results).text
        total_num = int(count_text.split()[-1].replace(',', ''))
        total_num_solr = self.retrieve_total_num_results_solr()
        assert total_num == total_num_solr, \
            'Total results on page: {0} does not match with solr total: {1}' \
            .format(total_num, total_num_solr)

    def validate_total_num_returned(self):
        total_num_results_text = self._get(self._number_of_results).text
        total_num_results = self.retrieve_total_num_results_solr()
        if total_num_results < 13:
            expected_total_num_results_text = 'Showing 1 - {0} of {0}'.format(total_num_results)
        else:
            expected_total_num_results_text = 'Showing 1 - {} of {:,}'.format(13, total_num_results)
        assert total_num_results_text == expected_total_num_results_text, \
            'Total results number text is: {0!r}, Expected: {1!r}' \
            .format(total_num_results_text, expected_total_num_results_text)

    def validate_subject_area(self):
        self._get(self._subject_area_menu)
        return self

    def retrieve_subject_area_name_on_page(self):
        subject_area_filter_bar = self._get(self._subject_area_filter_bar)
        subject_area_name = subject_area_filter_bar.find_element(*self._h1_tag)
        return subject_area_name

    def retrieve_total_num_results_solr(self):
        """
        retrieves the total number of results found from the solr query that is run by the subject
        area landing page and returns that value.
        """
        subject_area_name = self.retrieve_subject_area_name_on_page()
        if subject_area_name.text != 'All Subject Areas':
            subject_area = '+'.join(subject_area_name.text.lower().split())
            query_string = '?q=*:*&hl=false&fl=id,eissn,publication_date,title,' \
                           'journal_name,author_display,article_type,counter_total_all,' \
                           'alm_scopusCiteCount,alm_citeulikeCount,alm_mendeleyCount,' \
                           'alm_twitterCount,alm_facebookCount,retraction,expression_of_concern,' \
                           'striking_image,figure_table_caption&fq=doc_type:full&fq=!article_type' \
                           '_facet:"Issue+Image"&fq=journal_key:PLoSONE&fq=subject:"{0}"' \
                           '&sort=publication_date+desc,id+desc&rows=13' \
                           '&wt=json&facet=false'.format(subject_area)
        else:
            query_string = '?q=*:*&hl=false&fl=id,eissn,publication_date,title,journal_name,' \
                           'author_display,article_type,counter_total_all,alm_scopusCiteCount,' \
                           'alm_citeulikeCount,alm_mendeleyCount,alm_twitterCount,' \
                           'alm_facebookCount,retraction,expression_of_concern,striking_image,' \
                           'figure_table_caption&fq=doc_type:full&fq=!article_type_facet' \
                           ':"Issue+Image"&fq=journal_key:PLoSONE&sort=publication_date+desc,' \
                           'id+desc&rows=13&wt=json&facet=false'
        response = requests.get(self.solr_url + query_string)
        json_response = json.loads(response.text)
        total_num_results = json_response['response']['numFound']
        return total_num_results

    def open_related_content_dropdown(self):
        related_content_dropdown_arrow = self._get(self._related_content_dropdown_arrow)
        self._actions.move_to_element(related_content_dropdown_arrow) \
            .click_and_hold(related_content_dropdown_arrow).perform()

    def validate_related_content_dropdown(self):
        """
        This validates that the subject area in the black "filter bar" matches the bold link in the
        related content dropdown to the left (checks for class == here).
        """
        # Get the subject area name from the black filter bar
        subject_area_name = self.retrieve_subject_area_name_on_page()
        # Open the dropdown and get all the links
        self.open_related_content_dropdown()
        self._wait_for_element(self._get(self._dropdown_list))
        related_content_dropdown_arrow = self._get(self._related_content_dropdown_arrow)
        related_content_links = related_content_dropdown_arrow.find_elements(*self._li_tag)
        # Get the link element that has a class of "here" and extract its text.
        # Verify that it is equal to the subject area name in the black filter bar.
        for link in related_content_links:
            if link.get_attribute('class') == 'here':
                link_anchor = link.find_element(*self._a_tag)
                break
        current_subject_area = link_anchor.text
        assert subject_area_name.text == current_subject_area, \
            'Subject Area: {0} != Current Subject Area link: {1}' \
            .format(subject_area_name.text, current_subject_area)
        # Verify that the "you-are-here" icon is next to the currently selected subject area
        # in the dropdown
        you_are_here_icon = link_anchor.find_element(*self._span_tag)
        you_are_here_icon_image = you_are_here_icon.value_of_css_property("background-image")
        assert '/resource/img/you-are-here.png' in you_are_here_icon_image, \
            '"You are here" icon is not placed next to current subject area in dropdown'

        self._actions.move_to_element_with_offset(related_content_dropdown_arrow,120,0)\
            .perform()


    def validate_rss_link(self):
        rss_link = self._get(self._rss_link)
        self._is_link_valid(rss_link.find_element(*self._a_tag))

    def validate_cover_page_button_active(self):
        cover_page_link = self._get(self._cover_page_link)
        # If cover page button is active, then the results should be tiles with cover pages.
        self._get(self._subject_cover_view)
        assert cover_page_link.get_attribute(
                "class") == 'active', 'cover page link is not active after clicking on popular'
        background_color = cover_page_link.value_of_css_property("background-color")
        assert background_color in ('rgba(215, 223, 35, 1)', 'rgb(215, 223, 35)'), \
            'Background color for cover page button is {0!r}, expected: ' \
            'rgb(215, 223, 35) or rgba(215, 223, 35, 1)'.format(background_color) # lime

    def validate_list_articles_button_active(self):
        list_articles_link = self._get(self._list_articles_link)
        # If list articles button is active, then the results should be line items
        self._get(self._subject_list_view)
        assert list_articles_link.get_attribute(
                "class") == 'active', 'cover page link is not active after clicking on popular'
        background_color = list_articles_link.value_of_css_property("background-color")
        assert background_color in ('rgba(215, 223, 35, 1)', 'rgb(215, 223, 35)'), \
            'Background color for cover page button is {0!r}, expected: ' \
            'rgb(215, 223, 35) or rgba(215, 223, 35, 1)'.format(background_color)  # lime

    def validate_view_by_buttons(self):
        """
        Verifies the active state of the buttons. This should be independent of the active state
        of the Sort By buttons. E.g. If cover page is active, then selecting recent or popular
        should not change this state.
        """
        # Validate state of cover page button:
        self.click_cover_page_link()
        self.click_popular_link()
        self.validate_cover_page_button_active()
        self.click_recent_link()
        self.validate_cover_page_button_active()

        # Validate state of list articles button:
        self.click_list_articles_link()
        self.click_popular_link()
        self.validate_list_articles_button_active()
        self.click_recent_link()
        self.validate_list_articles_button_active()

    def validate_listview_items(self):
        listview_button = self._get(self._listview_button)
        listview_button.click()

        items = self._gets(self._items)
        for item in items:
            item_doi = item.get_attribute('data-doi')

            # Validate title presence
            title = item.find_element(*self._title)

            # checking status code for the link, if 404 - ingesting the article
            code = self._get_link_status_code(title)
            if code == 404:
                article_doi_text = item_doi.replace('/', '++')
                self.fetch_and_ingest_article(article_doi_text)
                self.delete_zip(article_doi_text)

            assert self._is_link_valid(title), 'Item {0} title link is invalid.'.format(item_doi)
            assert title.text, 'Item {0} title has no text.'.format(item_doi)
            assert title.is_displayed(), 'Item {0} title is not visible'.format(item_doi)

            # Validate authors
            authors = item.find_elements(*self._authors)
            for key, author in enumerate(authors):
                assert author.text, "Author {0} in item {1} has no text.".format(str(key), item_doi)
                assert author.is_displayed(), "Author {0} in item {1} is not visible." \
                    .format(str(key), item_doi)

            # Validate date
            date_attr = item.get_attribute('data-pdate')
            default_date_format = '%Y-%m-%dT%H:%M:%SZ'
            expected_date = datetime.strptime(date_attr, default_date_format).strftime(
                    'published %d %b %Y')
            date = item.find_element(*self._date)
            assert date.is_displayed(), 'Item {0} date is not visible'.format(item_doi)
            assert date.text == expected_date, 'Item {0} date: {1} is not the expected: {2}' \
                .format(item_doi, date.text, expected_date)

            # Validate ALM data
            alm_wrapper = item.find_element(*self._alm_wrapper)
            alm_loading = alm_wrapper.find_element(*self._alm_loading)
            if alm_loading.is_displayed():
                self._wait.until(exp_cond.visibility_of_element_located(
                        alm_wrapper.find_element(*self._alm_data)))

            alm_data = alm_wrapper.find_element(*self._alm_data)
            assert alm_data.is_displayed(), 'Item {0} alm data is not visible'.format(item_doi)
            assert alm_data.text, 'Item {0} alm data has no text'.format(item_doi)
