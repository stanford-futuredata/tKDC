package macrobase.kde;

import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import macrobase.util.TinyDataSource;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class RadiusKDETest {
    private RadiusKDE2 trainSimpleRadiusKDE(List<double[]> data, double epsilon) {
        double[] bw = (new BandwidthSelector()).findBandwidth(data);
        Kernel k = new GaussianKernel().initialize(bw);

        KDTree t = new KDTree().setSplitByWidth(true).build(data);
        RadiusKDE2 rKDE = new RadiusKDE2(t)
                .setBandwidth(bw)
                .setKernel(k)
                .setEpsilon(epsilon)
                .train();
        return rKDE;
    }

    private SimpleKDE trainSimpleKDE(List<double[]> data) {
        double[] bw = (new BandwidthSelector()).findBandwidth(data);
        Kernel k = new GaussianKernel().initialize(bw);
        SimpleKDE sKDE = new SimpleKDE()
                .setBandwidth(bw)
                .setKernel(k)
                .setIgnoreSelf(false)
                .train(data);
        return sKDE;
    }

    @Test
    public void testTinyExact() {
        List<double[]> data = new TinyDataSource().get();
        SimpleKDE sKDE = trainSimpleKDE(data);
        RadiusKDE2 rKDE = trainSimpleRadiusKDE(data, 0.0);

        for (double[] q : data) {
            double p1 = sKDE.density(q);
            double p2 = rKDE.density(q);
            assertThat(p2, closeTo(p1, 1e-8));
        }
    }

    @Test
    public void testGaussian() {
        int k=2;
        double[] mean = {2.0, 3.0};
        double[] factor = {1.0, 0.5};
        double[][] cov = new DiagonalMatrix(new double[] {4.0, 7.0}).getData();
        MultivariateNormalDistribution m = new MultivariateNormalDistribution(
                mean,
                cov
        );

        int n = 1000;
        double[][] rdata = m.sample(n);
        ArrayList<double[]> data = new ArrayList<>(Arrays.asList(rdata));

        SimpleKDE sKDE = trainSimpleKDE(data);
        RadiusKDE2 rKDE = trainSimpleRadiusKDE(data, 1e-3);

        for (double[] q : data) {
            double p1 = sKDE.density(q);
            double p2 = rKDE.density(q);
            assertThat(p2, closeTo(p1, 1e-3));
        }
    }
}
