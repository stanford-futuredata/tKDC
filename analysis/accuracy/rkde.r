library(ks)
library(readr)

calc_bandwidth <- function(x) {
  iqrange = apply(x, 2, IQR)
  scale_factor = dim(x)[1] ** (-1.0/(dim(x)[2]+4))
  return (scale_factor * iqrange)
}

setwd("~/Documents/Projects/tKDE/experiments")
dat = read_csv("../us_energy_1p0_metrics.csv")
metrics = as.matrix(dat[1:500000,1:2])
bw = calc_bandwidth(metrics)

ptm <- proc.time()
fhat <- kde(
  x=metrics,
  binned=TRUE,
  H=diag(bw)**2,
  eval.points = metrics,
  supp=10,
  verbose = TRUE
)
print(proc.time() - ptm)
densities = fhat["estimate"][[1]]

write.csv(densities, file="rkde.csv")

