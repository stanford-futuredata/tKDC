library(ks)
library(readr)
library(argparse)

parser <- ArgumentParser(description='Run KDE')
parser$add_argument("--path", required=TRUE)
parser$add_argument("--n", type="integer", required=TRUE)
parser$add_argument("--numScore", type="integer", required=TRUE)
parser$add_argument("--cols", type="integer", nargs="+", required=TRUE)
parser$add_argument("--out", default="")

parser$add_argument("--binned", type="logical", default=TRUE)

args = parser$parse_args()

calc_bandwidth <- function(x) {
  iqrange = apply(x, 2, IQR)
  scale_factor = dim(x)[1] ** (-1.0/(dim(x)[2]+4))
  return (scale_factor * iqrange)
}

# setwd("~/Documents/Projects/tKDE/experiments")
dat = read_csv(args$path)
metrics = as.matrix(dat[1:args$n,args$cols + 1])
bw = calc_bandwidth(metrics)
print("Bandwidth:")
print(bw)

ptm <- proc.time()
fhat <- kde(
  x=metrics,
  binned=args$binned,
  H=diag(bw)**2,
  eval.points = metrics[1:args$numScore,],
  supp=10,
  verbose = TRUE
)
print(proc.time() - ptm)
densities = fhat["estimate"][[1]]

if(args$out != ""){
    write.csv(densities, file=args$out)
}