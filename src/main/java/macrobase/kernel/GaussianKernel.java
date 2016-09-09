package macrobase.kernel;

import org.apache.commons.math3.util.FastMath;

/**
 * Standard normal Gaussian with zero mean
 */
public class GaussianKernel extends Kernel {
    @Override
    public double getDimFactor(int curDim) {
        return Math.pow(2*Math.PI,-.5*curDim);
    }

    @Override
    public double density(double[] d) {
        double dist2 = 0.0;
        for (int i=0; i < d.length; i++) {
            double del = d[i] * invBandwidth[i];
            dist2 += del * del;
        }
        return dimFactor * bwFactor * FastMath.exp(-.5 * dist2);
    }

    @Override
    public double[] getBounds(double[][] deltas) {
        double[] dist = new double[2];
        for (int i=0; i < deltas[0].length; i++) {
            double del = deltas[0][i] * invBandwidth[i];
            dist[0] += del * del;

            del = deltas[1][i] * invBandwidth[i];
            dist[1] += del * del;
        }

        if (dist[0] > 0 && (dist[1] - dist[0] > 4)) {
            return new double[]{
                    FastMath.scalb(dimFactor * bwFactor, -(int)(.5*dist[1]*1.5+1)),
                    FastMath.scalb(dimFactor * bwFactor, -(int)(.5*dist[0]*1.4))
            };
        } else {
            return new double[]{
                    dimFactor * bwFactor * FastMath.exp(-.5 * dist[1]),
                    dimFactor * bwFactor * FastMath.exp(-.5 * dist[0])
            };
        }
    }

    @Override
    public double upperBound(double[] d) {
        double dist2 = 0.0;
        for (int i=0; i < d.length; i++) {
            double del = d[i] * invBandwidth[i];
            dist2 += del * del;
        }
        return FastMath.scalb(dimFactor * bwFactor, -(int)(.5*dist2*1.4));
    }

    @Override
    public double lowerBound(double[] d) {
        double dist2 = 0.0;
        for (int i=0; i < d.length; i++) {
            double del = d[i] * invBandwidth[i];
            dist2 += del * del;
        }
        return FastMath.scalb(dimFactor * bwFactor, -(int)(.5*dist2*1.5+1));
    }
}
