package macrobase.knn;

import macrobase.kde.AlgebraUtils;
import macrobase.kde.KDTree;
import macrobase.kernel.BandwidthSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TreeKNN {
    private static final Logger log = LoggerFactory.getLogger(TreeKNN.class);

    // Parameters
    public int k;
    public double[] bw;
    public double dLB = -1;
    public double dHB = Double.MAX_VALUE;

    public int numPoints;
    public KDTree tree;
    public Metric metric;

    public TreeKNN(int k) {
        this.k = k;
    }

    public TreeKNN setBandwidth(double[] bw) {this.bw = bw; return this;}
    public TreeKNN setBounds(double dLB, double dHB) {
        this.dLB = dLB;
        this.dHB = dHB;
        return this;
    }
    public TreeKNN setTree(KDTree t) {this.tree = t;return this;}

    public TreeKNN train(List<double[]> data) {
        this.numPoints = data.size();
        if (tree == null) {
            this.tree = new KDTree().setSplitByWidth(true);
            this.tree.build(data);
        }
        if (bw == null) {
            bw = new BandwidthSelector().findBandwidth(data);
        }
        this.metric = new Metric(bw);

        return this;
    }

    public static Comparator<KNNEstimate> knnEstimateComparator = (o1, o2) -> {
        double d1 = o1.dL;
        double d2 = o2.dL;
        if (d1 < d2) {
            return -1;
        } else if (d1 > d2) {
            return 1;
        } else {
            return 0;
        }
    };


    public double score(double[] d) {
//        System.out.println("***Scoring***: "+Arrays.toString(d));
        int[] lowHiCount = new int[2];
        // k points in descending distance
        PriorityQueue<Double> points = new PriorityQueue<>(20, Collections.reverseOrder());
        // Tree nodes in ascending minimum distance
        PriorityQueue<KNNEstimate> nodes = new PriorityQueue<>(100, knnEstimateComparator);

        KNNEstimate initialEstimate = new KNNEstimate(metric, tree, d);
        boolean addNode = processNode(
                points, lowHiCount, d, initialEstimate
        );
        if (addNode) {
            nodes.add(initialEstimate);
        }

        while(!nodes.isEmpty()) {
//            System.out.println("Node PQ: "+nodes.size());
            if (lowHiCount[0] >= k || numPoints - lowHiCount[1] <= k-1) {
                break;
            }

            KNNEstimate curParent = nodes.poll();
            KNNEstimate[] children = curParent.split();
            for (KNNEstimate curChild: children) {
                addNode = processNode(
                        points, lowHiCount, d, curChild
                );
                if (addNode) {
                    nodes.add(curChild);
                }
            }
        }

//        System.out.println("low: "+lowHiCount[0]+" hi: "+lowHiCount[1]);
        return finalScore(lowHiCount, points);
    }

    public double finalScore(int[] lowHiCount, PriorityQueue<Double> points) {
        if (lowHiCount[0] >= k) {
            return dLB * (k / lowHiCount[0]);
        } else if (numPoints - lowHiCount[1] <= k - 1) {
            return dHB * (k) / (numPoints - lowHiCount[1]);
        } else if (points.size() >= k - lowHiCount[0]) {
            while (points.size() > k - lowHiCount[0]) {
                points.poll();
            }
            return points.poll();
        } else {
            throw new RuntimeException("Bad Score state");
        }
    }

    /**
     * Process a single node
     * @param points points within bounds to update
     * @param lowHiCounts count of points out of bounds
     * @param d query point
     * @param curNode current node
     * @return whether the node should be added to queue
     */
    public boolean processNode(
            PriorityQueue<Double> points,
            int[] lowHiCounts,
            double[] d,
            KNNEstimate curNode) {
        boolean addNode = false;
//        System.out.println("curnode: "+curNode.tree.getNBelow()+":"+ AlgebraUtils.array2dToString(curNode.tree.getBoundaries()));
//        System.out.println(curNode.tree.startIdx+"->"+curNode.tree.endIdx);
//        System.out.println("bounds: "+curNode.dL+":"+curNode.dH);
        if (curNode.dH < dLB) {
//            System.out.println("low");
            lowHiCounts[0] += curNode.tree.getNBelow();
        } else if (curNode.dL > dHB) {
//            System.out.println("hi");
            lowHiCounts[1] += curNode.tree.getNBelow();
        } else if (!points.isEmpty()
                && points.size() + lowHiCounts[0] >= k
                && curNode.dL > points.peek()) {
//            System.out.println("Pruned node");
        } else if (curNode.tree.isLeaf()) {
//            System.out.println("leaf");
            for (double[] item : curNode.tree.getItems()) {
//                System.out.println("item");
//                System.out.println(Arrays.toString(item));
                double itemDist = metric.dist(d, item);
//                System.out.println("dist: "+itemDist);
                if (itemDist <= dLB) {
                    lowHiCounts[0]++;
                } else if (itemDist >= dHB) {
                    lowHiCounts[1]++;
                } else {
                    points.add(itemDist);
                }
            }
        } else {
//            System.out.println("added");
            addNode = true;
        }

        while (!points.isEmpty() && points.size() + lowHiCounts[0] > k) {
            points.poll();
        }
//        System.out.println("low: "+lowHiCounts[0]+"hi: "+lowHiCounts[1]);
//        System.out.println("points: "+points.size()+ " peek: "+points.peek());

        return addNode;
    }
}
