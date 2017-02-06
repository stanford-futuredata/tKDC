data = ["energy", "home"]
dimensions = [1,2,3,4,5,6,7,8]
fnames = {
    "energy": "us_energy_1p0_metrics.csv",
    "home": "home_sensor.csv"
}
col_offset = {
    "energy": 0,
    "home": 3,
}

for cur_data in data:
    for cur_dim in dimensions:
        colstr = str(list(range(col_offset[cur_data], col_offset[cur_data]+cur_dim)))
        fname = fnames[cur_data]
        output = """inputPath: "data/{fname}"
inputColumns: {cols}
inputRows: 500000
numToScore: 1000

tKDEConf:
  algorithm: SIMPLEKDE
  percentile: 0.01

  kernel: gaussian
  bwMultiplier: 1.0
  ignoreSelfScoring: true

  calculateCutoffs: false
  useGrid: false
""".format(
            fname=fname,
            cols=colstr,
        )
        conf_file = "experiments/simplekde/conf/{dataset}_d{dim}.yaml".format(
            dataset=cur_data,
            dim=cur_dim
        )
        with open(conf_file, 'w') as f:
            f.write(output)