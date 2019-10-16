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

__author__ = 'jfesenko@plos.org'

import logging

from ..Base.MySQL import MySQL
from ..Base.LinkVerifier import LinkVerifier
from . import resources, utils


class TestCrossPub404:

    def test_all_cross_pubs(self):
        """
        Get a list of tuples containing (doi, journalKey) for all cross published articles
        """
        cross_pubs = MySQL().query("select doi, journalKey from articlePublishedJournals apj " +
                                   "inner join journal j on apj.journalID = j.journalID " +
                                   "inner join article a on apj.articleID = a.articleID " +
                                   "where apj.articleID in (select * from (" +
                                   "select articleID from articlePublishedJournals " +
                                   "group by articleID having count(*)>1) as id_list);")

        link_verifier = LinkVerifier()
        num_links = len(cross_pubs)
        error_list = []
        update_message_each_percent = 1
        logging.info('validating {} article links for all cross published articles. '
                     'This may take a while...'.format(num_links))

        # route link status messages to dev/null and only notify on error

        for i, tup in enumerate(cross_pubs):
            path = '/{0}/article?id={1}' \
                .format(resources.journal_sites['desktop'][tup[1].decode()],
                        tup[0].decode().replace('info:doi/', ''))
            article_url = utils.translate_to_prod_url(path)
            logging.info('Checking article: {0}'.format(article_url))
            try:
                utils.call_while_suppressing_stdout(link_verifier.is_link_valid, (article_url,))
            except AssertionError as e:
                logging.error('***ERROR ACCESSING URL: {}\n     ERROR DETAIL: {}'
                              .format(article_url, str(e)))
                error_list.append({'url': article_url,
                                   'error': str(e)})  # collect all errors before throwing exception

        if int(float(i + 1) / num_links * (100 / update_message_each_percent)) > \
                int(float(i) / num_links * (100 / update_message_each_percent)):
            logging.info('{}% completed'.format(int(float(i + 1) / num_links * 100)))

        num_errors = len(error_list)
        logging.info('{} cross published article links tested, {} successful, {} errors'
                     .format(num_links, num_links - num_errors, num_errors))
        if num_errors:
            raise AssertionError(str(error_list))

        return
