ps = [1,5,10,20,30,40,50,60,70,80,90,95,99]
for p in ps:
    output = """
inputPath: "bigdata/otmy3.csv"
inputColumnRange: 0-3
inputRows: 0
timeToScore: 60.0

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: {p}

  kernel: gaussian
  denormalized: false
  bwMultiplier: 1.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: false

  leafSize: 20
  splitByWidth: true

  useGrid: false
""".format(p=0.01 * p)
    with open("tmy3_{}.yaml".format(p), 'w') as f:
        f.write(output)