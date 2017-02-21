#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/r_raiders/

for i in 0 1 2 3 4 5 6 7
do
    echo "Running n $i"
    ./run2.sh $D/tmy3_t${i}.yaml | tee $D/out/tmy3_t${i}.out
done
