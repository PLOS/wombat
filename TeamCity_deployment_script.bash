#!/bin/bash
# TeamCity build script for dev, qa, stage deploys
# current_author = fcabrales@plos.org, gtimonina@plos.org

echo $PATH

if [ -z $TARGET_ENV ]; then
  echo "Need to set $TARGET_ENV"
  exit 1
fi

# Exporting the name of the current branch
export BRANCH_NAME=%teamcity.build.branch%

# Exporting the version control system number
export VCS_NUMBER=%build.vcs.number%

echo "Deploying release..."

# Printing branch name
echo $BRANCH_NAME

# Printing version control number
echo 'VCS_NUMBER: ' $VCS_NUMBER

# Printing TARGET_ENV
echo  'TARGET_ENV: ' $TARGET_ENV

# find debian package name to display it TC job in status line
ART_DIR="%teamcity.build.checkoutDir%/debian/"
DEB_PACKAGE=$(find $ART_DIR -name  *.deb -type f | grep -o wombat.*\.deb)
echo $DEB_PACKAGE

wraptly repo add -r trusty_$TARGET_ENV -p ubuntu debian/wombat_*.deb || exit 1
echo "##teamcity[buildStatus text='{build.status.text}: $TARGET_ENV: $DEB_PACKAGE']"
