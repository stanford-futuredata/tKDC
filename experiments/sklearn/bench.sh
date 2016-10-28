#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/sklearn

#for t in 0.0 0.1
#do
#    python3 $D/sklearn_perf.py \
#    --path data/room.csv --n 20000 --numScore 20000 --tol $t --cols 2 5 \
#    --out $D/scores/room_d2_t$t.csv \
#    | tee $D/output/room_d2_t$t.txt
#
#    python3 $D/sklearn_perf.py \
#    --path data/room.csv --n 20000 --numScore 20000 --tol $t --cols 2 5 3 6 \
#    --out $D/scores/room_d4_t$t.csv \
#    | tee $D/output/room_d4_t$t.txt
#done

for i in 2 4 8
do
    for t in 0.0 0.1
    do
        python3 $D/sklearn_perf.py \
        --path data/us_energy_1p0_metrics.csv --n 500000 --numScore 1000 --tol $t --cols `seq -s " " 0 $(($i-1))` \
        --out $D/scores/energy_d${i}_t$t.csv \
        | tee $D/output/energy_d${i}_t$t.txt

        python3 $D/sklearn_perf.py \
        --path data/home_sensor.csv --n 500000 --numScore 1000 --tol $t --cols `seq -s " " 3 $(($i+2))` \
        --out $D/scores/home_d${i}_t$t.csv \
        | tee $D/output/home_d${i}_t$t.txt
    done
done