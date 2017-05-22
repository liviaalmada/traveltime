package br.ufc.arida.analysis.model.measures;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
// TODO validation
public class NormalizedLpMetric implements DistanceMeasure<ProbabilisticCost> {

	private int p;
	private int size = 100;

	public NormalizedLpMetric(int p) {
		this.p = p;
	}

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2){// throws CostNotFoundException {
		double sum = 0, integral1 = 0, integral2 = 0;
		double[] density1=new double[size+1], density2= new double[size+1];
		for (int i = 0; i <= size; i++) {
			density1[i] = m1.density(i);
			density2[i] = m2.density(i);
			integral1 += Math.pow(density1[i],   p);
			integral2 += Math.pow(density2[i],   p);
		}
		
		integral1 = Math.pow(integral1, (1. / (double) p));
		integral2 = Math.pow(integral1, (1. / (double) p));

		for (int i = 0; i <= size; i++) {
			sum += Math.pow(Math.abs(density1[i]/integral1 - density2[i]/integral2), p);
		}
		return sum;

	}

}
