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
import time
from datetime import datetime

import requests
from selenium.webdriver.common.by import By

from ..Pages.AkitaLoginPage import AkitaLoginPage
from ..Pages.Article import Article
from ..Pages.ArticleNewComment import ArticleNewComment
from ..resources import rhino_url

__author__ = 'ivieira@plos.org'
default_date_format = '%Y-%m-%dT%H:%M:%S.%fZ'


class ArticleComments(Article):
    def __init__(self, driver, url_suffix):
        super(ArticleComments, self).__init__(driver, url_suffix)
        self._article_title_element = (By.CSS_SELECTOR, '.article-container h2')
        self._article_comment_item = (By.CSS_SELECTOR, '.article-container #threads li.cf')

    def get_comments_title(self):
        title_text = self._get(self._article_title_element).text
        title_part = title_text.split(' (')
        return title_part[0]

    def get_page_comments_count(self):
        title_text = self._get(self._article_title_element).text
        title_part = title_text.split(' (')
        title_count = int(title_part[1].replace(')', ''))
        return title_count

    def validate_comments(self):
        self._check_comments_availability(self._comments_thread)

        doi = self.extract_page_escaped_doi()
        comments = self.get_rhino_comments(doi)

        if len(comments) > 0:
            comments_el = self._gets(self._article_comment_item)
            for key, comment_el in enumerate(comments_el):
                comment = comments[key]

                # Assert replies count
                replies_count = str(comment['replyTreeSize'])
                replies_count_el_text = comment_el.find_element_by_css_selector(
                    '.responses span').text.strip()

                assert replies_count_el_text == replies_count, \
                    'Replies count: {0} is not the expected: {1}'\
                    .format(replies_count_el_text, replies_count)

                # Assert title
                title = comment['title'].strip()
                title_el_text = comment_el.find_element_by_css_selector('.title a').text.strip()

                assert title_el_text == title, \
                    'Comment title: "{0}" is not the expected: "{1}"'.format(title_el_text, title)

                # Assert most recent date
                most_recent_date = datetime.strptime(comment['mostRecentActivity'],
                                                     default_date_format).strftime(
                    '%d %b %Y %H:%M GMT')
                most_recent_date_el = comment_el.find_element_by_css_selector('.recent')
                most_recent_date_el_text = most_recent_date_el.text.replace('\n', ' ').replace(
                    ' MOST RECENT', '').strip()

                assert most_recent_date_el_text == most_recent_date, \
                    'Comment most recent: {0} is not the expected: {1}'\
                    .format(most_recent_date_el_text, most_recent_date)

                # Assert creation date
                creation_date = datetime.strptime(comment['created'], default_date_format)\
                    .strftime('%d %b %Y at %H:%M GMT')
                creation_date_el = comment_el.find_element_by_css_selector('.title span')
                creation_date_el_part = \
                    creation_date_el.text.replace('\n', ' ').strip().split(' on ')
                creation_date_el_text = creation_date_el_part[1]

                assert creation_date_el_text == creation_date, \
                    'Comment creation date: {0} is not the expected: {1}'\
                    .format(creation_date_el_text, creation_date)

    def validate_new_comment(self, page_path):
        if self.is_user_logged():
            self.click_sign_out()
        self._check_comments_availability(self._comments_thread)
        new_comment_button = self._driver.find_element_by_css_selector('#thread .post_comment a')
        new_comment_button.click()
        sign_in_page = AkitaLoginPage(self._driver)
        time.sleep(1)
        sign_in_page.successful_login()
        new_comment_page = ArticleNewComment(self._driver, page_path)
        comment_container = self._get(self._comment_container)
        self._scroll_into_view(comment_container)
        new_comment_page.validate_page_header()
        new_comment_page.validate_reply_header()
        new_comment_page.validate_form_fields()
        new_comment_page.validate_submitting_errors()

    @staticmethod
    def get_rhino_comments(doi):
        endpoint = '/articles/{0}/comments'.format(doi)
        response = requests.get(rhino_url + endpoint)
        comments = json.loads(response.text)
        # Sort comments by most recent activity timestamp (as in the Front End)
        comments.sort(
            key=lambda x: time.mktime(time.strptime(x['mostRecentActivity'], default_date_format)),
            reverse=True)

        return comments

    @staticmethod
    def filtered_comments(comments):
        """ Filter removed comments"""
        comments = list(filter(lambda item: item['isRemoved'] is False, comments))
        return comments
