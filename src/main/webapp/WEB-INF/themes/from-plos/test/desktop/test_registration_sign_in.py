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
This test validates sign in. It validates error handling: unregistered user, 
registered user with: null password submit, bad password submit, correct login,
available links based on state (ie: signed out: register, forgotten password, 
and resend confirmation), and log out.
"""

import pytest

from ..Base.Journal import Journal
from ..desktop.Pages.AkitaLoginPage import AkitaLoginPage
from ..desktop.Pages.HomePage import HomePage

import time


def build_homepage_paths():
    for journal in Journal.desktop_journals:
        homepage_path = Journal.build_homepage_path(journal)
        yield journal, homepage_path


def idfn(fixture_value):
    """
    The function to return simple parameter id instead of full article comment info
    """
    return fixture_value[0]


@pytest.mark.usefixtures("driver_get")
@pytest.mark.parametrize("path", build_homepage_paths(), ids=idfn)
class TestRegistrationSignIn:
    def test_homepage_sign_in(self, path):
        journal_key, homepage_path = path
        driver = self.driver
        homepage = HomePage(driver, homepage_path)
        homepage.click_sign_in()
        sign_in_page = AkitaLoginPage(driver)
        time.sleep(1)
        sign_in_page.validate_login_page_elements()
        sign_in_page.validate_failed_login()
        sign_in_page.successful_login()
        assert homepage.is_user_logged()
        homepage.click_sign_out()
        homepage.validate_sign_in_button()
