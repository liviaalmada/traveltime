package br.ufc.arida.analysis.model.measures;

import com.jmef.MixtureModel;
import com.jmef.PVector;

import br.ufc.arida.analysis.model.cost.GaussianCost;
import br.ufc.arida.analysis.model.cost.MixtureModelCost;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class LpMetric implements DistanceMeasure<ProbabilisticCost> {

	private int p;

	public LpMetric(int p) {
		this.p = p;
	}

	public double calculate(MixtureModel pdf1, MixtureModel pdf2, int p) {
		PVector[] points = new PVector[100];
		//PVector[] drawRandomPoints1 = pdf1.drawRandomPoints(50);
		//PVector[] drawRandomPoints2 = pdf2.drawRandomPoints(50);

		for (int i = 0; i < 100; i++) {
			PVector value = new PVector(1);
			value.array[0] = i;
			points[i] = value;
			// System.out.println(points[i]);
		}

		
		double calculate = calculate(points, pdf1, pdf2, p);
		if (calculate > 1) {
			System.out.println("Distance " + calculate);
			for (int i = 0; i < 100; i++) {
				System.out.println(points[i]);
			}
		}

		return calculate;
	}

	private double calculate(PVector[] points, MixtureModel pdf1, MixtureModel pdf2, int p) {
		double sum = 0;
		System.out.println(pdf1);
		System.out.println(pdf2);
		for (int i = 0; i < points.length; i++) {
			//if(Math.abs(pdf1.density(points[i]) - pdf2.density(points[i]))>1){
				System.out.println("PONTO E DENSIDADES: "+points[i]+" "+pdf1.density(points[i]) + " " + pdf2.density(points[i]) );
				System.out.println("DIFERENÇA "+Math.abs(pdf1.density(points[i]) - pdf2.density(points[i])));
				
				
			//}
			sum += Math.pow(Math.abs(pdf1.density(points[i]) - pdf2.density(points[i])), (double) p);
		}
		System.out.println("SOMA"+sum);
		return Math.pow(sum, (1. / (double) p));
	}

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		if (m1 instanceof MixtureModelCost && m2 instanceof MixtureModelCost)
			return calculate(((MixtureModelCost) m1).mm, ((MixtureModelCost) m2).mm, p);

		if (m1 instanceof GaussianCost && m2 instanceof GaussianCost)
			return calculate(((GaussianCost) m1).mm, ((GaussianCost) m2).mm, p);

		throw new Exception("Similarity not defined to" + m1.getClass() + "!");

	}

}
