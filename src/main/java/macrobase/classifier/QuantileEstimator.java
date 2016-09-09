package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
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

public class QuantileEstimator {
    private static final Logger log = LoggerFactory.getLogger(QuantileEstimator.class);

    public TreeKDEConf tConf;
    public KernelFactory kFactory;

    public double quantile;
    public double cutoff;
    public double tolerance;

    public QuantileEstimator(TreeKDEConf tConf) {
        this.tConf = tConf;
        kFactory = new KernelFactory(tConf.kernel);
    }

    /**
     * Figures out reservoir size and good starting quantiles
     * @return reservoir size
     */
    public int estimateQuantiles(List<double[]> metrics) {
        int rSize = 200;
        double curCutoff = -1;
        double curTolerance = -1;
        double curTarget = -1;

        double pCutoff = tConf.cutoffMultiplier * tConf.percentile * 100;
        double pTol = (1-tConf.tolMultiplier) * tConf.percentile * 100;
        double pTarget = tConf.percentile * 100;
        double[] pValues = {pCutoff, pTol, pTarget};
        log.debug("Tracking percentiles: {}", Arrays.toString(pValues));

        // Cache existing trees for reuse
        KDTree oldTree = null;
        while (rSize <= metrics.size()) {
            List<double[]> curData = metrics.subList(0, rSize);

            if (oldTree == null) {
                oldTree = trainTree(curData);
            }
            Percentile pCalc = calcQuantiles(
                    metrics.subList(0, rSize),
                    oldTree,
                    curCutoff,
                    curTolerance
            );

            TreeMap<Double, Double> curQuantValues = new TreeMap<>();
            for (double pV : pValues) {
                curQuantValues.put(pV, pCalc.evaluate(pV));
            }

            if (curQuantValues.get(pTarget) > curCutoff && curCutoff > 0) {
                log.debug("Bad Cutoff {} for {}, retrying", curCutoff, rSize);
                curCutoff *= 4;
            } else {
                curCutoff = curQuantValues.get(pCutoff);
                curTarget = curQuantValues.get(pTarget);
                curTolerance = curQuantValues.get(pTarget) - curQuantValues.get(pTol);
                log.debug("Estimated q {} for {}", curTarget, rSize);
                log.debug(curQuantValues.toString());

                if (rSize == metrics.size()) {
                    break;
                } else {
                    rSize = Math.min(4 * rSize, metrics.size());
                    oldTree = null;
                }
            }
        }

        quantile = curTarget;
        cutoff = curCutoff;
        tolerance = curTolerance;
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

        int numSamples = Math.min(data.size(), tConf.qSampleSize);
        long start = System.currentTimeMillis();
        double[] densities = new double[numSamples];
        for (int i=0; i < numSamples; i++) {
            densities[i] = tKDE.density(data.get(i));
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
