#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/p_raiders/sklearn

python3 $D/tmy3.py | tee $D/out/tmy3.out
