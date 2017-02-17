library(ks)
library(readr)
library(argparse)
library(jsonlite)

parser <- ArgumentParser(description='Run KDE')
parser$add_argument("--path", required=TRUE)
parser$add_argument("--n", type="integer", required=TRUE)
parser$add_argument("--numScore", type="integer", required=TRUE)
parser$add_argument("--cols", type="integer", nargs="+", required=TRUE)

args = parser$parse_args()

calc_bandwidth <- function(x) {
  iqrange = apply(x, 2, sd)
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
  binned=TRUE,
  H=diag(bw)**2,
  eval.points=metrics[1:args$numScore,],
  supp=5,
  verbose=TRUE
)
time_elapsed <- summary(proc.time() - ptm)[["user"]]
results = list(
    algorithm = "ks",
    dataset = args$path,
    dim = length(args$cols),
    num_train = args$n,
    num_test = args$numScore,
    train_time = 0.0,
    test_time = time_elapsed * 1000
)
print(time_elapsed)
print("Parsed Output:")
print(jsonlite::toJSON(results, auto_unbox=TRUE))