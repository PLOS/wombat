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


import logging
import requests
from requests.exceptions import HTTPError, Timeout
from socket import error as SocketError
from time import sleep

from .Config import verify_link_timeout, verify_link_retries, wait_between_retries

__author__ = 'jkrzemien@plos.org'


class LinkVerifier(object):
    """

    This class is in charge of validating a link is up and running (HTTP OK)

    Facilitates:

      1. *Caching* of previously verified links.
      2. *Retries* for failed links.

    The reason why I had to make a simple task (such as pinging an URL to see
    if it is alive) this convoluted was because PLoS integrated environment's
    links point to *production* sites and they seem to have some kind of *Throttling*
    feature on it.

    Point being, validating 100s of links quickly result in **TIME OUTs** from servers,
    but if we wait a little bit between pings and retry again the link seems to work fine.

    This class can be configured by the following settings:

    1. `verify_link_timeout` [[Config.py#verify_link_timeout]]
    2. `verify_link_retries` [[Config.py#verify_link_retries]]
    3. `wait_between_retries` [[Config.py#wait_between_retries]]

    """

    cache = {}
    timeout = verify_link_timeout
    max_retries = verify_link_retries
    wait_between_retries = wait_between_retries

    def __verify_link(self, url):
        successful = False
        attempts = 1
        while not successful and attempts < self.max_retries:
            try:
                response = requests.get(url, timeout=self.timeout, allow_redirects=True,
                                        verify=False)
                code = response.status_code
                successful = True
            except Timeout:
                code = "TIMED OUT"
                attempts += 1
                sleep(self.wait_between_retries)
        return code

    def get_link_status_code(self, link):
        """
        Return HTTP status code
         no caching the code, as it mostly used to fix 404 error if possible
        :param link: link to check
        :return: status code, int
        """
        try:
            # Cache HIT
            code = self.cache[link]
        except KeyError:
            # Cache MISS
            try:
                code = self.__verify_link(link)
            except HTTPError as e:
                code = e.code
            except SocketError as e:
                code = e.errno  # Probably an ECONNRESET...
                logging.error("Socket error: {0!s}".format(code))
        logging.info('HTTP {0}'.format(code))
        return code

    def is_link_valid(self, link):
        try:
            # Cache HIT
            code = self.cache[link]
        except KeyError:
            # Cache MISS
            try:
                code = self.__verify_link(link)
                # Save into cache
                self.cache[link] = code
            except HTTPError as e:
                code = e.code
            except SocketError as e:
                code = e.errno  # Probably an ECONNRESET...
                logging.error("Socket error: {0!s}".format(code))

        logging.info('HTTP {0}'.format(code))
        assert code == 200, 'Expected HTTP response code was 200 (OK), but instead I got: {0}'\
            .format(code)

        return True
