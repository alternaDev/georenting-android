#!/bin/sh
echo no | android create avd --force -n test -t android-21 --abi "google_apis/x86"
emulator -avd test -no-window &
android-wait-for-emulator
