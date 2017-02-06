library(ks)
library(readr)

calc_bandwidth <- function(x) {
  iqrange = apply(x, 2, IQR)
  scale_factor = dim(x)[1] ** (-1.0/(dim(x)[2]+4))
  return (scale_factor * iqrange)
}

dat = read_csv("../../data/us_energy_1p0_metrics.csv")
n = 5000
nScore = 5000
cols = c(1,2,3,4,5)

metrics = as.matrix(dat[1:n,cols])
bw = calc_bandwidth(metrics)
print("Bandwidth:")
print(bw)

fhat <- kde(
  x=metrics,
  binned=TRUE,
  H=diag(bw)**2,
  eval.points=metrics[1:nScore,],
  supp=5,
  bgridsize=rep(21,5),
  verbose=TRUE
)