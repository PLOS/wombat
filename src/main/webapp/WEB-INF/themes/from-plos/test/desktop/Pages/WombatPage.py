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

from selenium.webdriver.common.by import By

from ...Base.Config import base_url, collections_url, blogs_url
from ...Base.PlosPage import PlosPage

__author__ = 'jgray@plos.org'


# Variable definitions

class WombatPage(PlosPage):
    """
    Model an abstract base Wombat page for desktop
    """
    expected_isc_link = 'http://www.isc.org/'

    expected_privacy_link = 'https://www.plos.org/privacy-policy'

    expected_terms_link = 'https://www.plos.org/terms-of-use'

    expected_ads_link = 'https://www.plos.org/advertise/'

    expected_media_link = 'https://www.plos.org/media-inquiries'

    expected_pubs_link = 'https://www.plos.org/publications/journals/'

    expected_pbio_link = '{0!s}/plosbiology/'.format(base_url.rstrip('/'))

    expected_pmed_link = '{0!s}/plosmedicine/'.format(base_url.rstrip('/'))

    expected_pcbi_link = '{0!s}/ploscompbiol/'.format(base_url.rstrip('/'))

    expected_pcur_link = 'http://currents.plos.org/'

    expected_pgen_link = '{0!s}/plosgenetics/'.format(base_url.rstrip('/'))

    expected_ppat_link = '{0!s}/plospathogens/'.format(base_url.rstrip('/'))

    expected_pone_link = '{0!s}/plosone/'.format(base_url.rstrip('/'))

    expected_pntd_link = '{0!s}/plosntds/'.format(base_url.rstrip('/'))

    expected_porg_link = 'https://www.plos.org/'

    expected_blog_link = '{0}'.format(blogs_url)

    expected_coll_link = collections_url

    expected_feedback_link = 'mailto:webmaster@plos.org'

    expected_help_link = 'https://www.plos.org/contact'

    expected_lockss_link = '/lockss-manifest'

    expected_ca_corp_stmt = 'PLOS is a nonprofit 501(c)(3) corporation, #C2354500, based ' \
                            'in San Francisco, California, US'

    def __init__(self, driver, url_suffix=''):
        super(WombatPage, self).__init__(driver, url_suffix)

        # Locators - Instance members
        self._nav_user = (By.ID, 'user')
        self._sign_in_link = (By.CSS_SELECTOR, 'li.highlighted > a')
        self._nav_main = (By.ID, 'pagehdr')
        self._logo = (By.CSS_SELECTOR, "#pagehdr .logo a")
        self._search_widget = (By.XPATH, "//button[@type='submit']")

        self._publish_menu_ambra = (By.ID, "mn-02")
        self._publish_menu = (By.ID, 'publish')

        self._about_menu_ambra = (By.ID, "mn-03")
        self._about_menu = (By.ID, 'about')

        self._browse_menu = (By.ID, "browse")
        self._browse_menu_ambra = (By.ID, "browse")

        self._for_authors_menu = (By.ID, "for-authors")

        self._page_footer = (By.CSS_SELECTOR, '#pageftr > .row')
        self._footer_logo = (By.CLASS_NAME, 'logo-footer')
        self._footer_version = (By.CSS_SELECTOR, 'p[class="nav-special"] > a')
        self._footer_isc_link = (By.XPATH, '//p[@class="nav-special"]/a[2]')
        self._footer_privacy_policy_link = (By.CSS_SELECTOR, '#ftr-privacy')
        self._footer_terms_of_use_link = (By.CSS_SELECTOR, '#ftr-terms')
        self._footer_copyright_link = (By.CSS_SELECTOR, '#ftr-copyright')
        self._footer_advertise_link = (By.CSS_SELECTOR, '#ftr-advertise')
        self._footer_media_inquiries_link = (By.CSS_SELECTOR, '#ftr-media')
        self._footer_publications_link = (By.CSS_SELECTOR, 'li.ftr-header > a')
        self._footer_pbio_link = (By.ID, 'ftr-bio')
        self._footer_pmed_link = (By.ID, 'ftr-med')
        self._footer_pcbi_link = (By.ID, 'ftr-compbio')
        self._footer_pgen_link = (By.ID, 'ftr-gen')
        self._footer_ppat_link = (By.ID, 'ftr-path')
        self._footer_pone_link = (By.ID, 'ftr-one')
        self._footer_pntd_link = (By.ID, 'ftr-ntds')
        self._footer_porg_link = (By.ID, 'ftr-home')
        self._footer_blog_link = (By.ID, 'ftr-blog')
        self._footer_coll_link = (By.ID, 'ftr-collections')
        self._footer_feedback_link = (By.ID, 'ftr-feedback')
        self._footer_help_link = (By.ID, 'ftr-contact')
        self._footer_lockss_link = (By.ID, 'ftr-lockss')
        self._footer_ca_corp_stmt = (By.CLASS_NAME, 'footer-non-profit-statement')

    # POM Actions

    def validate_sign_in_button(self):
        self._get(self._nav_user).find_element_by_partial_link_text('sign in')
        return self

    def is_user_logged(self):
        btn = self._get(self._sign_in_link)
        return btn.text == 'sign out'

    def click_sign_in(self):
        sign_in_link = self._get(self._nav_user).find_element(*self._sign_in_link)
        sign_in_link.click()
        return self

    def click_sign_out(self):
        nav_user = self._get(self._nav_user)
        sign_out_link = nav_user.find_element_by_partial_link_text('sign out')
        self._scroll_into_view(sign_out_link)
        try:
            sign_out_link.click()
        except:
            self.click_covered_element(sign_out_link)
        return self

    def click_logo(self):
        logo_link = self._get(self._nav_main).find_element(*self._logo)
        logo_link.click()
        return self

    def click_search_widget(self):
        swidget_link = self._get(self._nav_main).find_element(*self._search_widget)
        swidget_link.click()
        return self

    def hover_browse(self):
        browse = self._get(self._browse_menu)
        self._actions.move_to_element(browse).perform()
        return self

    def hover_browse_ambra(self):
        browse = self._get(self._browse_menu_ambra)
        self._actions.move_to_element(browse).perform()
        return self

    def hover_publish(self):
        publish = self._get(self._publish_menu)
        self._actions.move_to_element(publish).perform()
        return self

    def hover_publish_ambra(self):
        publish = self._get(self._publish_menu_ambra)
        self._actions.move_to_element(publish).perform()
        return self

    def hover_about(self):
        about = self._get(self._about_menu)
        self._actions.move_to_element(about).perform()
        return self

    def hover_about_ambra(self):
        about = self._get(self._about_menu_ambra)
        self._actions.move_to_element(about).perform()
        return self

    def moveto_footer(self):
        footer = self._get(self._page_footer)
        self._scroll_into_view(footer)
        return self

    def validate_footer_logo(self):
        self._get(self._footer_logo)
        return self

    def validate_footer_nav_aux(self):
        """
        The footer implementation is split into four different sections. nav_aux contains the
        horizontal links along the left bottom of the footer.
        Validate the links point to the right place.
        :return: success/assertion exception
        """
        privacy_link = self._get(self._footer_privacy_policy_link).get_attribute('href')
        self.validate_text_exact(privacy_link.rstrip('/'), self.expected_privacy_link.rstrip('/'))
        terms_link = self._get(self._footer_terms_of_use_link).get_attribute('href')
        self.validate_text_exact(terms_link.rstrip('/'), self.expected_terms_link.rstrip('/'))
        ads_link = self._get(self._footer_advertise_link).get_attribute('href')
        self.validate_text_exact(ads_link.rstrip('/'), self.expected_ads_link.rstrip('/'))
        media_link = self._get(self._footer_media_inquiries_link).get_attribute('href')
        self.validate_text_exact(media_link.rstrip('/'), self.expected_media_link.rstrip('/'))
        help_link = self._get(self._footer_help_link).get_attribute('href')
        self.validate_text_exact(help_link.rstrip('/'), self.expected_help_link.rstrip('/'),
                                 'Incorrect help link')
        return self

    def validate_footer_link_column_one(self):
        """
        The footer implementation is split into four different sections. footer link column one
          contains the first set of vertical links pointing at the various PLOS publications.
        Validate the links point to the correct place.
        :return: success/assertion exception
        """
        pubs_link = self._get(self._footer_publications_link).get_attribute('href')
        self.validate_text_exact(pubs_link.rstrip('/'), self.expected_pubs_link.rstrip('/'))
        pbio_link = self._get(self._footer_pbio_link).get_attribute('href')
        self.validate_text_exact(pbio_link.rstrip('/'), self.expected_pbio_link.rstrip('/'))
        pmed_link = self._get(self._footer_pmed_link).get_attribute('href')
        self.validate_text_exact(pmed_link.rstrip('/'), self.expected_pmed_link.rstrip('/'))
        pcbi_link = self._get(self._footer_pcbi_link).get_attribute('href')
        self.validate_text_exact(pcbi_link.rstrip('/'), self.expected_pcbi_link.rstrip('/'))
        pgen_link = self._get(self._footer_pgen_link).get_attribute('href')
        self.validate_text_exact(pgen_link.rstrip('/'), self.expected_pgen_link.rstrip('/'))
        ppat_link = self._get(self._footer_ppat_link).get_attribute('href')
        self.validate_text_exact(ppat_link.rstrip('/'), self.expected_ppat_link.rstrip('/'))
        pone_link = self._get(self._footer_pone_link).get_attribute('href')
        self.validate_text_exact(pone_link.rstrip('/'), self.expected_pone_link.rstrip('/'))
        pntd_link = self._get(self._footer_pntd_link).get_attribute('href')
        self.validate_text_exact(pntd_link.rstrip('/'), self.expected_pntd_link.rstrip('/'))
        return self

    def validate_footer_link_column_two(self):
        """
        The footer implementation is split into four different sections. footer link column two
        contains the rightmost set of vertical links pointing at the various PLOS apocryphal sites.
        Validate the links point to the correct place.
        :return: success/assertion exception
        """
        porg_link = self._get(self._footer_porg_link).get_attribute('href')
        self.validate_text_exact(porg_link.rstrip('/'), self.expected_porg_link.rstrip('/'),
                                 'Incorrect plos.org link')

        blog_link = self._get(self._footer_blog_link).get_attribute('href')
        if 'blogs.plos.org' in blogs_url:
            self.validate_text_exact(blog_link.rstrip('/'), self.expected_blog_link.rstrip('/'),
                                     'Incorrect Blogs link')
        else:
            self.validate_text_contains(blog_link.rstrip('/'), 'blogs',
                                        'Incorrect Blogs link')

        coll_link = self._get(self._footer_coll_link).get_attribute('href')
        # TODO check  for collections-dev once AMBR-828 gets resolved
        # self.validate_text_exact(coll_link.rstrip('/'), self.expected_coll_link.rstrip('/'),
        #                          'Incorrect Collections link')
        expected_coll_link_env = self.expected_coll_link.rstrip('/')
        expected_coll_link_list = [expected_coll_link_env,
                                   expected_coll_link_env.replace('-dev', '')
                                   .replace('-qa', '').replace('-stage', '')]
        assert coll_link.rstrip('/') in expected_coll_link_list, 'Incorrect Collections link: {}'\
            .format(coll_link)

        feedback_link = self._get(self._footer_feedback_link).get_attribute('href')
        self.validate_text_exact(feedback_link, self.expected_feedback_link,
                                 'Incorrect feedback link')

        lockss_link = self._get(self._footer_lockss_link).get_attribute('href')
        relative_locks_href = '/' + lockss_link.rsplit('/')[-1]
        self.validate_text_exact(relative_locks_href, self.expected_lockss_link,
                                 'Incorrect LOCKSS link')

        ca_corp_stmt = self._get(self._footer_ca_corp_stmt).text
        self.validate_text_exact(ca_corp_stmt, self.expected_ca_corp_stmt, 'Incorrect corp link')

        return self
