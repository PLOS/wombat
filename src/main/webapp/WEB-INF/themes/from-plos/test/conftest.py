#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import logging
from os import getenv

import pytest

from .Base.Config import run_against_grid, grid_enabled_browsers, rhino_url
from .Base.WebDriverFactory import WebDriverFactory

_driver = None

# SELENIUM_BROWSERS_LIST contains a list of browsers expected to be a comma
# separated lists
# against which to multiplex tests. Default is to only run tests against
# Firefox.
#
standard_browsers = getenv('SELENIUM_BROWSERS_LIST', 'FIREFOX').replace(' ',
                                                                        "").split(
    ',')

# Factory object to instantiate drivers
factory = WebDriverFactory()

# Will contain a list of driver (not instantiated) for the current test
# variations (for all
# browsers)
_injected_drivers = []


def get_browser_list():
    """
    Method to get browsers to use as a parameter in the fixture that
    retrieving and injecting
    driver with Selenium Grid or WebDriver
    :return: enabled_browsers: list of dictionaries if we are using Selenium
    Grid,
    list of string for WebDriver
    """
    if run_against_grid:
        enabled_browsers = grid_enabled_browsers
    else:
        enabled_browsers = standard_browsers
    return enabled_browsers


def idfn(fixture_value):
    """
    Method to return meaningful readable id for parameterized fixture,
    as by default if the
    parameter value is a dictionary, it looks like 'get_driver0'
    :param fixture_value:
    :return: browser name, string
    """
    if isinstance(fixture_value, dict):
        return '{0}'.format(fixture_value['browserName'])
    else:
        return fixture_value


def get_driver(browser):
    """
    Simple method to retrieve the WebDriver/Proxy instances for this class
    to test method.
    """
    if run_against_grid:
        _driver = factory.setup_remote_webdriver(browser)
    else:
        _driver = factory.setup_webdriver(browser)
    return _driver


@pytest.fixture(scope="function", params=get_browser_list(), ids=idfn)
def driver_get(request):
    """
    Fixture to give driver instance to test method and destroy it after test
    finished.
    We are using this fixture now as class-level usefixtures decorator:
      @pytest.mark.usefixtures("driver_get")
    there are different ways to do it, like marking autouse=true,
    adding [pytest] usefixtures = driver_get to pytest.ini, etc.
    More details can be found here:
    https://docs.pytest.org/en/latest/fixture.html#using-fixtures-from
    -classes-modules-or-projects
    """
    web_driver = get_driver(request.param)
    request.cls.driver = web_driver

    def teardown():
        """
        Method in charge of destroying the WebDriver/Proxy instances
        once the test finished running (even upon test failure).
        """
        if web_driver:
            web_driver.quit()
        else:
            factory.teardown_webdriver()

    request.addfinalizer(teardown)


@pytest.fixture()
def nav_article(request, driver_get):
    def get_article(page_class, art_path, doi='', ingest=False):
        """
        Fixture to navigate the article page using page class, path to the
        article (url) and doi
        If the article page not found, ingesting from production
        :param article page_class
        :param art_path: string like
        '/DesktopPlosOne/article?id=10.1371/journal.pone.0218301'
           or '/DesktopPlosNtds/article/comments?id=info:doi/10.1371/journal
           .pntd.0001379'
        :param doi: string like: '10.1371/journal.pone.0008519', if  doi=''
        it will be extracted
            from art_path
        :return: article page
        """
        current_test = request.instance
        article_page = page_class(current_test.driver, art_path)
        if ingest:
            if 'page not found' in article_page._driver.title.lower():
                logging.info(
                    'article page not found, ingesting the article '
                    'with doi: {}'.format(doi))
                if not doi:
                    doi_idx = art_path.find('10.1371')
                    doi = art_path[doi_idx:].strip()
                article_doi_text = doi.replace('/', '++')
                article_page.fetch_and_ingest_article(article_doi_text)
                article_page.delete_zip(article_doi_text)
                logging.info('article ingested: {}'.format(art_path))
                article_page.refresh()
                article_page.page_ready()
        return article_page

    return get_article


@pytest.fixture()
def nav_annotation(request, driver_get):
    def get_annotation(page_class, page_path, uri=''):
        """
        Fixture to navigate the article page using page class, path to the
        article (url) and doi
        If the article page not found, ingesting from production
        :param article page_class
        :param page_path: string like
        '/DesktopPlosBiology/article/comment?id=10.1371/
            annotation/bcf5eb7a-8bf2-437e-ae27-d43b5cc749f6'
        :param uri: string like:
        '10.1371/annotation/bcf5eb7a-8bf2-437e-ae27-d43b5cc749f6'
        :return: article annotation page
        """
        current_test = request.instance
        article_page = page_class(current_test.driver, page_path, uri)

        def teardown():
            """
            Method in charge of cleaning: remove flags from annotation
            once the test finished running (even upon test failure)
            """
            annotation_uri = article_page._comment_data['commentUri']
            article_doi = article_page._comment_data['article']['doi']

            article_page.remove_all_flags_from_annotation(
                rhino_url, annotation_uri, article_doi)

        request.addfinalizer(teardown)

        return article_page

    return get_annotation


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item):
    """
    Extends the PyTest Plugin to take and embed screenshot in html report,
    whenever test fails. Also adding url (last link from the test)
    :param item:
    """
    pytest_html = item.config.pluginmanager.getplugin('html')
    outcome = yield
    report = outcome.get_result()
    extra = getattr(report, 'extra', [])

    if report.when == 'call':
        feature_request = item.funcargs['request']
        driver = feature_request.cls.driver
        if driver:
            extra.append(pytest_html.extras.url(
                driver.current_url))
            xfail = hasattr(report, 'wasxfail')
            if (report.skipped and xfail) or (report.failed and not xfail):
                screenshot_failure = \
                    driver.get_screenshot_as_base64()
                if screenshot_failure:
                    extra.append(pytest_html.extras.image(screenshot_failure, ''))
        report.extra = extra
