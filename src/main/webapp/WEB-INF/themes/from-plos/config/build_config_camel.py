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
import ConfigParser
import sys
from build_config_utils import *

def parse_arguments():
    parser = argparse.ArgumentParser(description='Parse command-line args')
    parser.add_argument('config_file', help='the path to the config file (camel_dev.cfg or camel_prod.cfg)')
    return parser.parse_args()

def generate_camel_properties(config):
    print(make_section_header('Common Configuration'))
    properties_entry(['How many times do we attempt to re-send a message'], [('plos.camel.maxRedelivery', '5')])
    properties_entry(['How many seconds do we wait between each redelivery'], [('plos.camel.redeliveryDelay', '600')])
    properties_entry(['Temporary location for processed ZIP archives'], [('plos.camel.temporaryDirectory', '/tmp')])
    properties_entry(['ActiveMQ port'], [('plos.camel.activemq.port', '61616')])
    properties_entry(['Where is ActiveMQ going to keep it\'s persistent files'],
        [('plos.camel.activemq.persistentStoreDir', config.get('common', 'persistent_store_dir'))])
    properties_entry(['Mail server parameters smtp://username@hostname?password=pwd', 'reconfigured and added .uri'],
        [('plos.camel.mail.uri', 'smtp://plos-org.mail.protection.outlook.com'), ('plos.camel.mail.host', 'plos-org.mail.protection.outlook.com')])
    properties_entry(['PLoS Administrator email address, That\'s the person who receives error messages.'],
        [('plos.camel.admin', config.get('common', 'camel_admin'))])
    print()

    print(make_section_header('JISC Syndication'))
    properties_entry(['Camel URL to JISC ftp host'], [('plos.jisc.ftp' , config.get('jisc', 'sftp_url'))])
    properties_entry(['Controls when to send aggregated emails', 'See http://www.opensymphony.com/quartz/wikidocs/CronTriggers%20Tutorial.html',
         'For Camel syntax see http://camel.apache.org/quartz.html', 'Send email every day at 5pm'], [('plos.jisc.cron', '0+0+17+*+*+?')])
    properties_entry(['Recipient(s) of confirmation email (can be comma delimited)'], [('plos.jisc.mailto' , config.get('jisc', 'mail_to'))])
    properties_entry(['CC of confirmation email (can be comma delimited)'], [('plos.jisc.mailcc', config.get('jisc', 'mail_cc'))])
    properties_entry(['From address of confirmation email'], [('plos.jisc.mailsender', 'do-not-reply@plos.org')])
    properties_entry(['response queue that Ambra consumes'], [('plos.jisc.response', 'activemq:plos.jisc.response')])
    print()

    print(make_section_header('PMC Syndication'))
    properties_entry(['Recipient(s) of confirmation email (can be comma delimited)'], [('plos.pmc.mailto' , config.get('pmc', 'mail_to'))])
    properties_entry(['CC of confirmation email (can be comma delimited)'], [('plos.pmc.mailcc', config.get('pmc', 'mail_cc'))])
    properties_entry(['From address of confirmation email'], [('plos.pmc.mailsender', 'do-not-reply@plos.org')])
    properties_entry(['Camel URL to PMC ftp host'], [('plos.pmc.ftp', config.get('pmc', 'ftp_url'))])
    properties_entry(['Controls when to send aggregated emails.', 'See http://www.opensymphony.com/quartz/wikidocs/CronTriggers%20Tutorial.html'
        'For Camel syntax see http://camel.apache.org/quartz.html', 'Send email every day at 6pm'], [('plos.pmc.cron', '0+0+18+*+*+?')])
    print()

    print(make_section_header('PubMed Syndication'))
    properties_entry(['Camel URL to PubMed ftp host'], [('plos.pubmed.ftp', config.get('pubmed', 'ftp_url')),
        ('plos.pubmed.xsl', 'pmc2pubmed.xsl')])
    print()

    print(make_section_header('Crossref Syndication'))
    properties_entry(['Camel URL for crossref'], [('plos.crossref.url', config.get('crossref', 'http_url'))])
    properties_entry(['Response queue for crossref (Ambra consumes this)'], [('plos.crossref.response', 'activemq:plos.crossref.response'),
        ('ambra.services.crossref.plos.doiurl', 'http://dx.plos.org/'), ('ambra.services.crossref.plos.email', config.get('crossref', 'email'))])
    print()


    print(make_section_header('Solr Indexing'))
    properties_entry(['URL of Solr server'], [('plos.solr.url', config.get('solr', 'update_url'))])
    properties_entry(['Location of XSL file'], [('plos.solr.xsl', 'article-solr.xsl')])
    properties_entry(['Optimize Solr index at 9PM'], [('plos.solr.optimizeCron', '0+0+21+*+*+?')])
    print()

    print(make_section_header('ALM'))
    properties_entry([], [('plos.alm.url', 'https://alm.plos.org/api/v3/articles'), ('plos.alm.apikey', '3pezRBRXdyzYW6ztfwft')])
    print()

    print(make_section_header('Saved Search Email'))
    properties_entry(['Send emails every Tuesday at 8 am'], [('ambra.sendemail.weekly', '0+0+20+?+*+3+*')])
    properties_entry(['Send emails 7th of every month at 8am.'], [('ambra.sendemail.monthly', '0+0+20+7+1/1+?+*')])
    properties_entry(['"from" address to send the emails.'], [('ambra.queue.fromEmailAddress', 'news@lists.plos.org')])
    properties_entry(['path for the header image in the email'], [('ambra.queue.image.path', 'http://mailings.plos.org/images/PLOS-generic.gif')])
    properties_entry(['send modeshould be set to qa or production'], [('ambra.queue.sendmail.sendMode', config.get('saved_search', 'send_mode'))])
    print()

    properties_entry(['the url to the rhino server'], [('ambra.queue.rhinoServer', config.get('rhino', 'url'))])
    print()

    print(make_section_header('PLoS Reports'))
    properties_entry(['Counter Database Properties'], [('plos.counter.driver', 'com.mysql.jdbc.Driver'),
        ('plos.counter.url', 'jdbc:mysql://db-misc.soma.plos.org:3306/plosreports'),
        ('plos.counter.username', 'plosqueue'), ('plos.counter.password', 'XmRm5Aru')])
    print()
    properties_entry(['Ambra Filestore Properties'], [('ambra.filestore.repoServer', config.get('repo', 'url')),
        ('ambra.filestore.repoServer.bucketname', config.get('repo', 'bucket_name')),
        ('ambra.filestore.implementation', 'contentRepo')])
    print()

    print(make_section_header('NED'))
    properties_entry(['NED API Properties'],[('ambra.services.nedapi.server', config.get('ned', 'url')),
        ('ambra.services.nedapi.username', config.get('ned', 'username')),
        ('ambra.services.nedapi.password', config.get('ned', 'password'))])

def properties_entry(comments, properties):
    for comment in comments:
        print(make_comment(comment))
    for name, value in properties:
        print(name + "=" + value)

def main():
    # parse command-line arguments
    args = parse_arguments()
    # load the config file
    config = ConfigParser.SafeConfigParser()
    config.readfp(open(args.config_file))
    with sys.stdout as f:
        generate_camel_properties(config)

if __name__ == '__main__':
    main()
