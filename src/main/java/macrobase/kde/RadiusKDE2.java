package macrobase.kde;

import macrobase.kernel.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class RadiusKDE2 implements DensityEstimator {
    private static final Logger log = LoggerFactory.getLogger(RadiusKDE2.class);

    public int k;
    public KDTree kdTree;
    public int numPoints;

    // Parameters
    public double[] bandwidth;
    public Kernel kernel;
    public double epsilon;
    public boolean ignoreSelf = false;

    // Calculated
    public double[] invBandwidth;
    public double radius = -1;
    public RangeQuery rangeQuery;
    public double selfPointDensity;

    // Diagnostics for last query
    public int numKernels;

    public RadiusKDE2(KDTree tree) {
        this.k = tree.k;
        this.kdTree = tree;
        this.numPoints = tree.getNBelow();
    }
    public RadiusKDE2 setBandwidth(double[] bw) {this.bandwidth = bw;return this;}
    public RadiusKDE2 setKernel(Kernel k) {this.kernel = k;return this;}
    public RadiusKDE2 setEpsilon(double e) {this.epsilon = e;return this;}
    public RadiusKDE2 setIgnoreSelf(boolean flag) {this.ignoreSelf = flag; return this;}

    public RadiusKDE2 train() {
        this.radius = kernel.invDensity(epsilon);
        this.invBandwidth = new double[k];
        for (int i = 0; i < k; i++) {
            this.invBandwidth[i] = 1/bandwidth[i];
        }
        this.rangeQuery = new RangeQuery(kdTree)
                .setScalingFactor(invBandwidth);

        this.selfPointDensity = kernel.density(new double[bandwidth.length]);
        return this;
    }

    public int checkIfVectorsWithinBall(double[][] minMaxVectors, double r2) {
        int k = minMaxVectors[0].length;
        double minSquaredDistance = 0.0;
        double maxSquaredDistance = 0.0;

        for (int i = 0; i < k; i++) {
            minSquaredDistance += minMaxVectors[0][i] * minMaxVectors[0][i];
            if (minSquaredDistance > r2) {
                return 1;
            }
            maxSquaredDistance += minMaxVectors[1][i] * minMaxVectors[1][i];
        }
        if (maxSquaredDistance < r2) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public double density(double[] q) {
        numKernels = 0;
        double totalDensity = 0.0;
        if (ignoreSelf) {
            totalDensity = -selfPointDensity;
        }
        double r2 = radius * radius;

        Deque<KDTree> nodesToProcess = new ArrayDeque<>(100);
        nodesToProcess.addFirst(this.kdTree);

        while(!nodesToProcess.isEmpty()) {
            KDTree curNode = nodesToProcess.pollFirst();
            double[][] minMaxVectors = curNode.getMinMaxDistanceVectors(q);
            for (int i = 0; i < k; i++) {
                minMaxVectors[0][i] *= invBandwidth[i];
                minMaxVectors[1][i] *= invBandwidth[i];
            }
            numKernels += 2;
            int status = checkIfVectorsWithinBall(minMaxVectors, r2);

            if (status == 1) {
                continue;
            } else if (status == 0) {
                if (curNode.isLeaf()) {
                    ArrayList<double[]> pts = curNode.getItems();
                    numKernels += pts.size();
                    for (double[] p : pts) {
                        double pointDistance = 0.0;
                        double del;
                        for (int i = 0; i<k; i++) {
                            del = (p[i] - q[i]) * invBandwidth[i];
                            pointDistance += del*del;
                        }
                        if (pointDistance <= r2) {
                            totalDensity += kernel.qdensity(p, q);
                        }
                    }
                } else {
                    KDTree[] children = curNode.getChildren(q);
                    nodesToProcess.addFirst(children[1]);
                    nodesToProcess.addFirst(children[0]);
                }
            } else {
                Deque<KDTree> nodesToAddAll = new ArrayDeque<>();
                nodesToAddAll.add(curNode);
                while (!nodesToAddAll.isEmpty()) {
                    KDTree tempNode = nodesToAddAll.poll();
                    if (tempNode.isLeaf()) {
                        ArrayList<double[]> pts = tempNode.getItems();
                        numKernels += pts.size();
                        for (double[] p : pts) {
                            totalDensity += kernel.qdensity(p, q);
                        }
                    } else {
                        nodesToAddAll.add(tempNode.loChild);
                        nodesToAddAll.add(tempNode.hiChild);
                    }
                }
            }
        }

        double scaledDensity;
        if (ignoreSelf) {
            scaledDensity = totalDensity / (numPoints - 1);
        } else {
            scaledDensity = totalDensity / numPoints;
        }
        return scaledDensity;
    }

    @Override
    public int getNumKernels() {
        return numKernels;
    }
}
