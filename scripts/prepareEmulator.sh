#!/bin/sh
echo no | android create avd --force -n test -t "Google Inc.:Google APIs:23" --abi google_apis/armeabi-v7a
emulator -avd test -no-window &
android-wait-for-emulator
