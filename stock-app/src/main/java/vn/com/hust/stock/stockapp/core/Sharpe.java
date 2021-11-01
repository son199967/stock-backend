package vn.com.hust.stock.stockapp.core;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sharpe {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sharpe.class);


    public final double getSharpeRatio(final double [] returns, final double riskFreeReturn) {
        final DescriptiveStatistics stats = new DescriptiveStatistics();
        for(double item : returns) {
            stats.addValue(item);
        }

        final int MONTH_IN_YEAR = 12;

        double mean = stats.getMean();
        double annualizedMean = mean * MONTH_IN_YEAR;

        LOGGER.info("mean={}", mean);
        LOGGER.info("annualMean={}",annualizedMean);

        double std = stats.getStandardDeviation();
        double annualizedStd = std * Math.sqrt( MONTH_IN_YEAR );

        LOGGER.info("std={}",std);
        LOGGER.info("annualStd={}",annualizedStd );

        double sharpeRatio = 0.0;
        sharpeRatio = (annualizedMean - (riskFreeReturn) ) / annualizedStd; // *  unbiasedFactor;

        LOGGER.info("sharpeRatio={}",sharpeRatio);

        LOGGER.info("");
        return sharpeRatio;

    }
}
