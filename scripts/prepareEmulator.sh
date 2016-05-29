#!/bin/sh
echo no | android create avd --force -n test -t android-21 --abi "google_apis/armeabi-v7a"
emulator -avd test -no-window -no-audio &
android-wait-for-emulator
- adb shell input keyevent 82 &
