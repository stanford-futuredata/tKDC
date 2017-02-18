#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/n_raiders/sklearn

for i in 0 1 2 3 4 5 6 7 8
do
    echo "Running n $i"
    python3 $D/gauss_n${i}.py | tee $D/out/gauss_n${i}.out
done
