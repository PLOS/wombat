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
Class for accessing XML data, returning a dom representation

"""
import logging

__author__ = 'jgray@plos.org'

import urllib.parse
from urllib.request import urlopen
import xml.etree.ElementTree as elementTree


class ParseXML(object):

    def get_auths(self, xmlpath):
        authors = []
        try:
            with urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)
            for author in root.findall(".//contrib[@contrib-type='author']"):
                fullname = ""
                fname = author.find('./name/given-names')
                lname = author.find('./name/surname')
                collab = author.find('./collab')
                if fname is not None and lname is not None:
                    fullname = fname.text + ' ' + lname.text
                else:
                    fullname = collab.text
                authors.append(fullname)
        except Exception as e:
            logging.error(e)
        return authors

    def get_corresp_auths(self, xmlpath):
        corresp_auth = []
        try:
            with urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)
            for contrib in root.findall(".//contrib[@contrib-type='author']"):
                fullname = ""
                corresp = contrib.find("./xref[@ref-type='corresp']")
                if corresp is not None:
                    fname_node = contrib.find('./name/given-names')
                    lname_node = contrib.find('./name/surname')
                    if fname_node is not None and lname_node is not None:
                        fullname = fname_node.text + ' ' + lname_node.text
                    else:

                        # Handle the rare author names that include HTML.  Example:
                        # "The <italic>PLoS Medicine</italic> Editors" (pmed.1001210, DPRO-1081)
                        fullname = ' '.join(
                            (x.strip() for x in contrib.find('./collab').itertext()))
                    corresp_auth.append(fullname)
        except Exception as e:
            logging.error(e)
        return corresp_auth

    def get_cocontributing_auths(self, xmlpath):
        cocontrib_auth = []
        try:
            with urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)
            for contrib in root.findall(".//contrib[@equal-contrib='yes']"):
                fullname = ""
                if contrib is not None:
                    fname_node = contrib.find('./name/given-names')
                    lname_node = contrib.find('./name/surname')
                    if fname_node is not None and lname_node is not None:
                        fullname = fname_node.text + ' ' + lname_node.text
                    else:
                        fullname = contrib.find('./collab').text
                    cocontrib_auth.append(fullname)
        except Exception as e:
            logging.error(e)
        return cocontrib_auth

    def get_customfootnote_auths(self, xmlpath):
        customfootnote_auth = []
        try:
            with urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)
            for contrib in root.findall(".//contrib[@contrib-type='author']"):
                fullname = ""
                customfootnote = contrib.find("./xref[@ref-type='fn']")
                if customfootnote is not None:
                    fn_rid_tag = customfootnote.get('rid')
                    locator = ".//fn[@id=\'" + fn_rid_tag + "\']"
                    for fn in root.findall(locator):
                        fn_type = fn.get('fn-type')
                        if fn_type != 'current-aff':
                            fname_node = contrib.find('./name/given-names')
                            lname_node = contrib.find('./name/surname')
                            if fname_node is not None and lname_node is not None:
                                fullname = fname_node.text + ' ' + lname_node.text
                            else:
                                fullname = contrib.find('./collab').text
                            customfootnote_auth.append(fullname)
        except Exception as e:
            logging.error(e)
        return customfootnote_auth

    def get_article_sections(self, xmlpath):
        article_sections = []
        patient_summary = False
        try:
            with urllib.request.urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)

            for abstract in root.findall(".//front/article-meta/abstract"):
                if not abstract.attrib:
                    article_sections.append('Abstract')
                else:
                    if str(abstract.attrib['abstract-type']):
                        if str(abstract.attrib['abstract-type']) == 'toc':
                            continue
                        else:
                            if str(abstract.attrib['abstract-type']) == 'patient':
                                patient_summary = True
                            else:
                                article_sections.append(abstract.find("./title").text)

            for section in root.findall(".//body/sec"):
                title = section.find("./title")
                if title.text:
                    article_sections.append("".join(title.itertext()))

            if root.findall(".//back/ack"):
                article_sections.append('Acknowledgments')

            if root.findall(".//front/article-meta/author-notes/fn[@fn-type='con']"):
                article_sections.append('Author Contributions')

            for refs in root.findall(".//back/ref-list"):
                title = refs.find("./title")
                article_sections.append(title.text)

            if patient_summary:
                article_sections.append('Patient Summary')

        except Exception as e:
            logging.error(e)
        return article_sections

    def get_metadata_sections(self, xmlpath):
        metadata_sections = []
        try:
            with urlopen(xmlpath) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)
            if root.findall(".//front/article-meta/elocation-id"):
                metadata_sections.append('Citation:')

            # TODO: Figure out when we render "Academic Editor:" vs. "Editor:" as a heading
            if root.findall(".//front/article-meta/contrib-group/contrib[@contrib-type='editor']"):
                metadata_sections.append('Editor:')

            if root.findall(".//front/article-meta/history/date[@date-type='received']"):
                metadata_sections.append('Received:')

            if root.findall(".//front/article-meta/history/date[@date-type='accepted']"):
                metadata_sections.append('Accepted:')

            if root.findall(".//front/article-meta/pub-date[@pub-type='epub']"):
                metadata_sections.append('Published:')

            if root.findall(".//front/article-meta/permissions/license/license-p"):
                copyright_text = root.find(
                    ".//front/article-meta/permissions/license/license-p").text
                try:
                    copyright_text.lower().index('public domain')
                except:
                    metadata_sections.append('Copyright:')

            if root.findall(".//front/article-meta/funding-group/funding-statement"):
                metadata_sections.append('Funding:')

            if root.findall(".//front/article-meta/author-notes/fn[@fn-type='conflict']"):
                metadata_sections.append('Competing interests:')

            if root.findall(".//back/glossary"):
                metadata_sections.append('Abbreviations:')

        except Exception as e:
            logging.error(e)
        return metadata_sections

    def get_sub_articles(self, xml_path):
        """
        Method to get sub-articles from article xml
        :param xml_path: path to the article xml
        :return:
        """
        sub_articles = []
        # patient_summary = False
        try:
            with urllib.request.urlopen(xml_path) as response:
                article_xml = response.read().decode('utf-8')
                root = elementTree.fromstring(article_xml)

            for sub_article in root.findall(".//sub-article"):
                sub_articles.append(sub_article.find(
                    "./front-stub/title-group/article-title").text)
        except Exception as e:
            logging.info(e)
        return sub_articles
