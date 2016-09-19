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
        TREEKDE, SIMPLEKDE
    }
    public Algorithm algorithm = Algorithm.TREEKDE;
    // Classifier
    public double percentile = 0.01;
    // Quantile Estimation
    public int qSampleSize = 10000;
    public double qTolMultiplier = 0.01;
    public double qCutoffMultiplier = 1.5;
    // KDE
    public String kernel = "gaussian";
    public double bwMultiplier = 1.0;
    public boolean ignoreSelfScoring = false;
    public boolean calculateCutoffs = true;
    public double tolMultiplier = 0.1;
    public double cutoffMultiplier = 1.5;
    public double tolAbsolute = 0.0;
    public double cutoffAbsolute = Double.MAX_VALUE;
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
