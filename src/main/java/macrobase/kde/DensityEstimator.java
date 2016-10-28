package macrobase.kde;

public interface DensityEstimator {
    double density(double[] d);
    int getNumKernels();
}
