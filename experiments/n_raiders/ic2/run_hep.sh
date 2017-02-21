#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/n_raiders/ic2

for i in 0 1 2 3 4 5 6 7 8 9 10
do
    echo "Running n $i"
    ./run2.sh $D/hep_n${i}.yaml | tee $D/out/hep_n${i}.out
done
