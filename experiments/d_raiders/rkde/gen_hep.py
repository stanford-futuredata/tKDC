ds = [1,2,3,4,8,16,27]
for i, d in enumerate(ds):
    output = """
inputPath: "bigdata/hep.csv"
inputColumnRange: 1-{d}
inputRows: 0
timeToScore: 600.0

tKDEConf:
  algorithm: RKDE
  percentile: 0.01

  kernel: gaussian
  denormalized: true
  useStdDev: true
  ignoreSelfScoring: true

  qSampleSize: 2000
  qReservoirMin: 1000
  qCutoffMultiplier: 1.2
  qTolMultiplier: 0.01

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(
        d=d,
    )
    with open("./hep_{d}.yaml".format(d=d), 'w') as f:
        f.write(output)