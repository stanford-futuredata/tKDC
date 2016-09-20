package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
import macrobase.kde.CompositeGrid;
import macrobase.kde.KDTree;
import macrobase.kde.TreeKDE;
import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.Kernel;
import macrobase.kernel.KernelFactory;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class QuantileBoundEstimator {
    private static final Logger log = LoggerFactory.getLogger(QuantileBoundEstimator.class);

    public TreeKDEConf tConf;
    public KernelFactory kFactory;

    public double qT, qL, qH;
    public double cutoff;
    public double tolerance;
    // Cache existing tree for reuse
    public KDTree tree;

    public static final int startingSampleSize = 200;
    public static double confidenceFactor = 2.5;

    public QuantileBoundEstimator(TreeKDEConf tConf) {
        this.tConf = tConf;
        kFactory = new KernelFactory(tConf.kernel);
    }

    /**
     * Figures out reservoir size and quantile bounds
     * @return reservoir size
     */
    public int estimateQuantiles(List<double[]> metrics) {
        int rSize = startingSampleSize;
        int sampleSize = startingSampleSize;
        double curCutoff = -1;
        double curTolerance = -1;

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
                    curCutoff,
                    curTolerance
            );
            double pT = tConf.percentile;
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
            log.debug("rSize: {}, cut: {}, tol: {}", rSize, curCutoff, curTolerance);
            log.debug("pL: {}, pT: {}, pH: {}, qL: {}, qT: {}, qH: {}", pL, pT, pH, qL, qT, qH);

            if (curCutoff <= qH && curCutoff > 0) {
                log.debug("Bad Cutoff, retrying", curCutoff, rSize);
                curCutoff *= 4;
            } else {
                if (rSize == metrics.size()) {
                    break;
                } else {
                    curCutoff = tConf.qCutoffMultiplier * qH;
                    curTolerance = tConf.qTolMultiplier * qL;
                    rSize = Math.min(4 * rSize, metrics.size());
                    sampleSize = Math.min(rSize, tConf.qSampleSize);
                    oldTree = null;
                }
            }
        }

        cutoff = qH;
        tolerance = tConf.qTolMultiplier * qL;
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
            double curCutoff,
            double curTolerance
    ) {
        double[] curBW = new BandwidthSelector()
                .setMultiplier(tConf.bwMultiplier)
                .findBandwidth(data);
        log.debug("Calculating scores for bw: {} on n={}", curBW, data.size());
        Kernel k = kFactory
                .get()
                .initialize(curBW);

        TreeKDE tKDE = new TreeKDE(tree);
        tKDE.setBandwidth(curBW);
        tKDE.setKernel(k);
        tKDE.setIgnoreSelf(tConf.ignoreSelfScoring);
        if (curCutoff > 0) {
            tKDE.setCutoff(curCutoff).setTolerance(curTolerance);
        }
        tKDE.setTrainedTree(tree);
        tKDE.train(data);

        int numSamples = sampleSize;
        long start = System.currentTimeMillis();
        double[] densities = new double[numSamples];
        for (int i=0; i < numSamples; i++) {
            double[] curSample = data.get(i);
            densities[i] = tKDE.density(curSample);
        }
        long elapsed = System.currentTimeMillis() - start;
        log.debug("Scored {} on {} @ {} / s",
                numSamples,
                data.size(),
                (float)numSamples * 1000/(elapsed));

        Percentile p = new Percentile();
        p.setData(densities);
        return p;
    }
}
