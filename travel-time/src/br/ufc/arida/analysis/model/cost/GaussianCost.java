package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;

public class GaussianCost implements ProbabilisticCost {
	MixtureModel mm;

	public GaussianCost(double mean, double sigma) {
		MixtureModel mm = new MixtureModel(1);
		PVector param = new PVector(2);
		mm.EF = new UnivariateGaussian();
		param.array[0] = mean;
		param.array[1] = sigma;
		mm.param[0] = param;
		mm.weight[0] = 1.0;
	}
	
	public GaussianCost(MixtureModel mm) throws Exception {
		if(mm.size == 1 && mm.EF instanceof UnivariateGaussian){
			this.mm = mm;
		}else{
			throw new Exception("It is not a univarite gaussian.");
		}
	}

	@Override
	public double density(double x) {
		PVector param = new PVector(1);
		param.array[0] = x;
		return mm.density(param);
	}

	@Override
	public String toString() {
		return "GaussianCost [" + mm.toString() + "]";
	}
}
