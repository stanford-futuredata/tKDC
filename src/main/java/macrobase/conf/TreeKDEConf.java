package macrobase.conf;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.List;

public class TreeKDEConf {
    // Classifier
    public double percentile = 0.01;
    // KDE
    public String kernel = "gaussian";
    public double tolMultiplier = 0.0;
    public double cutoffMultiplier = 1.5;
    public double bwMultiplier = 1.0;
    // Tree
    public int leafSize = 20;
    public boolean splitByWidth = true;
    // Quantile Estimation
    public int qSampleSize = 10000;
    // Grid
    public boolean useGrid = true;
    public List<Double> gridSizes = Arrays.asList(.8, .5);

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
