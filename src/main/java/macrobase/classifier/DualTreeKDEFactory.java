package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
import macrobase.kde.DualTreeKDE;
import macrobase.kde.KDTree;
import macrobase.kde.TreeKDE;
import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.Kernel;
import macrobase.kernel.KernelFactory;

import java.util.List;

public class DualTreeKDEFactory {
    public TreeKDEConf tConf;

    public DualTreeKDEFactory(TreeKDEConf tConf) {
        this.tConf = tConf;
    }

    public double[] getBandwidth(List<double[]> data) {
        double[] curBW = new BandwidthSelector()
                .setMultiplier(tConf.bwMultiplier)
                .findBandwidth(data);
        return curBW;
    }

    public Kernel getKernel(double[] bw) {
        return new KernelFactory(tConf.kernel)
                .get()
                .initialize(bw);
    }

    public KDTree getTree(List<double[]> data) {
        KDTree t = new KDTree()
                .setSplitByWidth(tConf.splitByWidth)
                .setLeafCapacity(tConf.leafSize);
        return t.build(data);
    }

    /**
     * Train KDE estimator
     * @param data training data (tree, bandwidth, etc...)
     * @return trained kde estimator
     */
    public DualTreeKDE getTreeKDE(List<double[]> data) {
        double[] bw = getBandwidth(data);
        Kernel k = getKernel(bw);
        KDTree tree = getTree(data);

        DualTreeKDE tKDE = new DualTreeKDE(tree);
        tKDE.setBandwidth(bw);
        tKDE.setKernel(k);
        tKDE.setTrainedTree(tree);
        tKDE.setIgnoreSelf(tConf.ignoreSelfScoring);

        if (tConf.calculateCutoffs) {
            QuantileBoundEstimator q = new QuantileBoundEstimator(tConf);
            q.estimateQuantiles(data);
            tKDE.setCutoff(q.cutoff);
            tKDE.setTolerance(q.tolerance);
        } else {
            tKDE.setCutoff(tConf.cutoffAbsolute);
            tKDE.setTolerance(tConf.tolAbsolute);
        }

        tKDE.train(data);

        return tKDE;
    }
}
