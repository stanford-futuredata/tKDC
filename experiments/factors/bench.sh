#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/factors
for i in 1 2 3 4 5 6
do
    ./run.sh $D/conf/c$i.yaml $D/scores/c$i.csv | tee $D/output/c$i.txt
done
