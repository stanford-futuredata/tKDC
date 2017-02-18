#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/p_raiders/ic2

for p in 1 5 10 20 30 40 50 60 70 80 90 95 99
do
    echo "Running p $p"
    ./run2.sh $D/tmy3_${p}.yaml | tee $D/out/tmy3_${p}.out
done
