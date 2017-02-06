#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/rks

for i in 2 3 4
#for i in 1
do
    Rscript $D/rkde.r \
    --path data/us_energy_1p0_metrics.csv --n 500000 --numScore 500000 \
    --binned --cols `seq -s " " 0 $(($i-1))` \
    --out $D/scores/energy_d${i}.csv \
    | tee $D/output/energy_d${i}.txt

    Rscript $D/rkde.r \
    --path data/home_sensor.csv --n 500000 --numScore 500000 \
    --binned --cols `seq -s " " 3 $(($i+2))`  \
    --out $D/scores/home_d${i}.csv \
    | tee $D/output/home_d${i}.txt
done

