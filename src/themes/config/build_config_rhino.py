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
import argparse
import base64
import sys
import zlib
import urlparse

from build_config_utils import *

def write_config_yaml(f, options):
    print('{label:#^{width}}'.format(label=' WARNING ', width=79), file=f)
    print(make_comment(
        '''Would you kindly be cautious when hand-editing this file. Copy/paste '''), file=f)
    print(make_comment(
        '''may insert improperly encoded space characters or other oddities. '''), file=f)
    print(file=f)

    print(('' if options.pretty_print else '#') + 'prettyPrintJson: true', file=f)
    print(make_comment(''' enable to use Gson pretty print JSON output'''), end='\n\n', file=f)

    def parse_url(url):
        parsed = urlparse.urlparse(url)
        if not parsed.scheme.startswith('http'):
            url = 'http://' + parsed.path
        return url
    default_repo = parse_url(options.repo_location)

    def get_repo(option):
        return parse_url(option) if option else default_repo

    print('contentRepo:')
    def print_crepo_bucket(name, repo, bucket, secondary_buckets, comment):
        print('  {0}:'.format(name))
        print(make_comment(comment, indent=4))
        print('    address: ' + repo)
        print('    bucket:  ' + bucket)
        if secondary_buckets is not None:
            print('    secondaryBuckets:', end='')
            if secondary_buckets:
                print()
                for secondary_bucket in secondary_buckets:
                    print('      - ' + secondary_bucket)
            else:
                print(' []')

    print_crepo_bucket('editorial',
                       get_repo(options.editorial_repo),
                       options.editorial_bucket, None,
                       "Editorial content written by Lemur.")
    print_crepo_bucket('corpus',
                       get_repo(options.corpus_repo),
                       options.corpus_bucket, [],
                       "Main corpus of articles.")
    print()

    print('httpConnectionPool:')
    print('  maxTotal:           ' + str(options.http_pool))
    print('  defaultMaxPerRoute: ' + str(options.http_pool))
    print(make_comment(
        '''
        HTTP connection management from Rhino (as a client) to other
        services (including content repo and taxonomy server).
        '''))
    print()

    print('taxonomy:')
    # Fill in the URL of the PLOS production taxonomy server if a thesaurus
    # key is provided. Else, leave both null to disable taxonomy features.
    print('  server:    '
          + (options.taxonomy_server if options.thesaurus else 'null'))
    print('  thesaurus: ' + (options.thesaurus if options.thesaurus else 'null'))
    print('  categoryBlacklist:')
    print('    - "/Earth sciences/Geography/Locations/"')
    print(make_comment(
        '''
        Configuration related to the taxonomic classification of articles.

        "server" and "thesaurus" specify the remote taxonomy server that
        will classify articles as they are ingested. If both are null,
        Rhino will skip the service call; it will still ingest articles,
        but will not write any categories for them at ingestion time.

        Any term that starts with a string in "categoryBlacklist" will be
        suppressed from all articles if the taxonomy service returns it.
        The "Locations" term is suppressed as a workaround for a problem
        where articles that mention geographic locations are
        over-aggressively categorized under "Earth sciences". See
        <https://developer.plos.org/jira/browse/AMEC-100>
        '''))
    print()

    print('userApi:')
    print('  server:                ' + options.user_api_server)
    print('  authorizationAppName:  dipro')
    print('  authorizationPassword: ' +
          (options.user_api_auth if options.user_api_auth else '""'))
    print(make_comment(
        '''
        Configuration for the API for user data (Named Entity Database, NED).
        '''))
    print()

    print('queue:')
    print('  brokerUrl:        ' + options.queue_location)
    print(make_comment('ActiveMQ address.', indent=2))
    print()
    print('  solrUpdate:       '
          'activemq:plos.solr.article.index?transacted=false')
    print('  solrDelete:       '
          'activemq:plos.solr.article.delete?transacted=false')
    print(make_comment('Queue destination keys.', indent=2))
    print()
    print('  syndicationRange: 30')
    print(make_comment('Number of days in the past to syndicate.', indent=2))
    print()

    print('manuscriptCustomMeta:')
    print('  revisionDate:     "Publication Update"')
    print('  publicationStage: "PLOS Publication Stage"')
    print(make_comment(
        '''
        Values to look for in manuscripts as
        "<custom-meta><meta-name>...</meta-name></custom-meta>" XML elements,
        where programmatically signficant data will be found.
        '''))
    print()

def build_argument_parser():
    """Configure this script's command-line arguments."""
    parser = argparse.ArgumentParser(description='Build a configuration file for Rhino')
    parser.add_argument('--pretty_print', action='store_true',
                        help='Use pretty print option for JSON output (Gson)')
    parser.add_argument('--repo_location', type=str, default='http://localhost:8002/',
                        help='The default URL for the content repo [default: %(default)s]')
    parser.add_argument('--corpus_repo', type=str, default=None,
                        help="The URL for the corpus's content repo, if different from default")
    parser.add_argument('--corpus_bucket', type=str, default='corpus',
                        help='The content repo bucket name for article corpus [default: %(default)s]')
    parser.add_argument('--editorial_repo', type=str, default=None,
                        help="The URL for editorial's content repo, if different from default")
    parser.add_argument('--editorial_bucket', type=str, default='plive',
                        help='The content repo bucket name for editorial content [default: %(default)s]')
    parser.add_argument('--http_pool', type=int, default=100,
                        help='Size of the HTTP connection pool [default: %(default)s]')
    parser.add_argument('--thesaurus', type=str, default=None,
                        help='Taxonomy thesaurus name [example: plosthes.2014-5]')
    parser.add_argument('--taxonomy_server', type=str, default='https://plos.accessinn.com:9136/servlet/dh',
                        help='Taxonomy server URL [default: %(default)s if thesaurus name is provided')
    parser.add_argument('--queue_location', type=str, default='tcp://localhost:61616',
                        help='Queue server URL [default: %(default)s')
    # TODO: Default user_api_server value for dev environments, when one exists
    parser.add_argument('--user_api_server', type=str, default='http://localhost/',
                        help='User API (NED) URL [default: %(default)s')
    parser.add_argument('--user_api_auth', type=str,
                        help='User API (NED) authorization key for "dipro" user')

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
    return zlib.decompress(base64.b64decode('''
eJzVlr1u6zAMhfc+hYHLjUZWCgIELV40NNr5MHr2ew4V5+aniQv0LqVdx5atj4c/Evrx5/Sf7eN
3E63R8s+Jy1KkNYMJjtZqra3sLzOG4eZ7xOVEhIvEnEp9ChqfbJrQAcZUS9Vj4iJCaRVTFGfQKg
Qq6TWeqhYFrfA6ir5QuhPpP+bptFpaLSXguBbaGDpGGSTypMPXRHULVqkxt5KjZeCHCK0jj5wzY
WPN/CIPDmPKK6ILC6A8GBnlamNwBPOa87qSCTe0FX9T6nPsQfSE9F1yDqhJjUAxMJNW8gpSprC4
4p4yS6gOmWvTe2I0CZOH2a1tZvAdmpVSKuIfZR1MJeMfREd+QNYonZY7jbVLixqwIsj31tFGUWJ
jLtpUDEVCTNYpDZXDcPQXsnQftaaQaIFsCPrTnb1kLVqcvUmZit4CAwGvFNbUmU14NXnM43qGEz
YyJ+FXzhQZJ5AVaGhFopEcRo3yQCBeOm7IvFU4iabE4YimhqXUuXgczIbwY814kw6NUWp0GQZFs
DxZPnsi5hNTYYGrRJ+31HuXSTWfPNvELr0+gZtMEfJEXNmPsWZis0E0G4k43buHUr5OG9BAorXR
D+K+uVB8Y26eO9xnCaCSvjHSaSk5rsACnfAQOwUDbYI3iSpiAwHzkXgKLehxA+L6ru+W0rzgI4S
IiIHvGz6FQo/9SvDunujsF+S+b4+L6tYglrmBczyAxWSbS9rOCIIfVNuJjIybmaR3wHBsbGm5Pk
ti0Vk/JCKCD2L2Hu7trcJwLYLUXBPDkJGIzhNp7deocwQNV0dABGPo9Ls8UAznpz7X9l5rtl7/E
vKgkapuR9xnZa4VveSRTXysEMvLm/f1biglxPy5LfvzRSNa0W09HRuJD0OLnz39mzuJLCCaQk5H
hn0PbfE0vNzcBxG7YW2IvB9mkg1oXyAfibTvhMzPcuv97be/4j+pBfYXjwwnGw==
''' ))


write_config()
