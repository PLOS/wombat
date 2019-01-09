#!/bin/sh

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


# The purpose of this script is to bundle the plos-themes repository into a
# tarball, for public consumption by users of the open-source Ambra stack,
# with all of the PLOS secrets redacted from it.
#
# This script is intended to be run only from its own directory. (We may
# want to factor out the relative paths and add an option for path
# arguments.) It must be part of a repository clone named `plos-themes/` and
# creates a file named `plos-example-themes.tar.gz` next to the plos-themes
# directory.
#
# You must ensure that there is no uncommitted junk in your local copy of
# the repository, or else it will be included in the tarball. Check this
# with `git status`.

###############################################################################
# Create the new directory

rm -rf ../../plos-example-themes/
rm ../../plos-example-themes.tar.gz
cp -r ../../plos-themes ../../plos-example-themes/

###############################################################################
# Delete secrets from the new directory

cd ../../plos-example-themes/

# Clear out IntelliJ project cruft
rm -rf .idea/
rm *.iml *.iws *.ipr

# It's all moot if we don't remove the history.
rm -rf .git

# This is general is a home for prod values, some of which may be secret.
rm -rf prodconfig/

# Delete everything from the config directory (because it has a lot of keys
# littered around, and more might be added to new files), except for the Python
# scripts with actual logic in them.
rm -rf config/
mkdir config/
cp ../plos-themes/config/build_config_rhino.py  config/
cp ../plos-themes/config/build_config_utils.py  config/
cp ../plos-themes/config/build_config_wombat.py config/

# Our API key to https://alm.plos.org is here.
find -name 'alm.yaml' | xargs rm

# Our API key to https://api.orcid.org is here.
find -name 'orcid.yaml' | xargs rm

# Contains credentials of unknown sensitivity for test user profiles.
rm test/Base/Config.py
find test/ -name 'resources.py' | xargs rm
find test/ -name '*.pyc' | xargs rm

###############################################################################
# Package up the deliverable

cd ..
tar -cf plos-example-themes.tar plos-example-themes/
gzip plos-example-themes.tar
