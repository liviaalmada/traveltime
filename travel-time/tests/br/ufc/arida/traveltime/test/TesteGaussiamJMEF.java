package br.ufc.arida.traveltime.test;

import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;

public class TesteGaussiamJMEF {
	public static void main(String[] args) {
		MixtureModel mm = new MixtureModel(1);
		PVector parameters = new PVector(2);
		mm.EF = new UnivariateGaussian();
		parameters.array[0] =  0.108222;
		parameters.array[1] = 0.011712 ;
		mm.param[0] = parameters;
		mm.weight[0] = 1.0;
		PVector v = new PVector(1);
		v.array[0] =  0.146773;
		System.out.println(mm.density(v));
	}
}
