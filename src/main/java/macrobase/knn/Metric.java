package macrobase.knn;

import java.util.Arrays;

public class Metric {
    public double[] bw;

    public Metric(double[] bw) {
        this.bw = bw;
    }

    public double dist(double[] d1) {
        double d = 0;
        for (int i = 0; i < d1.length; i++) {
            double del = d1[i]/bw[i];
            d += del * del;
        }
        return d;
    }

    public double dist(double[] d1, double[] d2) {
        double d = 0;
        for (int i = 0; i < d1.length; i++) {
            double del = (d2[i] - d1[i])/bw[i];
            d += del * del;
        }
        return d;
    }

    public String toString() {
        return "Euclidean: "+ Arrays.toString(bw);
    }
}
