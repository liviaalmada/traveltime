package br.ufc.arida.analysis.model.measures;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class CosineDistance implements DistanceMeasure<ProbabilisticCost> {

	
	private static final int size = 120;

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2){// throws Exception {
		double sum1 = 0, sum2 = 0, sum_prod=0;
		double d1 = 0, d2 = 0;
		if(m1==null ||m2 == null) return 1;
		for (int i = 0; i <= size; i++) {
			d1 = m1.density(i);
			d2 = m2.density(i);
			sum_prod += d1*d2;
			sum1 += d1*d1;
			sum2 += d2*d2;
		}
		double q = (Math.sqrt(sum1)*Math.sqrt(sum2));
		return Math.abs(1-(sum_prod/q));
	}
	
	

}
