ps = [1,2,3,4,5,10,20,30,40,50,60,70,80,90,95,99]
for p in ps:
    output = """
inputPath: "bigdata/bgauss.csv"
inputColumnRange: 0-1
inputRows: 100000000
timeToScore: 20.0

tKDEConf:
  algorithm: TREEKDE
  percentile: {p}

  qSampleSize: 20000
  qCutoffMultiplier: 1.1
  qTolMultiplier: 0.1

  kernel: gaussian
  denormalized: false
  bwMultiplier: 1.0
  useStdDev: true
  ignoreSelfScoring: true

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: true
  gridSizes:
    - 0.8

""".format(p=0.01 * p)
    with open("gauss_{}.yaml".format(p), 'w') as f:
        f.write(output)