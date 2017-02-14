package macrobase.kernel;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.List;

public class BandwidthSelector {
    private double userMultiplier = 1.0;
    public double scalarValue = -1.0;
    public boolean useStdDev = false;

    public BandwidthSelector setMultiplier(double m) {this.userMultiplier = m; return this;}
    public BandwidthSelector setValue(double value) {this.scalarValue = value; return this;}
    public BandwidthSelector setUseStdDev(boolean flag) {this.useStdDev = flag; return this;}

    public double[] findBandwidth(List<double[]> input) {
        int dim = input.get(0).length;
        int n = input.size();
        double scaleFactor = Math.pow(n, -1.0/(dim+4));

        Percentile pCalc = new Percentile();
        StandardDeviation sd = new StandardDeviation();
        double[] bandwidth = new double[dim];

        if (this.scalarValue > 0.0) {
            for (int i = 0; i < dim; i ++) {
                bandwidth[i] = scalarValue;
            }
            return bandwidth;
        }

        for (int d = 0; d < dim; d++) {
            double[] xs = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = input.get(i)[d];
            }
            double range;
            if (useStdDev) {
                range = sd.evaluate(xs);
            } else {
                pCalc.setData(xs);
                range = pCalc.evaluate(75) - pCalc.evaluate(25);
            }
            bandwidth[d] = range * scaleFactor * userMultiplier;
        }

        return bandwidth;
    }
}
