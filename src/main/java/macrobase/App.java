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

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        StopWatch sw = new StopWatch();
        sw.start();
        TreeKDE tKDE = new TreeKDEFactory(tConf).getTreeKDE(metrics);
        sw.stop();
        log.info("Trained in {}", sw.toString());
        long trainTime = sw.getTime();
        log.info("Cutoff: {}, Tolerance: {}", tKDE.getCutoff(), tKDE.getTolerance());

        double[] densities = new double[benchmarkConf.numToScore];
        sw.reset();
        sw.start();
        for (int i=0;i<densities.length;i++) {
            double curDensity = tKDE.density(metrics.get(i));
            densities[i] = curDensity;
        }
        sw.stop();
        long scoreTime = sw.getTime();
        log.info("Scored in {}", sw.toString());
        log.info("Scored {} @ {} / s",
                densities.length,
                (float)densities.length * 1000/(sw.getTime()));
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
