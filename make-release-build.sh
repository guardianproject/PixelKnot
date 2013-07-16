#!/bin/sh

set -e

if [ -e ~/.android/bashrc ]; then
    . ~/.android/bashrc
else
    echo "No ~/.android/bashrc found, 'android' and 'ndk-build' must be in PATH"
fi

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

echo "Running ndk-build:"
ndk-build -C $projectroot/external/F5Android

if [ -e ~/.android/ant.properties ]; then
    cp ~/.android/ant.properties $projectroot/app/
else
    echo "skipping release ant.properties"
fi

./setup-ant.sh

cd $projectroot/app/
ant release

apk=$projectroot/app/bin/$projectname-release.apk
if [ -e $apk ]; then
    gpg --detach-sign $apk
else
    echo $apk does not exist!
fi
