#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/scale_n
for i in 1 2 3 4 5 7 10
do
    ./run.sh $D/conf/c$i.yaml $D/scores/c$i.csv | tee $D/output/c$i.txt
done
