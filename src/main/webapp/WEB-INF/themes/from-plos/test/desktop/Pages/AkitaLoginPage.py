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
"""
Page Object Model for the Akita Login page
"""

import logging

from selenium.webdriver.common.by import By

from ...Base.PlosPage import PlosPage
from ...Base import Config

__author__ = 'achoe@plos.org'


class AkitaLoginPage(PlosPage):
    def __init__(self, driver):
        super(AkitaLoginPage, self).__init__(driver, '')
        # Locators - Instance members
        self._welcome_message = (By.TAG_NAME, 'h1')
        self._email_text_box_label = (By.XPATH, '//label[@for="username"]')
        self._email_text_box = (By.ID, 'username')
        self._password_text_box_label = (By.XPATH, '//label[@for="password"]')
        self._password_text_box = (By.ID, 'password')
        self._forgot_password_link = (By.CSS_SELECTOR, 'div.form-group a')
        self._sign_in_button = (By.CSS_SELECTOR, 'input.btn')
        self._account_register_resend_links = (By.CSS_SELECTOR, 'div.row + div > a')
        self._flash_error_message = (By.CSS_SELECTOR, 'div.alert')

    # POM Actions
    def validate_login_page_elements(self):
        """
        Validates elements on login page
        :return: None
        """
        assert self._get(self._welcome_message).text == 'Sign in to PLOS', \
            'Welcome text is not "Sign in to PLOS"'
        assert self._get(self._email_text_box_label).text == 'Email', \
            'Email label text is not "Email"'
        self._get(self._email_text_box)
        assert 'password' in self._get(self._password_text_box_label).text.lower(), \
            'Password label text is incorrect'
        self._get(self._password_text_box)
        assert self._get(self._forgot_password_link).text == 'Forgot your password?', \
            'Forgot password link text is not "Forgot your password?"'
        assert self._get(self._sign_in_button).get_attribute('value') == \
            'Sign In', 'Sign In button text is not "Sign In"'
        account_register_resend_links = self._gets(self._account_register_resend_links)
        new_account_registration_link = account_register_resend_links[0]
        resend_email_confirmation_link = account_register_resend_links[1]
        assert new_account_registration_link.text == 'Register for a New Account', \
            'Register link text is not "Register for a New Account"'
        assert resend_email_confirmation_link.text == 'Resend e-mail address confirmation', \
            'Resend link text is not "Resend e-mail address confirmation'

    def validate_failed_login(self):
        """
        Negative login cases:
        1. No username, no password
        2. No username, password
        3. Invalid username, no password
        4. Valid username, no password
        5. Valid username, invalid password
        :return: None
        """
        negative_cases = {
            'No username, no password': ['', ''],
            'No username, password': ['', Config.existing_user_pw],
            'Invalid username, no password': [Config.non_existing_user_email, ''],
            'Valid username, no password': [Config.existing_user_email, ''],
            'Valid username, invalid password': [Config.existing_user_email, 'blah']
        }

        expected_error_message_text_email = \
            'Incorrect email address. Please enter a valid e-mail address.'
        expected_error_message_text_pass = 'Incorrect password. Please enter a valid password.'

        for (case, credentials) in negative_cases.items():
            logging.info("Testing {0}".format(case))
            self.sign_in(credentials[0], credentials[-1])
            flash_error_message = self._get(self._flash_error_message)
            if 'password' in flash_error_message.text:
                self.validate_text_exact(flash_error_message.text, expected_error_message_text_pass,
                                         'Incorrect error message')
            elif 'email' in flash_error_message.text:
                self.validate_text_exact(
                        flash_error_message.text, expected_error_message_text_email,
                        'Incorrect error message')

    def successful_login(self):
        """
        Validates login with valid username and password. Checks that the homepage now has a link
        called "sign out"
        :return:
        """
        self.sign_in(Config.existing_user_email, Config.existing_user_pw)

    def enter_username_field(self, email):
        """
        Inputs email for the user
        :param email:
        :return:
        """
        email_input = self._get(self._email_text_box)
        email_input.clear()
        email_input.send_keys(email)

    def enter_password_field(self, password):
        """
        Inputs password
        :param password:
        :return:
        """
        password_input = self._get(self._password_text_box)
        password_input.clear()
        password_input.send_keys(password)

    def click_sign_in_button(self):
        """
        Clicks Sign In button
        :return:
        """
        sign_in_button = self._get(self._sign_in_button)
        sign_in_button.click()

    def sign_in(self, email, password):
        """
        Signs in with an email (username) and password
        :param email:
        :param password:
        :return:
        """
        self.enter_username_field(email)
        self.enter_password_field(password)
        self.click_sign_in_button()
