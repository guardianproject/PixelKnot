#!/bin/sh

set -e

. ~/.android/bashrc

projectroot=`pwd`
projectname=`sed -n 's,.*name="app_name">\(.*\)<.*,\1,p' app/res/values/strings.xml`

cd $projectroot/external/ActionBarSherlock
git reset --hard
git clean -fdx
cd $projectroot/external/F5Android
git reset --hard
git clean -fdx
cd $projectroot
git reset --hard
git clean -fdx
git submodule update --init --recursive

cp ~/.android/ant.properties $projectroot/app/
./setup-ant.sh

cd $projectroot/app/
ant release

apk=$projectroot/app/bin/$projectname-release.apk
if [ -e $apk ]; then
    gpg --detach-sign $apk
else
    echo $apk does not exist!
fi
