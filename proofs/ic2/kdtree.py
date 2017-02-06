from typing import Tuple

import numpy as np


def get_min_max_vector(v1, v2, q):
    d1 = q - v1
    d2 = q - v2
    d1_a = np.abs(d1)
    d2_a = np.abs(d2)
    furthest = np.maximum(d1_a, d2_a)
    closest = (d1 * d2 > 0) * np.minimum(d1_a, d2_a)
    return closest, furthest


class KDTree:
    LEAF_SIZE = 20

    def __init__(self, dim=1, split_axis=0, depth=0):
        self.dim = dim

        self.low = []
        self.high = []
        self.numPoints = 0
        self.points = []

        self.depth = depth
        self.splitAxis = split_axis
        self.splitValue = np.nan
        self.leftChild = None
        self.rightChild = None

    def build(self, pts):
        self.low = np.min(pts, axis=0)
        self.high = np.max(pts, axis=0)
        self.numPoints = len(pts)
        if self.numPoints <= self.LEAF_SIZE:
            self.points = pts
            return self

        k = self.splitAxis
        # self.splitValue = np.mean(np.percentile(pts[:, k], [10, 90]))
        self.splitValue = np.median(pts[:,k])
        left_points = pts[:, k] < self.splitValue
        right_points = ~left_points

        next_axis = (k + 1) % self.dim
        self.leftChild = KDTree(
            dim=self.dim, split_axis=next_axis, depth=self.depth+1
        ).build(pts[left_points])
        self.rightChild = KDTree(
            dim=self.dim, split_axis=next_axis, depth=self.depth+1
        ).build(pts[right_points])
        return self

    def is_leaf(self) -> bool:
        return (self.leftChild is None) and (self.rightChild is None)

    def calc_weight(self, kernel, query) -> Tuple[float, float]:
        closest, furthest = get_min_max_vector(self.low, self.high, query)
        return self.numPoints*kernel.pdf(furthest), self.numPoints*kernel.pdf(closest)

    def calc_exact(self, kernel, query) -> float:
        return np.sum(kernel.multi_pdf(query - self.points))

    def __repr__(self) -> str:
        return "Tree({d}:{n},{low},{high})".format(
            d=self.depth,
            n=self.numPoints,
            low=self.low,
            high=self.high
        )
