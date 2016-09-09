package macrobase;

import macrobase.classifier.QuantileEstimator;
import macrobase.conf.BenchmarkConf;
import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        String confPath = "conf/conf.yaml";
        if (args.length > 1) {
            confPath = args[1];
        }
        BenchmarkConf benchmarkConf = BenchmarkConf.load(confPath);

        StopWatch s = new StopWatch();
        s.start();
        List<double[]> metrics = new CSVDataSource(
                benchmarkConf.inputPath,
                benchmarkConf.inputColumns)
                .setLimit(benchmarkConf.inputRows)
                .get();
        s.stop();
        log.info("Loaded "+metrics.size()+" in "+s.toString());

        TreeKDEConf tConf = benchmarkConf.tKDEConf;
        QuantileEstimator qEstimator = new QuantileEstimator(tConf);
        int rSize = qEstimator.estimateQuantiles(metrics);
        log.info("Q: {}, T: {}, C: {}", qEstimator.quantile, qEstimator.tolerance, qEstimator.cutoff);
    }
}
