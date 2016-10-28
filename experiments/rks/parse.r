library(ks)
library(readr)
library(argparse)

parser <- ArgumentParser(description='Run KDE')
parser$add_argument("--binned", action="store_true", default=FALSE)

args = parser$parse_args()

print(args$binned)
