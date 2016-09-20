package macrobase.kde;

import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import macrobase.util.TinyDataSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CompositeGridTest {
    @Test
    public void testSimpleGrid() {
        List<double[]> data = new TinyDataSource().get();
        double[] bw = {2,2,2};
        double[] zeros = {0,0,0};
        Kernel k = new GaussianKernel().initialize(bw);
        SimpleKDE kde = new SimpleKDE()
                .setBandwidth(bw)
                .setIgnoreSelf(true)
                .train(data);

        double cutoff = 2e-4;
        CompositeGrid grid = new CompositeGrid(
                k,
                bw,
                Arrays.asList(1.0),
                cutoff
        ).train(data);

        for (double[] d : data) {
            double kdeDensity = kde.density(d);
            double gridDensity = grid.density(d);
            assertThat(gridDensity, lessThanOrEqualTo(kdeDensity));
            if (gridDensity > 0) {
                assertThat(gridDensity, greaterThan(cutoff));
            }
        }
    }
}
