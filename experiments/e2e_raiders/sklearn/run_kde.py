import numpy as np
import sklearn
import scipy.stats
import pandas as pd
import math
import time
import json

from sklearn.neighbors import (
    KernelDensity,
    KDTree,
)


def get_self_density(d, n, denorm=False):
    internal_bw = 1.0
    if denorm:
        internal_bw = 1.0/(math.sqrt(2*math.pi))

    return scipy.stats.multivariate_normal.pdf(
        np.zeros(d), 
        mean=np.zeros(d), 
        cov=np.identity(d)*internal_bw*internal_bw) / n


def estimate_kde_bw(data, use_std=False):
    if use_std:
        iqr = np.std(data, axis=0)
    else:
        q3 = np.percentile(data, 75, axis=0)
        q1 = np.percentile(data, 25, axis=0)
        iqr = q3 - q1
    bw = iqr * (data.shape[0])**(-1.0/(data.shape[1]+4))
    return bw


def run_benchmark(
        df_path, n, numScore, tol, cols,
        bwValue=None, bwMult=1.0,
        denorm=False, use_std=False):
    params = {
        "algorithm": "sklearn",
        "dataset": df_path,
        "dim": len(cols),
        "num_train": n,
        "num_test": numScore,
        "train_time": None,
        "test_time": None,
        # "num_kernels": None
    }
    print(params)
    data = pd.read_csv(df_path)[cols].iloc[:n].values

    trainstart = time.time()
    if bwValue is None:
        bw = bwMult*estimate_kde_bw(data, use_std=use_std)
        print("BW: {}".format(bw))
    else:
        bw = bwValue * np.ones(len(cols))
        print("BW: {}".format(bwValue))
    if numScore is None:
        numScore = len(data)

    internal_bw = 1
    if denorm:
        internal_bw = 1.0/(math.sqrt(2*math.pi))
    scaled_data = (data / bw) * internal_bw

    # Normalized Computations
    kde = KernelDensity(
        bandwidth=internal_bw,
        kernel='gaussian',
        algorithm='kd_tree',
        rtol=tol,
    )
    kde.fit(scaled_data)
    train_time = time.time() - trainstart
    params["train_time"] = 1000*train_time
    print("Trained in {}".format(train_time), flush=True)

    scorestart = time.time()
    scores = np.exp(kde.score_samples(scaled_data[:numScore]))
    score_time = time.time() - scorestart
    params["test_time"] = 1000*score_time
    print("Scored in {}".format(score_time), flush=True)
    print("Rate: {}".format(numScore/score_time))

    self_density = get_self_density(data.shape[1], data.shape[0])
    scores_minus_self = scores - self_density

    # scale scores back
    if denorm:
        final_scores = scores_minus_self
    else:
        final_scores = scores_minus_self / np.prod(bw)


    q = np.percentile(final_scores, 1.0)
    print("Quantile: {}".format(q))
    print("Final Output:")
    print(json.dumps(params))
    return final_scores

