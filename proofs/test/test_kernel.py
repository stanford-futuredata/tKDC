import unittest

import numpy as np
import scipy
import scipy.stats

from ic2.kernel import Kernel


class KernelTest(unittest.TestCase):
    def test_pdf(self):
        k = 2
        bw = 2.0

        mu = np.zeros(k)
        cov = np.diag(np.ones(k))*bw*bw
        dist = scipy.stats.multivariate_normal(mean=mu, cov=cov)
        kernel = Kernel(k=k, bw=bw)
        loc = np.array([0.5, 0.5])
        p1 = dist.pdf(loc)
        p2 = kernel.pdf(loc)
        self.assertAlmostEqual(p1, p2, delta=1e-7*p1)

    def test_invpdf(self):
        k = 2
        bw = 2.0
        kernel = Kernel(k=k, bw=bw)

        pdf = 0.001
        r = kernel.inverse_pdf(pdf)
        x = np.zeros(k)
        x[0] = r
        r_pdf = kernel.pdf(x)
        self.assertAlmostEqual(pdf, r_pdf, 7)
