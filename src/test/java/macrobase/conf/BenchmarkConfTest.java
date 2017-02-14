package macrobase.conf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BenchmarkConfTest {
    @Test
    public void testLoadFile() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test.yaml");
        assertEquals(true, bConf.tKDEConf.ignoreSelfScoring);
        assertEquals(3, bConf.inputColumns.size());
        assertEquals(TreeKDEConf.Algorithm.TREEKDE, bConf.tKDEConf.algorithm);
    }

    @Test
    public void testColumnRange() throws Exception {
        BenchmarkConf bConf = BenchmarkConf.load("src/test/resources/conf/test_colrange.yaml");
        assertEquals(1, bConf.startColumn);
    }
}
