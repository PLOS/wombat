#!/usr/bin/env bash

export PATH="$HOME/bin:$HOME/.pyenv/bin:$PATH"
eval "$(pyenv init -)"
eval "$(pyenv virtualenv-init -)"

wget https://github.com/mozilla/geckodriver/releases/download/v0.23.0/geckodriver-v0.23.0-linux64
.tar.gz
tar -xvzf geckodriver*
chmod +x geckodriver
mv geckodriver /opt/teamcity/bin/

# export PYTHONIOENCODING=UTF-8
echo $PYTHONIOENCODING

SCRIPT_DIR=%teamcity.build.workingDir%/test

cd $SCRIPT_DIR
pip install --upgrade -r requirements.txt

# create directory for html pytest report
mkdir ./pytest_report

rm Base/*.pyc
rm desktop/*.pyc
rm desktop/Pages/*.pyc

pyenv activate raro3
pyenv version

pytest -v -s -n auto -l --reruns 2 --reruns-delay 3 --timeout=600 --teamcity --html='
./pytest_report/pytest_report.html' desktop/

pkill firefox
pyenv deactivate
pyenv version
