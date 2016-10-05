package macrobase.knn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class SimpleKNN {
    public int k;
    public double[] bw;

    public List<double[]> data;
    public Metric metric;

    public SimpleKNN(int k) {this.k = k;}
    public SimpleKNN setBandwidth(double[] bw) {this.bw = bw; return this;}

    public SimpleKNN train(List<double[]> data) {
        this.metric = new Metric(bw);
        this.data = data;
        return this;
    }

    public double score(double[] query) {
        PriorityQueue<Double> distances = new PriorityQueue<>(k+1, Collections.reverseOrder());
//        System.out.println("query: "+ Arrays.toString(query));
        for (double[] d : data) {
            double curDistance = metric.dist(query, d);
//            System.out.println("d: "+ Arrays.toString(d));
//            System.out.println("dist: "+curDistance);
            if (distances.size() < k || curDistance < distances.peek()) {
                distances.add(curDistance);
            }
            if (distances.size() > k) {
                distances.poll();
            }
        }
        return distances.peek();
    }
}
