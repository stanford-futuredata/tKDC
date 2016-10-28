#!/usr/bin/env bash
set -e
set -o pipefail

D=experiments/rks

# for b in TRUE FALSE
# do
#     Rscript $D/rkde.r \
#     --path data/room.csv --n 20000 --numScore 20000 --binned $b --cols 2 5 \
#     --out $D/scores/room_d2_b$b.csv \
#     | tee $D/output/room_d2_b$b.txt

#     Rscript $D/rkde.r \
#     --path data/room.csv --n 20000 --numScore 20000 --binned $b --cols 2 5 3 6 \
#     --out $D/scores/room_d4_b$b.csv \
#     | tee $D/output/room_d4_b$b.txt
# done

for b in TRUE FALSE
do
    Rscript $D/rkde.r \
    --path data/us_energy_1p0_metrics.csv --n 500000 --numScore 500000 --binned $b --cols 0 1 \
    --out $D/scores/energy_d2_b$b.csv \
    | tee $D/output/energy_d2_b$b.txt

    Rscript $D/rkde.r \
    --path data/home_sensor.csv --n 500000 --numScore 500000 --binned $b --cols 3 4 \
    --out $D/scores/home_d2_b$b.csv \
    | tee $D/output/home_d2_b$b.txt
done

# for b in TRUE FALSE
# do
#     numScore=100000

#     Rscript $D/rkde.r \
#     --path data/us_energy_1p0_metrics.csv --n 500000 --numScore $numScore --binned $b --cols 0 1 2 3 \
#     --out $D/scores/energy_d4_b$b.csv \
#     | tee $D/output/energy_d4_b$b.txt

#     Rscript $D/rkde.r \
#     --path data/home_sensor.csv --n 500000 --numScore $numScore --binned $b --cols 3 4 5 6 \
#     --out $D/scores/home_d4_b$b.csv \
#     | tee $D/output/home_d4_b$b.txt
# done
