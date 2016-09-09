package macrobase.classifier;

import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class QuantileEstimatorTest {
    public static List<double[]> energyData;

    @BeforeClass
    public static void setUp() throws Exception {
        energyData = new CSVDataSource("src/test/resources/us_energy_10k.csv", 2).get();
    }

    @Test
    public void check1Percent() {
        TreeKDEConf tConf = new TreeKDEConf();
        tConf.percentile = 0.01;
        QuantileEstimator qEstimator = new QuantileEstimator(tConf);
        qEstimator.estimateQuantiles(energyData);
        // Value calculated from sklearn
        // bandwidth: [ 49.84778156, 10.74687233]
        assertThat(qEstimator.quantile, closeTo(2.4597e-7, 1e-8));
    }
}
