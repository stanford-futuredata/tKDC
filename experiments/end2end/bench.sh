#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/end2end
#./run.sh $D/conf/energy_n500_d2_tol1.yaml  $D/scores/energy_n500_d2_tol1.csv  | tee $D/output/energy_n500_d2_tol1.txt
#./run.sh $D/conf/energy_n500_d4_tol1.yaml  $D/scores/energy_n500_d4_tol1.csv  | tee $D/output/energy_n500_d4_tol1.txt
#./run.sh $D/conf/energy_n500_d8_tol1.yaml  $D/scores/energy_n500_d8_tol1.csv  | tee $D/output/energy_n500_d8_tol1.txt
#./run.sh $D/conf/energy_n500_d2_tol10.yaml $D/scores/energy_n500_d2_tol10.csv | tee $D/output/energy_n500_d2_tol10.txt
#./run.sh $D/conf/energy_n500_d4_tol10.yaml $D/scores/energy_n500_d4_tol10.csv | tee $D/output/energy_n500_d4_tol10.txt
#./run.sh $D/conf/energy_n500_d8_tol10.yaml $D/scores/energy_n500_d8_tol10.csv | tee $D/output/energy_n500_d8_tol10.txt
./run.sh $D/conf/energy_n50_d2_tol1.yaml  $D/scores/energy_n50_d2_tol1.csv  | tee $D/output/energy_n50_d2_tol1.txt
./run.sh $D/conf/energy_n50_d4_tol1.yaml  $D/scores/energy_n50_d4_tol1.csv  | tee $D/output/energy_n50_d4_tol1.txt
./run.sh $D/conf/energy_n50_d8_tol1.yaml  $D/scores/energy_n50_d8_tol1.csv  | tee $D/output/energy_n50_d8_tol1.txt
./run.sh $D/conf/energy_n50_d2_tol10.yaml $D/scores/energy_n50_d2_tol10.csv | tee $D/output/energy_n50_d2_tol10.txt
./run.sh $D/conf/energy_n50_d4_tol10.yaml $D/scores/energy_n50_d4_tol10.csv | tee $D/output/energy_n50_d4_tol10.txt
./run.sh $D/conf/energy_n50_d8_tol10.yaml $D/scores/energy_n50_d8_tol10.csv | tee $D/output/energy_n50_d8_tol10.txt
