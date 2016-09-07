package macrobase.kde;

public abstract class Kernel {
    protected int dim;
    // store the inverse of the bandwidth for speed
    protected double[] invBandwidth;

    // Cache these constant multipliers
    protected double dimFactor;
    protected double bwFactor;

    public Kernel initialize(double[] bs) {
        this.dim = bs.length;
        this.invBandwidth = new double[dim];
        this.dimFactor = getDimFactor(dim);

        this.bwFactor = 1;
        for (int i = 0; i < dim; i++) {
            invBandwidth[i] = 1.0/bs[i];
            bwFactor *= invBandwidth[i];
        }
        return this;
    }

    public abstract double getDimFactor(int curDim);
    public abstract double density(double[] d);

    public double[] getBounds(double[][] deltas) {
        return new double[]{density(deltas[0]), density(deltas[1])};
    }

    public double lowerBound(double[] d) {
        return density(d);
    }
    public double upperBound(double[] d) {
        return density(d);
    }
}
