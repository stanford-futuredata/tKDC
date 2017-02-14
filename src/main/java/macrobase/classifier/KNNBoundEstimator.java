package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
import macrobase.kde.KDTree;
import macrobase.kde.TreeKDE;
import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.Kernel;
import macrobase.kernel.KernelFactory;
import macrobase.knn.Metric;
import macrobase.knn.TreeKNN;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KNNBoundEstimator {
    private static final Logger log = LoggerFactory.getLogger(KNNBoundEstimator.class);

    public TreeKDEConf tConf;

    public double[] bw;
    public Metric metric;
    public double qT, qL, qH;
    public double dL, dH;
    // Cache existing tree for reuse
    public KDTree tree;

    public static final int startingSampleSize = 200;
    public static double confidenceFactor = 2.5;

    public KNNBoundEstimator(TreeKDEConf tConf) {
        this.tConf = tConf;
    }

    /**
     * Figures out reservoir size and quantile bounds
     * @return reservoir size
     */
    public int estimateQuantiles(List<double[]> metrics) {
        bw = new BandwidthSelector()
                .setMultiplier(tConf.bwMultiplier)
                .setValue(tConf.bwValue)
                .findBandwidth(metrics);
        metric = new Metric(bw);

        int rSize = startingSampleSize;
        int sampleSize = startingSampleSize;
        double curDH = -1;
        double curDL = -1;
        int curK = Math.max(2, rSize * tConf.k / metrics.size());
        log.debug("BW: {}", bw);

        KDTree oldTree = null;
        while (rSize <= metrics.size()) {
            List<double[]> curData = metrics.subList(0, rSize);

            if (oldTree == null) {
                oldTree = trainTree(curData);
            }
            Percentile pCalc = calcQuantiles(
                    metrics.subList(0, rSize),
                    sampleSize,
                    oldTree,
                    curDL,
                    curDH,
                    curK
            );
            double pT = 1- tConf.percentile;
            double pDelta = confidenceFactor * Math.sqrt(pT * (1-pT) / sampleSize);
            double pL = pT - pDelta;
            double pH = Math.min(1.0, pT + pDelta);
            qT = pCalc.evaluate(100 * pT);
            if (pL > 0.0) {
                qL = pCalc.evaluate(100 * pL);
            } else {
                qL = 0.0;
            }
            qH = pCalc.evaluate(100 * pH);
            log.debug("rSize: {}, dL: {}, dH: {}, k: {}", rSize, curDL, curDH, curK);
            log.debug("pL: {}, pT: {}, pH: {}, qL: {}, qT: {}, qH: {}", pL, pT, pH, qL, qT, qH);

            boolean cutoffHBad = curDH <= qH && curDH > 0;
//            boolean cutoffLBad = curDL >= qL && curDL > 0;
            boolean cutoffLBad = false;
            if (cutoffHBad) {
                log.debug("Bad CutoffH");
                curDH *= 4;
            }
//            if (cutoffLBad) {
//                log.debug("Bad CutoffL");
//                curDL /= 4;
//            }
            if (!cutoffHBad && !cutoffLBad){
                if (rSize == metrics.size()) {
                    break;
                } else {
                    curDH = tConf.qCutoffMultiplier * qH;
//                    curDL = qL / tConf.qCutoffMultiplier;
                    rSize = Math.min(4 * rSize, metrics.size());
                    sampleSize = Math.min(rSize, tConf.qSampleSize);
                    curK = Math.max(2, rSize * tConf.k / metrics.size());
                    oldTree = null;
                }
            }
        }

        dH = qH;
        dL = qL;
        tree = oldTree;
        return rSize;
    }

    public KDTree trainTree(
            List<double[]> data
    ) {
        KDTree t = new KDTree()
                .setSplitByWidth(tConf.splitByWidth)
                .setLeafCapacity(tConf.leafSize);
        return t.build(data);
    }

    public Percentile calcQuantiles(
            List<double[]> data,
            int sampleSize,
            KDTree tree,
            double dL,
            double dH,
            int curK
    ) {
        log.debug("dL: {}, dH: {}, k: {}", dL, dH, curK);
        log.debug("Quantiles on: {}", data.size());

        TreeKNN tKNN = new TreeKNN(curK)
                .setBandwidth(bw)
                .setTree(tree);
        if (dL >= 0) {
            tKNN.setBounds(dL, dH);
        }
        tKNN.train(data);

        int numSamples = sampleSize;
        long start = System.currentTimeMillis();
        double[] distances = new double[numSamples];
        for (int i=0; i < numSamples; i++) {
            double[] curSample = data.get(i);
            distances[i] = tKNN.score(curSample);
        }
        long elapsed = System.currentTimeMillis() - start;
        log.debug("Scored {} on {} @ {} / s",
                numSamples,
                data.size(),
                (float)numSamples * 1000/(elapsed));

        Percentile p = new Percentile();
        p.setData(distances);
        return p;
    }
}
