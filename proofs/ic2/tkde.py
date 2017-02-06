import heapq

from ic2.kdtree import KDTree


class PartialWeight:
    def __repr__(self):
        return str((self.tree, self.low, self.high))

    def __lt__(self, other):
        return self.delta > other.delta

    def __init__(self, tree: KDTree, low: float, high: float):
        self.tree = tree
        self.low = low
        self.high = high
        self.delta = high - low


class TKDE:
    def __init__(self, tree: KDTree, kernel, threshold=None, epsilon=0.0):
        self.tree = tree
        self.kernel = kernel
        self.threshold = threshold
        self.epsilon = epsilon

        self.diagnostics = {}

    def calc(self, query) -> float:
        total_low, total_high = self.tree.calc_weight(self.kernel, query)
        sub_trees = [PartialWeight(self.tree, total_low, total_high)]
        # done_trees = []

        box_kernels = 2
        exact_kernels = 0
        stopping_cause = "exact"
        while len(sub_trees) > 0:
            if self.threshold is not None:
                if total_low > self.threshold:
                    stopping_cause = "hi"
                    break
                elif total_high < self.threshold:
                    stopping_cause = "low"
                    break
            if (total_high - total_low) < self.epsilon:
                stopping_cause = "eps"
                break

            cur_partial = heapq.heappop(sub_trees)
            cur_tree = cur_partial.tree
            total_low -= cur_partial.low
            total_high -= cur_partial.high

            if cur_tree.is_leaf():
                exact_weight = cur_tree.calc_exact(self.kernel, query)
                exact_kernels += cur_tree.LEAF_SIZE
                total_low += exact_weight
                total_high += exact_weight
                # done_trees.append((cur_tree, exact_weight))
            else:
                children = [cur_tree.leftChild, cur_tree.rightChild]
                for child in children:
                    low, high = child.calc_weight(self.kernel, query)
                    box_kernels += 2
                    total_low += low
                    total_high += high
                    heapq.heappush(sub_trees, PartialWeight(child, low, high))

        pdf_estimate = (total_low+total_high)/(2 * self.tree.numPoints)
        self.diagnostics = {
            "exact_kernels": exact_kernels,
            "box_kernels": box_kernels,
            "stopping_cause": stopping_cause
        }
        return pdf_estimate
