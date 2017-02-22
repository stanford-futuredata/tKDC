for d in [1,2,3,4,8,16,32,64]:
    output = """
inputPath: "bigdata/hep.csv"
inputColumnRange: 0-{d}
inputRows: 0
timeToScore: 600.0

tKDEConf:
  algorithm: TREEKDE
  percentile: 0.01

  qSampleSize: 2000
  qReservoirMax: 1000000
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
""".format(
        d=d-1,
    )
    with open("./hep_{d}.yaml".format(d=d), 'w') as f:
        f.write(output)