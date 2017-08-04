package br.ufc.arida.analysis.model.measures;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class JeffreysDistance implements TrafficComparatorMeasure<ProbabilisticCost> {
	private static final int size = 100;

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) throws NullCostException {
		double kld = 0;
		for (int value = 0; value <= size; value++) {
			double pi = Math.max(m1.density(value),0.00001 );
			double qi = Math.max(m2.density(value),0.00001 );
			kld += (pi-qi) * Math.log(pi / qi);
		}
		return kld;
	}

	@Override
	public double calculate(Long id1, Long id2, ProbabilisticCost m1, ProbabilisticCost m2) throws NullCostException {
		return calculate(m1, m2);
	}

}
