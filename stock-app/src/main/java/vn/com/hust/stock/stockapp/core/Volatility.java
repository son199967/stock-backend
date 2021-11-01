package vn.com.hust.stock.stockapp.core;

import java.util.Objects;
import java.util.function.IntPredicate;

public class Volatility {
    private final double stdDeviation;
    private final double semiDeviation;

    public Volatility(double[] returns, IntPredicate filter)
    {
        Objects.requireNonNull(returns);

        double tempStandard = 0;
        double tempSemi = 0;
        int count = 0;

        double averageLogReturn = logAverage(returns, filter);

        for (int ii = 0; ii < returns.length; ii++)
        {
            if (!filter.test(ii))
                continue;

            double logReturn = Math.log(1 + returns[ii]);
            double add = Math.pow(logReturn - averageLogReturn, 2);

            tempStandard = tempStandard + add;
            count++;

            if (logReturn < averageLogReturn)
                tempSemi = tempSemi + add;
        }

        if (count <= 1)
        {
            stdDeviation = 0d;
            semiDeviation = 0d;
        }
        else
        {
            stdDeviation = Math.sqrt(tempStandard / (count - 1) * count);
            semiDeviation = Math.sqrt(tempSemi / (count - 1) * count);
        }
    }

    private double logAverage(double[] returns, IntPredicate filter)
    {
        double sum = 0;
        int count = 0;

        for (int ii = 0; ii < returns.length; ii++)
        {
            if (!filter.test(ii))
                continue;

            sum += Math.log(1 + returns[ii]);
            count++;
        }

        if (count == 0)
            return 0;

        return sum / count;
    }

    public double getStandardDeviation()
    {
        return stdDeviation;
    }

    public double getSemiDeviation()
    {
        return semiDeviation;
    }

    public double getExpectedSemiDeviation()
    {
        return stdDeviation / Math.sqrt(2);
    }

    public String getNormalizedSemiDeviationComparison()
    {
        double expectedSemiDeviation = getExpectedSemiDeviation();
        if (expectedSemiDeviation > semiDeviation)
            return ">"; //$NON-NLS-1$
        else if (expectedSemiDeviation < semiDeviation)
            return "<"; //$NON-NLS-1$
        return "="; //$NON-NLS-1$
    }
}
