#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/p_raiders/ic2

for p in 1 10 20 30 40 50 60 70 80 90 99
do
    echo "Running p $p"
    ./run2.sh $D/gauss_${p}.yaml | tee $D/out/gauss_${p}.out
done
