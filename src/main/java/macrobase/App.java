package macrobase;

import macrobase.classifier.KDEClassifier;
import macrobase.conf.BenchmarkConf;
import macrobase.conf.TreeKDEConf;
import macrobase.data.CSVDataSource;
import macrobase.kde.TreeKDE;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

        StopWatch sw = new StopWatch();
        sw.start();
        List<double[]> metrics = new CSVDataSource(
                benchmarkConf.inputPath,
                benchmarkConf.inputColumns)
                .setLimit(benchmarkConf.inputRows)
                .get();
        sw.stop();
        log.info("Loaded "+metrics.size()+" in "+sw.toString());

        TreeKDEConf tConf = benchmarkConf.tKDEConf;

        KDEClassifier classifier = new KDEClassifier(tConf);
        sw.reset();
        sw.start();
        classifier.train(metrics);
        sw.stop();
        long trainTime = sw.getTime();
        log.info("Trained in: {}", sw.toString());
        log.info("BW: {}", Arrays.toString(classifier.bandwidth));
        log.info("CutoffH: {}, CutoffL: {} Tolerance: {}",
                classifier.cutoffH,
                classifier.cutoffL,
                classifier.tolerance);

        sw.reset();
        sw.start();
        double[] densities = new double[metrics.size()];
        for (int i = 0; i < densities.length; i++) {
            densities[i] = classifier.density(metrics.get(i));
        }
        sw.stop();
        long scoreTime = sw.getTime();
        if (classifier.kde instanceof TreeKDE) {
            ((TreeKDE) classifier.kde).showDiagnostics();
        }
        log.info("Scored in {}", sw.toString());
        log.info("Scored @ {} / s",
                (float)densities.length * 1000/(scoreTime));
        log.info("Total Processing: {}", (double)(trainTime+scoreTime)/1000);

        if (outputPath != null) {
            BufferedWriter out = Files.newBufferedWriter(Paths.get(outputPath));
            out.write("density\n");
            for (double d : densities) {
                out.write(Double.toString(d)+"\n");
            }
            out.close();
        }

        Arrays.sort(densities);
        int expectedNumOutliers = (int)(metrics.size() * tConf.percentile);
        double quantile = densities[expectedNumOutliers];
        log.info("{} percentile: {}", tConf.percentile, quantile);
    }
}
