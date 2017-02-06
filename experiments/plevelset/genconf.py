ps = [1,2,3,4,5,10,20,30,40,50,60,70,80,90,95,99]
for p in ps:
    output = """inputPath: "data/us_energy_1p0_metrics.csv"
inputColumns: [0,1,2,3]
inputRows: 500000
numToScore: 500000

tKDEConf:
  algorithm: TREEKDE
  percentile: {p}

  qSampleSize: 20000
  qCutoffMultiplier: 1.5
  qTolMultiplier: 0.1

  kernel: gaussian
  bwMultiplier: 1.0
  ignoreSelfScoring: true

  calculateCutoffs: true

  leafSize: 20
  splitByWidth: true

  useGrid: true
  gridSizes:
    - 1.0
""".format(p=0.01 * p)
    with open("./conf/p{}.yaml".format(p), 'w') as f:
        f.write(output)