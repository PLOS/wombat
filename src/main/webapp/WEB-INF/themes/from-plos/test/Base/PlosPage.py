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
import re
from contextlib import suppress
from inspect import getfile, getsource
from os import getcwd, remove
from os.path import abspath, dirname, basename
from time import sleep, time

import requests
import wget
from bs4 import BeautifulSoup
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond
from selenium.webdriver.support.ui import WebDriverWait

from .CustomException import (
    ElementDoesNotExistAssertionError,
    ElementExistsAssertionError,
    ErrorTimeoutException
    )
from .CustomExpectedConditions import ElementToBeClickable
from .WebDriverListener import WebDriverListener
from ..Base.Config import (
    wait_timeout, base_url as config_base_url,
    collections_url,
    rhino_url, environment
    )
from ..Base.LinkVerifier import LinkVerifier

__author__ = 'jkrzemien@plos.org'


def build_prod_url_table():
    """Build a table from site keys to the base prod URL for each site."""
    for device in ['Desktop', 'Mobile']:

        # The prod URLs would be correct for the mobile site, but they will
        # resolve
        # only to desktop sites without a mobile User-Agent header. To avoid
        #  wrong
        # behavior, leave mobile site keys out of the table.

        for journal in ['PlosOne', 'PlosBiology', 'PlosMedicine',
                        'PlosCompBiol',
                        'PlosGenetics', 'PlosPathogens', 'PlosNtds',
                        'PlosClinicalTrials']:
            url = '{0}/{1}/'.format(config_base_url.rstrip('/'),
                                    journal.lower())
            yield (device + journal, url)
        url_collections = '{0}/'.format(collections_url.rstrip('/'))
        yield (device + 'PlosCollections', url_collections)


PROD_URL_TABLE = dict(build_prod_url_table())


class PlosPage(object):
    """
    Model an abstract base Journal page.
    """
    PROD_URL = ''
    logging.basicConfig(
        format='%(levelname)-s %(message)s [%(filename)s:%(lineno)d]',
        level=logging.INFO)

    def __init__(self, driver, url_suffix=''):

        # Internal WebDriver-related protected members
        self._driver = driver
        self._wait = WebDriverWait(self._driver, wait_timeout)
        self._actions = ActionChains(self._driver)

        base_url = self.__build_environment_url(url_suffix)

        # Prevents WebDriver from navigating to a page more than once
        # (there should be only one starting point for a test)
        if not hasattr(self._driver, 'navigated'):
            try:
                self._driver.get(base_url)
                self._driver.navigated = True
            # sometimes webdriver fails to load the page while parallel
            # pytest running,
            # adding another attempt to load
            except TimeoutException:
                logging.warning(
                    '\t[WebDriver Error] WebDriver timed out while trying to '
                    'load '
                    'the requested web page {0!r}.'.format(base_url))
                logging.warning('wait_timeout: {0!s}'.format(self._wait))
                try:
                    self._driver.refresh()
                    self._driver.navigated = True
                except TimeoutException as toe:
                    logging.error(
                        '\t[WebDriver Error] WebDriver timed out while '
                        'trying to load '
                        'the requested web page {0!r} after attempt to '
                        'refresh.'
                        .format(base_url))
                    logging.error('wait_timeout: {0!s}'.format(self._wait))
                    raise toe

        # Internal private member
        self.__linkVerifier = LinkVerifier()

        # Locators - Instance variables unique to each instance
        self._article_type_menu = (By.ID, 'article-type-menu')
        self._page_body = (By.TAG_NAME, 'body')

    # POM Actions
    def __build_environment_url(self, url_suffix):
        """
        *Private* method to detect on which environment we are running the
        test.
        Then builds up a URL accordingly
        1. urlSuffix: String representing the suffix to append to the URL.
        **Returns** A string representing the whole URL from where our test
        starts
        """
        journal_name = str(self)
        env = environment.lower()
        if env == 'prod':
            base_url = self._translate_to_prod_url(url_suffix) or self.PROD_URL
        elif 'Collections' in journal_name:
            base_url = collections_url + url_suffix
        else:
            base_url = config_base_url + url_suffix
        return base_url

    def _translate_to_prod_url(self, dev_path):
        """Translate a dev-box path to a production URL.

        Example:
          '/DesktopPlosBiology/article?id=10.1371/journal.pbio.1001199'
        to
          'https://journals.plos.org/plosbiology/article?id=10.1371/journal
          .pbio.1001199'

        Returns None if the path does not start with a token that matches a
        site key.
        """
        match = re.match(r'/(.*?)/(.*)', dev_path)
        if match:
            site_key, url_suffix = match.groups()
            if site_key in PROD_URL_TABLE:
                base_url = PROD_URL_TABLE[site_key]
                return base_url + url_suffix
        return None

    def _get(self, locator):
        try:
            return self._wait.until(exp_cond.visibility_of_element_located(
                locator)).wrapped_element
        except TimeoutException:
            logging.error(
                '\t[WebDriver Error] WebDriver timed out while trying to '
                'identify '
                'element by {0!s}.'.format(locator))
            raise ElementDoesNotExistAssertionError(locator)

    def _iget(self, locator):
        """
        Unlike the regular _get() function, this one will be successful for
        elements with a width
        and or height of zero; confusing name, but suggesting 'i' for
        invisible as a zero
        width/height element.
        :param locator: locator
        """
        try:
            return self._wait.until(
                exp_cond.presence_of_element_located(locator)).wrapped_element
        except TimeoutException:
            logging.error(
                '\t[WebDriver Error] WebDriver timed out while trying to '
                'identify element '
                'by {0}.'.format(locator))
            raise ElementDoesNotExistAssertionError(locator)

    def _check_for_invisible_element(self, locator):
        try:
            return self._wait.until(
                exp_cond.invisibility_of_element_located(locator)) \
                .wrapped_element
        except TimeoutException:
            print(
                '\t[WebDriver Error] WebDriver timed out while trying to '
                'look for hidden '
                'element by {0}.'.format(str(locator)))
            raise ElementDoesNotExistAssertionError(locator)

    def _check_for_invisible_element_boolean(self, locator):
        self.set_timeout(2)
        try:
            self._wait.until(exp_cond.invisibility_of_element_located(locator))
            return True
        except:
            return False
        finally:
            self.restore_timeout()

    def _wait_for_not_element(self, locator, multiplier=0.1):
        timeout = wait_timeout * multiplier
        self.set_timeout(timeout)
        try:
            return self._wait.until_not(
                exp_cond.visibility_of_element_located(locator))
        except TimeoutException:
            print(
                '\t[WebDriver Error] Found element using {0!s} (test was for '
                'element absence).'
                .format(locator))
            raise ElementExistsAssertionError(locator)
        finally:
            self.restore_timeout()

    def _gets(self, locator):
        try:
            # return self._wait.until(EC.presence_of_all_elements_located(
            # locator))
            return [x.wrapped_element for x in
                    self._wait.until(
                        exp_cond.presence_of_all_elements_located(locator))]
        except TimeoutException:
            logging.error(
                '\t[WebDriver Error] WebDriver timed out while trying to '
                'identify elements '
                'by {0}.'.format(str(locator)))
            raise ElementDoesNotExistAssertionError(locator)

    def _wait_for_element(self, element, multiplier=1):
        """
        We need a method that can be used to determine whether a page
        comprised of dynamic elements
          has fully loaded, or loaded enough to expose element.
        :param element: the item on a dynamic page we want to wait for
        :param multiplier: a multiplier, default (1) applied against the
        base wait_timeout to wait
          for element
        """
        timeout = wait_timeout * multiplier
        self.set_timeout(timeout)
        self._wait.until(ElementToBeClickable(element))
        self.restore_timeout()

    def _wait_on_lambda(self, wait_lambda, max_wait=30):
        """
        This is intended for use with lambdas having _gets or _get calls and
        therefore
        allows ElementDoesNotExistAssertionError's to occur and treats them
        the same
        as the lambda evaluating to false (continue wait).

         :param wait_lambda: lambda to evaluate every second, returning when
         it is true
         :param max_wait: maximum amount of time to wait for it to be true
        """
        for x in range(0, max_wait):
            saved_exception = None
            try:
                if wait_lambda():
                    return
                else:
                    sleep(1)
            except ElementDoesNotExistAssertionError as edneae:
                sleep(1)
                saved_exception = edneae

        if None is saved_exception:
            lambda_src = getsource(wait_lambda)
            raise TimeoutException(
                '{0} not satisfied before {1} seconds passed'.format(
                    lambda_src, max_wait))
        else:
            raise saved_exception

    def _scroll_into_view(self, element):
        self._driver.execute_script("javascript:arguments[0].scrollIntoView()",
                                    element)

    def click_covered_element(self, element):
        """
        Because sometimes we have  obscures content, we need a method
        specifically to click
        on element without click on that is on top. This breaks Selenium
        prevision on not
        clicking in elements that a regular user will not be able to click,
        so use it only for
        non core test component. Consider this a Konami like cheatcode for
        development purposes.
        :param element: webelement to receive the click
        :return: None
        """
        logging.debug('{0} is covered by the toolbar...'.format(element))
        self._driver.execute_script("javascript:arguments[0].click()", element)
        return None

    def scroll_by_pixels(self, pixels):
        """
        A generic method to scroll by x pixels (positive - down) or (
        negative - up)
        :return: void function
        """
        self._driver.execute_script(
            'javascript:scrollBy(0,{0})'.format(pixels))
        sleep(1)

    def _is_link_valid(self, link):
        return self.__linkVerifier.is_link_valid(link.get_attribute('href'))

    def _get_link_status_code(self, link):
        return self.__linkVerifier.get_link_status_code(
            link.get_attribute('href'))

    def _is_image_valid(self, img):
        return self.__linkVerifier.is_link_valid(img.get_attribute('src'))

    def traverse_to_frame(self, frame):
        # for frame in frames:
        print('\t[WebDriver] About to switch to frame "%s"...' % frame, )
        self._wait.until(
            exp_cond.frame_to_be_available_and_switch_to_it(frame))
        print('OK')

    def traverse_to_new_window(self):
        # Switch the last launched window
        logging.info('\t[WebDriver] About to switch the new window...')
        new_window = self._driver.window_handles[1]
        self._driver.switch_to_window(new_window)
        logging.info('OK')

    def traverse_from_frame(self):
        print('\t[WebDriver] About to switch to default content...', )
        self._driver.switch_to.default_content()
        print('OK')

    def traverse_from_window(self):
        # Return the the previous window
        logging.info('\t[WebDriver] About to switch to default content...')
        default_context = self._driver.window_handles[0]
        self._driver.switch_to_window(default_context)
        logging.info('OK')

    def open_page_in_new_window(self, page_title, original_url):
        """
        The method to handle opening new page
        :param page_title: page title to check new page loaded, string
        :param original_url: original page url to wait it is loaded after
        closing new window, string
        :return: new_page_url to assert correct page were visited, string
        """
        self._wait_for_number_of_windows_to_be(2)
        self.traverse_to_new_window()
        self._wait.until(exp_cond.title_contains(page_title))
        new_page_url = self.get_current_url()
        self._driver.close()
        self._wait_for_number_of_windows_to_be(1)
        self.traverse_from_window()
        self._wait.until(exp_cond.url_contains(original_url))
        return new_page_url

    def set_timeout(self, new_timeout):
        self._driver.implicitly_wait(new_timeout)
        self._wait = WebDriverWait(self._driver, new_timeout)

    def restore_timeout(self):
        self._driver.implicitly_wait(wait_timeout)
        self._wait = WebDriverWait(self._driver, wait_timeout)

    def get_text(self, s):
        soup = BeautifulSoup(s)
        clean_out = soup.get_text()
        print(clean_out)
        return clean_out

    def refresh(self):
        """
        refreshes the whole page
        """
        self._driver.refresh()

    def is_element_present(self, locator):
        try:
            self._driver.find_element(By.ID, locator)
            return True
        except NoSuchElementException:
            print('\t[WebDriver] Element %s does not exist.' % str(locator))
            return False

    def is_element_present_css(self, locator):
        try:
            self._driver.find_element(By.CSS_SELECTOR, locator)
            return True
        except NoSuchElementException:
            print('\t[WebDriver] Element %s does not exist.' % str(locator))
            return False

    def is_element_present_xpath(self, locator):
        try:
            self._driver.find_element(By.XPATH, locator)
            return True
        except NoSuchElementException:
            print('\t[WebDriver] Element %s does not exist.' % str(locator))
            return False

    def is_element_present_class(self, locator):
        try:
            self._driver.find_element(By.CLASS_NAME, locator)
            return True
        except NoSuchElementException:
            print('\t[WebDriver] Element %s does not exist.' % str(locator))
            return False

    def wait_for_animation(self, selector):
        while self.is_element_animated(selector):
            sleep(.5)

    def is_element_animated(self, selector):
        return self._driver.execute_script(
            'return jQuery({0}).is(":animated");'.format(json.dumps(selector)))

    def wait_until_ajax_complete(self):
        self._wait.until(lambda driver: self._driver.execute_script(
            "return jQuery.active == 0"))

    def wait_until_image_complete(self, image):
        self._wait.until(lambda driver: self._driver.execute_script(
            "return arguments[0].complete && typeof arguments[0]"
            ".naturalWidth != "
            "\"undefined\" && arguments[0].naturalWidth > 0", image))

    def _wait_for_text_to_be_present_in_element_value(self, locator, text,
                                                      multiplier=1):
        """
        Wait for a string be present in an element text
        :param locator: the page locator of the element with value that
        should have the text
        :param text: text to be present in the located element value
        :param multiplier: the multiplier of Config.wait_timeout to wait for
        a locator
        to be not present
        """
        timeout = wait_timeout * multiplier
        self.set_timeout(timeout)
        self._wait.until(
            exp_cond.text_to_be_present_in_element_value(locator, text))
        self.restore_timeout()

    def _check_element_font_weight(self, element, expected_font_weight):
        applied_font_weight = element.value_of_css_property("font-weight")
        if expected_font_weight == 'bold' or expected_font_weight == '700':
            assert applied_font_weight == 'bold' or applied_font_weight == \
                   '700', 'Applied font weight: {0} is different from' \
                          ' the expected: {1}'\
                   .format(applied_font_weight, expected_font_weight)
        elif expected_font_weight == 'normal' or expected_font_weight == '400':
            assert applied_font_weight == 'normal' or applied_font_weight == \
                   '400', 'Applied font weight: {0} is different from the ' \
                          'expected: {1}'.format(
                           applied_font_weight, expected_font_weight)
        else:
            assert applied_font_weight == expected_font_weight, \
                'Applied font weight: {0} is different from the expected:' \
                ' {1}'.format(applied_font_weight, expected_font_weight)

    def _page_ready(self, locator):
        """
        A fuction to validate that the dashboard page is loaded before
        interacting with it
        """
        self.set_timeout(5)
        try:
            self._wait_for_element(locator)
        except ElementDoesNotExistAssertionError:
            try:
                self._wait_for_element(locator)
            except ElementDoesNotExistAssertionError:
                self._wait_for_element(locator)
        self.restore_timeout()

    def get_current_url(self):
        """
        Returns the url of the current page, with any trailing arguments,
        if present
        :return: url
        """
        url = self._driver.current_url
        return url

    def _wait_for_number_of_windows_to_be(self, num_windows,
                                          multiplier=1):
        """
        Wait for the number of windows to be specific value
        :param num_windows: the expected number of windows
        :param multiplier: the multiplier of Config.wait_timeout to wait for
        a locator
        to be not present
        """
        timeout = wait_timeout * multiplier
        self.set_timeout(timeout)
        self._wait.until(exp_cond.number_of_windows_to_be(num_windows))
        self.restore_timeout()

    def fetch_and_ingest_article(self, article_doi):
        """
        The method to prepare zip file using production rhino
        and ingest article to the test environment.
        The methods ingest_zip, publish_article, populate_categories are
        mostly from article-admin
        application
        :param article_doi: article doi, corrected,
        like 10.1371++journal.pone.1234567
        :return: void function
        """

        # fetch - preparing zip to ingest
        prod_rhino__url = 'http://rhino-101.soma.plos.org:8006/v2/'
        article_cmd = 'articles/{0}/ingestions/1/ingestible'.format(
            article_doi)
        url = prod_rhino__url + article_cmd
        file_path = '{0}/Output/'.format(getcwd())

        # full path to zip file
        file_name = '{0}{1}.zip'.format(file_path, article_doi.replace(
            '10.1371++journal.', ''))

        wget.download(url, out=file_name)
        sleep(1)

        target_rhino_url = rhino_url  #
        # 'http://rhino-201.soma.plos.org:8006/v2/'

        logging.info(
            '[Ingesting article to rhino. url: ' + target_rhino_url +
            'articles]')
        self.ingest_zip(file_name, target_rhino_url)
        logging.info('[Publishing new revision]')
        self.publish_article(article_doi, target_rhino_url)
        self.populate_categories(article_doi, target_rhino_url)

    def delete_zip(self, article_doi):
        file_path = '{0}/Output/'.format(getcwd())

        # full path to zip file
        file_name = '{0}{1}.zip'.format(file_path, article_doi.replace(
            '10.1371++journal.', ''))
        with suppress(OSError):
            remove(file_name)

    def ingest_zip(self, zip, rhino_host, bucket=None):
        """
        Ingest a zip on a rhino host.
        :param zip: the full path to the zip file to be ingested.
        :param rhino_host: The rhino host to atttempt the ingest on.
        :param bucket: Optional. Choose a bucket in the repo to deposit files
         other than the default bucket, i.e. 'preprints'
        :return: The json response object of the ingest request.
        """
        base_url = '{0}/{1}'.format(rhino_host, 'articles')
        if bucket:
            base_url = '{0}?bucket={1}'.format(base_url, bucket)
        logging.info('Ingesting {0} to {1}'.format(basename(zip), base_url))
        with open(zip, mode='rb') as file:
            r = requests.post(base_url, files={'archive': file})

        if r.status_code != 201:
            logging.error(
                'Ingest of {0} on {1} failed! {2}'
                .format(basename(zip), rhino_host, r.text))

        logging.info('Ingest of {0} on {1} successful!'.format(basename(zip),
                                                               rhino_host))
        logging.debug(r.json())
        return r.json()

    def publish_article(self, doi, rhino_host, ingestion_number=1,
                        revision_number=1):
        """
        Publish an article by setting a revision.
        :param doi: The doi.
        :param rhino_host: The rhino host this is to happen on.
        :param ingestion_number: The ingestion number we want to point the
        revision/version to.
        :param revision_number: The revision number to use for this pubbed
        instance.
        :return: json of request response.
        """

        url = '{0}/{1}/{2}/{3}?revision={4}&ingestion={5}' \
            .format(rhino_host, 'articles', doi, 'revisions',
                    str(revision_number), str(ingestion_number))
        r = requests.post(url)
        if not r.ok:
            logging.error(
                'publishing failed for {0} on {1}, error message: {2}'
                .format(doi, rhino_host, r.text))

        logging.info(
            'Publish revision number {0} of {1} on {2} successful'.format(
                r.json()['revisionNumber'], doi, rhino_host))
        return r.json()

    def populate_categories(self, doi, rhino_host):
        """
        Make a POST to populate the categories of an article.
        :param doi: DOI of the article.
        :param rhino_host: Rhino host this should happen on.
        :return: None, or raise RhinoRequestError
        """

        url = '{0}/{1}/{2}/{3}'.format(rhino_host, 'articles', doi,
                                       'categories')
        response = requests.post(url)
        if not response.ok:
            logging.error(
                'Populate categories request for {0} on {1} encountered an '
                'error: {2}'.format(
                    doi, rhino_host, response.text))

        logging.info(
            'Populated categories for {0} on {1}'.format(doi, rhino_host))
        return response.text

    def click_on_link_same_window_and_back(self, link_to_check, page_title,
                                           page_url, original_url_to_check,
                                           check_title=True):
        """
        The method to click on specific link in 'Connect with us' section
        that opens required
        resourse in the same window, then go back to home page
        :param link_to_check: specific link to redirect and validate, string
        :param page_title: page title, or its part, to use it to wait for
        page loading, string
        :param page_url: page url, or its part, to use it to wait for page
        loading, string
        :param original_url_to_check: original url, or its part, to use it
        to wait after
        :param check_title: True if we are checking page title, False if
        checking page_url,
                default is True
        :return: current_url: actual url of visited page to assert, string

        """
        logging.info(
            'click on the link to open new page with title that contains: {}'
            .format(page_title))
        link_to_check.click()

        # TODO: saving screenshots to check redirecting to google scholar
        # page as I ran to the
        # issue when sometimes recapture is showing: "Please show you're not
        #  a robot" Remove next block if it's not needed anymore
        #
        current_page_url = self._driver._driver.current_url
        logging.info(
            'current page url after clicking: {0!s}'.format(current_page_url))
        logging.info('current page title after clicking: {0!s}'.format(
            self._driver._driver.title))
        item_name = page_url.split('/')[-1].replace('.', '_') + '_after'
        self._driver._driver.save_screenshot(
            self._generate_png_filename(item_name=item_name))

        try:
            if check_title:
                self._wait.until(exp_cond.title_contains(page_title))
            else:
                self._wait.until(exp_cond.url_contains(page_url))
        except TimeoutException:
            raise ErrorTimeoutException(page_title)

        current_url = self.get_current_url()
        self._driver.back()
        try:
            self._wait.until(exp_cond.url_contains(original_url_to_check))
        except TimeoutException:
            raise ErrorTimeoutException(original_url_to_check)

        return current_url

    def get_page_title(self):
        return self._driver.title

    @staticmethod
    def validate_text_exact(actual_text, expected_text,
                            message='Incorrect text'):
        """
        The method to assert that actual_text matches expected_text
        :param actual_text: string
        :param expected_text: string
        :param message: text to specify what exactly is incorrect,
        for example, 'Incorrect title',
          or header, etc. Optional, default value: 'Incorrect text'
        :return: void function
        """
        logging.info('Verifying text {0}:'.format(actual_text))
        assert actual_text == expected_text, \
            '{0}, expected: {1!r}, found: {2!r}'.format(message, expected_text,
                                                        actual_text)

    @staticmethod
    def validate_text_contains(actual_text, expected_part_text,
                               message='Incorrect text'):
        """
        The method to assert that actual_text contains expected_text
        :param actual_text: string
        :param expected_part_text: string
        :param message: text to specify what exactly is incorrect,
        for example, 'Incorrect title',
          or header, etc. Optional, default value: 'Incorrect text'
        :return: void function
        """
        logging.info('Verifying text {0}:'.format(actual_text))
        assert expected_part_text in actual_text, \
            '{0}, {1!r} was expected in {2!r}'.format(message,
                                                      expected_part_text,
                                                      actual_text)

    @staticmethod
    def normalize_spaces(text):
        """
        Helper method to leave strings with only one space between each word
        Used for string comparison when at least one string came
        from an HTML document
        :text: string
        :return: string
        """
        text = text.strip()
        # Replace non breakables spaces by spaces
        try:
            text = text.replace(u'\xa0', u' ')
        except UnicodeDecodeError:
            text = text.replace(u'\xa0', u' ')
        return re.sub(r'\s+', ' ', text)

    @staticmethod
    def _generate_png_filename(item_name):
        """
        Helper *internal* method to generate a file name for the screenshots
        """
        ts = time()
        timestamp = datetime.datetime.fromtimestamp(ts).strftime(
            '%Y%m%d-%H%M%S')
        path = dirname(abspath(getfile(WebDriverListener)))
        logging.info('Saving screenshot')
        logging.info(item_name + '-' + timestamp + '.png')
        return '{0}/../Output/{1}-{2}.png'.format(path, item_name, timestamp)

    @staticmethod
    def get_rhino_comments_count(doi):
        endpoint = '/articles/{0}/comments?count='.format(doi)
        response = requests.get(rhino_url + endpoint)
        counters = json.loads(response.text)

        return counters['all']
