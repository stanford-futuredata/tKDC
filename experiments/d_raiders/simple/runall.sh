#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/d_raiders/simple

for i in 1 2 4 8 16 32 64 128 256
do
    echo "Running n $i"
    ./run2.sh $D/mnist_${i}.yaml | tee $D/out/mnist_${i}.out
done
