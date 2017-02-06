import math
import numpy as np


class Kernel:
    def __init__(self, k: int, bw: float):
        self.k = k
        self.bw = bw
        self.invbw = 1.0 / bw
        self.constant = (2*math.pi*bw*bw)**(-k/2)

    def pdf(self, query: np.ndarray) -> float:
        d = self.invbw * query
        return self.constant * math.exp(-.5 * d.dot(d))

    def multi_pdf(self, queries: np.ndarray) -> np.ndarray:
        d = self.invbw * queries
        return self.constant * np.exp(-.5 * np.sum(d * d, axis=1))

    def inverse_pdf(self, val: float) -> float:
        r2 = -2*math.log(val) - self.k*math.log(self.bw*self.bw*2*math.pi)
        return math.sqrt(r2) * self.bw
