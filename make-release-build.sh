#!/bin/sh

. ~/.android/bashrc

projectname=`sed -n 's,.*name="app_name">\(.*\)<.*,\1,p' app/res/values/strings.xml`

cd external/ActionBarSherlock
git reset --hard
git clean -fdx
cd external/F5Android
git reset --hard
git clean -fdx
cd ../..
git reset --hard
git clean -fdx
git submodule update --init --recursive

cp ~/.android/ant.properties .

./setup-ant.sh
ant release

gpg --detach-sign bin/$projectname-release.apk
