package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;
import com.jmef.PVector;

public class MixtureModelCost implements ProbabilisticCost {

	public MixtureModel mm;

	public MixtureModelCost(MixtureModel mm) {
		this.mm = mm;
	}

	@Override
	public String toString() {
		return mm.toString();
	}

	@Override
	public double density(double x) {
		PVector p = new PVector(1);
		p.array[0] = x;
		return mm.density(p);
	}

}
