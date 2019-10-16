#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from os import getenv
import pytest

from ..Base.WebDriverFactory import WebDriverFactory
from ..Base.Config import run_against_grid, run_against_appium, grid_enabled_browsers, \
    appium_enabled_browsers, device_type

# SELENIUM_BROWSERS_LIST contains a list of browsers expected to be a comma separated lists
# against which to multiplex tests. Default is to only run tests against Firefox.
#
standard_browsers = getenv('SELENIUM_BROWSERS_LIST', 'FIREFOX').replace(' ', "").split(',')

# Factory object to instantiate drivers
factory = WebDriverFactory()
_injected_drivers = []

mobile_user_agents = [
    {'user_agent': 'Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38'
                   ' (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1',
     'mobile_emulator': 'iPhone'},
    {'user_agent': 'Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36'
                   '(KHTML, like Gecko) Chrome/68.0.3440.75 Mobile Safari/537.36',
     'mobile_emulator': 'Android'}
]


def get_browser_list():
    """
    Method to get browsers to use as a parameter in the fixture that retrieving and injecting
    driver with Selenium Grid or WebDriver
    :return: enabled_browsers: list of dictionaries if we are using Selenium Grid,
    list of string for WebDriver
    """
    if run_against_grid:
        enabled_browsers = grid_enabled_browsers
    else:
        enabled_browsers = []
        for sb in standard_browsers:
            for ua in mobile_user_agents:
                enabled_browsers.append({'browserName': sb,
                                         'user_agent': ua['user_agent'],
                                         'mobile_emulator': ua['mobile_emulator']})
    return enabled_browsers


def idfn(fixture_value):
    """
    Method to return meaningful readable id for parameterized fixture, as by default if the
    parameter value is a dictionary, it looks like 'get_driver0'
    :param fixture_value:
    :return: browser name, string
    """
    return '{0}_{1}'.format(fixture_value['browserName'], fixture_value['mobile_emulator'])


def get_driver(browser):
    """
    Simple method to retrieve the WebDriver/Proxy instances for this class to test method.
    """
    if run_against_grid:
        _driver = factory.setup_remote_webdriver(browser)
    else:
        _driver = factory.setup_webdriver(browser['browserName'],
                                          user_agent=browser['user_agent'])
    return _driver


@pytest.fixture(scope="function", params=get_browser_list(), ids=idfn)
def driver_get(request):
    """
    Fixture to give driver instance to test method and destroy it after test finished.
    We are using this fixture now as class-level usefixtures decorator:
      @pytest.mark.usefixtures("driver_get")
    there are different ways to do it, like marking autouse=true,
    adding [pytest] usefixtures = driver_get to pytest.ini, etc.
    More details can be found here:
    https://docs.pytest.org/en/latest/fixture.html#using-fixtures-from-classes-modules-or-projects
    """
    if 'skip_iphone' in request.node.keywords:
        request.cls.skip_specific_validation = False
        if isinstance(request.param, dict) \
                and request.param.get('mobile_emulator') \
                and 'iPhone' in request.param['mobile_emulator']:
            request.cls.skip_specific_validation = True
    web_driver = get_driver(request.param)
    request.cls.driver = web_driver

    def teardown():
        """
        Method in charge of destroying the WebDriver/Proxy instances
        once the test finished running (even upon test failure).
        """
        factory = WebDriverFactory()
        if web_driver:
            web_driver.quit()
        else:
            factory.teardown_webdriver()

    request.addfinalizer(teardown)
