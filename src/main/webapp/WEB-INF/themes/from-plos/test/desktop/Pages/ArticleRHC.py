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

__author__ = 'jgray@plos.org'

import json
import logging
import operator
import random
import re
from urllib.parse import unquote, unquote_plus, quote_plus

import requests
from selenium.webdriver.common.by import By

from .Article import Article
from .. import resources
from ...Base.Utils import strip_html


class ArticleRHC(Article):
    """
    Model an abstract of the right hand column of the Article page.
    """

    def __init__(self, driver, url_suffix=''):
        super(ArticleRHC, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._license = (By.ID, 'licenseShort')
        self._article_title = (By.ID, 'artTitle')
        self._article_doi = (By.ID, 'artDoi')
        self._article_right_hand_column = (By.CLASS_NAME, 'article-aside')
        self._right_hand_column_download_div = (By.CLASS_NAME, 'dload-menu')
        self._right_hand_column_download_pdf = (By.ID, 'downloadPdf')
        self._right_hand_column_download_menu = (By.CLASS_NAME, 'dload-hover')
        self._right_hand_column_download_menu_popout = (By.CLASS_NAME, 'dload-xml')
        self._right_hand_column_download_citation = (By.ID, 'downloadCitation')
        self._right_hand_column_download_xml = (By.ID, 'downloadXml')
        self._right_hand_column_print_div = (By.ID, 'printArticle')
        self._right_hand_column_print_popout = (By.CLASS_NAME, 'print-options')
        self._right_hand_column_print_local = (By.ID, 'printBrowser')
        self._right_hand_column_print_ezreprint = (By.XPATH, "//li/a[@title='Odyssey Press']")
        self._right_hand_column_print_order_reprints = (By.XPATH, "//li/a[@title='Order Reprints']")
        self._right_hand_column_crossmark_link = (By.ID, 'open-crossmark')
        self._right_hand_column_crossmark_logo = (By.CSS_SELECTOR, "a[data-target='crossmark']")
        self._crossmark_iframe = (By.ID, 'crossmark-dialog-frame')
        self._crossmark_iframe_doi = (By.CLASS_NAME, 'doi')
        self._crossmark_closer = (By.XPATH, '//button[@title="close"]')
        self._related_articles_div = (By.XPATH, "//div[@class='related-articles-container']")
        self._right_hand_column_share_div = (By.ID, 'shareArticle')
        self._share_reddit = (By.ID, 'shareReddit')
        self._share_google = (By.ID, 'shareGoogle')
        self._share_facebook = (By.ID, 'shareFacebook')
        self._share_linkedin = (By.ID, 'shareLinkedIn')
        self._share_mendeley = (By.ID, 'shareMendeley')
        self._share_pubchase = (By.ID, 'sharePubChase')
        self._share_twitter = (By.ID, 'twitter-share-link')
        self._share_email = (By.ID, 'shareEmail')
        self._twitter_hashtag = ''
        self._right_hand_column_iitfc_div = (By.CLASS_NAME, 'aside-container')
        self._right_hand_column_iitfc_link_list = (By.ID, 'collectionList')
        self._right_hand_column_iitfc_anchor = (By.XPATH, "//ul[@id='collectionList']/li/a")
        self._subject_area_div = (By.CSS_SELECTOR, "div.subject-areas-container")
        self._subject_area_header = (By.CSS_SELECTOR, ".subject-areas-container > h3")
        self._subject_area_info = (By.CSS_SELECTOR, ".subject-areas-container #subjInfo")
        self._subject_area_info_text = (By.CSS_SELECTOR, ".subject-areas-container #subjInfoText")
        self._subject_area_text_link = (By.CSS_SELECTOR, ".subject-areas-container #subjInfoText a")
        self._subject_area_list = (By.ID, 'subjectList')
        self._subject_area_list_li = (By.CSS_SELECTOR, '#subjectList > li')
        self._subject_area_taxoflag = (By.CSS_SELECTOR, 'span.taxo-flag')
        self._floater_close = (By.XPATH, "//div[@class='close-floater' and @title='close']")
        self._no_flag_button = (By.ID, "noFlag")
        self._flag_it_button = (By.ID, "flagIt")
        self._cat_links = (By.CSS_SELECTOR, '#subjectList a')
        self._all_aside_divs_with_class = (By.CSS_SELECTOR, '.article-aside > div[class]')
        self._alm_signpost = (By.ID, 'almSignposts')

    # POM Actions
    def assert_right_hand_column(self):
        self._get(self._article_right_hand_column)
        return self

    def assert_download_div(self):
        self._get(self._right_hand_column_download_div)
        return self

    def assert_download_pdf_button(self):
        self._get(self._right_hand_column_download_pdf)
        return self

    def validate_download_pdf(self):
        download_pdf = self._get(self._right_hand_column_download_pdf)
        article_doi_text = Article.extract_page_doi(self)
        pdf_link = download_pdf.get_attribute('href')
        lstripped_link = re.sub(r'^.*id=', '', pdf_link)
        rstripped_link = lstripped_link.replace('&type=printable', '')
        final_link = rstripped_link.replace("%2F", "/")
        assert article_doi_text == final_link
        return self

    def moveto_section_header_doi(self):
        article_doi = self._get(self._article_doi)
        self._actions.move_to_element(article_doi).perform()
        return self

    def assert_download_menu(self):
        dl_menu = self._get(self._right_hand_column_download_menu)
        self._actions.move_to_element(dl_menu).perform()
        # reset mouseover state
        self.moveto_section_header_doi()
        return self

    def validate_citation_download(self):
        article_doi_text = Article.extract_page_doi(self)
        dl_menu = self._get(self._right_hand_column_download_menu)
        self._actions.move_to_element(dl_menu).click_and_hold(dl_menu).perform()
        download_cite = self._get(self._right_hand_column_download_citation)
        cite_link = download_cite.get_attribute('href')
        lstripped_link = re.sub(r'^.*id=', '', cite_link).replace('%2F', '/')
        assert lstripped_link == article_doi_text
        # reset mouseover state
        self.moveto_section_header_doi()
        return self

    def validate_xml_download(self):
        article_doi_text = Article.extract_page_doi(self)
        dl_menu = self._get(self._right_hand_column_download_menu)
        self._actions.move_to_element(dl_menu).perform()
        download_xml = self._get(self._right_hand_column_download_xml)
        xml_link = download_xml.get_attribute('href')
        lstripped_link = re.sub(r'^.*id=', '', xml_link)
        rstripped_link = re.sub(r'\.XML', '', lstripped_link)
        final_link = rstripped_link.replace("%2F", "/").replace('&type=manuscript', '')
        assert article_doi_text == final_link
        return self

    def assert_print_div(self):
        self._get(self._right_hand_column_print_div)
        return self

    def validate_local_print(self):
        print_menu = self._get(self._right_hand_column_print_div)
        self._actions.move_to_element(print_menu).perform()
        self._get(self._right_hand_column_print_local)
        # reset mouseover state
        self.moveto_section_header_doi()
        return self

    def validate_ezreprint(self):
        ezreprint = ''
        order_reprints = ''
        article_doi_text = Article.extract_page_doi(self)
        article_title_text = self._get(self._article_title).text
        print_menu = self._get(self._right_hand_column_print_div)
        self._actions.move_to_element(print_menu).perform()
        self.set_timeout(1)
        try:
            ezreprint = self._get(self._right_hand_column_print_ezreprint)
        except:
            order_reprints = self._get(self._right_hand_column_print_order_reprints)
        self.restore_timeout()
        if ezreprint:
            ezreprint_href = ezreprint.get_attribute('href')
        else:
            ezreprint_href = order_reprints.get_attribute('href')
        # validate reprint order request type
        re.search('.*(reprint_order\.php).*', ezreprint_href).group(1)
        # validate correct doi
        logging.info("Validating that we are passing the doi to the reprint service")
        ezreprint_href_doi = \
            re.search('.*(10.1371%2Fjournal.p.{3}.\d{7}).*', ezreprint_href).group(1)
        ezreprint_href_doi_replaced = ezreprint_href_doi.replace('%2F', '/')
        assert ezreprint_href_doi_replaced == article_doi_text
        # validate correct article title in order
        logging.info("Validating that we are passing the correct parameters to the reprint service")
        ezreprint_href_title = re.search('.*title=(.*)&author_name=.*', ezreprint_href).group(1)
        ezreprint_href_title_decoded = ezreprint_href_title.encode('utf-8')

        ezreprint_href_title_decoded = unquote(ezreprint_href_title_decoded.decode('utf-8'))
        ezreprint_href_title_decoded = ' '.join(ezreprint_href_title_decoded.split())
        article_title_text_decoded = article_title_text.encode('utf-8')
        logging.info(article_title_text_decoded)
        logging.info(ezreprint_href_title_decoded)

        assert article_title_text_decoded.decode('utf-8') == ezreprint_href_title_decoded, (
            article_title_text_decoded.decode('utf-8'), ezreprint_href_title_decoded)
        # reset mouseover state
        self.moveto_section_header_doi()
        return self

    def validate_crossmark_link(self):
        article_type_text = self._get(self._article_doi).text
        article_doi_text = article_type_text.lstrip('DOI: ')
        crossmark_link = self._get(self._right_hand_column_crossmark_link)
        crossmark_link.click()
        # Switch to iframe
        xmark_iframe = self._crossmark_iframe
        self.traverse_to_frame(xmark_iframe[1])
        xm_doi = self._get(self._crossmark_iframe_doi).text
        xm_doi_stripped = xm_doi.lstrip('https://doi.org/')
        assert xm_doi_stripped == article_doi_text
        # switch to default context
        self.traverse_from_frame()
        closer = self._get(self._crossmark_closer)
        closer.click()
        return self

    def assert_crossmark_logo(self):
        self._get(self._right_hand_column_crossmark_logo)
        return self

    def validate_related_articles(self):
        # get the doi
        article_doi_text = Article.extract_page_escaped_doi(self)

        all_divs = self._gets(self._all_aside_divs_with_class)
        related_articles_div = [div for div in all_divs
                                if 'related-articles-container' in div.get_attribute('class')]

        # make a call to rhino to get the related article information
        response = requests.get(
            resources.rhino_url + '/articles/' + article_doi_text + '/relationships')
        json_response = json.loads(response.text)
        related_articles_outbound = json_response['outbound']
        related_articles_inbound = json_response['inbound']

        if not (related_articles_outbound or related_articles_inbound):
            # check to see if there is any related articles
            # get all div's under aside.article_aside and check if there is no div
            # with 'related-articles-container' class

            # related articles div should not exist
            assert not related_articles_div, '{0!r} div should not exist' \
                .format('related-articles-container')

        else:
            # check to make sure that the related articles in the rhino response matches up with the
            # related articles in the related articles div

            # get the related article links from the page
            div = self._get(self._related_articles_div)
            related_article_links = div.find_elements_by_tag_name("a")

            if len(related_articles_inbound) == len(related_article_links):
                assert len(related_articles_inbound) == len(related_article_links)
                self.validate_related_articles_helper(related_articles_inbound,
                                                      related_article_links)
            elif len(related_articles_outbound) == len(related_article_links):
                assert len(related_articles_outbound) == len(related_article_links)
                self.validate_related_articles_helper(related_articles_outbound,
                                                      related_article_links)

        return self

    def validate_related_articles_helper(self, related_articles, related_article_links):
        for related_article in related_articles:
            found = False

            for related_article_link in related_article_links:
                # http://stackoverflow.com/questions/753052/strip-html-from-strings-in-python
                # picked a simpler solution in the thread
                related_article['title'] = re.sub(r'(<!--.*?-->|<[^>]*>)', '',
                                                  related_article['title'])
                # info:doi/ string needs to be removed if it is there
                related_article_doi = related_article['doi'].replace("info:doi/", "")

                # check the article title and the doi in the link
                if (
                    related_article['title'].strip() == related_article_link.text.strip()) \
                        and (related_article_doi in related_article_link.get_attribute("href")):
                    found = True
                    break
            assert found

    def assert_share_menu(self):
        self._wait_for_element(self._get(self._right_hand_column_share_div))
        share_div = self._get(self._right_hand_column_share_div)
        self._actions.move_to_element(share_div).perform()

        self._get(self._share_reddit)
        self._get(self._share_google)
        self._get(self._share_facebook)
        self._get(self._share_linkedin)
        self._get(self._share_mendeley)
        self._get(self._share_pubchase)
        self._get(self._share_twitter)
        self._get(self._share_email)
        # reset mouseover state
        self.moveto_section_header_doi()
        return self

    def validate_share_menu_items(self):
        self._scroll_into_view(self._get(self._alm_signpost))
        share_div = self._get(self._right_hand_column_share_div)
        self._actions.move_to_element(share_div).perform()

        doi = Article.extract_page_doi(self)
        title = self._get(self._article_title).text

        escaped_doi_url = quote_plus("https://dx.plos.org/" + doi)
        title = re.sub(r'<[^>]*>', '', title)

        # validate the url

        # reddit
        expected_link = "https://www.reddit.com/submit?url=" + escaped_doi_url
        actual_link = self._get(self._share_reddit).get_attribute("href")
        assert actual_link == expected_link

        # google+
        expected_link = "https://plus.google.com/share?url=" + escaped_doi_url
        actual_link = self._get(self._share_google).get_attribute("href")
        assert actual_link == expected_link

        # facebook
        partial_expected_link = "https://www.facebook.com/share.php?u=" + escaped_doi_url
        actual_link = self._get(self._share_facebook).get_attribute("href")
        assert partial_expected_link in actual_link
        actual_link_title = re.search('.*t=(.*)', actual_link).group(1)
        actual_link_title_encode = actual_link_title.encode('utf-8')
        actual_link_title_encode = unquote(actual_link_title_encode.decode('utf-8'))
        title_encode = title.encode('utf-8')
        assert title_encode.decode('utf-8') == actual_link_title_encode, \
            (title_encode, actual_link_title_encode)
        # linkedin
        partial_expected_link = "https://www.linkedin.com/shareArticle?url=" + escaped_doi_url
        actual_link = self._get(self._share_linkedin).get_attribute("href")
        assert partial_expected_link in actual_link

        partial_text = "&summary=Checkout%20this%20article%20I%20found%20at%20PLOS"
        assert partial_text in actual_link
        actual_link_title = re.search('.*title=(.*)&summary.*', actual_link).group(1)
        actual_link_title_encode = actual_link_title.encode('utf-8')

        actual_link_title_encode = unquote(actual_link_title_encode.decode('utf-8'))
        assert title_encode.decode('utf-8') in actual_link_title_encode, \
            (title_encode, actual_link_title_encode)

        # mandeley
        expected_link = "https://www.mendeley.com/import/?url=" + escaped_doi_url
        actual_link = self._get(self._share_mendeley).get_attribute("href")
        assert actual_link == expected_link, (actual_link, expected_link)

        # pubchase
        expected_link = "https://www.pubchase.com/library?add_aid=" + doi + "&source=plos"
        actual_link = self._get(self._share_pubchase).get_attribute("href")
        assert actual_link == expected_link

        # twitter
        partial_expected_link = "https://twitter.com/intent/tweet?url=" + escaped_doi_url
        actual_link = self._get(self._share_twitter).get_attribute("href")
        assert partial_expected_link in actual_link

        partial_title = self._twitter_hashtag + ': ' + title
        partial_title = partial_title[:100]
        actual_link_title = re.search('.*PLOS.*%3A%20(.*)%20...', actual_link).group(1)
        actual_link_title_encode = actual_link_title.encode('utf-8')
        actual_link_title_encode = unquote(actual_link_title_encode.decode('utf-8'))
        tweet_title = re.search('.*?PLOS.*?: (.*)', partial_title).group(1)
        tweet_title = tweet_title.encode('utf-8')
        logging.info(tweet_title)
        logging.info(actual_link_title_encode)
        assert (tweet_title.decode('utf-8') in actual_link_title_encode) or (
            actual_link_title_encode in tweet_title.decode('utf-8'))
        # email
        partial_email_link = "mailto:?subject=" + title
        actual_link = unquote(self._get(self._share_email).get_attribute("href"))
        assert partial_email_link in actual_link, \
            'partial link: {0} is not in actual link: {1}'.format(partial_email_link, actual_link)

        # # reset mouseover state
        # self.moveto_section_header_doi()

    def validate_included_in_the_following_collection(self):
        doi = self.extract_page_escaped_doi()
        endpoint = 'articles/{0}'.format(doi)
        url = '{0}/{1}?lists=collection'.format(resources.rhino_url, endpoint)
        response = requests.get(url)
        collections = json.loads(response.text)

        if not collections:
            return self

        # we need only 'collection' with type 'collection'
        collections = [item for item in collections if item['type'] == 'collection']
        if not collections:
            return self

        collections_el = self._driver.find_elements_by_css_selector('ul#collectionList li a')

        assert len(collections_el) == len(collections), \
            'The included collections length: {0} is not ' \
            'the expected: {1}'.format(str(len(collections_el)), str(len(collections)))

        for key, collection in enumerate(collections):
            collection_el = collections_el[key]

            # Validate collection title
            title = collection_el.text.strip()
            expected_title = strip_html(collection['title']).strip().replace('amp;', '')
            assert title == expected_title, 'The collection title: {0} is not the expected: {1}' \
                .format(title, expected_title)

            # Validate collection key
            link = collection_el.get_attribute('href')
            link_parts = link.split('/')
            expected_key = collection['key'].strip()
            assert expected_key in link_parts, \
                'The collection link: {0} don\'t have the expected key: {1}'\
                .format(link, expected_key)

        return self

    def assert_subject_areas(self):
        self._get(self._subject_area_div)
        return self

    def click_subject_area_link(self):
        subject_area_list = self._get(self._subject_area_list)
        subject_area_links = subject_area_list.find_elements_by_tag_name('a')
        random.choice(subject_area_links).click()

    def click_floating_header_closer(self):
        try:
            floater_closer = self._get(self._floater_close)
            floater_closer.click()
        except:
            pass
        return self

    def validate_subject_areas(self):
        journal, journal_name = self.get_journal_info()
        subject_area_div = self._get(self._subject_area_div)
        self._scroll_into_view(subject_area_div)
        self.scroll_by_pixels(-120)
        self.close_floating_title_top()

        self._get(self._subject_area_div)

        # verify the "Subject Areas" heading
        header = self._get(self._subject_area_header)  #
        assert "Subject Areas" in header.text

        # verify the "?" button and text
        subj_info_div = self._get(self._subject_area_info)
        assert subj_info_div.text == "?"
        self._get(self._subject_area_info)
        subj_info_text_div_i = self._iget(self._subject_area_info_text)

        info_text = "For more information about PLOS Subject Areas, click here.\nWe want your " \
                    "feedback. Do these Subject Areas make sense for this article? " \
                    "Click the target next to the incorrect Subject Area and let us know. " \
                    "Thanks for your help!"
        actual_text = self.normalize_spaces(subj_info_text_div_i.get_attribute('innerText'))

        self.validate_text_exact(actual_text=actual_text,
                                 expected_text=self.normalize_spaces(info_text))

        helpful_link = self._iget(self._subject_area_text_link)
        actual_link = helpful_link.get_attribute("href")
        expected_link = "https://github.com/PLOS/plos-thesaurus/blob/develop/README.md"
        assert expected_link in actual_link

        # verify that the subject areas are sorted in the weight order
        article_doi_text = Article.extract_page_escaped_doi(self)
        url = '{0}/articles/{1}/categories'.format(resources.rhino_url, article_doi_text)
        response = requests.get(url)
        categories = json.loads(response.text)

        # subject_area_list = self._get(self._subject_area_list)

        collapsed_categories = {}
        for category in categories:
            short_category = category['path'].split('/')[-1]
            if short_category not in collapsed_categories:
                collapsed_categories[short_category] = category['weight']

        expected_categories = sorted(collapsed_categories.items(), key=operator.itemgetter(0))
        expected_categories = sorted(expected_categories, key=operator.itemgetter(1), reverse=True)

        subject_area_links = self._gets(self._cat_links)
        actual_categories = [sb_link.text for sb_link in subject_area_links]
        assert len(actual_categories) == len(expected_categories)
        subject_area_not_found = False
        not_found_categories = \
            [category for category in actual_categories
             if category not in collapsed_categories.keys()]
        if not_found_categories:
            logging.info("Not found category list: {0!r}".format(not_found_categories))
            subject_area_not_found = True
        else:
            logging.info('all expected_categories found in actual_categories')

        # validate correct link ('href') to search by clicking on the subject area,
        # we are not clicking and checking request here, as it is covered by
        # test_search_result -> test_subject_area_search
        # just validating 'href' as a string
        subject_area_links = self._gets(self._cat_links)
        actual_hrefs = [href.get_attribute('href') for href in subject_area_links]
        flag = False

        for i, category in enumerate(actual_categories):
            actual_href = actual_hrefs[i]
            actual_href_unquote = unquote_plus(actual_href)
            expected_href = '/{0!s}/search?filterSubjects={1!s}&filterJournals={2!s}&q=' \
                .format(journal["journal_url_name"], category, journal["rhinoJournalKey"])
            logging.info('Validaing expected ref: {0!s} vs actual ref: {1!s}'
                         .format(expected_href, actual_href))
            try:
                assert expected_href in actual_href_unquote
            except:
                logging.error('Expected href does not match subject area\'s actual href')
                flag = True

        # validate feedback mechanism: feedback on whether or not a subject area term is
        # appropriately assigned to an article
        self.validate_flagging()

        assert not flag, 'subject area links have incorrect href'
        assert not subject_area_not_found, \
            'Expected category was not found in actual categories'

        return self

    def validate_flagging(self):
        """
        The method to validate subject area flagging/feedback mechanism:
        feedback on whether or not a subject area term is appropriately assigned to an article
        :return:
        """
        self._wait_for_element(self._get(self._subject_area_list))
        subject_area_lis = self._gets(self._subject_area_list_li)
        # check 1 random category from the list with random answer for flagging (yes/no)
        selected_category = random.choice(subject_area_lis)
        selected_category_name = selected_category.find_element_by_tag_name("a").text
        selected_yes_no = random.choice([self._no_flag_button, self._no_flag_button])
        selected_yes_no_text = "no flag" if selected_yes_no == self._no_flag_button else "flag it"
        logging.info('selected category: {0!r}, selected choice to flag: {1!r}'
                     .format(selected_category_name, selected_yes_no_text))
        sa_link = selected_category.find_element_by_tag_name("a")
        # TODO: verify that when you hover over the button, it changes state
        # self._actions.move_to_element(sa_link).perform()
        # b_color = sa_link.value_of_css_property("background-color")
        # #3c63af
        # assert b_color == 'rgba(60, 99, 175, 1)'
        # TODO: verify that when you hover over the circle, it changes state
        # need to capture the image change
        sa_circle = selected_category.find_element_by_tag_name("span")
        sa_circle.click()
        self._wait_for_element(selected_category.find_element_by_class_name("taxo-explain"))
        popup_text = (selected_category.find_element_by_class_name("taxo-explain")).text
        assert "Is the Subject Area" in popup_text
        assert "\"{}\"".format(sa_link.text) in popup_text
        assert "applicable to this article?".format(sa_link.text) in popup_text
        yes_no_button = selected_category.find_element(*selected_yes_no)
        yes_no_button.click()
        # TODO: verify that the change was made in the db?
        # check to make sure that clicked circle state has been retained.
        subject_area_list = self._get(self._subject_area_list)
        subject_area_lis = subject_area_list.find_elements_by_tag_name('li')
        for i, sa_li in enumerate(subject_area_lis):
            sa_circle = sa_li.find_element_by_tag_name("span")
            taxo_flag_class_name = sa_circle.get_attribute('class')
            expected_taxo_flag_class_name = \
                'taxo-flag flagged' if selected_yes_no == self._flag_it_button else 'taxo-flag'
            current_category = sa_li.find_element_by_tag_name("a").text
            self.validate_text_exact(actual_text=taxo_flag_class_name,
                                     expected_text=expected_taxo_flag_class_name,
                                     message='Incorrect web page taxo-flag class for the '
                                             'category: {0!r}'.format(current_category))
