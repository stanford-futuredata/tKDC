#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/simplekde
./run.sh $D/conf/simple_energy_d2.yaml $D/scores/simple_energy_d2.csv | tee $D/output/simple_energy_d2.txt
./run.sh $D/conf/simple_energy_d4.yaml $D/scores/simple_energy_d4.csv | tee $D/output/simple_energy_d4.txt
./run.sh $D/conf/simple_energy_d8.yaml $D/scores/simple_energy_d8.csv | tee $D/output/simple_energy_d8.txt
