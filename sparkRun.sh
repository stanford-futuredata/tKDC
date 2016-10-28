#!/usr/bin/env bash

mvn exec:java@spark -Dexec.args="$*"
