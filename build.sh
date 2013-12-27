#!/bin/bash
set -e
cp -r test/* src
export PATH=$PATH:$GOROOT/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANT_HOME/bin
ant stop_ts start_ts uninstall emma debug install test fetch-test-report stop_ts
