#! usr/bin/env python3
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
PlosCollectionCollectionPage.py
"""

import json
import logging
import requests
import time

from selenium.webdriver.common.by import By

from ...Base.CustomException import ElementDoesNotExistAssertionError
from ...Base.Config import collections_url, verify_link_retries, verify_link_timeout, \
    wait_between_retries, cms_host as config_cms_host, cms_bucket as config_cms_bucket, environment
from .CollectionsPage import CollectionsPage
from .Menu import Menu

__author__ = 'jgray@plos.org'

# Need to set to proper url per environment
url_suffix = '/DesktopPlosCollections/'


class PlosCollectionsCollectionPage(CollectionsPage):
    """
    Model the PLoS Collections Journal page.
    """
    if environment == 'prod':
        CURRENT_URL = (collections_url + '/')
    else:
        CURRENT_URL = (collections_url + url_suffix)

    def __init__(self, driver, selected_collection):
        super(PlosCollectionsCollectionPage, self).__init__(driver,
                                                            url_suffix + selected_collection)
        logging.info('Validating collection: {0}'.format(selected_collection))

        #  POM - Instance members
        self._menu = Menu(driver)

        #  Locators - Instance members
        self._joey_container = (By.CLASS_NAME, 'lemur')
        self._collection_header_div = (By.CLASS_NAME, 'collection-header')
        self._hero_image = (By.CSS_SELECTOR, 'div.hero img')
        self._hero_image_credits = (By.CLASS_NAME, 'header-image-credit')
        self._collection_header_info_div = (By.CLASS_NAME, 'collection-header-info')
        self._collection_header_info_div_title = (By.CLASS_NAME, 'collection-title')
        self._collection_header_info_div_blurb = (By.CLASS_NAME, 'collection-blurb')
        self._collection_header_info_div_toggle = (
            By.CLASS_NAME, 'collection-header-info-toggle-button')
        self._collection_toolbar = (By.CLASS_NAME, 'collection-toolbar')
        self._collection_toolbar_section_dropdown = (By.CLASS_NAME, 'dropdown-toggle')
        self._collection_toolbar_section_items = (By.CSS_SELECTOR, 'ul.dropdown-menu li a')
        self._collection_toolbar_facebook_icon = (
            By.CSS_SELECTOR, 'ul.navbar-right a i.fa-facebook-square')
        self._collection_toolbar_twitter_icon = (By.CSS_SELECTOR, 'ul.navbar-right a i.fa-twitter')
        self._collection_toolbar_envelope_icon = (
            By.CSS_SELECTOR, 'ul.navbar-right a i.fa-envelope')
        self._collection_billboard_div = (By.CLASS_NAME, 'collection-billboard')
        self._collection_billboard_title = (By.CLASS_NAME, 'billboard-title')
        self._collection_billboard_button = (By.CLASS_NAME, 'billboard-button')
        self._collection_preview_textwidget = (By.CLASS_NAME, 'preview-textwidget')
        self._collection_preview_textwidget_title = (By.CLASS_NAME, 'preview-textwidget-title')
        self._collection_preview_textwidget_blurb = (By.CLASS_NAME, 'preview-textwidget-blurb')
        self._collection_preview_textwidget_button = (By.CLASS_NAME, 'button-text-widget')
        self._collection_rsswidget_div = (By.CLASS_NAME, 'rss-widget')
        self._collection_data_test_selector = (By.CSS_SELECTOR, '.rss-widget [data-test-selector]')
        self._collection_rsswidget_title = (By.CLASS_NAME, 'rss-widget-title')
        self._collection_rsswidget_feeditem = (By.CLASS_NAME, 'rss-widget-feed-item')
        self._collection_rsswidget_feeditem_datetime = (
            By.CLASS_NAME, 'rss-widget-feed-item-datetime')
        self._collection_rsswidget_feeditem_title = (By.CLASS_NAME, 'rss-widget-feed-item-title')
        self._collection_rsswidget_feeditem_author = (By.CLASS_NAME, 'rss-widget-feed-item-author')
        self._collection_item = (By.CLASS_NAME, 'collection-item')
        self._collection_item_img = (By.CLASS_NAME, 'collection-item-thumbnail')
        self._collection_item_img_credit = (By.CSS_SELECTOR, 'div.collection-item-image-credit')
        self._collection_item_title = (By.CSS_SELECTOR, 'h4.collection-item-title div a')
        self._collection_item_authors = (By.CSS_SELECTOR, 'p.collection-item-authors div')
        self._collection_item_publisher = (By.CSS_SELECTOR, 'span.collection-item-publisher div')
        self._collection_item_pubdate = (By.CLASS_NAME, 'collection-item-pub-date')

    # POM Actions
    def validate_browse_links(self):
        """
        Check for agreement between number, and list of menu items of the Browse menu, then do a get
        on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was
        received.
        """
        expected_links = {
            'Biology & Life Sciences': '{0}s/biology-life-sciences'.format(self.CURRENT_URL),
            'Computer & Information Sciences':
                '{0}s/computer-information-sciences'.format(self.CURRENT_URL),
            'Earth & Environmental Sciences':
                '{0}s/earth-environmental-sciences'.format(self.CURRENT_URL),
            'Medicine & Health Sciences': '{0}s/medicine-health-sciences'.format(self.CURRENT_URL),
            'Research Analysis & Science Policy':
                '{0}s/research-analysis-science-policy'.format(self.CURRENT_URL)}
        self._menu.validate_browse_links(expected_links)

    def validate_about_links(self):
        """
        Check for agreement between number, and list of menu items of the About menu, then do a get
        on those targets to ensure they exist. (Return a 200)
        :return: Success on success or Error message indicating what non-200 error code was received
        """
        expected_links = {
            'About PLOS Collections': '{0}s/about'.format(self.CURRENT_URL),
            'Propose a Special Collection':
                '{0}s/propose-a-special-collection'.format(
                self.CURRENT_URL),
            'Finances for Special Collections':
                '{0}s/finances-for-special-collections'.format(self.CURRENT_URL)}
        self._menu.validate_about_links(expected_links)

    def validate_joey_container(self):
        """
        Check for existence of CMS provided fixture data container for Collection page.
        """
        logging.info('Verifying presence of a little Joey in the Wombat pouch')
        self._get(self._joey_container)

    def get_info_div_width(self):
        """
        extract css property for collection hero information overlay div
        :return: integer value in pixels
        """
        info_div = self._get(self._collection_header_info_div)
        info_div_width = info_div.value_of_css_property('width')[:-2]
        return info_div_width

    @staticmethod
    def verify_arbitrary_web_asset(link):
        """
        Attempt to validate the validity of a web resources (link, image, etc.)
        :param link: URL
        :return: http return code or "TIMED OUT"
        """
        code = None
        successful = False
        attempts = 1
        while not successful and attempts < verify_link_retries:
            try:
                response = requests.get(link, timeout=verify_link_timeout,
                                        allow_redirects=True, verify=False)
                code = response.status_code
                successful = True
            except requests.exceptions.Timeout:
                code = "TIMED OUT"
                attempts += 1
                time.sleep(wait_between_retries)
        return code

    def validate_collection_header(self):
        """
        # Verify the existence of the CMS derived Collection header div elements and their functions
        """
        collection_url = self._driver._driver.current_url
        logging.info('Navigating to collection: {}'.format(collection_url))
        logging.info('Verifying the Collection Header div')
        self._get(self._collection_header_div)
        logging.info('Verifying Hero Image')
        heroimg = self._get(self._hero_image)
        link = heroimg.get_attribute('src')
        code = self.verify_arbitrary_web_asset(link)
        assert code == 200
        logging.info('Verifying Collection header info block...')
        orig_info_div_width = self.get_info_div_width()
        self._get(self._collection_header_info_div_title)
        # The following item can occasionally be pushed off visible DOM if the title (above) is
        #     particularly long
        try:
            self._get(self._collection_header_info_div_blurb)
        except ElementDoesNotExistAssertionError:
            logging.warning('Collection blurb pushed off visible DOM by long title')
        logging.info('Verifying Collection Image Credits')
        header_div = self._get(self._collection_header_div)
        self._actions.move_to_element_with_offset(header_div, 0, 200).perform()  # will center
        # over overlaying div otherwise
        logging.info('Verifying Collection Image Credits is hidden when mouse moves away')
        self._wait_for_element(self._get(self._collection_toolbar))
        toolbar = self._get(self._collection_toolbar)
        self._actions.move_to_element(toolbar).perform()  # move away from collection header
        # to make the credit div disappear
        # transition property is opacity: 0 outside, and 1 when mouse over image
        # need time to update web elements after moving
        time.sleep(1)
        image_credits = self._iget(self._hero_image_credits)
        opacity_outside = image_credits.value_of_css_property("opacity")
        assert opacity_outside == "0", 'Collection Image Credit should not be visible'
        self._actions.move_to_element(image_credits).click_and_hold(image_credits).perform()
        # need time to update web elements after moving
        time.sleep(1)
        opacity_mouse_over = image_credits.value_of_css_property("opacity")
        assert opacity_mouse_over == "1", \
            'Collection Image Credit should be visible with mouse over the image'
        # move away from collection header to make the credit div disappear
        self._actions.move_to_element(toolbar).perform()
        time.sleep(1)
        logging.info('Verifying header toggle...')
        self._wait_for_element(self._get(self._collection_header_info_div_toggle))
        toggle = self._get(self._collection_header_info_div_toggle)
        toggle_text = self._get(self._collection_header_info_div_toggle).text.strip()
        self.validate_text_exact(actual_text=toggle_text,
                                 expected_text='More', message='Incorrect toggle text')
        toggle.click()
        self._wait_for_element(self._get(self._collection_header_info_div_toggle))
        self._get(self._collection_header_info_div_toggle)
        toggle_text = self._get(self._collection_header_info_div_toggle).text.strip()
        self.validate_text_exact(actual_text=toggle_text,
                                 expected_text='Less', message='Incorrect toggle text')
        expanded_info_div_width = self.get_info_div_width()
        assert int(expanded_info_div_width) > int(orig_info_div_width)
        toggle.click()
        self._wait_for_element(self._get(self._collection_header_info_div_toggle))
        toggle_text = self._get(self._collection_header_info_div_toggle).text.strip()
        self.validate_text_exact(actual_text=toggle_text,
                                 expected_text='More', message='Incorrect toggle text')
        final_info_div_width = self.get_info_div_width()
        assert int(orig_info_div_width) == int(final_info_div_width)

    def validate_collection_toolbar(self):
        """
        Validate the existence of the CMS derived collection toolbar elements and their functions
        """
        logging.info('Validating Collections Toolbar...')
        self._get(self._collection_toolbar)
        section_menu = self._get(self._collection_toolbar_section_dropdown)
        section_menu.click()
        section_menu_items = self._gets(self._collection_toolbar_section_items)
        for item in section_menu_items:
            link = item.get_attribute('href')
            assert '#' in link  # cheap way of looking for named anchor
            code = self.verify_arbitrary_web_asset(link)
            assert code == 200
        section_menu.click()
        self._get(self._collection_toolbar_facebook_icon)
        self._get(self._collection_toolbar_twitter_icon)
        self._get(self._collection_toolbar_envelope_icon)

    def extract_fixture_data(self):
        """
        Extract the CMS derived fixture data for the current page from the Content Repository and
        then strip script elements resulting in a JSON object parsable as a dictionary.
        :return: JSON object
        """
        cms_host = config_cms_host
        cms_bucket = config_cms_bucket
        if environment == 'prod':
            fixture_addr = cms_host + 'objects/' + cms_bucket + '?key=c.' + \
                           self._driver.current_url.split('/')[3]
        else:
            fixture_addr = cms_host + 'objects/' + cms_bucket + '?key=c.' + \
                           self._driver.current_url.split('/')[5]
        fixture = requests.get(fixture_addr)
        fixture_data = json.loads(fixture.text)
        logging.info(json.loads(fixture.text))
        return fixture_data['content'].replace(
            '<script type=\"application/javascript\">\n      window.fixtureData = ', '') \
            .split(';\n    </script>')[0]

    def validate_billboard(self):
        """
        Determine whether current page has a defined billboard and whether it is present on
        the page.
        Validate that element has title, and if it has a defined button, there is text for that
        button and a valid web resource is targeted by its action.
        """
        logging.info('Validating Collections billboard...')
        self.set_timeout(10)
        fixture_data_json = self.extract_fixture_data()
        billboard = json.loads(fixture_data_json)
        billboard_object = billboard['billboards'][0]['enabled']
        if billboard_object is True:
            self._get(self._collection_billboard_div)
        else:
            self.restore_timeout()
            logging.info('No billboard defined for Collection.')
            return
        self.restore_timeout()
        billboard_title = self._get(self._collection_billboard_title).text.strip()
        assert len(billboard_title) > 0, 'Billboard Title is length 0 exclusive of whitespace'
        button_text = self._get(self._collection_billboard_button).text.strip()
        assert len(button_text) > 0, 'Billboard Button text is length 0 exclusive of whitespace'
        link = self._get(self._collection_billboard_button).get_attribute('href')
        if link is not None:
            code = self.verify_arbitrary_web_asset(link)
            assert code != 404
        else:
            assert False, 'Billboard Button has a null link definition!'

    def validate_text_widget(self):
        """
        Look at fixture data to determine whether there is one or more defined text widget objects
        For each widget validate title and text to be present and not null, and if a button is
         present, validate text of that button and its target web resource
        :return: integer representing number of text widgets examined
        """
        logging.info('Validating Text Widget(s) in RHC...')
        widget_count = 0
        self.set_timeout(10)
        fixture_data_json = self.extract_fixture_data()

        collection = json.loads(fixture_data_json)
        widget_objects = collection['widgets']

        text_widgets = []

        for widget in widget_objects:
            if widget['category'] == 'text':
                text_widgets.append(widget)

        if len(text_widgets) > 0:
            tws = self._gets(self._collection_preview_textwidget)
            for tw in tws:
                tw_title = self._get(self._collection_preview_textwidget_title).text.strip()
                assert len(tw_title) > 0, 'No defined textwidget title, exclusive of spaces'
                tw_blurb = self._get(self._collection_preview_textwidget_blurb).text.strip()
                assert len(tw_blurb) > 0, "No defined textwidget body text, exclusive of spaces"
                try:
                    self._get(self._collection_preview_textwidget_button)
                    tw_button_text = self._get(
                        self._collection_preview_textwidget_button).text.strip()
                    assert len(tw_button_text) > 0, \
                        "No defined textwidget button text, exclusive of spaces"
                    link = self._get(self._collection_preview_textwidget_button) \
                        .get_attribute('href')
                    code = self.verify_arbitrary_web_asset(link)
                    assert code != 404
                except ElementDoesNotExistAssertionError:
                    logging.error('No button defined for text widget')
                finally:
                    self.restore_timeout()
                widget_count = len(tws)
                return widget_count
        else:
            self.restore_timeout()
            logging.info('No TextWidget defined for Collection')
            return widget_count

    def validate_rss_widget(self):
        """
        Look at fixture data to determine whether there is one or more defined RSS widget objects
        For each widget validate title and RSS Feed Items. For each feed item, validate data/time,
        title and author

        :return: integer representing number of RSS widgets examined
        """
        logging.info('Validating RSS Widget(s) in RHC...')
        self.set_timeout(10)
        fixture_data_json = self.extract_fixture_data()
        collection = json.loads(fixture_data_json)
        widget_objects = collection['widgets']
        widget_count = 0
        rss_widgets = []

        for widget in widget_objects:
            if widget['category'] == 'rss':
                rss_widgets.append(widget)

        # TODO: check if we have more than 1 rss widget, example:
        # http://collections.plos.org/developing-computational-biology

        if rss_widgets:
            # The following selector is only present if there is a feed error
            data_test_selector = self._gets(self._collection_data_test_selector)
            if len(data_test_selector) >= 1:
                logging.warning('Blog feed is down, nothing to check')
            else:
                rssws = self._gets(self._collection_rsswidget_div)
                for rssw in rssws:
                    self._wait_for_element(self._get(self._collection_rsswidget_title))
                    rss_title = self._get(self._collection_rsswidget_title).text.strip()
                    assert len(rss_title) > 0, 'No defined RSSWidget title, exclusive of spaces'
                    logging.info('RSS title is {0!r}'.format(rss_title))
                    self._wait_for_element(self._get(self._collection_rsswidget_feeditem))
                    feed_items = self._gets(self._collection_rsswidget_feeditem)
                    assert len(feed_items) > 0, 'No feed items defined for RSS Widget'
                    for item in feed_items:
                        item_datetime = item.find_element(
                            *self._collection_rsswidget_feeditem_datetime).text.strip()
                        assert len(
                            item_datetime) > 0, 'No defined RSS Item date/time, exclusive of ' \
                                                'spaces'
                        item_title = item.find_element(
                            *self._collection_rsswidget_feeditem_title).text.strip()
                        assert len(item_title) > 0, 'No defined RSS Item Title, exclusive of spaces'
                        item_author = item.find_element(
                            *self._collection_rsswidget_feeditem_author).text.strip()
                        assert len(
                            item_author) > 0, 'No defined RSS Item Author, exclusive of spaces'
                self.restore_timeout()
            widget_count = len(rss_widgets)
        else:
            self.restore_timeout()
            logging.info('No RSS Widget defined for Collection')
        return widget_count

    def validate_rss_widget_function(self):
        """
        Look at fixture data to determine whether there is one or more defined RSS widget objects
        Validate the feed is not indicating an error state
        :return: boolean: True if functional, False if erroring blog feed
        """
        logging.info('Validating RSS Widget(s) in RHC...')
        fixture_data_json = self.extract_fixture_data()
        collection = json.loads(fixture_data_json)
        widget_objects = collection['widgets']
        rss_widgets = []

        for widget in widget_objects:
            if widget['category'] == 'rss':
                rss_widgets.append(widget)

        if rss_widgets:
            # The following selector is only present if there is a feed error
            try:
                self._gets(self._collection_data_test_selector)
            except ElementDoesNotExistAssertionError:
                return True
        else:
            logging.info('There is no rss widget for the collection {0!r}'
                         .format(collection['collection']['slug']))
        return True

    def validate_collection_items(self):
        """
        For each collection item in the collection, validate image source, title, link, author list,
        publisher and pubdate
        """
        logging.info('Validating Collection Items...')
        items = self._driver.find_elements_by_class_name('collection-item')
        assert len(items) > 0, "There are no collections items in this published collection!"
        for item in items:
            link = item.find_element_by_class_name('collection-item-thumbnail').get_attribute('src')
            code = self.verify_arbitrary_web_asset(link)
            assert code != 404
            title = item.find_element_by_class_name('collection-item-title').text.strip()
            assert len(
                title) > 0, 'Title text for item ' + title + ', is null, excluding whitespace'
            link = self._get(self._collection_item_title).get_attribute('href')
            code = self.verify_arbitrary_web_asset(link)
            assert code != 404
            authors_p = item.find_element_by_class_name('collection-item-authors').text.strip()
            authors = authors_p.split(',')
            assert len(authors) > 0 and len(
                authors[0].strip()) != 0, 'Author list for item ' + title + ' is empty'
            publisher = item.find_element_by_class_name(
                'collection-item-publisher-value').text.strip()
            assert len(publisher) > 0, \
                'Zero length publisher string for item ' + title + ' exclusive of white space.'
            pubdate = item.find_element_by_class_name('collection-item-pub-date-value').text.strip()
            assert len(pubdate) > 0, \
                'Zero length published date for item ' + title + ' exclusive of white space.'

    def validate_collection_items_thumb(self):
        items = self._gets(self._collection_item)
        assert len(items) > 0, "There are no collections items in this published collection!"
        for i, item in enumerate(items):
            title_el = self._gets(self._collection_item_title)[i]
            title_text = title_el.text.strip()
            thumbnails = self._gets(self._collection_item_img)
            thumbnail = thumbnails[i]
            self._scroll_into_view(thumbnail)
            self.move_to_elem(thumbnail)
            time.sleep(1)
            image_credits = self._gets(self._collection_item_img_credit)
            time.sleep(1)
            image_credit = image_credits[i]
            opacity_mouse_over = image_credit.value_of_css_property("opacity")
            assert opacity_mouse_over == "1", 'Collection Image Credit should be visible ' \
                                              'with mouse over the image'

            self._actions.move_to_element(title_el).perform()
            opacity_outside = image_credit.value_of_css_property("opacity")
            assert opacity_outside == "0", 'Collection Item Image Credit within collection ' \
                                           'item {0} should not be visible'.format(title_text)
            # checking the first item for now
            # TODO: check the whole list
            break

    def move_to_elem(self, elem):
        self._scroll_into_view(elem)
        self.scroll_by_pixels(-160)
        for i in range(5):
            try:
                self._actions.move_to_element_with_offset(elem, 10, 10) \
                    .click_and_hold(elem).perform()
                time.sleep(0.5)
                break
            except IndexError:
                self.scroll_by_pixels(-160)

    def page_ready(self):
        """
        Ensure the page is fully ready for testing by waiting for the joey container
        """
        self._wait_for_element(self._get(self._joey_container))
        logging.info('Ready to test!')
