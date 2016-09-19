package macrobase.kde;

import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TreeKDE {
    private static final Logger log = LoggerFactory.getLogger(TreeKDE.class);

    // ** Basic stats parameters
    private int numPoints;
    private double[] bandwidth;
    private Kernel kernel;
    // If we reuse the training as the scoring set, whether to ignore the weight provided by query point
    private boolean ignoreSelf=false;

    // ** Tree parameters
    private KDTree tree;
    // Whether the user provided a pre-populated tree
    private boolean trainedTree = false;
    // Total density error tolerance
    private double tolerance = 0;
    // Cutoff point at which point we no longer need accuracy
    private double cutoff = Double.MAX_VALUE;

    // ** Diagnostic Measurements
    public long finalCutoff[] = new long[10];
    public long numNodesProcessed[] = new long[10];
    public int numScored = 0;

    // ** Cached values
    private double unscaledTolerance;
    private double unscaledCutoff;
    private double selfPointDensity;

    public TreeKDE(KDTree tree) {
        this.tree = tree;
    }

    public TreeKDE setIgnoreSelf(boolean b) {this.ignoreSelf = b; return this;}
    public TreeKDE setTolerance(double t) {this.tolerance = t; return this;}
    public TreeKDE setCutoff(double cutoff) {this.cutoff = cutoff; return this;}
    public TreeKDE setBandwidth(double[] bw) {this.bandwidth = bw; return this;}
    public TreeKDE setKernel(Kernel k) {this.kernel = k; return this;}
    public TreeKDE setTrainedTree(KDTree tree) {this.tree=tree; this.trainedTree=true; return this;}

    public double getCutoff() {return this.cutoff;}
    public double getTolerance() {return this.tolerance;}
    public KDTree getTree() {return this.tree;}

    public double[] getBandwidth() {return bandwidth;}

    public void train(List<double[]> data) {
        if (data.isEmpty()) {
            throw new RuntimeException("Empty Training Data");
        }
        this.numPoints = data.size();
        this.unscaledTolerance = tolerance * numPoints;
        this.unscaledCutoff = cutoff * numPoints;

        if (bandwidth == null) {
            bandwidth = new BandwidthSelector().findBandwidth(data);
        }
        if (kernel == null) {
            kernel = new GaussianKernel();
            kernel.initialize(bandwidth);
        }
        this.selfPointDensity = kernel.density(new double[bandwidth.length]);

        if (!trainedTree) {
            StopWatch sw = new StopWatch();
            sw.start();
            this.tree.build(data);
            sw.stop();
            log.debug("built kd-tree on {} points in {}", data.size(), sw.toString());
        }
    }

    public static Comparator<ScoreEstimate> scoreEstimateComparator = (o1, o2) -> {
        if (o1.totalWMax < o2.totalWMax) {
            return 1;
        } else if (o1.totalWMax > o2.totalWMax) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Calculates density * N
     * @param d query point
     * @return unnormalized density
     */
    private double pqScore(double[] d) {
        // Initialize Score Estimates

        // Nodes in ascending order (deepest leaf first)
        List<KDTree> initialChildList = new LinkedList<>();
        if (false) {
            KDTree curNode = this.tree;
            while (!curNode.isLeaf()) {
                KDTree[] curNodeChildren = curNode.getChildren(d);
                curNode = curNodeChildren[0];
                initialChildList.add(0, curNodeChildren[1]);
            }
            initialChildList.add(0, curNode);
        } else {
            initialChildList = Collections.singletonList(this.tree);
        }

        double totalWMin = 0.0;
        double totalWMax = 0.0;
        long curNodesProcessed = 0;
        if (ignoreSelf) {
            totalWMin -= selfPointDensity;
            totalWMax -= selfPointDensity;
        }

        PriorityQueue<ScoreEstimate> pq = new PriorityQueue<>(100, scoreEstimateComparator);
        for (KDTree childNode : initialChildList) {
            ScoreEstimate childNodeEstimate = new ScoreEstimate(this.kernel, childNode, d);
            pq.add(childNodeEstimate);
            totalWMin += childNodeEstimate.totalWMin;
            totalWMax += childNodeEstimate.totalWMax;
            if (totalWMin > unscaledCutoff) {
                totalWMax = Double.MAX_VALUE;
            }
            curNodesProcessed++;
        }

//        System.out.println("\nScoring : "+Arrays.toString(d));
//        System.out.println("tolerance: "+unscaledTolerance);
//        System.out.println("cutoff: "+unscaledCutoff);
        boolean useMinAsFinalScore = false;
        while (!pq.isEmpty()) {
//            System.out.println("minmax: "+totalWMin+", "+totalWMax);
            if (totalWMax - totalWMin < unscaledTolerance) {
                numNodesProcessed[0] += curNodesProcessed;
                finalCutoff[0]++;
                break;
            } else if (totalWMin > unscaledCutoff) {
                numNodesProcessed[1] += curNodesProcessed;
                finalCutoff[1]++;
                useMinAsFinalScore = true;
                break;
            }
//            else if (totalWMax < unscaledCutoff) {
//                numNodesProcessed[1] += curNodesProcessed;
//                finalCutoff[1]++;
//                break;
//            }
            ScoreEstimate curEstimate = pq.poll();
//            System.out.println("current box:\n"+ AlgebraUtils.array2dToString(curEstimate.tree.getBoundaries()));
//            System.out.println("split: "+curEstimate.tree.getSplitDimension() + ":"+curEstimate.tree.getSplitValue());
            totalWMin -= curEstimate.totalWMin;
            totalWMax -= curEstimate.totalWMax;

            if (curEstimate.tree.isLeaf()) {
                double exact = exactDensity(curEstimate.tree, d);
                totalWMin += exact;
                totalWMax += exact;
            } else {
                ScoreEstimate[] children = curEstimate.split(this.kernel, d);
                curNodesProcessed += 2;
                for (ScoreEstimate child : children) {
                    totalWMin += child.totalWMin;
                    totalWMax += child.totalWMax;
                    pq.add(child);
                }
            }
        }
        if (pq.isEmpty()) {
            finalCutoff[3]++;
        }
        numScored++;
        if (useMinAsFinalScore) {
            // totalWMax can be completely inaccurate if we stop based on cutoff
            return totalWMin;
        } else {
            return (totalWMin + totalWMax) / 2;
        }
    }

    private double exactDensity(KDTree t, double[] d) {
        double score = 0.0;
        for (double[] dChild : t.getItems()) {
            double[] diff = d.clone();
            for (int i = 0; i < diff.length; i++) {
                diff[i] -= dChild[i];
            }
            double delta = kernel.density(diff);
            score += delta;
        }
        return score;

    }

    public void showDiagnostics() {
        log.debug("Final Loop Cutoff: tol {}, totalcutoff {}, completion {}",
                finalCutoff[0],
                finalCutoff[1],
                finalCutoff[2]);
        log.debug("Avg # of nodes processed: tol {}, totalcutoff {}",
                (double)numNodesProcessed[0]/finalCutoff[0],
                (double)numNodesProcessed[1]/finalCutoff[1]
                );
    }

    /**
     * Returns normalized pdf
     */
    public double density(double[] d) {
        if (ignoreSelf) {
            return pqScore(d) / (numPoints-1);
        } else {
            return pqScore(d) / numPoints;
        }
    }
}
