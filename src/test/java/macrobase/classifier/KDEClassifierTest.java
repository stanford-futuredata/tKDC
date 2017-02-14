package macrobase.classifier;

import macrobase.conf.BenchmarkConf;
import macrobase.data.CSVDataSource;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class KDEClassifierTest {
    public static List<double[]> getData(BenchmarkConf bConf) throws Exception {
        return new CSVDataSource(bConf.inputPath, bConf.inputColumns)
                .setLimit(bConf.inputRows)
                .get();
    }

    @Test
    public void check1Percent() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test_med.yaml");
        List<double[]> data = getData(bConf);
        List<double[]> trueDensities = new CSVDataSource(
                "src/test/resources/simple_energy_d2_small.csv",
                0,0
        ).get();
        double[] tDensities = new double[data.size()];

        KDEClassifier kde = new KDEClassifier(bConf.tKDEConf);
        kde.train(data);

        double[] densities = new double[data.size()];
        for (int i = 0; i < densities.length; i++) {
            densities[i] = kde.density(data.get(i));
            tDensities[i] = trueDensities.get(i)[0];
        }

        Percentile p = new Percentile(1.0);
        double kdeCutoff = p.evaluate(densities);
        double trueCutoff = p.evaluate(tDensities);
        assertThat(kdeCutoff, closeTo(trueCutoff, 0.01 * trueCutoff));

        int disagree = 0;
        for (int i = 0; i < densities.length; i++) {
            boolean curOutlier = densities[i] < kdeCutoff;
            boolean trueOutlier = tDensities[i] < trueCutoff;
            if (curOutlier != trueOutlier) {
                disagree++;
            }
        }
        assertThat(disagree, is(0));
    }
}
