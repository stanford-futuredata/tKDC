ds = [1, 2, 4, 8, 16, 32, 64, 128, 256]
for i, d in enumerate(ds):
    output = """
inputPath: "bigdata/pmnist.csv"
inputColumnRange: 0-{d}
inputRows: 0
timeToScore: 20.0

tKDEConf:
  algorithm: TREEKDE
  percentile: 0.01
  qSampleSize: 2000
  qReservoirMin: 1000
  qCutoffMultiplier: 1.2
  qTolMultiplier: 0.01

  kernel: gaussian
  denormalized: true
  bwMultiplier: 3.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(
        d=d-1,
    )
    with open("./mnist_{d}.yaml".format(d=d), 'w') as f:
        f.write(output)