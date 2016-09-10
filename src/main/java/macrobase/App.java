package macrobase;

import macrobase.classifier.TreeKDEFactory;
import macrobase.conf.BenchmarkConf;
import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import macrobase.kde.TreeKDE;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
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

        StopWatch sw = new StopWatch();
        sw.start();
        TreeKDE tKDE = new TreeKDEFactory(tConf).getTreeKDE(metrics);
        sw.stop();
        log.info("Trained in {}", sw.toString());
        log.info("Cutoff: {}, Tolerance: {}", tKDE.getCutoff(), tKDE.getTolerance());

        double[] densities = new double[metrics.size()];
        sw.reset();
        sw.start();
        for (int i=0;i<metrics.size();i++) {
            double curDensity = tKDE.density(metrics.get(i));
            densities[i] = curDensity;
        }
        sw.stop();
        log.info("Scored in {}", sw.toString());
        log.info("Scored {} @ {} / s",
                densities.length,
                (float)densities.length * 1000/(sw.getTime()));
        Percentile p = new Percentile(1.0);
        p.setData(densities);
        double quantile = p.evaluate();
        log.info("{} percentile: {}", tConf.percentile, quantile);
    }
}
