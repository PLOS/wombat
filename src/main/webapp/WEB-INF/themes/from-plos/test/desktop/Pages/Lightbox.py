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

import json
import logging
from random import randint
import requests
import sys

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as exp_cond
from selenium.webdriver.support.ui import WebDriverWait

from .. import resources
from ..Pages.Article import Article
from ..Pages.ArticleBody import ArticleBody

__author__ = 'ivieira@plos.org'


class Lightbox(ArticleBody):
    """
    Model for Lightbox modal.

    Allows the following methods:
    1. validate_article_abstract_view_in_lightbox

    """

    def __init__(self, driver, url_suffix=''):
        super(Lightbox, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._article_body_lightbox_close_button = (By.CLASS_NAME, 'lb-close')
        self._article_body_lightbox_title = (By.ID, 'lb-title')
        self._article_body_lighbox_authors = (By.CSS_SELECTOR, '#lb-authors > span')
        self._article_body_title = (By.ID, 'artTitle')
        self._article_body_lightbox_zoom_in_button = (By.ID, 'lb-zoom-max')
        self._article_body_lightbox_zoom_reset_button = (By.CLASS_NAME, 'reset-zoom-btn')
        self._article_body_lightbox_loader = (By.CLASS_NAME, 'loader')
        self._article_body_lightbox_main_image = (By.CLASS_NAME, 'main-lightbox-image')

    def wait_for_lightbox_loading(self):
        self._check_for_invisible_element_boolean(self._article_body_lightbox_loader)

    def _waif_for_main_image_loaded(self):
        image = self._get(self._article_body_lightbox_main_image)
        self.set_timeout(3)
        try:
            self._wait.until(exp_cond.element_to_be_clickable(image))
            return True
        except:
            return False
        finally:
            self.restore_timeout()

    def open_lightbox(self):
        self._get(self._article_body_first_figure_inline).click()
        self.wait_for_lightbox_loading()

    def close_lightbox(self):
        self._get(self._article_body_lightbox_close_button).click()

    def validate_article_first_figure_inline(self):
        assert self._get(self._article_body_first_figure_inline)
        return self

    def validate_article_lightbox_close_button(self):
        self.open_lightbox()
        assert self._get(self._article_body_lightbox_close_button), 'No close button on the page'
        self.close_lightbox()
        return self

    def validate_article_lightbox_title(self):
        article_body_title = self._get(self._article_body_title).text
        self.open_lightbox()
        article_body_lightbox_title = self._get(self._article_body_lightbox_title).text
        assert article_body_title in article_body_lightbox_title
        self.close_lightbox()
        return self

    def validate_article_lightbox_authors(self):
        doi = Article.extract_page_escaped_doi(self)
        logging.info("   Calling Rhino api v1 to get article authors information...")
        current_ingestion = self.get_article_current_ingestion()
        response = requests.get(
                '{0}/articles/{1}/ingestions/{2}/authors'
                .format(resources.rhino_url, doi, current_ingestion))
        json_response = json.loads(response.text)
        json_authors = json_response['authors']
        if json_authors:
            if len(json_authors) > 4:  # AMBR-246: If there are more than 4 authors
                # show the first two and then the last one
                json_authors = [json_authors[0], json_authors[1],
                                json_authors[len(json_authors) - 1]]
            self.open_lightbox()
            lightbox_authors = self._gets(self._article_body_lighbox_authors)

            authors = [author.text for author in lightbox_authors]

            assert len(authors) <= 4, '{0!s} authors are shown in the lightbox. Not more ' \
                                      'than 4 authors should be displayed'.format(len(authors))

            for author in json_authors:
                assert author['fullName'] in authors, \
                    '{0} not in authors'.format(author['fullName'])

            self.close_lightbox()
            return self

    def _get_transform_matrix_from_image(self):
        image = self._get(self._article_body_lightbox_main_image)
        image_matrix = image.value_of_css_property('transform')
        image_matrix = image_matrix.replace('matrix(', '').replace(')', '').split(', ')
        matrix = map(lambda item: float(item), image_matrix)
        return matrix

    def _get_zoom_rate_from_image(self):
        image_matrix = self._get_transform_matrix_from_image()
        if sys.version_info[0] < 3:
            return image_matrix[0]
        else:
            return next(image_matrix)

    def _calculate_image_viewport_dimensions(self):
        image_container = self._driver.find_element_by_class_name('img-container')
        lightbox_footer = self._driver.find_element_by_id('lightbox-footer')
        lightbox_header = self._driver.find_element_by_class_name('lb-header')

        image_container_height = image_container.size['height'] - float(
                image_container.value_of_css_property('padding-top').replace('px', '')) - float(
                image_container.value_of_css_property('padding-bottom').replace('px', ''))
        lightbox_footer_height = lightbox_footer.size['height'] - float(
                lightbox_footer.value_of_css_property('padding-top').replace('px', '')) - float(
                lightbox_footer.value_of_css_property('padding-bottom').replace('px', ''))
        lightbox_header_height = lightbox_header.size['height'] - float(
                lightbox_header.value_of_css_property('padding-top').replace('px', '')) - float(
                lightbox_header.value_of_css_property('padding-bottom').replace('px', ''))

        viewport_dimensions = {
            'width': float(image_container.size['width']),
            'height': float(
                image_container_height - lightbox_footer_height - lightbox_header_height)
        }

        return viewport_dimensions

    def _get_main_image_expected_initial_position(self):
        image = self._get(self._article_body_lightbox_main_image)
        viewport_dimensions = self._calculate_image_viewport_dimensions()
        expected_position = {
            'top': int((viewport_dimensions['height'] - image.size['height']) / 2),
            'left': int((viewport_dimensions['width'] - image.size['width']) / 2)
        }
        return expected_position

    def range_position(self, expected_position):
        return range(expected_position - 1, expected_position + 2)

    def validate_article_lightbox_zoom_reset(self):
        self.open_lightbox()
        zoom_in_btn = self._get(self._article_body_lightbox_zoom_in_button)
        zoom_reset_btn = self._get(self._article_body_lightbox_zoom_reset_button)
        main_image = self._get(self._article_body_lightbox_main_image)
        self.wait_until_image_complete(main_image)
        initial_zoom_rate = self._get_zoom_rate_from_image()

        # Validate if we have the elements we need to test
        assert zoom_in_btn, 'No zoom in button on the page'
        assert zoom_reset_btn, 'No reset zoom button on the page'
        assert main_image, 'No main image on the page'

        # Validate if the image is centered
        image_matrix = self._get_transform_matrix_from_image()
        initial_expected_position = self._get_main_image_expected_initial_position()
        image_matrix = list(image_matrix)
        range_left = self.range_position(initial_expected_position['left'])
        range_top = self.range_position(initial_expected_position['top'])
        assert int(image_matrix[4]) in range_left, \
            'The image initial left position: {0!s} is not in the expected range: {1!s}'\
            .format(image_matrix[4], range_left)

        assert int(image_matrix[5]) in range_top, \
            'The image initial top position: {0!s} is not in the expected range: {1!s}'\
            .format(image_matrix[5], initial_expected_position['top'])

        click_limit = randint(10, 30)
        click_counter = 1
        while click_counter <= click_limit:
            zoom_in_btn.click()
            click_counter += 1

        # Validate if the zoom rate is the same from the expected click count
        added_zoom_rate = self._get_zoom_rate_from_image() - initial_zoom_rate
        base_zoom_rate = 0.05
        calculated_zoom_rate = round(added_zoom_rate / click_limit, 5)
        assert str(calculated_zoom_rate) == str(base_zoom_rate), \
            'The base zoom rate: {0!s} is different from the expected: {1!s}'\
            .format(calculated_zoom_rate, base_zoom_rate)

        # Reset the image zoom and validate if the zoom rate is equal the initial zoom rate
        zoom_reset_btn.click()
        new_zoom_rate = self._get_zoom_rate_from_image()
        assert str(new_zoom_rate) == str(initial_zoom_rate), \
            'The zoom rate: {0!s} is different from the initial zoom rate: {1!s}'\
            .format(new_zoom_rate, initial_zoom_rate)

        self.close_lightbox()
        return self

    def validate_article_lightbox_caption(self):
        self.open_lightbox()
        show_more_button = self._driver.find_element_by_id('view-more')
        show_more_wrapper = self._driver.find_element_by_id('view-more-wrapper')
        show_less_wrapper = self._driver.find_element_by_id('view-less-wrapper')

        assert show_more_button
        assert show_more_wrapper
        assert show_less_wrapper

        self.close_lightbox()
        return self

    def _get_article_figures_id_list(self):
        figures = self._driver.find_elements_by_class_name('figure')
        figures_id = map(lambda figure: figure.get_attribute('data-doi'), figures)
        return figures_id

    def _get_main_image_id(self):
        self._waif_for_main_image_loaded()
        main_image = self._driver.find_element_by_class_name('main-lightbox-image')
        main_image_id = main_image.get_attribute('src').split('id=')[1].replace('info:doi/', '')
        return main_image_id

    def _wait_main_image_change_id(self, new_id):
        wait = WebDriverWait(self._driver, 10)
        image_lightbox = self._get(
                (By.CSS_SELECTOR, 'img.main-lightbox-image[src*="' + new_id + '"]'))
        self.wait_until_image_complete(image_lightbox)

    def validate_article_lightbox_prev_next_buttons(self):
        figures_id = list(self._get_article_figures_id_list())
        if len(figures_id) == 1:
            logging.info('There is only one image on the lightbox, cannot validate next button')
            return self

        self.open_lightbox()
        next_image_button = self._driver.find_element_by_class_name('next-fig-btn')
        prev_image_button = self._driver.find_element_by_class_name('prev-fig-btn')

        assert next_image_button, 'No next image button on the page'
        assert prev_image_button, 'No previous image button on the page'

        if len(figures_id) == 2:
            max_button_clicks = 1
        else:
            max_button_clicks = randint(1, len(figures_id) - 1)

        next_button_click_count = 0
        prev_button_click_count = 0
        current_figure_index = 0

        while next_button_click_count < max_button_clicks:
            next_image_button.click()
            current_figure_index += 1
            expected_id = figures_id[current_figure_index]
            self._wait_main_image_change_id(expected_id)
            main_image_id = self._get_main_image_id()
            assert main_image_id == expected_id, \
                'Next image ID: {0!r} is different from the expected: {1!r}'\
                .format(main_image_id, expected_id)
            next_button_click_count += 1

        while prev_button_click_count < max_button_clicks:
            prev_image_button.click()
            current_figure_index -= 1
            expected_id = figures_id[current_figure_index]
            self._wait_main_image_change_id(expected_id)
            main_image_id = self._get_main_image_id()
            assert main_image_id == expected_id, \
                'Previous image ID: {0!r} is different from the expected: {1!r}'\
                .format(main_image_id, expected_id)
            prev_button_click_count += 1

        self.close_lightbox()
        return self

    def validate_article_lightbox_show_in_context_button(self):
        self.open_lightbox()
        show_in_context_button = self._driver.find_element_by_class_name('show-context')

        assert show_in_context_button, 'No "Show in Context" button on the page'

        # we need href attribute to use it as a part of dynamic figure locator
        figure_el_id = show_in_context_button.get_attribute('href').split('#')[1]
        show_in_context_button.click()
        # locating figure in context webelement using dynamically defined locator
        figure_in_context = self._driver.execute_script(
                "return $('.figure[data-doi*={}]')[0]".format(figure_el_id.split('-')[1]))

        assert self.element_in_viewport(self._driver, figure_in_context), \
            'Inline figure is not visible at the viewport'

        return self

    def validate_article_lightbox_drawer(self):
        self.open_lightbox()
        lightbox_items = list(self._get_article_figures_id_list())
        total_images = len(lightbox_items)

        if total_images == 1:
            logging.info('Only one item for the lightbox, skipping drawer test')
            self.close_lightbox()
            return self

        open_drawer_button = self._driver.find_element_by_class_name('all-fig-btn')

        assert open_drawer_button, 'No all figures button on the page'

        # Test if drawer is opening
        open_drawer_button.click()
        drawer = self._driver.find_element_by_id('figures-list')
        drawer_class = drawer.get_attribute('class')
        assert drawer_class == 'figures-list-open', 'Figure drawer is not open'

        items = drawer.find_elements_by_class_name('change-img')

        # Test if the images are clickable and changing main image as expected
        items_limit = 2
        items_count = 1
        for item in items:
            if (items_count <= items_limit):
                expected_id = item.get_attribute('data-doi')
                item.click()
                self._wait_main_image_change_id(expected_id)
                main_image_id = self._get_main_image_id()
                assert main_image_id == expected_id, \
                    'Visible image ID: {0!r} is different from the expected: {1!r}'\
                    .format(main_image_id, expected_id)
            items_count += 1

        # Test if drawer is closing
        open_drawer_button.click()
        drawer = self._driver.find_element_by_id('figures-list')
        drawer_class = drawer.get_attribute('class')
        assert drawer_class != 'figures-list-open', 'Figure drawer is open'

        self.close_lightbox()
        return self

    def validate_article_lightbox_download_buttons(self):
        self.open_lightbox()
        download_links = self._driver.find_elements_by_xpath(
            "//div[@id='download-buttons']/div[@class='item']/a")
        assert download_links, 'No download link on the page'

        for link in download_links:
            assert self._is_link_valid(link) is True, \
                'Invalid download link for: {0!r}'.format(link.text)

        self.close_lightbox()
        return self

    def element_in_viewport(self, driver, elem):
        """
        The method to check if web element is in the page viewport, checking left top
        Using JavaScript due to not reliable webdriver coordinates computing
        :param driver: driver instance
        :param elem: web element to check
        :return: True if left top corner of webelement is in the wiewport
        """
        elem_rect = driver.execute_script(
                "return arguments[0].getBoundingClientRect()", elem)
        elem_left_bound = elem_rect["left"]
        elem_top_bound = elem_rect["top"]

        win_width = driver.execute_script('return document.documentElement.clientWidth')
        win_height = driver.execute_script('return document.documentElement.clientHeight')

        return all((0 <= elem_left_bound <= win_width,
                    0 <= elem_top_bound <= win_height)
                   )
