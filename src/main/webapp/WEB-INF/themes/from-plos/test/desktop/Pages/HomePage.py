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

import datetime
import json
import logging
import requests
import time

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond

from ...Base.Config import solr_url, base_url
from ...Base import Utils
from .. import resources

from .WombatPage import WombatPage

__author__ = 'jgray@plos.org'


class HomePage(WombatPage):
    """
    Model an abstract base Journal page.
    """

    def __init__(self, driver, url_suffix=''):
        super(HomePage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._hero_div = (By.ID, "hero")
        self._tier1_block1 = (By.ID, "cellcat-1-1")
        self._tier1_block2 = (By.ID, "cellcat-1-2")
        self._tier1_block3 = (By.ID, "cellcat-1-3")
        self._tier2_block1 = (By.ID, "cellcat2-1")
        self._tier2_block2 = (By.ID, "cellcat2-2")
        self._tier2_block3 = (By.ID, "cellcat2-3")
        self._tier3_block1 = (By.ID, "cellcat3-1")
        self._tier3_block2 = (By.ID, "cellcat3-2")
        self._tier3_block3 = (By.ID, "cellcat3-3")
        self._tier3_block4 = (By.ID, "cellcat3-4")
        self._recent_article_div = (By.ID, "article-list")
        self._recent_article_div_header = (By.CSS_SELECTOR, "#article-list h3")
        self._recent_article_div_list = (By.XPATH, '//div[@id=\'article-list\']/section/ul/li')
        self._recent_article_div_link = \
            (By.XPATH, '//div[@id=\'article-list\']/div[@class=\'more-link\']/a')
        self._article_results = (By.ID, "article-results")
        self._journal_title = (By.CSS_SELECTOR, "#pagehdr .logo a")
        self._search_form = (By.ID, 'controlBarSearch')
        self._filter_item_date = (By.ID, 'filter-date')
        self._filter_item_journal = (By.XPATH, "(//div[@class='filter-item'])[2]")
        # Twitter locators
        self._twitter = (By.ID, 'twitter-widget-0')
        self._twitter_header = (By.CSS_SELECTOR, 'h1.timeline-Header-title')
        self._twitter_list = (By.CSS_SELECTOR, 'ol.timeline-TweetList >li')
        # Blog section locators
        self._blogs = (By.ID, 'blogs')
        self._blogs_header = (By.CSS_SELECTOR, 'div#blogs > div.block-header > a')  # div#blogs a
        self._see_all_blogs_link = (By.ID, 'blogLink')
        # Carousel section locators
        self._carousels = (By.CSS_SELECTOR, 'article[id^=carousel-]')
        self._jcarousel_component = (By.CLASS_NAME, 'jcarousel')
        self._block_header = (By.CLASS_NAME, 'block-header')
        self._carousel_control = (By.CLASS_NAME, 'carousel-control')  #
        self._jcarousel_prev = (By.CSS_SELECTOR, 'a.jcarousel-prev')
        self._jcarousel_next = (By.CSS_SELECTOR, 'a.jcarousel-next')
        self._carousel_current_item = \
            (By.CSS_SELECTOR, '.number[data-js=\'carousel-current-item\']')
        self._carousel_total_index = (By.CSS_SELECTOR, '.number[data-js=\'carousel-total-index\']')
        self._carousel_items = (By.CSS_SELECTOR, 'ul > li > h3 > a')
        # 'Connect with Us' section locators
        self._connect_with_us = (By.CSS_SELECTOR, 'div#social-links > h3')
        self._social_links_list = (By.CSS_SELECTOR, 'li[id^=social-link] > a')
        # 'Current issue' section locators
        self._current_issue = (By.ID, 'issue')
        self._current_issue_img = (By.CSS_SELECTOR, 'img[alt=\'Current Issue\']')
        self._current_issue_link = (By.CSS_SELECTOR, '#issue > p.boxtitle > a')
        self._current_issue_subhead = (By.CSS_SELECTOR, '#issue > p.boxtitle > span.subhead')
        # 'Publish with PLOS' section locators
        self._submission_links = (By.ID, 'submission-links')
        self._publish_with_plos = (By.CSS_SELECTOR, '#submission-links > h3')
        self._submission_instructions = (By.ID, 'submissionInstruct')
        self._submit_your_manuscript = (By.ID, 'submissionManu')
        self._submit_now = (By.CSS_SELECTOR, 'article h1')
        self._issue_article_section = (By.CSS_SELECTOR, 'main article')

        # POM Actions

    def validate_hero(self):
        self._get(self._hero_div)
        return self

    def validate_tier1(self):
        self._get(self._tier1_block1)
        self._get(self._tier1_block2)
        self._get(self._tier1_block3)
        return self

    def validate_tier2(self):
        self._get(self._tier2_block1)
        self._get(self._tier2_block2)
        self._get(self._tier2_block3)
        return self

    def validate_tier3(self):
        self._get(self._tier3_block1)
        self._get(self._tier3_block2)
        self._get(self._tier3_block3)
        self._get(self._tier3_block4)
        return self

    def validate_recent_article_div(self):
        journal_title = self._get(self._journal_title).text
        # Validate Div heading Link
        self._get(self._recent_article_div)

        recent_article_div_header = self._get(self._recent_article_div_header)
        assert 'Recently Published Articles'.upper() == recent_article_div_header.text, \
            '{0} Recently Published Article Heading not present in div block'.format(journal_title)

        # Validate number of displayed articles
        recent_article_div_list = self._gets(self._recent_article_div_list)
        assert len(recent_article_div_list) == 3, \
            '{0} Less than three recent articles displayed'.format(journal_title)

        journal = Utils.get_first_dict_by_value(resources.sevenjournals, 'journalTitle',
                                                journal_title[5:])
        # get the recent article list from Solr
        article_list = self.retrieve_recent_articles_from_solr(str(journal['rhinoJournalKey']))
        # validate that the articles displayed in the div exist in the list returned by rhino
        articles_on_homepage = self._get(self._article_results).find_elements_by_tag_name('a')
        for art in articles_on_homepage:
            full_url = art.get_attribute('href')
            full_url_parts = full_url.split('id=')
            doi = full_url_parts[1]
            # TODO: Debug RARO-203
            assert Utils.get_first_dict_by_value(article_list, 'id', doi) is not None, \
                'The recent article displayed does not match rhino in {0}'.format(journal_title)

        # Validate See All Articles Link
        see_all_link = self._get(self._recent_article_div_link)
        assert see_all_link.text.upper() == 'SEE ALL ARTICLES', \
            'Wrong link text in Recent Articles div'
        # Validate that clicking on the link takes you to a search result page
        see_all_link.click()
        self._get(self._search_form)
        # Validate that the filters are set correctly
        today = datetime.date.today()
        one_month_ago = today - datetime.timedelta(days=30)
        filter_criteria = '{0} TO {1}'\
            .format(one_month_ago.strftime('%b %d, %Y'), today.strftime('%b %d, %Y'))

        filter_item_date = self._get(self._filter_item_date)
        filter_item_journal = self._get(self._filter_item_journal)
        assert filter_criteria.strip().replace(" 0", " ") == str(filter_item_date.text.strip()), \
            '{0} All Article Query Incorrect: Incorrect Date Criteria '.format(journal_title)
        assert str(journal['journalTitle']) in filter_item_journal.text, \
            '{0} All Article Query Incorrect: Incorrect Journal Criteria '.format(journal_title)

    def retrieve_recent_articles_from_solr(self, journal):
        if journal.upper() == 'PLOSMEDICINE':
            facet_url = "?q=*:*&wt=json&indent=true&fq=doc_type:full&fq=journal_key:" + journal + \
                        "&rows=1000&hl=false&facet=false&fl=id,title,title_display," \
                        "journal_name,author_display&sort=publication_date+desc,id+desc"
        else:
            # TODO: check article type filter for specific journals, check order (homepage.yaml)
            facet_url = "?q=*:*&wt=json&indent=true&fq=doc_type:full" \
                        "&fq=!article_type_facet:%22Issue+Image%22&fq=journal_key:" + journal + \
                        "&rows=1000&hl=false&facet=false&fl=id,title,title_display,journal_name," \
                        "author_display&sort=publication_date+desc,id+desc"

        response = requests.get(solr_url + facet_url, params=None)
        response = json.loads(response.text)

        return response['response']['docs']

    def get_journal_info(self):
        """
        The method to get journal information from resourses, using journal title from the page
        :return: journal: dictionary, journal_name: string
        """
        journal_title = self._get(self._journal_title).text
        journal = Utils.get_first_dict_by_value(
                resources.sevenjournals, 'journalTitle', journal_title[5:].title())
        journal_name = 'PLOS {0!s}'.format(journal['journalTitle'])

        return journal, journal_name

    def get_carousel_items(self, carousel):
        """
        The function to get carousel item numbers, total number of items and carousel web element
        to control items
        :param carousel: parent carousel wrap web element to find carousel item numbers controls
        :return: item_numbers: list of carousel item numbers,
                 total_index: total number of items,
                 carousel_control: parent control web element to get item numbers controls
        """
        jcarousel_component = carousel.find_element(*self._jcarousel_component)
        carousel_control = carousel.find_element(*self._carousel_control)
        total_index = int(carousel_control.find_element(*self._carousel_total_index).text)
        item_numbers = jcarousel_component.find_elements(*self._carousel_items)

        return item_numbers, total_index, carousel_control

    def get_carousel_next_item(self, carousel_control, direction):
        """
        The function to get next or previous carousel item by clicking on '>' or '<' carousel links,
        depending on specified direction
        :param carousel_control: parent control web element to get item numbers controls
        :param direction: string to specify direction, possible values: 'next', 'previous'
        :return: current_index: current item index, int
        """
        button_to_click = self._jcarousel_next if direction == 'next' else self._jcarousel_prev
        next_item = carousel_control.find_element(*button_to_click)
        next_item.click()
        time.sleep(1)
        current_index = int(carousel_control.find_element(*self._carousel_current_item).text)

        return current_index

    def get_social_links(self):
        social_links_list = self._gets(self._social_links_list)
        return social_links_list

    def _validate_social_links(self, actual_links, expected_links):
        # Validations
        for link in actual_links:
            self._wait_for_element(link)
            link_text = link.text.strip()
            # logging.info('Verifying link "{0}"'.format(link_text))
            assert link_text in expected_links, \
                "'{0}' was not among '{1}'".format(link_text, expected_links)
            # logging.info("PRESENT /", )
            assert link.get_attribute('href') == expected_links[link_text], \
                "'{0}' != '{1}'".format(link.get_attribute('href'), expected_links[link_text])
            # logging.info("HREF OK /", )
            # TODO: remove 'if' when (if) LMR-2306 gets resolved
            if link_text not in ('Guidelines for Editors', 'Find and Read Articles'):
                assert self._is_link_valid(link) is True

    def click_on_social_link_new_window(self, idx, page_title):
        """
        The method to click on specific link in 'Connect with us' section that opens new
        window with required resourse, then go back to home page
        :param idx: index of specific link in the link list, int
        :param page_title: page title, or its part, to use it to wait for page loading, string
        :return: current_url: url of visited social page to assert, string
                 link_text: link text, string
        """
        actual_social_links = self.get_social_links()
        social_link = actual_social_links[idx]  # Twitter
        link_text = social_link.get_attribute('title').strip()
        social_link.click()
        current_url = self.open_page_in_new_window(
                page_title=page_title, original_url=base_url)
        return current_url, link_text

    def click_on_social_link_same_window(self, idx, page_title):
        """
        The method to click on specific link in 'Connect with us' section that opens required
        resourse in the same window, then go back to home page
        :param idx: index of specific link in the link list, int
        :param page_title: page title, or its part, to use it to wait for page loading, string
        :return: current_url: url of visited social page to assert, string
                 link_text: link text, string
        """
        actual_social_links = self.get_social_links()
        social_link = actual_social_links[idx]  # Blogs
        link_text = social_link.get_attribute('title').strip()
        social_link.click()
        self._wait.until(exp_cond.title_contains(page_title))
        current_url = self.get_current_url()
        self._driver.back()
        self._wait.until(exp_cond.url_contains(base_url))
        return current_url, link_text

    def get_current_issue_keys(self, journal):
        """
        The method to find key dates of current issue for specific journal with rhino request
        :param journal: journal - one of the sevenjournals from resourses.py, dictionary
        :return: cIMonth and cIYear from the response, strings
        """
        delimiter = '?' if journal['journalKey'] == 'One' else '/'
        request_text = '{0}/journals/{1}{2}currentIssue' \
            .format(resources.rhino_url, journal['rhinoJournalKey'], delimiter)

        logging.info(request_text)
        response = requests.get(request_text)
        issue_data = json.loads(response.text)
        data_parent = (issue_data['parentVolume'])
        c_i_month = '2004 \u2013' if journal['journalKey'] == 'Medicine' \
            else issue_data['displayName']
        c_i_year = data_parent['displayName']

        return c_i_month, c_i_year
