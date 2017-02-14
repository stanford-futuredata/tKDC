package macrobase.kernel;

public abstract class Kernel {
    public boolean denormalized = false;

    protected int dim;
    // store the inverse of the bandwidth for speed
    protected double[] invBandwidth;

    // Cache these constant multipliers
    protected double dimFactor;
    protected double bwFactor;

    public Kernel setDenormalized(boolean flag) {
        this.denormalized = flag;
        return this;
    }

    public Kernel initialize(double[] bs) {
        this.dim = bs.length;
        this.invBandwidth = new double[dim];
        for (int i = 0; i < dim; i++) {
            invBandwidth[i] = 1.0 / bs[i];
        }

        if (this.denormalized) {
            this.dimFactor = 1.0;
            this.bwFactor = 1.0;
        } else {
            this.dimFactor = getDimFactor(dim);
            this.bwFactor = 1;
            for (int i = 0; i < dim; i++) {
                bwFactor *= invBandwidth[i];
            }
        }
        return this;
    }

    public abstract double getDimFactor(int curDim);
    public abstract double density(double[] d);
    public abstract double invDensity(double p);
}
