#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/smallacc/sklearn

for i in 2 4 8
do
    for t in 0.0 0.1
    do
        python3 $D/sklearn_perf.py \
        --path data/us_energy_1p0_metrics.csv --n 50000 --numScore 50000 \
        --tol $t --cols `seq -s " " 0 $(($i-1))` \
        --out $D/scores/energy_d${i}_t$t.csv \
        | tee $D/output/energy_d${i}_t$t.txt

        python3 $D/sklearn_perf.py \
        --path data/home_sensor.csv --n 50000 --numScore 50000 \
        --tol $t --cols `seq -s " " 3 $(($i+2))` \
        --out $D/scores/home_d${i}_t$t.csv \
        | tee $D/output/home_d${i}_t$t.txt

    done
done

#for t in 0.0 0.1
#do
#    python3 $D/sklearn_perf.py \
#    --path data/shuttle.csv --n 43500 --numScore 43500 \
#    --tol $t --cols 0 2 \
#    --out $D/scores/shuttle_d2_t$t.csv \
#    | tee $D/output/shuttle_d2_t$t.txt
#
#    python3 $D/sklearn_perf.py \
#    --path data/shuttle.csv --n 43500 --numScore 43500 \
#    --tol $t --cols 0 2 4 5\
#    --out $D/scores/shuttle_d4_t$t.csv \
#    | tee $D/output/shuttle_d4_t$t.txt
#
#    python3 $D/sklearn_perf.py \
#    --path data/shuttle.csv --n 43500 --numScore 43500 \
#    --tol $t --cols 0 2 4 5 6 7 8\
#    --out $D/scores/shuttle_d7_t$t.csv \
#    | tee $D/output/shuttle_d7_t$t.txt
#done
