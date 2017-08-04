package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;

public class GaussianCost implements ProbabilisticCost {
	private MixtureModel mm;
	private PVector parameters;

	public GaussianCost(double mean, double sigma) {
		mm = new MixtureModel(1);
		parameters = new PVector(2);
		mm.EF = new UnivariateGaussian();
		parameters.array[0] = mean;
		parameters.array[1] = sigma;
		mm.param[0] = parameters;
		mm.weight[0] = 1.0;
	}
	
	public double getMean() {
		return parameters.array[0] ;
	}
	
	public double getStd() {
		return parameters.array[1];
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
		if(mm!=null)return "" + ((PVector)mm.param[0]).array[0]+";" +((PVector)mm.param[0]).array[1]+ "";
		else return "";
	}


	public double[] getParameters() {
		double[] param = new double[2];
		param[0] = ((PVector)mm.param[0]).array[0];
		param[1] = ((PVector)mm.param[0]).array[1];
		return param;
	}
	
	 
}
