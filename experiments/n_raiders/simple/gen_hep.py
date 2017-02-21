import numpy as np

ns = (10000 * 2**np.arange(0,11)).astype(int)
for i, n in enumerate(ns):
    output = """
inputPath: "bigdata/hep.csv"
inputColumnRange: 1-27
inputRows: {n}
numToScore: 10000000
timeToScore: 180.0

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: 0.01

  kernel: gaussian
  denormalized: true
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: false

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(
        n=n,
    )
    with open("./hep_n{i}.yaml".format(i=i), 'w') as f:
        f.write(output)
