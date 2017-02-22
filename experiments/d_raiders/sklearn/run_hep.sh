#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/d_raiders/sklearn

for i in 1 2 3 4 8 16 27
do
    echo "Running n $i"
    python $D/hep_${i}.py | tee $D/out/hep_${i}.out
done
