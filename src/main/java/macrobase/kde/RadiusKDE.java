package macrobase.kde;

import macrobase.kernel.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RadiusKDE {
    private static final Logger log = LoggerFactory.getLogger(RadiusKDE.class);

    public int k;
    public KDTree kdTree;
    public int numPoints;

    // Parameters
    public double[] bandwidth;
    public Kernel kernel;
    public double epsilon;

    // Calculated
    public double[] invBandwidth;
    public double radius = -1;
    public RangeQuery rangeQuery;

    // Diagnostics for last query
    public int numKernels;


    public RadiusKDE(KDTree tree) {
        this.k = tree.k;
        this.kdTree = tree;
        this.numPoints = tree.getNBelow();
    }
    public RadiusKDE setBandwidth(double[] bw) {this.bandwidth = bw;return this;}
    public RadiusKDE setKernel(Kernel k) {this.kernel = k;return this;}
    public RadiusKDE setEpsilon(double e) {this.epsilon = e;return this;}

    public RadiusKDE train() {
        this.radius = kernel.invDensity(epsilon);
        this.invBandwidth = new double[k];
        for (int i = 0; i < k; i++) {
            this.invBandwidth[i] = 1/bandwidth[i];
        }
        this.rangeQuery = new RangeQuery(kdTree)
                .setScalingFactor(invBandwidth);
        return this;
    }

    public double density(double[] q) {
        ArrayList<double[]> neighbors = rangeQuery.query(q, radius);
        numKernels = neighbors.size();
        double[] delta = new double[this.k];
        double totalDensity = 0.0;
        for (double[] n : neighbors) {
            for (int i = 0; i < this.k; i++) {
                delta[i] = q[i] - n[i];
            }
            totalDensity += kernel.density(delta);
        }

        return totalDensity / numPoints;
    }
}
