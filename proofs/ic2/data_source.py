import numpy as np
import scipy
import scipy.special
import scipy.stats

from ic2.kernel import Kernel


class DataSource:
    THRESHOLD_SAMPLE_SIZE = 1000000

    def __init__(self, k: int, num_train, num_query,
                 bw=None, threshold=None, p=None):
        self.k = k
        self.mu = np.zeros(k)
        self.cov = np.diag(np.ones(k))
        self.dist = scipy.stats.multivariate_normal(mean=self.mu, cov=self.cov)

        if threshold is None and p is None:
            raise Exception("Need threshold or p")
        elif threshold is not None:
            self.threshold = threshold
        else:
            np.random.seed(0)
            self.threshold = np.percentile(
                self.dist.pdf(self.sample(self.THRESHOLD_SAMPLE_SIZE)),
                p * 100
            )

        if bw is None:
            self.bw = num_train ** (-1 / (k + 4))
        else:
            self.bw = bw
        self.kernel = Kernel(k=k, bw=self.bw)

        np.random.seed(1)
        self.train_data = self.sample(num_train)
        np.random.seed(2)
        self.query_data = self.sample(num_query)

    def sample(self, sample_size: int):
        return np.random.multivariate_normal(mean=self.mu, cov=self.cov, size=sample_size)
