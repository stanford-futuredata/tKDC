package macrobase.knn;

import macrobase.kde.KDTree;
import macrobase.util.TinyDataSource;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TreeKNNTest {

    @Test
    public void testTinyExact() throws Exception {
        List<double[]> data = new TinyDataSource().get();
        double[] bw = {1, 1, 1};
        SimpleKNN knn = new SimpleKNN(3)
                .setBandwidth(bw)
                .train(data);
        TreeKNN tknn = new TreeKNN(3)
                .setBandwidth(bw)
                .train(data);

        double[] scores = new double[data.size()];
        for (int i = 0; i < scores.length; i++) {
            assertEquals(
                    knn.score(data.get(i)),
                    tknn.score(data.get(i)),
                    1e-10);
        }
    }

    @Test
    public void testTinyBounded() throws Exception {
        List<double[]> data = new TinyDataSource().get();
        double[] bw = {1, 1, 1};
        SimpleKNN knn = new SimpleKNN(3)
                .setBandwidth(bw)
                .train(data);
        double bL = 8.0;
        double bH = 10.0;
        KDTree tree = new KDTree()
                .setLeafCapacity(2)
                .setSplitByWidth(true)
                .build(data);
        TreeKNN tknn = new TreeKNN(3)
                .setBandwidth(bw)
                .setBounds(bL, bH)
                .setTree(tree)
                .train(data);
//        System.out.println(tree.rawString());
        double[] scores = new double[data.size()];
        for (int i = 0; i < scores.length; i++) {
            double[] q = data.get(i);
            double kScore = knn.score(q);
            double tScore = tknn.score(q);
            if (kScore < bL) {
                assertThat(tScore, lessThanOrEqualTo(bL));
            } else if (kScore > bH) {
                assertThat(tScore, greaterThanOrEqualTo(bH));
            } else {
                assertEquals(kScore, tScore, 1d-10);
            }
        }

    }
}
