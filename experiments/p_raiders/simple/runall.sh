#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/p_raiders/simple

./run2.sh $D/tmy3.yaml | tee $D/out/tmy3.out
