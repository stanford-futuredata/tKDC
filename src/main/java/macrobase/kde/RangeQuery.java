package macrobase.kde;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

public class RangeQuery {
    public KDTree t;
    public int k;
    public double[] scalingFactor;

    public int[] counts;

    public RangeQuery(KDTree t) {
        this.t = t;
        this.k = t.k;
        this.scalingFactor = new double[this.k];
        for (int i=0; i<this.k; i++) {
            this.scalingFactor[i] = 1.0;
        }
        this.counts = new int[3];
    }

    public RangeQuery setScalingFactor(double[] s) {
        this.scalingFactor = s.clone();
        return this;
    }

    public ArrayList<double[]> query(double[] q, double radius) {
        int k = this.k;
        double r2 = radius * radius;

        Deque<KDTree> nodesToProcess = new ArrayDeque<>(100);
        nodesToProcess.addFirst(this.t);

        ArrayList<double[]> neighbors = new ArrayList<>(100);
        while(!nodesToProcess.isEmpty()) {
            KDTree curNode = nodesToProcess.pollFirst();
            double[][] minMaxVectors = curNode.getMinMaxDistanceVectors(q);
            for (int i = 0; i < k; i++) {
                minMaxVectors[0][i] *= scalingFactor[i];
                minMaxVectors[1][i] *= scalingFactor[i];
            }
            int status = checkIfVectorsWithinBall(minMaxVectors, r2);
            counts[status+1]++;

            if (status == 1) {
                continue;
            } else if (status == 0) {
                if (curNode.isLeaf()) {
                    ArrayList<double[]> pts = curNode.getItems();
                    for (double[] p : pts) {
                        double pointDistance = 0.0;
                        double del;
                        for (int i = 0; i<k; i++) {
                            del = (p[i] - q[i]) * scalingFactor[i];
                            pointDistance += del*del;
                        }
                        if (pointDistance <= r2) {
                            neighbors.add(p);
                        }
                    }
                } else {
                    KDTree[] children = curNode.getChildren(q);
                    nodesToProcess.addFirst(children[1]);
                    nodesToProcess.addFirst(children[0]);
                }
            } else {
                addAllPointsToList(curNode, neighbors);
            }
        }

        return neighbors;
    }

    public void addAllPointsToList(KDTree t, ArrayList<double[]> list) {
        Deque<KDTree> nodes = new ArrayDeque<>();
        nodes.add(t);
        while (!nodes.isEmpty()) {
            KDTree curNode = nodes.poll();
            if (curNode.isLeaf()) {
                list.addAll(curNode.getItems());
            } else {
                nodes.add(curNode.loChild);
                nodes.add(curNode.hiChild);
            }
        }
        return;
    }

    public int checkIfVectorsWithinBall(double[][] minMaxVectors, double r2) {
        int k = minMaxVectors[0].length;
        double minSquaredDistance = 0.0;
        double maxSquaredDistance = 0.0;

        for (int i = 0; i < k; i++) {
            minSquaredDistance += minMaxVectors[0][i] * minMaxVectors[0][i];
            if (minSquaredDistance > r2) {
                return 1;
            }
            maxSquaredDistance += minMaxVectors[1][i] * minMaxVectors[1][i];
        }
        if (maxSquaredDistance < r2) {
            return -1;
        } else {
            return 0;
        }
    }

}
