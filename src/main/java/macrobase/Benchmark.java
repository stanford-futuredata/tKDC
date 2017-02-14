package macrobase;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import macrobase.kde.KDTree;
import macrobase.kde.RadiusKDE;
import macrobase.kde.TreeKDE;
import macrobase.kernel.BandwidthSelector;
import macrobase.kernel.GaussianKernel;
import macrobase.kernel.Kernel;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.*;

public class Benchmark extends KDEApp {
    private static final Logger log = LoggerFactory.getLogger(Benchmark.class);

    public static MultivariateNormalDistribution getGaussian(int k) {
        double[] mean = new double[k];
        double[] covVector = new double[k];
        for (int i = 0; i < k; i++) {
            covVector[i] = 1.0;
        }
        double[][] cov = new DiagonalMatrix(covVector).getData();

        MultivariateNormalDistribution m = new MultivariateNormalDistribution(
                mean,
                cov
        );
        return m;
    }

    public static double calcTreshold(MultivariateNormalDistribution m, double p) {
        int NUM_SAMPLES = 100000;
        m.reseedRandomGenerator(0);
        double[] pdfs = new double[NUM_SAMPLES];
        for (int i = 0; i < NUM_SAMPLES; i++) {
            pdfs[i] = m.density(m.sample());
        }
        return (new Percentile().evaluate(pdfs, p * 100));
    }

    public static Map<String, String> getOutputRow(
            int k, int n_train, int n_test, double p,
            double bw, double threshold, double epsilonFactor,
            String algorithm,
            Map<String, Double> metrics
    ) {
        Map<String, String> row = new HashMap<String, String>();
        row.put("algorithm", algorithm);
        row.put("k", String.valueOf(k));
        row.put("n_train", String.valueOf(n_train));
        row.put("n_test", String.valueOf(n_test));
        row.put("p", String.valueOf(p));
        row.put("bw", String.valueOf(bw));
        row.put("threshold", String.valueOf(threshold));
        row.put("eps_factor", String.valueOf(epsilonFactor));
        for (String key : metrics.keySet()) {
            row.put(key, String.valueOf(metrics.get(key)));
        }
        return row;
    }

    public static List<Map<String, String>> runGaussianTest(
            int k, int n_train, int n_test, double p, double epsFactor,
            boolean onlyTKDE
    ) {
        log.info("k: {}, n: {}", k, n_train);
        MultivariateNormalDistribution m = getGaussian(k);
        double bwScalar = Math.pow(10000.0,-1.0/(k+4));
        double threshold = calcTreshold(m, p);

        double[] bw = new double[k];
        for (int i = 0; i < k; i++) {
            bw[i] = bwScalar;
        }

        List<double[]> trainData = Arrays.asList(m.sample(n_train));
        List<double[]> testData = Arrays.asList(m.sample(n_test));

        List<Map<String, String>> rows = new ArrayList<>(2);

        if (!onlyTKDE) {
            ImmutableMap<String, Double> rResults = runRKDE(
                    trainData,
                    bw,
                    testData,
                    threshold * epsFactor
            );
            rows.add(getOutputRow(
                    k, n_train, n_test, p, bwScalar, threshold, epsFactor,
                    "rkde",
                    rResults
            ));
            log.info("rKDE results: {}", rResults.toString());
        }

        ImmutableMap<String, Double> tResults = runTKDE(
                trainData,
                bw,
                testData,
                threshold * epsFactor,
                threshold
        );
        rows.add(getOutputRow(
                k, n_train, n_test, p, bwScalar, threshold, epsFactor,
                "tkde",
                tResults
        ));
        log.info("tKDE results: {}", tResults.toString());

        return rows;
    }

    public static ImmutableMap<String, Double> runTKDE(
            List<double[]> data, double[] bw, List<double[]> queries,
            double epsilon, double cutoff
    ) {
        int n = data.size();
        int k = data.get(0).length;
        Kernel kernel = new GaussianKernel()
                .initialize(bw);

        long startTime = System.currentTimeMillis();
        KDTree t = new KDTree().setSplitByWidth(true).build(data);
        TreeKDE tKDE = new TreeKDE(t)
                .setBandwidth(bw)
                .setCutoffH(cutoff)
                .setCutoffL(cutoff)
                .setIgnoreSelf(false)
                .setKernel(kernel)
                .setTolerance(epsilon)
                .setTrainedTree(t)
                .train(data);
        long trainTime = System.currentTimeMillis() - startTime;

        long totalKernels = 0;
        startTime = System.currentTimeMillis();
        for (double[] q : queries) {
            tKDE.numKernels = 0;
            tKDE.density(q);
            totalKernels += tKDE.numKernels;
        }
        long scoreTime = System.currentTimeMillis() - startTime;

        double avgScoreTime = ((double)scoreTime) / queries.size();
        double avgNumKernels = ((double)totalKernels) / queries.size();
        return ImmutableMap.<String, Double>builder()
                .put("train_time", (double)trainTime)
                .put("score_time", avgScoreTime)
                .put("num_kernels", avgNumKernels)
                .build();

    }

    public static ImmutableMap<String, Double> runRKDE(
            List<double[]> data, double[] bw, List<double[]> queries,
            double epsilon
    ) {
        int n = data.size();
        int k = data.get(0).length;
        Kernel kernel = new GaussianKernel().initialize(bw);

        long startTime = System.currentTimeMillis();
        KDTree t = new KDTree().setSplitByWidth(true).build(data);
        RadiusKDE rKDE = new RadiusKDE(t)
                .setBandwidth(bw)
                .setKernel(kernel)
                .setEpsilon(epsilon)
                .train();
        log.info("Radius: {}",rKDE.radius);
        long trainTime = System.currentTimeMillis() - startTime;

        long totalKernels = 0;
        startTime = System.currentTimeMillis();
        for (double[] q : queries) {
            rKDE.density(q);
            totalKernels += rKDE.numKernels;
        }
        long scoreTime = System.currentTimeMillis() - startTime;

        double avgScoreTime = ((double)scoreTime) / queries.size();
        double avgNumKernels = ((double)totalKernels) / queries.size();
        return ImmutableMap.<String, Double>builder()
                .put("train_time", (double)trainTime)
                .put("score_time", avgScoreTime)
                .put("num_kernels", avgNumKernels)
                .build();
    }

    public static void writeAllResults(
            List<Map<String, String>> results,
            String experimentName, String fileName
    ) throws Exception {
        long seconds = System.currentTimeMillis() / 1000;
        String fName = String.format(
                "experiments/%s/%s_%d.csv",
                experimentName,
                fileName,
                seconds
        );
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fName)));

        List<String> keys = new ArrayList<>(results.get(0).keySet());
        out.println(Joiner.on(',').join(keys));
        for (Map<String, String> row : results) {
            List<String> vals = new ArrayList<>(keys.size());
            for (String key : keys) {
                vals.add(row.get(key));
            }
            out.println(Joiner.on(',').join(vals));
        }
        out.close();
    }

    public static String getDateString() {
        Calendar cal = Calendar.getInstance();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(cal.getTime());
    }

    public static List<Map<String, String>> runGaussianSuiteForDimension(
            int k, int numTrials, int maxRKDETrial
    ) {
        List<Map<String, String>> results = new ArrayList<>(numTrials);
        int[] ns = new int[numTrials];
        boolean[] onlyTKDE = new boolean[numTrials];
        ns[0] = 10000;
        for (int i = 1; i < numTrials; i++) {
            ns[i] = (int)(ns[i-1] * 1.5);
            onlyTKDE[i] = (i > maxRKDETrial);
        }

        for (int i = 0; i < numTrials; i++) {
            results.addAll(
                    runGaussianTest(k, ns[i], 10000, 0.1, 0.01, onlyTKDE[i])
            );
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        List<Map<String, String>> results;
//        results = runGaussianSuiteForDimension(2, 15, 10);
        results = runGaussianSuiteForDimension(16, 15, 10);
        writeAllResults(results, "asymptotics", "gaussian_4");
    }
}
