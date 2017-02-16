./run2.sh experiments/e2e_raiders/ic2/bgauss.yaml | tee experiments/e2e_raiders/ic2/out/bgauss.out
./run2.sh experiments/e2e_raiders/ic2/tmy3.yaml | tee experiments/e2e_raiders/ic2/out/tmy3.out
./run2.sh experiments/e2e_raiders/ic2/hep.yaml | tee experiments/e2e_raiders/ic2/out/hep.out
./run2.sh experiments/e2e_raiders/ic2/home.yaml | tee experiments/e2e_raiders/ic2/out/home.out
./run2.sh experiments/e2e_raiders/ic2/mnist_64.yaml | tee experiments/e2e_raiders/ic2/out/mnist_64.out

python3 experiments/e2e_raiders/sklearn/bgauss.py | tee experiments/e2e_raiders/sklearn/out/bgauss.out
python3 experiments/e2e_raiders/sklearn/tmy3.py | tee experiments/e2e_raiders/sklearn/out/tmy3.out
python3 experiments/e2e_raiders/sklearn/hep.py | tee experiments/e2e_raiders/sklearn/out/hep.out
python3 experiments/e2e_raiders/sklearn/home.py | tee experiments/e2e_raiders/sklearn/out/home.out
python3 experiments/e2e_raiders/sklearn/mnist_64.py | tee experiments/e2e_raiders/sklearn/out/mnist_64.out

./run2.sh experiments/e2e_raiders/simple/bgauss.yaml | tee experiments/e2e_raiders/simple/out/bgauss.out
./run2.sh experiments/e2e_raiders/simple/tmy3.yaml | tee experiments/e2e_raiders/simple/out/tmy3.out
./run2.sh experiments/e2e_raiders/simple/hep.yaml | tee experiments/e2e_raiders/simple/out/hep.out
./run2.sh experiments/e2e_raiders/simple/home.yaml | tee experiments/e2e_raiders/simple/out/home.out
./run2.sh experiments/e2e_raiders/simple/mnist_64.yaml | tee experiments/e2e_raiders/simple/out/mnist_64.out

./run2.sh experiments/e2e_raiders/nocut/bgauss.yaml | tee experiments/e2e_raiders/nocut/out/bgauss.out
./run2.sh experiments/e2e_raiders/nocut/tmy3.yaml | tee experiments/e2e_raiders/nocut/out/tmy3.out
./run2.sh experiments/e2e_raiders/nocut/hep.yaml | tee experiments/e2e_raiders/nocut/out/hep.out
./run2.sh experiments/e2e_raiders/nocut/home.yaml | tee experiments/e2e_raiders/nocut/out/home.out

./run2.sh experiments/e2e_raiders/rkde/bgauss.yaml | tee experiments/e2e_raiders/rkde/out/bgauss.out
./run2.sh experiments/e2e_raiders/rkde/tmy3.yaml | tee experiments/e2e_raiders/rkde/out/tmy3.out
./run2.sh experiments/e2e_raiders/rkde/hep.yaml | tee experiments/e2e_raiders/rkde/out/hep.out
./run2.sh experiments/e2e_raiders/rkde/home.yaml | tee experiments/e2e_raiders/rkde/out/home.out

thresholds:
gauss: 0.0016
tmy3: 1.9e-20
hep: 6.0e-13
home: 1.01e-8
mnist64: 7.0e-6
mnist700

datasets:
gauss x
tmy3_8 x
hep x
home x
mnist_64 o
mnist700
tmy3_4

algs:
ic2
sklearn
simple
nocut
rkde
ks
grid ?
