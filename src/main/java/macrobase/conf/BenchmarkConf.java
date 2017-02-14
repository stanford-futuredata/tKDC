package macrobase.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkConf {
    public String inputPath;
    public List<Integer> inputColumns;

    public String inputColumnRange = "";
    public int startColumn = 0;
    public int endColumn = -1;

    public int inputRows;
    public int numToScore;

    public TreeKDEConf tKDEConf;

    public static BenchmarkConf load(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        BenchmarkConf newConf = mapper.readValue(new File(fileName), BenchmarkConf.class);

        if (newConf.inputColumnRange.contains("-")) {
            String range = newConf.inputColumnRange;
            int sep = range.indexOf("-");
            newConf.startColumn = Integer.parseInt(range.substring(0,sep));
            newConf.endColumn = Integer.parseInt(range.substring(sep+1));
            newConf.inputColumns = null;
        }
        if (newConf.inputRows <= 0) {
            newConf.inputRows = Integer.MAX_VALUE;
        }
        if (newConf.numToScore == 0) {
            newConf.numToScore = newConf.inputRows;
        }
        return newConf;
    }
}
