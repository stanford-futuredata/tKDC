package macrobase.kde;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RangeQueryTest {
    @Test
    public void testSimple() {
        double[][] rdata = {
                {0.0, 0.0},
                {1.0, 1.0},
                {2.0, 2.0}
        };
        ArrayList<double[]> data = new ArrayList<>();
        data.addAll(Arrays.asList(rdata));

        KDTree t = new KDTree().build(data);
        RangeQuery r = new RangeQuery(t);

        double[] q = {1.0, 1.0};
        ArrayList<double[]> neighbors = r.query(q, 1.5);
        assertThat(neighbors.size(), equalTo(3));

        q[0] = 2.0; q[1] = 2.0;
        assertThat(r.query(q,1.5).size(), equalTo(2));

        q[0] = 10.0; q[1] = 20.0;
        assertThat(r.query(q,2.0).size(), equalTo(0));
    }

    @Test
    public void testGaussian() {
        double[] mean = {0.0};
        double[] bw = {0.5};
        double[][] cov = new DiagonalMatrix(new double[] {4.0}).getData();
        MultivariateNormalDistribution m = new MultivariateNormalDistribution(
                mean,
                cov
        );

        int n = 10000;
        double[][] rdata = m.sample(n);
        ArrayList<double[]> data = new ArrayList<>(Arrays.asList(rdata));
        KDTree t = new KDTree().build(data);
        RangeQuery r = new RangeQuery(t).setScalingFactor(bw);

        double[] q = {0.0};
        ArrayList<double[]> neighbors = r.query(q, 1.0);
        assertThat((double)neighbors.size()/n, is(closeTo(0.68, 0.05)));
    }

    @Test
    public void test2DGaussian() {
        int k=2;
        double[] mean = {2.0, 3.0};
        double[] factor = {1.0, 0.5};
        double[][] cov = new DiagonalMatrix(new double[] {4.0, 7.0}).getData();
        MultivariateNormalDistribution m = new MultivariateNormalDistribution(
                mean,
                cov
        );

        int n = 10000;
        double[][] rdata = m.sample(n);
        ArrayList<double[]> data = new ArrayList<>(Arrays.asList(rdata));
        KDTree t = new KDTree().build(data);
        RangeQuery r = new RangeQuery(t).setScalingFactor(factor);

        double[] q = {1.0, 2.0};
        double d = 2.0;
        ArrayList<double[]> neighbors = r.query(q, d);

        for (double[] curNeighbor : neighbors) {
            double d2 = 0.0;
            for (int j = 0; j < k; j++) {
                double del = (curNeighbor[j] - q[j])*factor[j];
                d2 += del * del;
            }
            assertThat(d2, lessThanOrEqualTo(d * d));
        }

        int correctCount = 0;
        for (double[] curPoint : data) {
            double d2 = 0.0;
            for (int j = 0; j < k; j++) {
                double del = (curPoint[j] - q[j])*factor[j];
                d2 += del * del;
            }
            if (d2 <= d*d) {
                correctCount++;
            }
        }
        assertThat(neighbors.size(), is(equalTo(correctCount)));
    }
}
