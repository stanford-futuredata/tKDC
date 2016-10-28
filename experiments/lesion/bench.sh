#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/lesion
for i in 0 1 2 3 4
do
    ./run.sh $D/conf/c$i.yaml $D/scores/c$i.csv | tee $D/output/c$i.txt
done
