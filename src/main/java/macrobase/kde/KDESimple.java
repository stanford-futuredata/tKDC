package macrobase.kde;

import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KDESimple {
    private static final Logger log = LoggerFactory.getLogger(KDESimple.class);

    private List<double[]> trainPoints;
    private double[] bandwidth;
    private BandwidthSelector bwSelector;
    private Kernel kernel;

    public KDESimple() {
        bwSelector = new BandwidthSelector();
    }

    public KDESimple setBandwidth(double[] bw) {this.bandwidth = bw; return this;}
    public KDESimple setKernel(Kernel k) {this.kernel = k; return this;}

    public double[] getBandwidth() {
        return bandwidth;
    }

    public void train(List<double[]> data) {
        this.trainPoints = data;
        // Only calculate bandwidth if it hasn't been set by user
        if (bandwidth == null) {
            bandwidth = bwSelector.findBandwidth(data);
        }
        if (kernel == null) {
            kernel = new GaussianKernel();
        }
        kernel.initialize(bandwidth);
    }

    private double rawDensity(double[] d) {
        double score = 0.0;
        for (double[] v : trainPoints) {
            double[] testVec = d.clone();
            for (int i = 0; i < testVec.length; i++) {
                testVec[i] -= v[i];
            }
            double delta = kernel.density(testVec);
            score += delta;
        }
        return score;
    }

    public double density(double[] d) {
        return rawDensity(d) / trainPoints.size();
    }

}
