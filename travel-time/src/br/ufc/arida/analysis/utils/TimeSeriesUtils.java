package br.ufc.arida.analysis.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.ufc.arida.analysis.timeseries.model.CostTimeSeries;

public class TimeSeriesUtils {

	/**
	 * This method calculates Uncertain Moving Average measure between two time
	 * series. This measure was proposed in by Dallachiesa, Michele, et al. on
	 * the paper "Uncertain time-series similarity: return to the basics." in
	 * Proceedings of the VLDB Endowment 5.11 (2012): 1662-1673.
	 * 
	 * @return UMA distance between ts1 and ts2
	 * @throws Exception
	 */
	public static double uma(CostTimeSeries ts1, CostTimeSeries ts2, int windowSize) throws Exception {
		if (ts1.getNumberOfIntervals() == ts2.getNumberOfIntervals()) {
			return euclideanDistance(UMAFilter(ts1, windowSize), UMAFilter(ts2, windowSize));
		}
		throw new Exception("Undefined measure for diferents sizes of series!");
	}

	/**
	 * This method calculates Uncertain Exponential Moving Average measure
	 * between two time series. This measure was proposed in by Dallachiesa,
	 * Michele, et al. on the paper "Uncertain time-series similarity: return to
	 * the basics." in Proceedings of the VLDB Endowment 5.11 (2012): 1662-1673.
	 * 
	 * @return UEMA distance between ts1 and ts2
	 * @throws Exception
	 */
	public static double uema(CostTimeSeries ts1, CostTimeSeries ts2, int windowSize, double lambda) throws Exception {
		if (ts1.getNumberOfIntervals() == ts2.getNumberOfIntervals()) {
			return euclideanDistance(UEMAFilter(ts1, windowSize, lambda), UEMAFilter(ts2, windowSize, lambda));
		}
		throw new Exception("Undefined measure for diferents sizes of series!");
	}

	private static double[] UEMAFilter(CostTimeSeries ts, int windowSize, double lambda) {
		int size = ts.getNumberOfIntervals();

		double[] uemaFilter = new double[size];
		double[] mean = new double[size];
		double[] std = new double[size];
		double denominator = 0.0;

		// calculate mean and standard deviation for all cost sets
		for (int i = 0; i < size; i++) {
			double[] values = ts.arrayOfCosts(i);
			DescriptiveStatistics statistics = new DescriptiveStatistics(values);
			mean[i] = statistics.getMean();
			std[i] = statistics.getStandardDeviation();
		}

		// define value of index i (pmi)
		for (int i = 0; i < size; i++) {
			// lower bound and upper bound of window
			int minIndex = Math.max(0, i - windowSize);
			int maxIndex = Math.min(size - 1, i + windowSize);

			uemaFilter[i] = 0.0;
			denominator = 0.0;
			for (int j = minIndex; i <= maxIndex; i++) {
				uemaFilter[i] += mean[j]*Math.exp(-1.*lambda*Math.abs(j-i))/std[j];
				denominator += Math.exp(-1.*lambda*Math.abs(j-i));
			}
			// exponential average
			uemaFilter[i] = uemaFilter[i]/denominator;
		}
		return uemaFilter;
	}

	private static double euclideanDistance(double[] ts1, double[] ts2) throws Exception {
		if (ts1.length == ts2.length) {
			double d = 0.0;
			for (int i = 0; i < ts2.length; i++) {
				d += Math.pow(ts1[i] - ts2[i], 2);
			}
			return Math.sqrt(d);
		}

		throw new Exception("Undefined measure for diferents sizes of vectors!");

	}

	private static double[] UMAFilter(CostTimeSeries ts, int windowSize) {
		int size = ts.getNumberOfIntervals();

		double[] umaFilter = new double[size];
		double[] mean = new double[size];
		double[] std = new double[size];

		// calculate mean and standard deviation for all cost sets
		for (int i = 0; i < size; i++) {
			double[] values = ts.arrayOfCosts(i);
			DescriptiveStatistics statistics = new DescriptiveStatistics(values);
			mean[i] = statistics.getMean();
			std[i] = statistics.getStandardDeviation();
		}

		// define value of index i (pmi)
		for (int i = 0; i < size; i++) {
			// lower bound and upper bound of window
			int minIndex = Math.max(0, i - windowSize);
			int maxIndex = Math.min(size - 1, i + windowSize);

			umaFilter[i] = 0.0;
			for (int j = minIndex; i <= maxIndex; i++) {
				umaFilter[i] += mean[j] / std[j];
			}
			// mean of values in the window
			umaFilter[i] = umaFilter[i] / (maxIndex - minIndex + 1);
		}
		return umaFilter;
	}

}
