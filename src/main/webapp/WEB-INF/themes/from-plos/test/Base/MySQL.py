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

"""
Class for accessing MySQL databases and be able to run queries against it, retrieving results
and/or performing modifications.

Python's MySQL connector can be installed via the following command:

  sudo pip install --allow-external mysql-connector-python mysql-connector-python

"""

from mysql.connector.pooling import MySQLConnectionPool
from contextlib import closing
from .Config import dbconfig

__author__ = 'jgray@plos.org'


class MySQL(object):
    def __init__(self):
        self._cnxpool = MySQLConnectionPool(pool_name="mysqlPool", pool_size=3, **dbconfig)

    def _get_connection(self):
        return self._cnxpool.get_connection()

    def query(self, query, query_args_tuple=None):
        cnx = self._get_connection()

        with closing(cnx.cursor()) as cursor:
            cursor.execute(query, query_args_tuple)
            results = cursor.fetchall()

        cnx.close()

        return results

    def modify(self, query, query_args_tuple=None):
        cnx = self._get_connection()

        with closing(cnx.cursor()) as cursor:
            cursor.execute(query, query_args_tuple)
            cnx.commit()

        cnx.close()
