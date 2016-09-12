package macrobase.kde;

import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DualTreeKDE {
    private static final Logger log = LoggerFactory.getLogger(DualTreeKDE.class);

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

    public DualTreeKDE(KDTree tree) {
        this.tree = tree;
    }

    public DualTreeKDE setIgnoreSelf(boolean b) {this.ignoreSelf = b; return this;}
    public DualTreeKDE setTolerance(double t) {this.tolerance = t; return this;}
    public DualTreeKDE setCutoff(double cutoff) {this.cutoff = cutoff; return this;}
    public DualTreeKDE setBandwidth(double[] bw) {this.bandwidth = bw; return this;}
    public DualTreeKDE setKernel(Kernel k) {this.kernel = k; return this;}
    public DualTreeKDE setTrainedTree(KDTree tree) {this.tree=tree; this.trainedTree=true; return this;}

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

    public static Comparator<DualScore> scoreEstimateComparator = (DualScore o1, DualScore o2) -> {
        if (o1.queryDataWMin < o2.queryDataWMax) {
            return 1;
        } else if (o1.queryDataWMin > o2.queryDataWMax) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Calculates density * N
     * @param query list of query points
     * @return unnormalized density
     */
    private double[] pqScore(List<double[]> query) {
        // Initialize Score Estimates
        int n = query.size();
        double[] totalWMin = new double[n];
        double[] totalWMax = new double[n];
        boolean[] resolved = new boolean[n];
        int nResolved = 0;

        long curNodesProcessed = 0;
        for (int i = 0; i < n; i++) {
            if (ignoreSelf) {
                totalWMin[i] -= selfPointDensity;
                totalWMax[i] -= selfPointDensity;
            }
        }

        PriorityQueue<DualScore> pq = new PriorityQueue<>(100, scoreEstimateComparator);
        DualScore initialNode = new DualScore(
                this.kernel,
                this.tree,
                this.tree // TODO: support when query is not training
        );
        pq.add(initialNode);
        for (int i = 0; i < n; i++) {
            totalWMin[i] += initialNode.dataWMin;
            totalWMax[i] += initialNode.dataWMax;
        }
        curNodesProcessed++;

        boolean useMinAsFinalScore = false;
        while (!pq.isEmpty()) {
            if (nResolved == n) {
                break;
            }
            DualScore curTask = pq.poll();
            // Check if task has any unresolved query elements
            boolean allResolved = true;
            for (int i=curTask.query.startIdx;i<curTask.query.endIdx;i++) {
                int elt = curTask.query.idxs[i];
                if (!resolved[elt]) {
                    allResolved = false;
                }
            }
            if (allResolved) {
//                log.info("Removed task: {}", curTask.toString());
                continue;
            }

//            log.info("totalWMin: {}", Arrays.toString(totalWMin));
//            log.info("totalWMax: {}", Arrays.toString(totalWMax));
//            log.info("remove idxs: {}", Arrays.toString(
//                    Arrays.copyOfRange(
//                            curTask.query.idxs, curTask.query.startIdx, curTask.query.endIdx)));
//            log.info("remove curWMin: {}, curWMax: {}", curTask.dataWMin, curTask.dataWMax);
            for (int i=curTask.query.startIdx;i<curTask.query.endIdx;i++) {
                int elt = curTask.query.idxs[i];
                totalWMin[elt] -= curTask.dataWMin;
                totalWMax[elt] -= curTask.dataWMax;
            }
//            log.info("totalWMin: {}", Arrays.toString(totalWMin));
//            log.info("totalWMax: {}", Arrays.toString(totalWMax));

            if (curTask.isLeaf()) {
//                log.info("add leaf node");
                ArrayList<double[]> leafQueryItems = curTask.query.getItems();
                int i = curTask.query.startIdx;
                for (double[] d : leafQueryItems) {
                    int elt = curTask.query.idxs[i];
                    double exact = exactDensity(curTask.data, d);
                    totalWMin[elt] += exact;
                    totalWMax[elt] += exact;
                    i++;
                }
            } else {
//                log.info("Add children:");
                DualScore[] children = curTask.split(this.kernel);
                curNodesProcessed += children.length;
                for (DualScore child : children) {
//                    log.info("Child: {}", child.toString());
                    for (int i=child.query.startIdx; i<child.query.endIdx; i++) {
                        int elt = child.query.idxs[i];
                        totalWMin[elt] += child.dataWMin;
                        totalWMax[elt] += child.dataWMax;
                    }
                    pq.add(child);
                }
            }

            // Check on all the elements we affected this round
            for (int i=curTask.query.startIdx;i<curTask.query.endIdx;i++) {
                int elt = curTask.query.idxs[i];
                if (totalWMin[elt] > unscaledCutoff) {
                    resolved[elt] = true;
                }
                if (totalWMax[elt] - totalWMin[elt] < unscaledTolerance) {
                    resolved[elt] = true;
                }
            }
        }
//        log.info("totalWMin: {}", Arrays.toString(totalWMin));
//        log.info("totalWMax: {}", Arrays.toString(totalWMax));
        double[] finalEstimates = new double[n];
        for (int i=0;i<n;i++) {
            finalEstimates[i] = 0.5 * (totalWMin[i] + totalWMax[i]);
        }
        return finalEstimates;
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
    public double[] densities(List<double[]> test) {
        double[] densities = pqScore(test);
        for (int i = 0; i < densities.length; i++) {
            if (ignoreSelf) {
                densities[i] /= (numPoints-1);
            } else {
                densities[i] /= numPoints;
            }
        }
        return densities;
    }
}
