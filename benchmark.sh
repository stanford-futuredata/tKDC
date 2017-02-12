#!/usr/bin/env bash

mvn exec:java@benchmark -Dexec.args="$*"
