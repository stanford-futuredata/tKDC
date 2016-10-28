#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/smallacc/ks

for b in FALSE
do
    if $b = TRUE
    then
        bflag="--binned"
    else
        bflag=""
    fi
    Rscript $D/rkde.r \
    --path data/shuttle.csv --n 43500 --numScore 43500 \
    ${bflag} --cols 0 2 \
    --out $D/scores/shuttle_d2_b$b.csv \
    | tee $D/output/shuttle_d2_b$b.txt

    Rscript $D/rkde.r \
    --path data/shuttle.csv --n 43500 --numScore 43500 \
    ${bflag} --cols 0 2 4 5 \
    --out $D/scores/shuttle_d4_b$b.csv \
    | tee $D/output/shuttle_d4_b$b.txt
done

for i in 2 4
do
    for b in FALSE
    do
        if $b = TRUE
        then
            bflag="--binned"
        else
            bflag=""
        fi
        Rscript $D/rkde.r \
        --path data/us_energy_1p0_metrics.csv --n 50000 --numScore 50000 \
        ${bflag} --cols `seq -s " " 0 $(($i-1))` \
        --out $D/scores/energy_d${i}_b${b}.csv \
        | tee $D/output/energy_d${i}_b${b}.txt

        Rscript $D/rkde.r \
        --path data/home_sensor.csv --n 50000 --numScore 50000 \
        ${bflag} --cols `seq -s " " 3 $(($i+2))`  \
        --out $D/scores/home_d${i}_b${b}.csv \
        | tee $D/output/home_d${i}_b${b}.txt
    done
done
