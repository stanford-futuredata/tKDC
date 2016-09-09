package macrobase.kde;

import macrobase.util.TinyDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class TreeKDETest {
    public static List<double[]> tinyData;

    @BeforeClass
    public static void setUp() {
        tinyData = new TinyDataSource().get();
    }


    @Test
    public void simpleTest() throws Exception {
        List<double[]> data = tinyData;

        KDTree tree = new KDTree().setLeafCapacity(3);
        TreeKDE kde = new TreeKDE(tree).setTolerance(0.0);
        kde.train(data);

        KDESimple kdeSimple = new KDESimple();
        kdeSimple.train(data);

        assertArrayEquals(kdeSimple.getBandwidth(), kde.getBandwidth(), 1e-10);
        for (double[] datum : data) {
            double dSimple = kdeSimple.density(datum);
            double dTree = kde.density(datum);
            assertEquals(dSimple, dTree, dSimple*1e-10);
        }
    }

    private void approxTest(
            List<double[]> data,
            double tol,
            double cutoff,
            boolean ignoreSelf
    ) {
        KDTree tree = new KDTree().setLeafCapacity(3);
        TreeKDE kde = new TreeKDE(tree)
                .setTolerance(tol)
                .setCutoff(cutoff)
                .setIgnoreSelf(ignoreSelf);
        kde.train(data);

        double checkTol = Math.max(tol, 1e-12);

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
            double estDensity = kde.density(d);

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

        approxTest(data, 0.0, Double.MAX_VALUE, true);
    }

    @Test
    public void testIgnoreSelfExact() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 0.0, Double.MAX_VALUE, true);
    }

    @Test
    public void testTolerance() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-5, 0.0, false);
    }

    @Test
    public void testCutoff() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 0.0, 7e-4, false);
    }

    @Test
    public void testToleranceCutoff() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-5, 7e-4, false);
    }

    @Test
    public void testIgnoreSelfApprox() throws Exception {
        List<double[]> data = tinyData;
        approxTest(data, 1e-7, 1e-6, true);
    }
}
