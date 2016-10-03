import numpy as np
import sklearn
import scipy.stats
import timeit
import pandas as pd
import math
import itertools
import time
import argparse

from sklearn.neighbors import (
    KernelDensity,
    KDTree,
)

def get_self_density(d, n):
    return scipy.stats.multivariate_normal.pdf(
        np.zeros(d), 
        mean=np.zeros(d), 
        cov=np.identity(d)) / n
def estimate_kde_bw(data):
    q3 = np.percentile(data, 75, axis=0)
    q1 = np.percentile(data, 25, axis=0)
    iqr = q3 - q1
    bw = iqr * (data.shape[0])**(-1.0/(data.shape[1]+4))
    return bw
def get_scores(data, numScore=None, tol=0, bw=None):
    trainstart = time.time()
    if bw is None:
        bw = estimate_kde_bw(data)
    if numScore is None:
        numScore = len(data)
    scaled_data = data / bw
    
    # Normalized Computations
    kde = KernelDensity(
        bandwidth=1,
        kernel='gaussian',
        algorithm='kd_tree',
        rtol=tol,
    )
    kde.fit(scaled_data)
    print("Trained in {}".format(time.time()-trainstart), flush=True)
    scorestart = time.time()
    scores = np.exp(kde.score_samples(scaled_data[:numScore]))
    print("Scored in {}".format(time.time()-scorestart), flush=True)
    
    self_density = get_self_density(data.shape[1], data.shape[0])
    scores_minus_self = scores - self_density
    
    # Denormalize
    denorm_scores = scores_minus_self / np.prod(bw)
    return denorm_scores

def run_benchmark(df, n, numScore, tol, cols):
    print("n: {}, numScore: {}, tol: {}, cols: {}".format(n, numScore, tol, cols))
    data = df[cols].iloc[:n].values
    start = time.time()
    scores = get_scores(data, numScore=numScore, tol=tol)
    q = np.percentile(scores, 1.0)
    print("Quantile: {}".format(q))
    elapsed = time.time() - start
    print("Elapsed: {}".format(elapsed))
    return scores

parser = argparse.ArgumentParser(description='Benchmark SKLearn')
parser.add_argument(
    '--path', required=True
)
parser.add_argument(
    '--n', required=True, type=int
)
parser.add_argument(
    '--numScore', required=True, type=int
)
parser.add_argument(
    '--tol', required=True, type=float
)
parser.add_argument(
    '--cols', required=True, type=int, nargs='+'
)
parser.add_argument(
    '--out'
)

# python3 experiments/sklearn/sklearn_perf.py --path ../hello.txt --n 4 --numScore 4 --tol 0.1 --cols 0 1 --out out.txt

def main():
    args = parser.parse_args()
    print(args)
    df = pd.read_csv(args.path)
    scores = run_benchmark(df, args.n, args.numScore, args.tol, args.cols)
    if args.out:
        df_scores = pd.DataFrame({"densities": scores})
        df_scores.to_csv(args.out, index=False)

main()