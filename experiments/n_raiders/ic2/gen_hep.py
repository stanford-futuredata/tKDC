import numpy as np

ns = (10000 * 2**np.arange(0,11)).astype(int)
for i, n in enumerate(ns):
    output = """
inputPath: "bigdata/hep.csv"
inputColumnRange: 1-27
inputRows: {n}
timeToScore: 180.0
numToScore: 10000000

tKDEConf:
  algorithm: TREEKDE
  percentile: 0.01

  qSampleSize: 2000
  qReservoirMax: 10000000
  qCutoffMultiplier: 1.3
  qTolMultiplier: 0.01

  kernel: gaussian
  denormalized: true
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(n=n)
    with open("./hep_n{i}.yaml".format(i=i), 'w') as f:
        f.write(output)