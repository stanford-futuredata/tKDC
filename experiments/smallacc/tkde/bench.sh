#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/smallacc/tkde

for i in 2 4 7
do
    echo "Running shuttle $i"
    ./run.sh $D/conf/shuttle_d$i.yaml  $D/scores/shuttle_d$i.csv  | tee $D/output/shuttle_d$i.txt
done

for i in 2 4 8
do
    echo "Running energy $i"
    ./run.sh $D/conf/energy_d$i.yaml  $D/scores/energy_d$i.csv  | tee $D/output/energy_d$i.txt
    echo "Running home $i"
    ./run.sh $D/conf/home_d$i.yaml  $D/scores/home_d$i.csv  | tee $D/output/home_d$i.txt
done

