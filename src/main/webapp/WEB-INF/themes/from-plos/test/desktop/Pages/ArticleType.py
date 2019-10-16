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

from test.Base.PlosPage import PlosPage
from selenium.webdriver.support import expected_conditions as EC


class ArticleType(PlosPage):
    def __init__(self, driver, journal_key, url_suffix, article_type):
        self.PROD_URL = url_suffix
        super(ArticleType, self).__init__(driver, url_suffix)
        self._journal_key = journal_key
        self._article_type = article_type

    def assert_article_type_text(self):
        article_type_el = self._driver.find_element_by_id('artType')
        # Wait until the new page is loaded
        self._wait.until(EC.visibility_of(article_type_el))

        assert article_type_el.text == self._article_type, 'Article type: %s is not the expected %s' % (
        article_type_el.text, self._article_type)

    def assert_content_presence(self):
        title = self._driver.find_element_by_css_selector('.title-authors #artTitle')
        content = self._driver.find_element_by_css_selector('.article-content')
        footer = self._driver.find_element_by_css_selector('footer#pageftr')
