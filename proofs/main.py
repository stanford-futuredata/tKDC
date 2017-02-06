import time

import numpy as np
import pandas as pd
import scipy
import scipy.special
import scipy.stats

from ic2.kdtree import KDTree
from ic2.kernel import Kernel
from ic2.tkde import TKDE


def benchmark(k=1, p=0.1, eps=0.0, bw=None, num_training=10000, num_query=100):
    mu = np.zeros(k)
    cov = np.diag(np.ones(k))
    dist = scipy.stats.multivariate_normal(mean=mu, cov=cov)
    sample_size = 1000000
    np.random.seed(0)
    threshold = np.percentile(dist.pdf(
        np.random.multivariate_normal(mean=mu, cov=cov, size=sample_size)
    ), p * 100)
    # print("Threshold: {}".format(threshold))

    np.random.seed(0)
    data = np.random.multivariate_normal(mean=mu, cov=cov, size=num_training)
    if bw is None:
        bw = num_training**(-1/(k + 4))
    # print("BW: {}".format(bw))
    # kernel = scipy.stats.multivariate_normal(mean=mu, cov=cov * (bw*bw))
    kernel = Kernel(k=k, bw=bw)
    start_time = time.time()
    t = KDTree(dim=k).build(data)
    print("Constructed Tree in: {}".format(time.time() - start_time))
    raw_threshold = threshold*num_training
    tkde = TKDE(
        t,
        kernel,
        threshold=raw_threshold,
        epsilon=eps*raw_threshold
    )

    query_data = np.random.multivariate_normal(mean=mu, cov=cov, size=num_query)
    results = []
    start_time = time.time()
    for q in query_data:
        est_pdf = tkde.calc(q)
        results.append(tkde.diagnostics.copy())
    print("Finished in: {}".format(time.time() - start_time))
    return pd.DataFrame(results)


def run_size_test():
    dim = 10
    sizes = (5000.0 * (1.3**np.arange(18))).astype(int)
    kernels = []
    for s in sizes:
        print("Processing size: {}".format(s))
        cur_result = benchmark(k=dim, N=s, p=0.01, bw=.5)
        avgs = cur_result.mean()
        kernels.append(avgs["exact"] + avgs["box"])
    df = pd.DataFrame({
        "sizes": sizes,
        "kernels": kernels
    })
    df.to_csv("kernel_size_dim{}.csv".format(dim), index=False)


def run_dim_test():
    size = 100000
    dims = range(1, 12)
    kernels = []
    for d in dims:
        print("Processing dim: {}".format(d))
        cur_result = benchmark(k=d, N=size, p=0.01, bw=.05)
        avgs = cur_result.mean()
        kernels.append(avgs["exact"] + avgs["box"])
    df = pd.DataFrame({
        "dim": dims,
        "kernels": kernels
    })
    df.to_csv("kernel_dim_fixedbw.csv", index=False)


def test_single_points(k, N, p, qs):
    mu = np.zeros(k)
    cov = np.diag(np.ones(k))
    dist = scipy.stats.multivariate_normal(mean=mu, cov=cov)
    sample_size = 1000000
    np.random.seed(0)
    threshold = np.percentile(dist.pdf(
        np.random.multivariate_normal(mean=mu, cov=cov, size=sample_size)
    ), p * 100)
    print("Threshold: {}".format(threshold))

    np.random.seed(0)
    data = np.random.multivariate_normal(mean=mu, cov=cov, size=N)
    bw = 0.05
    # bw = N ** (-1/(k + 4))
    # print("BW: {}".format(bw))
    # kernel = scipy.stats.multivariate_normal(mean=mu, cov=cov * (bw*bw))
    kernel = Kernel(k=k, bw=bw)
    start_time = time.time()
    t = KDTree(dim=k).build(data)
    print("Constructed Tree in: {}".format(time.time() - start_time))
    raw_threshold = threshold*N
    eps = 0.01
    tkde = TKDE(t, kernel,
                threshold=raw_threshold,
                epsilon=eps*raw_threshold
                )
    return [(q[0],)+tkde.calc(q) for q in qs]


def run_vary_distance_test():
    k = 2
    rs = np.arange(0, 6, .1)
    qs = np.zeros((len(rs), k))
    for i in range(len(rs)):
        qs[i][0] = rs[i]

    res = test_single_points(k, 1000000, p=0.01, qs=qs)
    df = pd.DataFrame(res, columns=["r", "pdf", "exact", "box", "cause"])
    print(df)


def run_incremental_n_test():
    k=2
    exact_calcs = []
    for n in [100000, 200000, 400000, 800000]:
        res = benchmark(k=2, N=n, p=0.01, bw=0.05)
        res["used_exact"] = (res["exact"] > 0)
        exact_calcs.append(res["used_exact"].sum())
    print(exact_calcs)


if __name__ == "__main__":
    df = benchmark(10, p=0.1, eps=0.0, bw=None, num_training=20000, num_query=1000)
    print(df[["box_kernels", "exact_kernels"]].sum())
