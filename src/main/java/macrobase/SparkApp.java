package macrobase;

import macrobase.conf.BenchmarkConf;
import macrobase.data.CSVDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SparkApp extends KDEApp {
    private static final Logger log = LoggerFactory.getLogger(SparkApp.class);

    public static void main(String[] args) throws Exception {
        System.out.println("Hello Spark");
        List<double[]> metrics = load(args);

        SparkSession spark = SparkSession
                .builder()
                .appName("JavaKDE")
                .getOrCreate();

    }
}
