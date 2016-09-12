package macrobase.kde;

import macrobase.kernel.Kernel;

public class DualScore {
    public KDTree query, data;
    public double wMax, wMin;
    public double dataWMax, dataWMin;
    public double queryDataWMax, queryDataWMin;

    public DualScore(Kernel kernel, KDTree data, KDTree query) {
        this.data = data;
        this.query = query;
        double[][] minMaxD = AlgebraUtils.getMinMaxDistanceBetweenBoxes(
                data.getBoundaries(),
                query.getBoundaries()
        );
        this.wMax = kernel.density(minMaxD[0]);
        this.wMin = kernel.density(minMaxD[1]);

        int n = data.getNBelow();
        this.dataWMax = wMax * n;
        this.dataWMin = wMin * n;

        this.queryDataWMax = dataWMax * query.getNBelow();
        this.queryDataWMin = dataWMin * query.getNBelow();
    }

    public boolean isLeaf() {
        return query.isLeaf() && data.isLeaf();
    }

    public DualScore[] split(Kernel kernel) {
        DualScore[] children = null;
        if (!data.isLeaf() && !query.isLeaf()) {
            children = new DualScore[4];
            children[0] = new DualScore(kernel, this.data.getLoChild(), this.query.getLoChild());
            children[1] = new DualScore(kernel, this.data.getLoChild(), this.query.getHiChild());
            children[2] = new DualScore(kernel, this.data.getHiChild(), this.query.getLoChild());
            children[3] = new DualScore(kernel, this.data.getHiChild(), this.query.getHiChild());
        } else if (data.isLeaf() && query.isLeaf()) {
            children = null;
        } else if (data.isLeaf()) {
            children = new DualScore[2];
            children[0] = new DualScore(kernel, this.data, this.query.getLoChild());
            children[1] = new DualScore(kernel, this.data, this.query.getHiChild());
        } else if (query.isLeaf()) {
            children = new DualScore[2];
            children[0] = new DualScore(kernel, this.data.getLoChild(), this.query);
            children[1] = new DualScore(kernel, this.data.getHiChild(), this.query);
        }
        return children;
    }

    @Override
    public String toString() {
        return String.format(
                "[%f, %f] q:%d d:%d dw[%f, %f] w[%f, %f]",
                queryDataWMin,
                queryDataWMax,
                query.getNBelow(),
                data.getNBelow(),
                dataWMin,
                dataWMax,
                wMin,
                wMax
                );
    }
}
