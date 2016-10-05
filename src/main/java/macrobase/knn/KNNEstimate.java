package macrobase.knn;

import macrobase.kde.KDTree;

public class KNNEstimate {
    public Metric metric;
    public KDTree tree;
    public double[] query;
    public double dL, dH;

    public KNNEstimate(Metric metric, KDTree tree, double[] query) {
        this.metric = metric;
        this.tree = tree;
        this.query = query;

        double[][] dVecs = tree.getMinMaxDistanceVectors(query);
        dL = metric.dist(dVecs[0]);
        dH = metric.dist(dVecs[1]);
    }

    public KNNEstimate[] split() {
        KNNEstimate[] children = new KNNEstimate[2];
        children[0] = new KNNEstimate(metric, tree.getLoChild(), query);
        children[1] = new KNNEstimate(metric, tree.getHiChild(), query);
        return children;
    }

    public String toString() {
        return String.format("s: %d, dL: %f, dH: %f",
                tree.getSplitDimension(),
                dL,
                dH);
    }
}
