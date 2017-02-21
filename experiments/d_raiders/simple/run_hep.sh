#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/d_raiders/simple

for i in 1 2 3 4 8 16 27
do
    echo "Running n $i"
    ./run2.sh $D/hep_${i}.yaml | tee $D/out/hep_${i}.out
done
