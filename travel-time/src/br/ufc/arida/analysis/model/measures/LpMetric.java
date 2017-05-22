package br.ufc.arida.analysis.model.measures;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class LpMetric implements DistanceMeasure<ProbabilisticCost> {

	private int p;
	public int size = 120;

	public LpMetric(int p) {
		this.p = p;
	}

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) {//throws CostNotFoundException {
		double sum = 0;
		for (double i = 0; i <= size; i++) {
			sum += Math.pow(Math.abs(m1.density(i) - m2.density(i)), (double) p);
		}
		return Math.pow(sum, (1. / (double) p));

	}

}
