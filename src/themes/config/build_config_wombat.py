#!/usr/bin/env python

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

from __future__ import print_function
from __future__ import with_statement
from collections import OrderedDict
import argparse
import base64
import itertools
import os
import sys
import zlib
from build_config_utils import *


def write_config_yaml(f, options):
    print('{label:#^{width}}'.format(label=' WARNING ', width=79), file=f)
    print(make_comment(
        '''Would you kindly be cautious when hand-editing this file. Copy/paste '''), file=f)
    print(make_comment(
        '''may insert improperly encoded space characters or other oddities. '''), file=f)
    print(file=f)

    server_url = options.server

    print('server: ' + server_url, file=f)
    print(make_comment(
            '''URL of the server for the service API ("Rhino")'''), file=f)
    print(file=f)

    print('themeSources:', file=f)
    print('  - type: filesystem', file=f)
    print('    path: ' + options.root, file=f)
    print(make_comment(
        '''Location of the plos-themes directory'''), file=f)
    print(file=f)

    if options.solrUrl and options.journals_collection and options.preprints_collection:
        print('solr:', file=f)
        print('    url: {0}'.format(options.solrUrl), file=f)
        print('    journals_collection: {0}'.format(options.journals_collection), file=f)
        print('    preprints_collection: {0}'.format(options.preprints_collection), file=f)
        print(make_comment(
                '''URL and collections of the Solr server.
                '''), file=f)
        print(file=f)

    if options.email:
        print('mailServer: {0}'.format(options.email), file=f)
        print(make_comment(
                '''Hostname of the SMTP email server.
                '''), file=f)
        print(file=f)

    compiledAssetDir = (options.assets if options.assets else 'null')
    print('compiledAssetDir: {0}'.format(compiledAssetDir), file=f)
    print(make_comment(
            '''The local directory to which to write compiled JavaScript and
            CSS assets.

            If null, JavaScript and CSS code will not be compiled, so it
            will show up unminified for "dev mode".
            '''), file=f)
    print(file=f)

    # These are hard-coded because we use pretty much the same for everything.
    # Could add a command-line argument if needed.
    print('httpConnectionPool:', file=f)
    print('  maxTotal:           {0}'.format(options.http_pool), file=f)
    print('  defaultMaxPerRoute: {0}'.format(options.http_pool), file=f)
    print(make_comment(
        '''
        Values for HTTP connection management. Setting these too low can cause
        serious performance degradation. For details, see
        org.apache.http.impl.conn.PoolingHttpClientConnectionManager,
        http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
        '''), file=f)
    print(file=f)

    memcached_fields = [
        ('memcachedHost', 'localhost'),
        ('memcachedPort', '11211'),
        ('cacheAppPrefix', 'wombat'),
        ]
    print('cache:')
    for name, default_value in memcached_fields:
        # The memcached option is just a boolean that tells us whether to use
        # the defaults or null. Null will disable Memcached in Wombat.
        value = default_value if options.memcached else 'null'
        print('  {0}: {1}'.format(name, value), file=f)
    print(make_comment(
            '''Parameters for Memcached. Set all to null to disable Memcached.
            Otherwise, all three are required.
            '''), file=f)
    print(file=f)

    print('cas:')
    print('  casUrl: {0}'.format(options.cas), file=f)
    print('  loginUrl: {0}/login'.format(options.cas), file=f)
    print('  logoutUrl: {0}/logout'.format(options.cas), file=f)
    print(file=f)

    print('commentsDisabled: {0}'.format(options.disable_comments), file=f)
    print(make_comment(
            '''Set to true to disable comment creation'''), file=f)
    print(file=f)

    if options.root_page_path:
        print('rootPagePath: {0}'.format(options.root_page_path), file=f)
        print(make_comment(
            '''local filesystem path to an HTML document that will be displayed at the root
            page (journals.plos.org/)
            If excluded or set to null, the root page will be the Wombat debug page
            '''
        ), file=f)
        print(file=f)

    if options.dev_feature:
        print('enableDevFeatures:', file=f)
        for feature in options.dev_feature:
            print('  - {0}'.format(feature), file=f)
        print(make_comment(
            '''Flags to enable features under development.
            '''), file=f)
        print(file=f)


def build_argument_parser():
    """Configure this script's command-line arguments."""
    parser = argparse.ArgumentParser(description='Build a configuration file for Wombat')

    parser.add_argument('--server', type=str, default='http://localhost:8080/',
                        help='The URL for the service component ("Rhino") [default: %(default)s]')
    parser.add_argument('--root', type=str, default=os.path.abspath('..'),
                        help='The root path of the PLOS themes [default: %(default)s]')
    parser.add_argument('--solrUrl', type=str, default='http://localhost:8983/solr/',
                        help='The URL of the Solr server [default: %(default)s]')
    parser.add_argument('--journals_collection', type=str, default='journals_dev',
                        help='The journals collection to use for Solr [default: %(default)s]')
    parser.add_argument('--preprints_collection', type=str, default='preprints_dev',
                        help='The preprints collection to use for Solr [default: %(default)s]')
    parser.add_argument('--email', type=str, default=None,
                        help='The hostname of the SMTP email server [default: %(default)s]')
    parser.add_argument('--assets', type=str, default=None,
                        help='The path at which to compile JS and CSS assets [default: %(default)s]')
    parser.add_argument('--memcached', action='store_true',
                        help='Include this to use Memcached')
    parser.add_argument('--http_pool', type=int, default=100,
                        help='Size of the HTTP connection pool [default: %(default)s]')
    parser.add_argument('--prodsites', action='store_true',
                        help='Mimic production site definitions')
    parser.add_argument('--cas', type=str, default='https://register.plos.org/cas',
                        help='The URL for the CAS server [default: %(default)s]')
    parser.add_argument('--dev_feature', nargs='*', type=str, default=[],
                        help='Activated dev features')
    parser.add_argument('--root_page_path', type=str,
                        help='Local filesystem path to an HTML document to display at root')
    parser.add_argument('--disable_comments', action='store_true',
                        help='Include this to disable comment creation')

    return parser

def write_config():
    options = build_argument_parser().parse_args()

    with sys.stdout as f:
        write_config_yaml(f, options)
        try:
            print(bonus(), file=f)
        except:
            pass

def bonus():
    return zlib.decompress(base64.b64decode(
            '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'
            ))

write_config()
