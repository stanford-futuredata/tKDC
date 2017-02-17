ds = [1, 2, 4, 8, 16, 32, 64, 128, 256]
for i, d in enumerate(ds):
    output = """
inputPath: "bigdata/pmnist.csv"
inputColumnRange: 0-{d}
inputRows: 0
timeToScore: 30.0

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: 0.01

  kernel: gaussian
  denormalized: true
  bwMultiplier: 3.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: false

  useGrid: false
""".format(
        d=d-1,
    )
    with open("./mnist_{d}.yaml".format(d=d), 'w') as f:
        f.write(output)