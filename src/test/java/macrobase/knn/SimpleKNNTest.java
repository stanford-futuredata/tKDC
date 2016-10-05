package macrobase.knn;

import macrobase.util.TinyDataSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleKNNTest {

    @Test
    public void testTinyData() throws Exception {
        List<double[]> data = new TinyDataSource().get();
        double[] bw = {1, 1, 1};
        SimpleKNN knn = new SimpleKNN(2)
                .setBandwidth(bw)
                .train(data);
        double[] scores = new double[data.size()];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = knn.score(data.get(i));
        }
        assertEquals(scores[0], 4.0, 1e-10);
    }
}
