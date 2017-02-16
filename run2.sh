#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="macrobase.KDEApp" -Dexec.args="$*"
