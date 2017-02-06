import time

import numpy as np
import pandas as pd
import scipy
import scipy.special
import scipy.stats
import sklearn
import sklearn.neighbors

from ic2.data_source import DataSource
from ic2.kdtree import KDTree
from ic2.kernel import Kernel
from ic2.rkde import RKDE
from ic2.tkde import TKDE


def run_test(k, n, bw, epsilon=0.01):
    ds = DataSource(k=k, num_train=n, num_query=1000, p=0.5, bw=bw)
    tree = sklearn.neighbors.KDTree(ds.train_data)
    rkde = RKDE(kernel=ds.kernel, tree=tree, threshold=epsilon * ds.threshold)
    print("Initialized")

    params = {
        "k": k,
        "n": n,
        "bw": bw,
        "e": epsilon,
    }
    diagnostics = []
    for q in ds.query_data:
        rkde.calc(q)
        row = rkde.diagnostics.copy()
        row.update(params)
        diagnostics.append(row)
    diagnostics = pd.DataFrame(diagnostics)
    return diagnostics.groupby(list(params.keys())).mean()


def test():
    ks = [1, 4, 16]
    ns = [1000, 10000, 100000, 200000]
    epsilon = 0.01
    total_res = []
    for k in ks:
        bw = 1000**(-1/(k+4))
        for n in ns:
            print("k: {}, n: {}".format(k, n))
            cur_res = run_test(k, n, bw, epsilon)
            total_res.append(cur_res)
    total_res = pd.concat(total_res)
    print(total_res)
    total_res.to_csv("output/radius.csv")

if __name__ == "__main__":
    test()