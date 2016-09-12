package macrobase.kde;

import macrobase.util.TinyDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class DualTreeKDETest {
    public static List<double[]> tinyData;

    @BeforeClass
    public static void setUp() {
        tinyData = new TinyDataSource().get();
    }


    @Test
    public void simpleTest() throws Exception {
        List<double[]> data = tinyData;

        KDTree tree = new KDTree().setLeafCapacity(3);
        DualTreeKDE kde = new DualTreeKDE(tree).setTolerance(0.0);
        kde.train(data);

        KDESimple kdeSimple = new KDESimple();
        kdeSimple.train(data);

        assertArrayEquals(kdeSimple.getBandwidth(), kde.getBandwidth(), 1e-10);
        double[] treeScores = kde.densities(data);
        for (int i = 0; i < data.size(); i++) {
            double[] datum = data.get(i);
            double dSimple = kdeSimple.density(datum);
            double dTree = treeScores[i];
            assertEquals(dSimple, dTree, dSimple*1e-10);
        }
    }

    private void approxTest(
            List<double[]> data,
            double tol,
            double cutoff,
            boolean ignoreSelf,
            boolean splitByWidth
    ) {
        KDTree tree = new KDTree()
                .setLeafCapacity(3)
                .setSplitByWidth(splitByWidth)
                ;
        DualTreeKDE kde = new DualTreeKDE(tree)
                .setTolerance(tol)
                .setCutoff(cutoff)
                .setIgnoreSelf(ignoreSelf);
        kde.train(data);

        double checkTol = Math.max(tol, 1e-12);

        double[] treeScores = kde.densities(data);

        for (int i=0;i<data.size();i++) {
            double[] d = data.get(i);
            KDESimple kdeSimple = new KDESimple();

            if (!ignoreSelf) {
                kdeSimple.train(data);
            } else {
                ArrayList<double[]> subData = new ArrayList<>(data);
                subData.remove(i);
                kdeSimple.setBandwidth(kde.getBandwidth());
                kdeSimple.train(subData);
            }

            double trueDensity = kdeSimple.density(d);
            double estDensity = treeScores[i];

            if (trueDensity < cutoff) {
                assertEquals(trueDensity, estDensity, checkTol);
            }
        }
    }

    @Test
    public void testTwoPointIgnoreSelf() {
        List<double[]> data = new ArrayList<>();
        data.add(new double[] {0.0, 0.0});
        data.add(new double[] {1.0, 1.0});

        approxTest(data, 0.0, Double.MAX_VALUE, true, true);
    }

    @Test
    public void testIgnoreSelfExact() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 0.0, Double.MAX_VALUE, true, true);
    }

    @Test
    public void testExactSplitMedian() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 0.0, Double.MAX_VALUE, false, false);
    }

    @Test
    public void testTolerance() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-5, 0.0, false, true);
    }

    @Test
    public void testCutoff() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 0.0, 7e-4, false, true);
    }

    @Test
    public void testToleranceCutoff() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-5, 7e-4, false, true);
    }

    @Test
    public void testIgnoreSelfApprox() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-7, 1e-6, true, true);
    }
}
