tols = [0.01, 0.1, 1.0, 10.0, 100.0]
for i, tol in enumerate(tols):
    output = """
inputPath: "bigdata/otmy3.csv"
inputColumnRange: 0-3
inputRows: 0
timeToScore: 60.0

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
  qTolMultiplier: {tol}

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(
        tol=tol,
    )
    with open("./tmy3_t{i}.yaml".format(i=i), 'w') as f:
        f.write(output)