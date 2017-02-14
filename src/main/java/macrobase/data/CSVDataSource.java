package macrobase.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVDataSource implements DataSource {
    public String fileName;
    public List<Integer> columns;
    public int limit = Integer.MAX_VALUE;
    public boolean hasHeader = true;

    public int startColumn;
    public int endColumn;

    public CSVDataSource(String fileName, List<Integer> columns) {
        this.fileName = fileName;
        this.columns = columns;
    }

    public CSVDataSource(String filename, int startColumn, int endColumn) {
        this.fileName = filename;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    public CSVDataSource setHasHeader(boolean flag) {
        this.hasHeader = flag;
        return this;
    }

    public CSVDataSource setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public List<double[]> get() throws Exception {
        Reader in = new FileReader(fileName);
        CSVFormat format = CSVFormat.RFC4180;
        if (hasHeader) {
            format = format.withFirstRecordAsHeader();
        }
        Iterable<CSVRecord> records = format.parse(in);

        ArrayList<double[]> results = new ArrayList<>();
        if (columns == null) {
            int n = endColumn - startColumn + 1;
            int rowCount = 0;
            for (CSVRecord row : records) {
                double[] dRow = new double[n];
                for (int i = 0; i < n; i++) {
                    dRow[i] = Double.parseDouble(row.get(i+startColumn));
                }
                results.add(dRow);
                rowCount++;
                if (rowCount >= limit) {
                    break;
                }
            }
        } else {
            int n = columns.size();

            int rowCount = 0;
            for (CSVRecord row : records) {
                double[] dRow = new double[n];
                for (int i = 0; i < row.size(); i++) {
                    int j = columns.indexOf(i);
                    if (j != -1) {
                        dRow[j] = Double.parseDouble(row.get(i));
                    }
                }
                results.add(dRow);
                rowCount++;
                if (rowCount >= limit) {
                    break;
                }
            }
        }

        in.close();
        return results;
    }
}
