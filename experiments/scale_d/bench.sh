#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/scale_d
for i in 1 2 3 4 5 6 7 8
do
    ./run.sh $D/conf/c$i.yaml $D/scores/c$i.csv | tee $D/output/c$i.txt
done
