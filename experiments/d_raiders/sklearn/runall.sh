#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/d_raiders/sklearn

for i in 1 2 4 8 16 32 64 128 256
do
    echo "Running n $i"
    python3 $d/mnist_${1}.py | tee $D/out/mnist_${i}.out
done
