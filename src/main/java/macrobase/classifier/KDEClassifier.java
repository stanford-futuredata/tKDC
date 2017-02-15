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
                .setValue(tConf.bwValue)
                .setUseStdDev(tConf.useStdDev)
                .findBandwidth(data);
        Kernel kernel = new KernelFactory(tConf.kernel)
                .get()
                .setDenormalized(tConf.denormalized)
                .initialize(bandwidth);

        if (tConf.calculateCutoffs) {
            QuantileBoundEstimator q = new QuantileBoundEstimator(tConf);
            q.estimateQuantiles(data);
            cutoffH = q.cutoffH;
            cutoffL = q.cutoffL;
            tolerance = q.tolerance;
            tree = q.tree;
        } else {
            cutoffH = tConf.cutoffHAbsolute;
            cutoffL = tConf.cutoffLAbsolute;
            tolerance = tConf.tolAbsolute;
        }
        log.debug("Set up Kernel");

        if (tConf.algorithm == TreeKDEConf.Algorithm.TREEKDE) {
            if (tree == null) {
                tree = new KDTree()
                        .setSplitByWidth(tConf.splitByWidth)
                        .setLeafCapacity(tConf.leafSize)
                        .build(data);
            }
            log.debug("Trained Tree");

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
        } else if (tConf.algorithm == TreeKDEConf.Algorithm.RKDE) {
            if (tree == null) {
                tree = new KDTree()
                        .setSplitByWidth(tConf.splitByWidth)
                        .setLeafCapacity(tConf.leafSize)
                        .build(data);
            }
            log.debug("Trained Tree");
            RadiusKDE2 rkde = new RadiusKDE2(tree)
                    .setBandwidth(bandwidth)
                    .setKernel(kernel)
                    .setEpsilon(tolerance)
                    .setIgnoreSelf(tConf.ignoreSelfScoring)
                    .train();
            log.debug("Radius KDE with Radius: {}", rkde.radius);
            kde = rkde;

        } else {
            SimpleKDE sKDE = new SimpleKDE()
                    .setBandwidth(bandwidth)
                    .setKernel(kernel)
                    .setIgnoreSelf(tConf.ignoreSelfScoring)
                    .train(data);
            kde = sKDE;
        }

        if (tConf.useGrid) {
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

    @Override
    public int getNumKernels() {
        return kde.getNumKernels();
    }
}
