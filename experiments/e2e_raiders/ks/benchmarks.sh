#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/e2e_raiders/ks

Rscript $D/rkde.r \
--path bigdata/bgauss.csv --n 100000000 --numScore 50000 --cols 0 1 \
| tee $D/out/bgauss.out

Rscript $D/rkde.r \
--path bigdata/otmy3.csv --n 1822080 --numScore 5000 --cols 0 1 2 3\
| tee $D/out/tmy3_4.out
