package macrobase.knn;

import macrobase.classifier.KNNBoundEstimator;
import macrobase.classifier.QuantileBoundEstimator;
import macrobase.conf.BenchmarkConf;
import macrobase.data.CSVDataSource;
import macrobase.kernel.BandwidthSelector;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class KNNBoundEstimatorTest {
    public static List<double[]> getData(BenchmarkConf bConf) throws Exception {
        return new CSVDataSource(bConf.inputPath, bConf.inputColumns)
                .setLimit(bConf.inputRows)
                .get();
    }

    @Test
    public void check1Percent() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test_med_knn.yaml");
        List<double[]> energyData = getData(bConf);

        KNNBoundEstimator qEstimator = new KNNBoundEstimator(bConf.tKDEConf);
        qEstimator.estimateQuantiles(energyData);
        // Value calculated from sklearn
        // bandwidth: [ 49.84778156, 10.74687233]
        assertThat(qEstimator.qT, closeTo(5.5296, 0.01));
    }
}
