package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;

public class MixtureModelParser implements ICostParser {

	@Override
	public void save(ProbabilisticCost probabilisticCost, String path) {
		if(probabilisticCost instanceof MixtureModelCost){
			MixtureModel.save(((MixtureModelCost)probabilisticCost).mm, path);
		}
	}

	@Override
	public ProbabilisticCost load(String path) {
		return new MixtureModelCost(MixtureModel.load(path));
	}

}
