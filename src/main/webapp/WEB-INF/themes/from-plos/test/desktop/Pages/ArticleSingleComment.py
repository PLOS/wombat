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
import time
from datetime import datetime

import requests
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond

from .AkitaLoginPage import AkitaLoginPage
from .Article import Article
from .ArticleComments import default_date_format
from .ArticleNewComment import ArticleNewComment
from ..resources import rhino_url
from ...Base import Utils
from ...Base.Config import base_url

__author__ = 'ivieira@plos.org'


class ArticleSingleComment(Article):
    def __init__(self, driver, url_suffix, uri):
        super(ArticleSingleComment, self).__init__(driver, url_suffix)
        self._comment_uri = uri
        self._comment_data = self.get_rhino_comment(self._comment_uri)
        self._response_section = (By.ID, 'responses')
        self._comment_not_available = (By.ID, 'comments_temp_unavailable')
        self._close_flag_modal = (By.CSS_SELECTOR, "div.flagConfirm span.close_confirm")
        self._response_container = (By.CSS_SELECTOR, '#responses .reply.subresponse')

    @staticmethod
    def _create_short_url(url):
        if len(url) > 25:
            short_url = url[0:25]
            if url[len(url) - 1] == '.':
                short_url += '.'

            return short_url + '...'

        return url

    @staticmethod
    def _clear_body_line(line):
        line = line.strip()
        line = line.replace('  ', ' ')
        line = line.replace('\t', ' ')
        return line

    def _validate_comment(self, comment_el, comment_data):
        # Validate title
        title = comment_el.find_element_by_css_selector('h3.response_title').text
        assert title == comment_data['title'], \
            'Title: {0} is not the expected: {1}'.format(title, comment_data['title'])

        # Validate creation date
        date = comment_el.find_element_by_css_selector('h4 .replyTimestamp strong').text
        formatted_date = datetime.strptime(comment_data['created'], default_date_format).strftime(
            '%d %b %Y at %H:%M GMT')
        assert date == formatted_date, \
            'Creation date: {0} is not the expected: {1}'.format(date, formatted_date)

        # Validate body
        # body = comment_el.find_element_by_css_selector('.response_body').text.encode("utf8")
        # body_parts = body.splitlines()
        # body_parts = map(self._clear_body_line, body_parts)
        # body_parts = filter(None, body_parts)
        # body_text = comment_data['body'].encode("utf8")
        # highlight_text = comment_data['highlightedText'].encode('utf8')
        # json_urls = re.findall('http[s]?://(?:[-a-zA-Z0-9@:%_+.~#?&//=]
        # |(?:%[-a-zA-Z0-9@:%_+.~#?&//=]))+', body_text)
        # print json_urls
        # json_short_urls = map(self._create_short_url, json_urls)
        # print json_short_urls
        # for key, json_url in enumerate(json_urls):
        #   body_text = body_text.replace(json_url, json_short_urls[key])

        # if highlight_text:
        #   body_text = highlight_text + '\n\n'+ body_text

        # body_text_parts = body_text.splitlines()
        # body_text_parts = map(self._clear_body_line, body_text_parts)
        # body_text_parts = filter(None, body_text_parts)

        # assert body_parts == body_text_parts, 'Comment body: "{0}" is not the expected:
        # "{1}"'.format(body_parts, body_text_parts)

        # Validate competing interest
        if self._check_for_invisible_element_boolean(
                self.is_element_present("competing_interests")) is True:
            competing_statement = comment_data['competingInterestStatement']
            competing_interest = comment_el.find_element_by_css_selector(
                '.response_content .competing_interests').text
            if competing_statement['hasCompetingInterests']:
                expected_interest = 'Competing interests declared: {0}'.format(
                    competing_statement['body'])
                assert competing_interest == expected_interest, \
                    'Competing interest: {0} is not the expected: {1}'.format(
                        competing_interest, expected_interest)
                pass
            else:
                expected_interest = 'No competing interests declared.'
                assert competing_interest == expected_interest, \
                    'Competing interest: {0} is not the expected: {1}' \
                    .format(competing_interest, expected_interest)

    def validate_comment_content(self):
        self._check_comments_availability(self._response_section)

        comment_data = self._comment_data
        comment_el = self._driver.find_element_by_css_selector(
            '.response[data-uri="{0}"]'.format(comment_data['commentUri']))
        self._validate_comment(comment_el, comment_data)
        self._validate_response_form()
        self._validate_concern_form()

    def validate_replies(self):
        self._check_comments_availability(self._response_section)
        replies = self._comment_data['replies']
        if len(replies) > 0:
            for reply_data in replies:
                reply_el = self._driver.find_element_by_css_selector(
                    '.response[data-uri="{0}"]'.format(reply_data['commentUri']))
                self._validate_comment(reply_el, reply_data)

    def _validate_response_form(self):
        if self.is_user_logged():
            self.click_sign_out()

        self.click_respond_btn()
        self._wait.until(exp_cond.url_contains('nedcas'))
        sign_in_page = AkitaLoginPage(self._driver)
        time.sleep(1)
        sign_in_page.successful_login()
        self._wait.until(exp_cond.url_contains(base_url))
        self.click_respond_btn()
        comment_data = self._comment_data
        self._wait.until(exp_cond.url_contains('annotation'))
        response_container = self._get(self._response_container)
        self._scroll_into_view(response_container)
        new_comment_page = ArticleNewComment(self._driver, comment_uri=comment_data['commentUri'])

        new_comment_page.validate_reply_header()
        new_comment_page.validate_form_fields()
        new_comment_page.validate_submitting_errors()

    def click_respond_btn(self):
        comment_data = self._comment_data
        comment_el = self._driver.find_element_by_css_selector(
            '.response[data-uri="{0}"]'.format(comment_data['commentUri']))
        respond_btn = comment_el.find_element_by_css_selector('.respond.btn')
        respond_btn.click()

    def click_concern_btn(self):
        comment_data = self._comment_data
        comment_el = self._driver.find_element_by_css_selector(
            '.response[data-uri="{0}"]'.format(comment_data['commentUri']))
        concern_btn = comment_el.find_element_by_css_selector('.flag.btn')
        self.click_on_hidden_button(concern_btn)

    def _validate_concern_form(self):
        if self.is_user_logged():
            sign_link = self._get(self._sign_in_link)
            self.click_on_hidden_button(sign_link)
            self.page_ready('sign in')

        time.sleep(1)
        self.click_concern_btn()
        time.sleep(1)
        sign_in_page = AkitaLoginPage(self._driver)
        time.sleep(1)
        sign_in_page.successful_login()
        self._wait.until(exp_cond.url_contains('annotation'))
        self.click_concern_btn()
        comment_data = self._comment_data
        container_selector = '.response[data-uri="{0}"] .report_container'.format(
            comment_data['commentUri'])
        time.sleep(1)

        concern_form_container = self._driver.find_element_by_css_selector(container_selector)
        concern_form = concern_form_container.find_element_by_css_selector('.flagForm')
        concern_form_success = concern_form_container.find_element_by_css_selector('.flagConfirm')

        assert not concern_form_success.is_displayed(), \
            'Concern form success is displayed when it should not.'
        assert concern_form.is_displayed(), 'Concern form is not displayed when it should.'

        # Validate title
        title = concern_form.find_element_by_css_selector('h4')
        expected_title = "Why should this posting be reviewed?"

        assert title.is_displayed(), 'Concern form title is not displayed.'
        assert title.text == expected_title, 'The Concern form title: {0} is not the expected: ' \
                                             '{1}'.format(title.text, expected_title)

        # Validate form submittion
        error_messages_wrapper = concern_form.find_element_by_css_selector('#responseSubmitMsg')
        missing_comment_error = error_messages_wrapper.find_element_by_css_selector(
            '.commentErrorMessage[data-error-key="missingComment"]')

        submit_btn = concern_form.find_element_by_css_selector('fieldset .btn_submit')
        additional_info_textarea = concern_form.find_element_by_css_selector(
            'fieldset textarea[name="additional_info"]')

        submit_btn.click()
        self.wait_until_ajax_complete()
        self._wait.until(exp_cond.visibility_of(error_messages_wrapper))
        assert missing_comment_error.is_displayed(), 'Missing concern comment error message ' \
                                                     'is not displayed.'

        additional_info_textarea.send_keys('Integration test team testing...')
        submit_btn.click()
        self.wait_until_ajax_complete()
        assert concern_form_success.is_displayed(), 'Concern form success is not displayed ' \
                                                    'when it should.'
        close_btn = concern_form_container \
            .find_element_by_css_selector('div.flagConfirm span.close_confirm')
        self.click_on_hidden_button(close_btn)
        self.close_floating_title_top()

    def click_on_hidden_button(self, hidden_btn):
        self._scroll_into_view(hidden_btn)
        self.scroll_by_pixels(-120)
        for i in range(10):
            try:
                hidden_btn.click()
                break
            except:
                self.scroll_by_pixels(-120)
                time.sleep(1)
                logging.warning('the button is not clickable, moving up by 120 pixels, '
                                'step {0!s}'.format(i))

    @staticmethod
    def get_rhino_comment(uri):
        escaped_uri = Utils.escape_doi(uri)
        endpoint = '/comments/{0}'.format(escaped_uri)
        response = requests.get(rhino_url + endpoint)
        comment = json.loads(response.text)

        return comment

    @staticmethod
    def remove_all_flags_from_annotation(rhino_host, annotation_uri, article_doi):
        """
        Remove all flags from an annotation.
        :param rhino_host: Rhino host
        :param annotation_uri: Annotation to be updated.
        :param article_doi:
        :return: None
        """

        annotation_uri = Utils.escape_doi(annotation_uri)
        article_doi = Utils.escape_doi(article_doi)
        url = '{0}/{1}/{2}/{3}/{4}/{5}' \
            .format(rhino_host, 'articles', article_doi, 'comments', annotation_uri, 'flags')
        response = requests.delete(url)
        if not response.ok:
            msg = '/flags DELETE request for {0} on {1}, status code of {2}: {3}' \
                .format(url, rhino_host, str(response.status_code), response.text)
            logging.error(msg)
        else:
            logging.info('Flags for {0} successfully removed!'.format(annotation_uri))
