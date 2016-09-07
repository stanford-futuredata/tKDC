package macrobase.data;

import java.util.List;

public interface DataSource {
    List<double[]> get() throws Exception;
}