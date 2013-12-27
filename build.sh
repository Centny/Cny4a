#!/bin/bash
set -e
ant stop_ts start_ts uninstall emma debug install test fetch-test-report stop_ts
