package br.ufc.arida.analysis.model.measures;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import br.ufc.arida.analysis.model.cost.GaussianCost;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class KolnogorovSmirnovDistance implements TrafficComparatorMeasure<ProbabilisticCost> {

	private static final int SIZE = 10000;
	
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) throws NullCostException, UndefinedCostException {
		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		if(m1==null || m2 == null) throw new NullCostException();		
		if(m1 instanceof GaussianCost && m2 instanceof GaussianCost) {
			NormalDistribution n1 = new NormalDistribution(((GaussianCost) m1).getMean(),((GaussianCost) m1).getStd());
			NormalDistribution n2 = new NormalDistribution(((GaussianCost) m2).getMean(),((GaussianCost) m2).getStd());
			return test.kolmogorovSmirnovStatistic(n1.sample(SIZE), n2.sample(SIZE));			
		}
		throw new UndefinedCostException("Distance not defined for this type of cost.");
	}

	@Override
	public double calculate(Long id1, Long id2, ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		return calculate(m1, m2);
	}

	
	
	public static void main(String[] args) {
		KolnogorovSmirnovDistance d = new KolnogorovSmirnovDistance();
		ProbabilisticCost g1 = new GaussianCost(17.065050501635028,4.611676377769677);
		ProbabilisticCost g2 = new GaussianCost(11.893263412963583,3.4903004789476233);
		try {
			System.out.println(d.calculate(g1, g2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
