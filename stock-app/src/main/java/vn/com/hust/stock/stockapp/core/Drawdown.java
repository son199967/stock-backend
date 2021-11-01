package vn.com.hust.stock.stockapp.core;

import java.time.LocalDate;

public class Drawdown {
    private double maxDD;
    private Interval maxDDDuration;
    private Interval intervalMaxDD;
    private Interval recoveryTime;

    public Drawdown(double[] values, LocalDate[] dates, int startAt)
    {
        if (values.length != dates.length)
            throw new IllegalArgumentException();

        if (startAt >= values.length)
            throw new IllegalArgumentException();

        double peak = values[startAt] + 1;
        double bottom = values[startAt] + 1;
        LocalDate lastPeakDate = dates[startAt];
        LocalDate lastBottomDate = dates[startAt];

        maxDD = 0;
        intervalMaxDD = Interval.of(lastPeakDate, lastPeakDate);
        maxDDDuration = Interval.of(lastPeakDate, lastPeakDate);
        recoveryTime = Interval.of(lastBottomDate, lastPeakDate);
        Interval currentDrawdownDuration = null;
        Interval currentRecoveryTime = null;

        for (int ii = startAt; ii < values.length; ii++)
        {
            double value = values[ii] + 1;
            currentDrawdownDuration = Interval.of(lastPeakDate, dates[ii]);
            currentRecoveryTime = Interval.of(lastBottomDate, dates[ii]);

            if (value > peak)
            {
                peak = value;
                lastPeakDate = dates[ii];

                if (currentDrawdownDuration.isLongerThan(maxDDDuration))
                    maxDDDuration = currentDrawdownDuration;

                if (currentRecoveryTime.isLongerThan(recoveryTime))
                    recoveryTime = currentRecoveryTime;
                // Reset the recovery time calculation, as the recovery is
                // now complete
                lastBottomDate = dates[ii];
                bottom = value;
            }
            else
            {
                double drawdown = (peak - value) / peak;
                if (drawdown > maxDD)
                {
                    maxDD = drawdown;
                    intervalMaxDD = Interval.of(lastPeakDate, dates[ii]);
                }
            }
            if (value < bottom)
            {
                bottom = value;
                lastBottomDate = dates[ii];
            }
        }

        // check if current drawdown duration is longer than the max
        // drawdown duration currently calculated --> use it because it is
        // the longest duration even if we do not know how much longer it
        // will get

        if (currentDrawdownDuration != null && currentDrawdownDuration.isLongerThan(maxDDDuration))
            maxDDDuration = currentDrawdownDuration;

        if (currentRecoveryTime != null && currentRecoveryTime.isLongerThan(recoveryTime))
            recoveryTime = currentRecoveryTime;
    }

    public Interval getLongestRecoveryTime()
    {
        return recoveryTime;
    }

    public double getMaxDrawdown()
    {
        return maxDD;
    }

    public Interval getIntervalOfMaxDrawdown()
    {
        return intervalMaxDD;
    }

    public Interval getMaxDrawdownDuration()
    {
        return maxDDDuration;
    }
}
