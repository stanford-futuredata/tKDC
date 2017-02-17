import numpy as np

ns = (10000 * 3**np.arange(0,9)).astype(int)
for i, n in enumerate(ns):
    output = """
inputPath: "bigdata/bgauss.csv"
inputColumnRange: 0-1
inputRows: {n}
timeToScore: 30.0

tKDEConf:
  algorithm: TREEKDE
  percentile: 0.01

  qSampleSize: 20000
  qCutoffMultiplier: 1.1
  qTolMultiplier: 0.1

  kernel: gaussian
  denormalized: false
  bwMultiplier: 1.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(n=n)
    with open("./gauss_n{i}.yaml".format(i=i), 'w') as f:
        f.write(output)