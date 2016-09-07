package macrobase;

import macrobase.data.CSVDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        StopWatch s = new StopWatch();
        s.start();
        List<double[]> metrics = new CSVDataSource("us_energy_1p0_metrics.csv", 8)
                .setLimit(100000)
                .get();
        s.stop();
        log.info("Loaded "+metrics.size()+" in "+s.toString());
    }
}
