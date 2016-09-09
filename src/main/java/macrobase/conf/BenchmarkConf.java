package macrobase.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BenchmarkConf {
    public String inputPath;
    public List<Integer> inputColumns;
    public int inputRows;

    public TreeKDEConf tKDEConf;

    public static BenchmarkConf load(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(fileName), BenchmarkConf.class);
    }
}
