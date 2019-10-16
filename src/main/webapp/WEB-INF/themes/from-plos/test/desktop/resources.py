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
This Resource File sets variables that are used in individual
test cases. It eventually should be replaced with more robust, 
less static, variable definitions. 
"""

# General resources
# set friendly_testhostname to 'prod' to run suite against production
# Two fields need to be changed to support running tests in your local development
# environment, first, set friendly_testhostname to localhost, then correct the 
# base_url value if you are using a port or key different than 8081 and wombat.

from ..Base.Config import environment, rhino_url, blogs_url
from ..Base.styles import BIO_GREEN, MED_PURPLE, LIME

friendly_testhostname = environment
if friendly_testhostname == 'prod':
    base_url = ''
    rhino_url = rhino_url
elif friendly_testhostname == 'localhost':
    base_url = 'http://localhost:8081/wombat'
    rhino_url = 'http://one-' + friendly_testhostname + '.plosjournals.org/v2'
elif friendly_testhostname == 'sc01':
    base_url = 'http://one-' + friendly_testhostname + '.plosjournals.org:8046/'
    rhino_url = 'http://rhino-' + friendly_testhostname + '.plosjournals.org:8006/api/v2'
else:
    base_url = 'http://one-' + friendly_testhostname + '.plosjournals.org:8006/wombat'
    rhino_url = 'http://one-' + friendly_testhostname + '.plosjournals.org:8006/v2'

journals = [{'journalKey': 'One', 'journalTitle': 'One'},
            {'journalKey': 'Medicine', 'journalTitle': 'Medicine'},
            {'journalKey': 'Pathogens', 'journalTitle': 'Pathogens'},
            {'journalKey': 'CompBiol', 'journalTitle': 'Computational Biology'},
            {'journalKey': 'Genetics', 'journalTitle': 'Genetics'},
            {'journalKey': 'Ntds', 'journalTitle': 'Neglected Tropical Diseases'},
            {'journalKey': 'Collections', 'journalTitle': 'Collections'},
            {'journalKey': 'Biology', 'journalTitle': 'Biology'}
            ]

sevenjournals = [
    {'journalKey': 'One',
     'rhinoJournalKey': 'PLoSONE',
     'journalTitle': 'One',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plosone+OR+%23plosone+OR+%5C%22'
                          'PLOS+One%5C%22+OR+%5C%22plosone%5C%22+lang%3Aen',
     'journalBlogTitle': 'EveryONE',
     'journalBlogTarget': '{0}/everyone/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plosone',
     'journal_url_name': 'plosone',
     'journal_brand_color' : LIME,
     },
    {'journalKey': 'Biology',
     'rhinoJournalKey': 'PLoSBiology',
     'journalTitle': 'Biology',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plosbiology+OR+%23plosbiology+OR+%22'
                          'PLOS+Bio%22+OR+%22PLOS+Biology%22+OR+%22plosbiology%22+OR+%22'
                          'plosbio%22+lang%3Aen',
     'journalBlogTitle': 'PLOS Biologue',
     'journalBlogTarget': '{0}/biologue/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plosbiology',
     'journal_url_name': 'plosbiology',
     'journal_brand_color' : BIO_GREEN,
     },
    {'journalKey': 'Medicine',
     'rhinoJournalKey': 'PLoSMedicine',
     'journalTitle': 'Medicine',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plosmedicine+OR+%23plosmedicine+OR+%22'
                          'PLOS+Med%22+OR+%22PLOS+Medicine%22+OR+%22plosmedicine%22+OR+%22'
                          'plosmed%22+lang%3Aen',
     'journalBlogTitle': 'Speaking of Medicine',
     'journalBlogTarget': '{0}/speakingofmedicine/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plosmedicine',
     'journal_url_name': 'plosmedicine',
     'journal_brand_color' : MED_PURPLE,
     },
    {'journalKey': 'Pathogens',
     'rhinoJournalKey': 'PLoSPathogens',
     'journalTitle': 'Pathogens',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plospathogens+OR+%23'
                          'plospathogens+OR+journal.ppat.+OR+%22PLOS+Pathogens%22+OR+%22'
                          'plospathogens%22+OR+%22journal.ppat.%22+lang%3Aen',
     'journalBlogTitle': 'Speaking of Medicine',
     'journalBlogTarget': '{0}/speakingofmedicine/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plospathogens',
     'journal_url_name': 'plospathogens',
     'journal_brand_color' : MED_PURPLE,
     },
    {'journalKey': 'CompBiol',
     'rhinoJournalKey': 'PLoSCompBiol',
     'journalTitle': 'Computational Biology',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40ploscompbiol+OR+%23ploscompbiol+OR+%22'
                          'PLOS+CompBiol%22+OR+%22PLOS+CompBio%22+OR+%22'
                          'PLOS+Computational+Biology%22+OR+%22ploscomputationalbiology%22+OR+%22'
                          'ploscompbiol%22+OR+%22ploscompbio%22+lang%3Aen',
     'journalBlogTitle': 'PLOS Biologue',
     'journalBlogTarget': '{0}/biologue/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/ploscompbiol',
     'journal_url_name': 'ploscompbiol',
     'journal_brand_color' : BIO_GREEN,
     },
    {'journalKey': 'Genetics',
     'rhinoJournalKey': 'PLoSGenetics',
     'journalTitle': 'Genetics',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plosgenetics+OR+%23'
                          'plosgenetics+OR+%22PLOS+Gen%22+OR+%22PLOS+Genetics%22+OR+%22'
                          'plosgenetics%22+lang%3Aen',
     'journalBlogTitle': 'PLOS Biologue',
     'journalBlogTarget': '{0}/biologue/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plosgenetics',
     'journal_url_name': 'plosgenetics',
     'journal_brand_color' : BIO_GREEN,
     },
    {'journalKey': 'Ntds',
     'rhinoJournalKey': 'PLoSNTD',
     'journalTitle': 'Neglected Tropical Diseases',
     'journalTweetQuery': 'https://twitter.com/search/?q=%40plosntds+OR+%23plosntds+OR+%22'
                          'PLOS+NTDs%22+OR+%22plosntds%22+lang%3Aen',
     'journalBlogTitle': 'Speaking of Medicine',
     'journalBlogTarget': '{0}/speakingofmedicine/'.format(blogs_url.rstrip('/')),
     'journalTweetTarget': 'https://twitter.com/plosntds',
     'journal_url_name': 'plosntds',
     'journal_brand_color' : MED_PURPLE,
     }
]

plosclinicaltrials_journal = \
    {'journalKey': 'PLoSClinicalTrials',
     'rhinoJournalKey': 'PLoSClinicalTrials',
     'journalTitle': 'Clinical Trials',
     'journal_url_name': 'plosclinicaltrials',
     }

# Header resources
search_term = 'Retinal'
search_term_root = 'Retin'
searchterms = ['MicroRNA',
               'Small interfering RNA',
               'Transgenic animals',
               'Transgenic plants',
               'Trait loci',
               'Flight mechanics biology',
               'Hand foot and mouth disease',
               'Ontology and logic',
               'Immunity to infections',
               'Microbial drug resistance',
               'Stomates',
               'Food habits']

# registration resources
non_existing_user_email = 'jgray1@plos.org'
existing_user_email = 'jgray@plos.org'
new_user_id_local_base = 'sealresq'
new_user_id_domain = '@gmail.com'
new_user_id_index = 250

# doi.org text url
dx_doi_url = 'https://doi.org/'

# We just need a subset of collection for our non-production monitoring - enough
#     to cover the natural variances in collections such as text vs blog widget,
#     presence of buttons in text widget, etc.
collections = [
    'about-my-lab',
    'achieving-hiv-impact',
    'acidification-impacts',
    'aging',
    'allostery',
    'altmetrics',
    'animal-research',
    'apoc',
    'autophagy',
    'barcode-of-life',
    'big-food',
    'bigdata',
    'biocurators',
    'biomaterials',
    'biology-10th-anniversary',
    'blue-marble-health',
    'bridging-communities',
    'cancer-genome',
    'cancer-immunotherapy',
    'cancer-research',
    'cb-starting-early',
    'cell-biology-picks',
    'censeam',
    'challenges',
    'chemical-tools-probes',
    'chess',
    'clinicalimmuno-allergiesanaphylaxis',
    'clinicalimmuno-autoimmune',
]

article_type_list = {
    'PLoSONE': {
        'pone.0146628': 'CORRECTION',
        'pone.0146627': 'CORRECTION',
        'pone.0144326': 'OVERVIEW',
        'pone.0140319': 'COLLECTION REVIEW',
        'pone.0142437': 'FORMAL COMMENT',
        'pone.0142287': 'FORMAL COMMENT',
        'pone.0141008': 'RETRACTION',
        'pone.0093751': 'RESEARCH ARTICLE',
        'pone.0093695': 'RESEARCH ARTICLE',
        'pone.0151308': 'CORRECTION',
    },
    'PLoSBiology': {
        'pbio.1002531': 'RESEARCH ARTICLE',
        'pbio.1002327': 'SYNOPSIS',
        'pbio.1002320': 'SYNOPSIS',
        'pbio.1001377': 'COMMUNITY PAGE',
        'pbio.1000114': 'FEATURE',
        'pbio.0060259': 'FEATURE',
        'pbio.1002340': 'COMMUNITY PAGE',
        'pbio.1002310': 'COMMUNITY PAGE',
        'pbio.0020337': 'JOURNAL CLUB',
        'pbio.0020297': 'JOURNAL CLUB',
        'pbio.1002522': 'PRIMER',
        'pbio.1002323': 'PRIMER',
        'pbio.1002322': 'UNSOLVED MYSTERY',
        'pbio.1002276': 'UNSOLVED MYSTERY',
        'pbio.1002334': 'EDITORIAL',
        'pbio.1001671': 'OBITUARY',
        'pbio.1001780': 'BOOK REVIEW/SCIENCE IN THE MEDIA',
        'pbio.1001785': 'BOOK REVIEW/SCIENCE IN THE MEDIA',
        'pbio.0040228': 'CORRESPONDENCE AND OTHER COMMUNICATIONS',
        'pbio.0040068': 'CORRESPONDENCE AND OTHER COMMUNICATIONS',
        'pbio.1001096': 'HISTORICAL AND PHILOSOPHICAL PERSPECTIVES',
        'pbio.1001071': 'HISTORICAL AND PHILOSOPHICAL PERSPECTIVES',
        'pbio.1001503': 'BOOK REVIEW',
        'pbio.1002314': 'RETRACTION',
        'pbio.1002456': 'META-RESEARCH ARTICLE',
        'pbio.1002333': 'META-RESEARCH ARTICLE',
    },
    'PLoSCompBiol': {
        'pcbi.1004725': 'PERSPECTIVE',
        'pcbi.1004726': 'PERSPECTIVE',
        'pcbi.1000965': 'OBITUARY',
        'pcbi.0030018': 'OBITUARY',
        'pcbi.1004512': 'EDUCATION',
        'pcbi.1004393': 'EDUCATION',
        'pcbi.1004323': 'MESSAGE FROM ISCB',
        'pcbi.1004320': 'MESSAGE FROM ISCB',
        'pcbi.1004095': 'TOPIC PAGE',
        'pcbi.1003844': 'TOPIC PAGE',
    },
    'PLoSMedicine': {
        'pmed.1002081': 'RESEARCH ARTICLE',
        'pmed.1001284': 'HEALTH IN ACTION',
        'pmed.1001927': 'ESSAY',
        'pmed.1001926': 'ESSAY',
        'pmed.1002097': 'EDITORIAL',
        'pmed.0010031': 'MESSAGE FROM THE PLOS FOUNDERS',
        'pmed.0040318': 'CORRESPONDENCE',
        'pmed.0040300': 'CORRESPONDENCE',
        'pmed.1001929': 'POLICY FORUM',
        'pmed.1001918': 'POLICY FORUM',
        'pmed.1001343': 'THE PLOS MEDICINE DEBATE',
        'pmed.1001342': 'THE PLOS MEDICINE DEBATE',
        'pmed.1001901': 'HEALTH IN ACTION',
        'pmed.1001897': 'HEALTH IN ACTION',
        'pmed.1000235': 'NEGLECTED DISEASES',
        'pmed.1000176': 'NEGLECTED DISEASES',
        'pmed.1000306': 'LEARNING FORUM',
        'pmed.1000092': 'LEARNING FORUM',
        'pmed.0040111': 'CASE REPORT',
        'pmed.0030331': 'CASE REPORT',
        'pmed.1001627': 'RESEARCH IN TRANSLATION',
        'pmed.1001616': 'RESEARCH IN TRANSLATION',
        'pmed.0030389': 'BEST PRACTICE',
        'pmed.0030123': 'BEST PRACTICE',
        'pmed.1000138': 'STUDENT FORUM',
        'pmed.0050002': 'STUDENT FORUM',
        'pmed.1001895': 'GUIDELINES AND GUIDANCE',
        'pmed.1001886': 'GUIDELINES AND GUIDANCE',
        'pmed.1001923': 'COLLECTION REVIEW',
    },
    'PLoSNTD': {
        'pntd.0004266': 'EXPERT COMMENTARY',
        'pntd.0004039': 'EXPERT COMMENTARY',
        'pntd.0004074': 'VIEWPOINTS',
        'pntd.0004040': 'VIEWPOINTS',
        'pntd.0003351': 'PHOTO QUIZ',
        'pntd.0003291': 'PHOTO QUIZ',
        'pntd.0000946': 'SYMPOSIUM',
        'pntd.0000840': 'SYMPOSIUM',
        'pntd.0003987': 'POLICY PLATFORM',
        'pntd.0003734': 'POLICY PLATFORM',
        'pntd.0004106': 'HISTORICAL PROFILES AND PERSPECTIVES',
        'pntd.0003542': 'HISTORICAL PROFILES AND PERSPECTIVES',
        'pntd.0004436': 'SYMPOSIUM',
        'pntd.0004071': 'SYMPOSIUM',
        'pntd.0003845': 'FROM INNOVATION TO APPLICATION',
        'pntd.0003400': 'FROM INNOVATION TO APPLICATION',
        'pntd.0001372': 'DEBATE',
    },
    'PLoSGenetics': {
        'pgen.1005548': 'INTERVIEW',
        'pgen.1005351': 'INTERVIEW',
        'pgen.1000068': 'SPECIAL REPORT',
        'pgen.1000064': 'SPECIAL REPORT',
        'pgen.0020019': 'SPECIAL REPORT',
        'pgen.0020062': 'TECHNICAL REPORT',
        'pgen.1005499': 'EXPRESSION OF CONCERN',
        'pgen.1005736': 'DEEP READS',
        'pgen.1004887': 'DEEP READS',
    },
    'PLoSPathogens': {
        'ppat.1005696': 'RESEARCH ARTICLE',
        'ppat.1005229': 'OPINION',
        'ppat.1005087': 'OPINION',
        'ppat.1005283': 'PEARLS',
        'ppat.1005258': 'PEARLS',
        'ppat.1005234': 'EXPRESSION OF CONCERN',
        'ppat.1005313': 'RESEARCH MATTERS',
        'ppat.1005245': 'RESEARCH MATTERS',
    }
}

subject_areas = ['biology_and_life_sciences',
                 'medicine_and_health_sciences',
                 'critical_care_and_emergency_medicine',
                 'dna',
                 'rna',
                 'fur',
                 'earth',
                 'catalogs',
                 'women\'s_health',
                 ]
