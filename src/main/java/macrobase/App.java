package macrobase;

import macrobase.classifier.KDEFactory;
import macrobase.conf.BenchmarkConf;
import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import macrobase.kde.SimpleKDE;
import macrobase.kde.TreeKDE;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        String confPath = "conf/conf.yaml";
        if (args.length > 0) {
            confPath = args[0];
        }
        String outputPath = null;
        if (args.length > 1) {
            outputPath = args[1];
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

        double[] densities = new double[benchmarkConf.numToScore];
        long trainTime = 0;
        long scoreTime = 0;
        StopWatch sw = new StopWatch();

        if (tConf.algorithm == TreeKDEConf.Algorithm.TREEKDE) {
            sw.start();
            TreeKDE tKDE = new KDEFactory(tConf).getTreeKDE(metrics);
            log.info("Trained");
            log.info("BW: {}", Arrays.toString(tKDE.getBandwidth()));
            log.info("Cutoff: {}, Tolerance: {}", tKDE.getCutoff(), tKDE.getTolerance());
            sw.stop();
            trainTime = sw.getTime();

            sw.reset();
            sw.start();
            for (int i = 0; i < densities.length; i++) {
                double curDensity = tKDE.density(metrics.get(i));
                densities[i] = curDensity;
            }
            sw.stop();
            scoreTime = sw.getTime();
        } else if (tConf.algorithm == TreeKDEConf.Algorithm.SIMPLEKDE) {
            sw.start();
            SimpleKDE kde = new KDEFactory(tConf).getSimpleKDE(metrics);
            log.info("Trained");
            log.info("BW: {}", Arrays.toString(kde.getBandwidth()));
            sw.stop();
            trainTime = sw.getTime();

            sw.reset();
            sw.start();
            for (int i = 0; i < densities.length; i++) {
                double curDensity = kde.density(metrics.get(i));
                densities[i] = curDensity;
            }

            sw.stop();
            scoreTime = sw.getTime();
        }
        log.info("Trained in {}", trainTime);
        log.info("Scored in {}", scoreTime);
        log.info("Scored {} @ {} / s",
                densities.length,
                (float)densities.length * 1000/(scoreTime));
        log.info("Total Processing: {}", (double)(trainTime+scoreTime)/1000);

        if (outputPath != null) {
            BufferedWriter out = Files.newBufferedWriter(Paths.get(outputPath));
            for (double d : densities) {
                out.write(Double.toString(d)+"\n");
            }
            out.close();
        }

        Percentile p = new Percentile(1.0);
        p.setData(densities);
        double quantile = p.evaluate();
        log.info("{} percentile: {}", tConf.percentile, quantile);
    }
}
