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


from selenium.webdriver.support import expected_conditions as exp_cond
from selenium.webdriver.common.by import By

from ..Pages.Article import Article

__author__ = 'ivieira@plos.org'


class ArticleNewComment(Article):
    def __init__(self, driver, url_suffix='', comment_uri=None):
        super(ArticleNewComment, self).__init__(driver, url_suffix)
        self._new_comment_content = (By.CLASS_NAME, 'reply_content')
        if comment_uri:
            self._form_container = self._driver.find_element_by_css_selector(
                    '.response[data-uri="{0}"] .respond_container'.format(comment_uri))
        else:
            self._form_container = self._driver.find_element_by_css_selector('#thread')

    def validate_page_header(self):
        header = self._form_container.find_element_by_css_selector('h2')
        expected_text = 'Start a Discussion'

        assert header.is_displayed(), 'The header is not visible'
        assert header.text == expected_text, 'The header text: {0} is not the expected: {1}'\
            .format(header.text, expected_text)

    def validate_reply_header(self):
        reply_header = self._form_container.find_element_by_css_selector('.reply h4')
        expected_text = 'Post Your Discussion Comment'

        self._wait.until(exp_cond.visibility_of(reply_header))
        assert reply_header.is_displayed(), 'The reply header is not visible'
        assert reply_header.text == expected_text, \
            'The reply header text: {0} is not the expected: {1}'\
            .format(reply_header.text, expected_text)

    def validate_form_fields(self):
        # Validate title field
        comment_title_field = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #comment_title')
        assert comment_title_field.is_displayed(), 'The comment title field is not visible'

        # Validate comment field
        comment_body_field = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #comment')
        assert comment_body_field.is_displayed(), 'The comment body field is not visible'

        # Validate no competing radio input
        no_competing_radio_text = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset label[for="no_competing"]').text
        no_competing_radio_expected_text = "No, I don't have any competing interests to declare"
        if self._is_competing_interest_active():
            self._deactivate_competing_interest()

        competing_interests_textarea = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset .competing_text #competing_interests')

        assert no_competing_radio_text == no_competing_radio_expected_text, \
            'The no competing radio text: {0} is not the expected: {1}'.format(
                    no_competing_radio_text, no_competing_radio_expected_text)
        assert not competing_interests_textarea.is_enabled(), \
            'The competing area textare is enabled when should not.'

        # Validate yes competing radio input
        yes_competing_radio_text = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset label[for="yes_competing"]').text
        yes_competing_radio_expected_text = \
            "Yes, I have competing interests to declare (enter below):"
        if not self._is_competing_interest_active():
            self._activate_competing_interest()

        assert yes_competing_radio_text == yes_competing_radio_expected_text, \
            'The yes competing radio text: {0} is not the expected: {1}'\
            .format(yes_competing_radio_text, yes_competing_radio_expected_text)
        assert competing_interests_textarea.is_enabled(), \
            'The competing area textare is not enabled when should be.'

    def validate_submitting_errors(self):
        # Fields
        comment_title_field = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #comment_title')
        competing_interests_textarea = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset .competing_text #competing_interests')

        submit_button = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset .btn_submit')

        self._deactivate_competing_interest()
        # Validation with all fields empty
        submit_button.click()
        self._validate_displayed_messages()

        # Validation with title filled
        comment_title_field.send_keys('Test')
        submit_button.click()
        self._validate_displayed_messages()

        # Validation with title and body filled, CI null but activated
        self._activate_competing_interest()
        submit_button.click()
        self._validate_displayed_messages()

        # Validation with title, body and CI filled
        competing_interests_textarea.send_keys('Test')
        submit_button.click()
        self._validate_displayed_messages()

    def _validate_displayed_messages(self):
        # Error messages
        self.wait_until_ajax_complete()
        error_messages_wrapper = self._form_container.find_element_by_css_selector(
                '.reply #responseSubmitMsg')
        missing_title_error = error_messages_wrapper.find_element_by_css_selector(
                '.commentErrorMessage[data-error-key="missingTitle"]')
        missing_body_error = error_messages_wrapper.find_element_by_css_selector(
                '.commentErrorMessage[data-error-key="missingBody"]')
        missing_ci_error = error_messages_wrapper.find_element_by_css_selector(
                '.commentErrorMessage[data-error-key="missingCi"]')

        # Fields
        comment_title_field = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #comment_title')
        comment_body_field = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #comment')
        competing_interests_textarea = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset .competing_text #competing_interests')

        # Values
        comment_title_value = comment_title_field.get_attribute('value')
        comment_body_value = comment_body_field.get_attribute('value')
        competing_interests_value = competing_interests_textarea.get_attribute('value')

        self._wait.until(exp_cond.visibility_of(error_messages_wrapper))
        if not comment_title_value:
            assert missing_title_error.is_displayed(), \
                'The missing title error message is not displayed with empty title.'
        if not comment_body_value:
            assert missing_body_error.is_displayed(), \
                'The missing body error message is not displayed with empty body.'
        if self._is_competing_interest_active() and not competing_interests_value:
            assert missing_ci_error.is_displayed(), \
                'The missing CI error message is not displayed with empty CI and CI activated.'
        if not self._is_competing_interest_active():
            assert not missing_ci_error.is_displayed(), \
                'The missing CI error message is displayed with and CI deactivated.'

    def _is_competing_interest_active(self):
        no_competing_radio = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #no_competing')
        return not no_competing_radio.is_selected()

    def _activate_competing_interest(self):
        yes_competing_radio = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #yes_competing')
        yes_competing_radio.click()

    def _deactivate_competing_interest(self):
        no_competing_radio = self._form_container.find_element_by_css_selector(
                '.reply .cf fieldset #no_competing')
        no_competing_radio.click()

    def page_ready(self):
        self._wait_for_element(self._get(self._new_comment_content))
