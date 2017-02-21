for d in [1,2,3,4,8,16,27]:
    output = """
inputPath: "bigdata/hep.csv"
inputColumnRange: 1-{d}
inputRows: 0
timeToScore: 600.0

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: 0.01

  kernel: gaussian
  denormalized: true
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: false

  useGrid: false
""".format(
        d=d,
    )
    with open("./hep_{d}.yaml".format(d=d), 'w') as f:
        f.write(output)