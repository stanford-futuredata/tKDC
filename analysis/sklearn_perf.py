import numpy as np
import sklearn
import scipy.stats
import matplotlib
import matplotlib.pyplot as plt
import timeit
import pandas as pd
import math
import itertools
import time
import pickle

from sklearn.neighbors import (
    KernelDensity,
    KDTree,
)
from sklearn.preprocessing import (
    RobustScaler
)

def get_self_density(d, n):
    return scipy.stats.multivariate_normal.pdf(
        np.zeros(d), 
        mean=np.zeros(d), 
        cov=np.identity(d)) / n;
def estimate_kde_bw(data):
    q3 = np.percentile(data, 75, axis=0)
    q1 = np.percentile(data, 25, axis=0)
    iqr = q3 - q1
    bw = iqr * (data.shape[0])**(-1.0/(data.shape[1]+4))
    return bw
def get_scores(data, numScore=None, tol=0, bw=None):
    print("Starting", flush=True)
    trainstart = time.time()
    if bw is None:
        bw = estimate_kde_bw(data)
    if numScore is None:
        numScore = len(data)
    print("BW calculated", flush=True)
    scaled_data = data / bw
    
    # Normalized Computations
    kde = KernelDensity(
        bandwidth=1,
        kernel='gaussian',
        algorithm='kd_tree',
        rtol=tol,
    )
    kde.fit(scaled_data)
    print("Trained", flush=True)
    print("Trained in {}".format(time.time()-trainstart), flush=True)
    scorestart = time.time()
    scores = np.exp(kde.score_samples(scaled_data[:numScore]))
    print("Scored", flush=True)
    print("Scored in {}".format(time.time()-scorestart), flush=True)
    
    self_density = get_self_density(data.shape[1], data.shape[0])
    scores_minus_self = scores - self_density
    
    # Denormalize
    denorm_scores = scores_minus_self / np.prod(bw)
    return denorm_scores
def compare_outliers(scores1, scores2):
    cut1 = np.percentile(scores1, 1.0)
    flag1 = scores1 < cut1
    
    cut2 = np.percentile(scores2, 1.0)
    flag2 = scores2 < cut2
    return np.sum(flag1 & flag2) / np.sum(flag1 | flag2)

def run_benchmarks(df, ns, numScores, tols, dims):
    timings = {}
    scores = {}
    combinations = itertools.product(ns, numScores, tols, dims)
    for t in combinations:
        n = t[0]
        numScore = t[1]
        tol = t[2]
        dim = t[3]
        print("n: {}, numScore: {}, tol: {}, dim: {}".format(n, numScore, tol, dim))
        columns = list(range(dim))
        data = df[columns].iloc[:n].values
        start = time.time()
        scores[t] = get_scores(data, numScore=numScore, tol=tol)
        elapsed = time.time() - start
        print("Elapsed: {}".format(elapsed))
        timings[t] = elapsed
    return {"timings": timings, "scores": scores}

energy = pd.read_csv("../us_energy_1p0_metrics.csv")
presults = run_benchmarks(
    df=energy,
    ns=[500000],
    numScores=[5000],
    tols=[0.0,0.1],
    dims=[2,4,8]
)