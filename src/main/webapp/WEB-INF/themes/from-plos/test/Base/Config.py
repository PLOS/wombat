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
from smart_getenv import getenv

from selenium.webdriver import DesiredCapabilities

# === Logging Level ===
logging.basicConfig(level=logging.INFO)

# === WebDriver specific section ===

# WebDriver's implicit timeout (in seconds)
# wait_timeout = 60 ( => env variable)

# WebDriver's Page Load timeout (in seconds)
# page_load_timeout = 60 ( => env variable)

# Framework's link verification timeout (in seconds)
verify_link_timeout = 5

# Framework's link verification retries
verify_link_retries = 5

# Framework's link verification wait time between retries (in seconds)
wait_between_retries = 3

"""
Set **WEBDRIVER_ENVIRONMENT** env variable/default value to one of these:

1. **prod** will run tests against **PRODUCTION** site (each POM should know it prod site)
2. **dev** will run tests against the site defined by `base_url` variable.

When **WEBDRIVER_ENVIRONMENT** is set to "prod" then **WEBDRIVER_TARGET_URL** is ignored

Set **WEBDRIVER_TARGET_URL** env variable/default value to desired URL to run suite against it
when **WEBDRIVER_ENVIRONMENT** is `dev`
"""

# SALT dev
# Host ips
# 10.5.4.24 ploscompbiol.org plosdefault.org plosmedicine.org ploscollections.org
#           plosclinicaltrials.org
# 10.5.4.24 plosone.org plosntds.org plosbiology.org plosgenetics.org plospathogens.org
#           www.ploscompbiol.org
# 10.5.4.24 www.plosdefault.org www.plosmedicine.org www.ploscollections.org
#           www.plosclinicaltrials.org
# 10.5.4.24 www.plosone.org www.plosntds.org www.plosbiology.org www.plosgenetics.org
#           www.plospathogens.org
# 10.5.4.24 www.plosjournals.org journals.plos.org collections.plos.org www.collections.plos.org
#           www.journals.plos.org
# 10.136.128.75 register.plos.org
# 10.5.4.24 dx.plos.org
# 10.136.128.44 community.plos.org
# Environment variables
environment = getenv('WEBDRIVER_ENVIRONMENT', default='prod')
base_url = getenv('WEBDRIVER_TARGET_URL', default='https://journals-dev.plos.org/')
cms_host = getenv('WEBDRIVER_CMS_CREPO_URL',
                  default='http://journals-dev1-contentrepo1.soma.plos.org:8002/v1/')
cms_bucket = getenv('WEBDRIVER_CMS_CREPO_BUCKET', default='plive')
collections_url = getenv('WEBDRIVER_COLLECTIONS_URL', default='https://collections-dev.plos.org')
crepo_host = getenv('WEBDRIVER_CREPO_HOST', default='journals-dev1-contentrepo1.soma.plos.org')
mysql_host = getenv('WEBDRIVER_MYSQL_HOST', default='db-ambra-dev.soma.plos.org')
mysql_user = getenv('WEBDRIVER_MYSQL_USER', default='ambra')
mysql_password = getenv('WEBDRIVER_MYSQL_PASSWORD', default='')
rhino_url = getenv('WEBDRIVER_RHINO_URL',
                   default='http://journals-dev1-backend1.soma.plos.org:8006/v2')
crepo_bucket = getenv('WEBDRIVER_CREPO_BUCKET', default='mogilefs-prod-repo')
crepo_port = getenv('WEBDRIVER_CREPO_PORT', default='8002')
base_url_mobile = getenv('WEBDRIVER_MOBILE_TARGET_URL', default='https://journals-dev.plos.org/')
solr_url = getenv('WEBDRIVER_SOLR_URL',
                  default='http://solr-mega-dev.soma.plos.org/solr/journals_dev/select')
alm_url = getenv('WEBDRIVER_ALM_URL', default='https://alm.plos.org/api/v5/articles')
alm_api_key = getenv('WEBDRIVER_ALM_API_KEY', default='3pezRBRXdyzYW6ztfwft')
counter_url = getenv('WEBDRIVER_COUNTER_URL',
                     default='http://counter-201.soma.plos.org/api/v1.0/stats/totals/doi/')
existing_user_email = getenv('WEBDRIVER_USER_EMAIL', default='jgray__plos.org@mailinator.com')
existing_user_pw = getenv('WEBDRIVER_USER_PASSWORD', default='')
non_existing_user_email = getenv('WEBDRIVER_USER_EMAIL_NONEXIST',
                                 default='jgray158535test@mailinator.com')
device_type = getenv('WEBDRIVER_DEVICE_TYPE', default='desktop')
blogs_url = getenv('WEBDRIVER_BLOGS_URL', default='https://blogs.plos.org')
headless_browser = getenv('WEBDRIVER_HEADLESS', default=False, type=bool)
# WebDriver's Page Load timeout (in seconds)
page_load_timeout = getenv('WEBDRIVER_PAGELOAD_TIMEOUT', default=60, type=int)
wait_timeout = getenv('WEBDRIVER_WAIT_TIMEOUT', default=60, type=int)
script_timeout = getenv('WEBDRIVER_SCRIPT_TIMEOUT', default=30, type=int)

"""
Create a DB Configuration for use my MySQL.py
"""

dbconfig = {'user': mysql_user,
            'password': mysql_password,
            'host': mysql_host,
            'port': 3306,
            'database': 'ambra',
            'connection_timeout': 10
            }

repo_config = {'transport': 'http',
               'host': crepo_host,
               'port': crepo_port,
               'path': '/v1',
               }

# === Appium (stand-alone) module configuration section ===


"""
Since a Macintosh box can run both Android SDK & VMs *and* IOS Simulator it would be wise to
have Appium deployed on Macintosh hardware.

But in case it is not possible, you can still point to an Appium node for Android and a different
one for IOS, separately.
"""

run_against_appium = getenv('USE_APPIUM_SERVER', default=False, type=bool)
appium_android_node_url = getenv('APPIUM_NODE_URL', default='http://10.136.100.186:4723/wd/hub')
appium_ios_node_url = getenv('APPIUM_NODE_URL', default='http://10.136.100.186:4723/wd/hub')

# **Android** capabilities definition
ANDROID = {
    'browserName': 'Browser',
    'platformName': 'Android',
    'platformVersion': '4.4',
    'deviceName': 'Android Emulator'
}

# **iOS** capabilities definition
IOS = {
    'browserName': 'Safari',
    'platformName': 'iOS',
    'platformVersion': '7.1',
    'deviceName': 'iPhone Simulator'
}

# List of Appium (stand-alone mode) enabled browsers
appium_enabled_browsers = [
    ANDROID,
    IOS
]

# === Selenium Grid configuration section ===


"""
Set **USE_SELENIUM_GRID** env variable/default value to desired one of these:

1. **True** will run tests against ALL browsers in PLoS's Selenium Grid
2. **False** will run tests against your local **Firefox** browser

Set **SELENIUM_GRID_URL** env variable/default value to point to PLoS's Grid hub node.

Running through Grid **takes precedence** among all other configurations.

Ex: If you have both `USE_APPIUM_SERVER` **AND** `USE_SELENIUM_GRID` options set to **True**,
then tests will be run against the **Grid**.

You can *still* include IOS and ANDROID capabilities as *enabled browsers* in the Grid and will be 
run against Appium.

"""

run_against_grid = getenv('USE_SELENIUM_GRID', default=False, type=bool)
selenium_grid_url = getenv('SELENIUM_GRID_URL', default='http://10.136.104.99:4444/wd/hub')

"""

List of all PLoS's browsers enabled in the **Selenium Grid**.
Ignored when **run_against_grid** is set to **False**.

"""

grid_enabled_browsers = [
    DesiredCapabilities.FIREFOX,
    DesiredCapabilities.INTERNETEXPLORER,
    DesiredCapabilities.CHROME,
    DesiredCapabilities.SAFARI,
    IOS,
    ANDROID
]

"""
Defines the single browser with which we would like to run the test suite.
If no environment variable is defined, default to FIREFOX.
"""

browser_ = getenv('SELENIUM_BROWSER_TO_USE', default='FIREFOX')

# === Performance metric gathering configuration section ===


"""

Set **browsermob_proxy_enabled** to either *True* or *False* to enable or disable the use of
**BrowserMob Proxy** to gather performance metrics while the tests navigate the web sites.

Set **browsermob_proxy_path** variable to the path of the **BrowserMob Proxy** *binary* in your
machine.

"""
browsermob_proxy_enabled = getenv('USE_BROWSERMOB_PROXY', default=False, type=bool)
browsermob_proxy_path = '/opt/browsermob/bin/browsermob-proxy'
