import time

import kdtree
import numpy as np
import scipy
import scipy.stats

from ic2.kernel import Kernel


def integration_test():
    k = 2
    p = 0.01
    TRAIN_SIZE = 100000
    TEST_SIZE = 2000

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
    training_data = np.random.multivariate_normal(mean=mu, cov=cov, size=TRAIN_SIZE)
    bw = TRAIN_SIZE ** (-1 / (k + 4))
    print("BW: {}".format(bw))
    # kernel = scipy.stats.multivariate_normal(mean=mu, cov=cov * (bw*bw))
    kernel = Kernel(k=k, bw=bw)
    start_time = time.time()
    t = kdtree.KDTree(dim=k).build(training_data)
    print("Constructed Tree in: {}".format(time.time() - start_time))
    raw_threshold = threshold * TRAIN_SIZE
    eps = 0.01
    tkde = tkde.TKDE(
        t,
        kernel,
        threshold=raw_threshold,
        epsilon=eps * raw_threshold
    )

    np.random.seed(1)
    test_data = np.random.multivariate_normal(mean=mu, cov=cov, size=1000)
    test_pdfs = np.array([tkde.calc(test_query)[0] for test_query in test_data])
    est_threshold = np.percentile(test_pdfs, p * 100)
    print("Est Threshold: {}".format(est_threshold))

    actual_test_pdfs = dist.pdf(test_data)
    disagree_on = (~((actual_test_pdfs < threshold) == (test_pdfs < threshold)))
    n_disagree = np.sum(disagree_on)
    print("disagree on: {} ".format(n_disagree))
    print(test_pdfs[disagree_on])
    print(actual_test_pdfs[disagree_on])


if __name__ == "__main__":
    integration_test()
