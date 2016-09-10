package macrobase.classifier;

import macrobase.conf.BenchmarkConf;
import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class QuantileEstimatorTest {
    public static List<double[]> getData(BenchmarkConf bConf) throws Exception {
        return new CSVDataSource(bConf.inputPath, bConf.inputColumns)
                .setLimit(bConf.inputRows)
                .get();
    }

    @Test
    public void check1Percent() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test_med.yaml");
        List<double[]> energyData = getData(bConf);

        QuantileEstimator qEstimator = new QuantileEstimator(bConf.tKDEConf);
        qEstimator.estimateQuantiles(energyData);
        // Value calculated from sklearn
        // bandwidth: [ 49.84778156, 10.74687233]
        assertThat(qEstimator.quantile, closeTo(2.4597e-7, 1e-8));
    }
}
