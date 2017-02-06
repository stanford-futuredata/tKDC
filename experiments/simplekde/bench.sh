#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/simplekde
#for i in 2 4
#do
#    echo "Running room $i"
#    ./run.sh $D/conf/room_d$i.yaml  $D/scores/room_d$i.csv  | tee $D/output/room_d$i.txt
#done

for i in 1 2 3 4 5 6 7 8
do
    echo "Running energy $i"
    ./run.sh $D/conf/energy_d$i.yaml  $D/scores/energy_d$i.csv  | tee $D/output/energy_d$i.txt
    echo "Running home $i"
    ./run.sh $D/conf/home_d$i.yaml  $D/scores/home_d$i.csv  | tee $D/output/home_d$i.txt
done

