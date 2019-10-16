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

class Journal:
    def __init__(self):
        pass

    desktop_journals = {
        'PLoSONE': 'DesktopPlosOne',
        'PLoSMedicine': 'DesktopPlosMedicine',
        'PLoSGenetics': 'DesktopPlosGenetics',
        'PLoSCompBiol': 'DesktopPlosCompBiol',
        'PLoSCollections': 'DesktopPlosCollections',
        'PLoSNTD': 'DesktopPlosNtds',
        'PLoSBiology': 'DesktopPlosBiology',
        # 'PLoSClinicalTrials': 'DesktopPlosClinicalTrials',
        'PLoSPathogens': 'DesktopPlosPathogens'
        }
    mobile_journals = {
        'PLoSONE': 'MobilePlosOne',
        'PLoSMedicine': 'MobilePlosMedicine',
        'PLoSGenetics': 'MobilePlosGenetics',
        'PLoSCompBiol': 'MobilePlosCompBiol',
        'PLoSCollections': 'MobilePlosCollections',
        'PLoSNTD': 'MobilePlosNtds',
        'PLoSBiology': 'MobilePlosBiology',
        'PLoSClinicalTrials': 'MobilePlosClinicalTrials',
        'PLoSPathogens': 'MobilePlosPathogens'
        }

    @staticmethod
    def build_journal_base_path(journal_key):
        return '/' + Journal.desktop_journals[journal_key]

    @staticmethod
    def build_homepage_path(journal_key):
        return Journal.build_journal_base_path(journal_key) + '/'

    @staticmethod
    def build_volumes_path(journal_key):
        return Journal.build_journal_base_path(journal_key) + '/volume'

    @staticmethod
    def build_article_path(journal_key, doi):
        return Journal.build_journal_base_path(journal_key) + ('/article?id={0}'.format(doi))

    @staticmethod
    def build_article_comments_path(journal_key, doi):
        return Journal.build_journal_base_path(journal_key) + (
            '/article/comments?id={0}'.format(doi))

    @staticmethod
    def build_article_single_comment_path(journal_key, uri):
        return Journal.build_journal_base_path(journal_key) + (
            '/article/comment?id={0}'.format(uri))

    @staticmethod
    def build_article_peer_review_path(journal_key, doi):
        return Journal.build_journal_base_path(journal_key) + (
            '/article/peerReview?id={0}'.format(doi))
