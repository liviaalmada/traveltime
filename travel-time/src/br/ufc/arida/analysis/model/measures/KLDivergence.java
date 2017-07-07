package br.ufc.arida.analysis.model.measures;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class KLDivergence implements TrafficComparatorMeasure<ProbabilisticCost> {
	private static final int size = 160;
	
	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		double kld = 0;
		for (int value = 0; value <= size; value++) {
			double pi = m1.density(value);
			double qi = m2.density(value);
			kld += pi * Math.log(pi/qi);
		}
		return kld;
	}

	@Override
	public double calculate(Long id1, Long id2, ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		return calculate(m1, m2);
	}

}
