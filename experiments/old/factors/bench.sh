#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/cutofftolerance
./run.sh $D/conf/tol0p0cut1p0.yaml $D/scores/tol0p0cut1p0.csv | tee $D/output/tol0p0cut1p0.txt
./run.sh $D/conf/tol0p0cut1p5.yaml $D/scores/tol0p0cut1p5.csv | tee $D/output/tol0p0cut1p5.txt
./run.sh $D/conf/tol0p0cutInf.yaml $D/scores/tol0p0cutInf.csv | tee $D/output/tol0p0cutInf.txt
./run.sh $D/conf/tol0p1cut1p0.yaml $D/scores/tol0p1cut1p0.csv | tee $D/output/tol0p1cut1p0.txt
./run.sh $D/conf/tol0p1cut1p5.yaml $D/scores/tol0p1cut1p5.csv | tee $D/output/tol0p1cut1p5.txt
./run.sh $D/conf/tol0p1cutInf.yaml $D/scores/tol0p1cutInf.csv | tee $D/output/tol0p1cutInf.txt
