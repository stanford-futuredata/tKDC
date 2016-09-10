package macrobase.kde;

import java.util.Arrays;
import java.util.List;

public class AlgebraUtils {
    public static double[][] getBoundingBoxRaw(List<double[]> data) {
        int d = data.get(0).length;
        double[][] boundaries = new double[d][2];
        // Calculate boundaries of the data in the tree
        for (int i = 0; i < d; i++) {
            double maxI = -Double.MAX_VALUE;
            double minI = Double.MAX_VALUE;
            for (int j = 0; j < data.size(); j++) {
                double cur = data.get(j)[i];
                if (cur < minI) {minI = cur;}
                if (cur > maxI) {maxI = cur;}
            }
            boundaries[i][0] = minI;
            boundaries[i][1] = maxI;
        }
        return boundaries;
    }

    public static String array2dToString(double[][] data) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (double[] curRow : data) {
            s.append(Arrays.toString(curRow));
            s.append(",");
        }
        s.append("]");
        return s.toString().trim();
    }
}
