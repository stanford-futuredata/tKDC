import numpy as np
from sklearn.neighbors.kd_tree import KDTree

from ic2.kernel import Kernel


class RKDE:
    def __init__(self, kernel: Kernel, tree: KDTree, threshold: float):
        self.kernel = kernel
        self.tree = tree
        self.data = np.array(tree.data)
        self.radius = kernel.inverse_pdf(threshold)
        self.diagnostics = {}

    def calc(self, query: np.ndarray) -> float:
        all_indices = self.tree.query_radius(
            np.array([query]), r=self.radius, return_distance=False
        )
        distances = self.data[all_indices[0]] - query
        self.diagnostics = {
            "num_kernels": len(all_indices[0])
        }
        return np.sum(self.kernel.multi_pdf(distances)) / len(self.data)

