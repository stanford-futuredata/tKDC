import numpy as np

ns = (10000 * 3**np.arange(0,9)).astype(int)
for i, n in enumerate(ns):
    if n > 1000000:
        numToScore = 100
    else:
num
    output = """
inputPath: "bigdata/bgauss.csv"
inputColumnRange: 0-1
inputRows: {n}
numToScore: 1000

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: 0.01

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
