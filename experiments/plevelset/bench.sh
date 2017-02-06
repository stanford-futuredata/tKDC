#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/plevelset

for p in 1 10 20 30 40 50 60 70 80 90 99
do
    echo "Running p $p"
    ./run.sh $D/conf/p${p}.yaml  $D/scores/p${p}.csv  | tee $D/output/p${p}.txt
done
