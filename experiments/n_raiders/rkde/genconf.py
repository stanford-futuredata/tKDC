import numpy as np

ns = (10000 * 3**np.arange(0,9)).astype(int)
for i, n in enumerate(ns):
    output = """
inputPath: "bigdata/bgauss.csv"
inputColumnRange: 0-1
inputRows: {n}
timeToScore: 600

tKDEConf:
  algorithm: RKDE
  percentile: 0.01

  kernel: gaussian
  denormalized: false
  bwMultiplier: 1.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: false
  tolAbsolute: 2.0e-4

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(
        n=n,
    )
    with open("./gauss_n{i}.yaml".format(i=i), 'w') as f:
        f.write(output)
