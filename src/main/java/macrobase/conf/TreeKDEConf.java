package macrobase.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TreeKDEConf {
    // High Level Algorithm
    public enum Algorithm {
        TREEKDE, SIMPLEKDE, TREEKNN, RKDE
    }
    public Algorithm algorithm = Algorithm.TREEKDE;
    // Classifier
    public double percentile = 0.01;
    // Quantile Estimation
    public int qSampleSize = 10000;
    public int qReservoirMin = 200;
    public int qReservoirMax = 2000000;
    public double qTolMultiplier = 0.01;
    public double qCutoffMultiplier = 1.5;
    // KDE
    public String kernel = "gaussian";
    public boolean denormalized = false;
    public double bwValue = -1.0;
    public double bwMultiplier = 1.0;
    public boolean useStdDev = false;

    public boolean ignoreSelfScoring = false;
    public boolean calculateCutoffs = true;
    public double tolAbsolute = 0.0;
    public double cutoffHAbsolute = Double.MAX_VALUE;
    public double cutoffLAbsolute = 0.0;
    // KNN
    public int k = 10;
    // Tree
    public int leafSize = 20;
    public boolean splitByWidth = true;
    // Grid
    public boolean useGrid = true;
    public List<Double> gridSizes = Arrays.asList(.8, .5);

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static TreeKDEConf load(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(fileName), TreeKDEConf.class);
    }
}
