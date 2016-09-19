package macrobase.classifier;

import macrobase.conf.BenchmarkConf;
import macrobase.data.CSVDataSource;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class QuantileBoundEstimatorTest {
    public static List<double[]> getData(BenchmarkConf bConf) throws Exception {
        return new CSVDataSource(bConf.inputPath, bConf.inputColumns)
                .setLimit(bConf.inputRows)
                .get();
    }

    @Test
    public void check1Percent() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test_med.yaml");
        List<double[]> energyData = getData(bConf);

        QuantileBoundEstimator qEstimator = new QuantileBoundEstimator(bConf.tKDEConf);
        qEstimator.estimateQuantiles(energyData);
        // Value calculated from sklearn
        // bandwidth: [ 49.84778156, 10.74687233]
        assertThat(qEstimator.qT, closeTo(2.4597e-7, 1e-8));
    }
}
