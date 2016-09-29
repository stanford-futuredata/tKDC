package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
import macrobase.kde.*;
import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.Kernel;
import macrobase.kernel.KernelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KDEClassifier implements DensityEstimator {
    private static final Logger log = LoggerFactory.getLogger(KDEClassifier.class);

    public TreeKDEConf tConf;

    public double[] bandwidth;
    public double cutoffH, cutoffL, tolerance;
    public KDTree tree;
    public DensityEstimator kde;
    public CompositeGrid grids;

    public KDEClassifier(TreeKDEConf conf) {
        this.tConf = conf;
    }

    public KDEClassifier train(List<double[]> data) {
        bandwidth = new BandwidthSelector()
                .setMultiplier(tConf.bwMultiplier)
                .findBandwidth(data);
        Kernel kernel = new KernelFactory(tConf.kernel).get().initialize(bandwidth);

        if (tConf.calculateCutoffs) {
            QuantileBoundEstimator q = new QuantileBoundEstimator(tConf);
            q.estimateQuantiles(data);
            cutoffH = q.cutoffH;
            cutoffL = q.cutoffL;
            tolerance = q.tolerance;
            tree = q.tree;
        } else {
            cutoffH = tConf.cutoffAbsolute;
            tolerance = tConf.tolAbsolute;
        }

        if (tConf.algorithm == TreeKDEConf.Algorithm.TREEKDE) {
            if (tree == null) {
                tree = new KDTree()
                        .setSplitByWidth(tConf.splitByWidth)
                        .setLeafCapacity(tConf.leafSize)
                        .build(data);
            }

            TreeKDE tkde = new TreeKDE(tree)
                    .setBandwidth(bandwidth)
                    .setKernel(kernel)
                    .setTrainedTree(tree)
                    .setIgnoreSelf(tConf.ignoreSelfScoring)
                    .setCutoffH(cutoffH)
                    .setCutoffL(cutoffL)
                    .setTolerance(tolerance)
                    .train(data);
            kde = tkde;
        } else {
            SimpleKDE sKDE = new SimpleKDE()
                    .setBandwidth(bandwidth)
                    .setKernel(kernel)
                    .setIgnoreSelf(tConf.ignoreSelfScoring)
                    .train(data);
            kde = sKDE;
        }

        if (tConf.calculateCutoffs && tConf.useGrid) {
            grids = new CompositeGrid(
                    kernel,
                    bandwidth,
                    tConf.gridSizes,
                    cutoffH)
                    .setIgnoreSelf(tConf.ignoreSelfScoring)
                    .train(data);
        }
        return this;
    }

    public double density(double[] d) {
        if (tConf.useGrid) {
            double gridDensity = grids.density(d);
            if (gridDensity > 0) {
                return gridDensity;
            }
        }
        return kde.density(d);
    }
}
