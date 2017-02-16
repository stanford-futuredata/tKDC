#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/n_raiders/ic2

for i in 0 1 2 3 4 5 6 7 8
do
    echo "Running n $i"
    ./run.sh $D/gauss_n${i}.yaml | tee $D/out/gauss_n${i}.txt
done