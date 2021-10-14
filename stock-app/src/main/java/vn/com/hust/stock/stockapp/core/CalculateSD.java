package vn.com.hust.stock.stockapp.core;

import java.util.List;
import java.util.concurrent.Callable;

public class CalculateSD implements Callable<Double> {
    private final int DAY;
    private final List<Double> numArray;
    private final  int start;

    public CalculateSD(int DAY, List<Double> numArray, int start) {
        this.DAY = DAY;
        this.numArray = numArray;
        this.start = start;
    }

    @Override
    public Double call() throws Exception {

        double sum = 0.0, standardDeviation = 0.0;
        int length = DAY;

        for(int j=0;j<DAY;j++) {
            sum += numArray.get(j+start);
        }

        double mean = sum/length;
        for(int j=0;j<DAY;j++) {
            standardDeviation += Math.pow(numArray.get(j+start) - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }
}
