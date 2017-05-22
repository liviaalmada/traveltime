package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;

public class MixtureModelParser implements ICostParser {

	@Override
	public void save(ProbabilisticCost probabilisticCost, String path) throws Exception {
		if(probabilisticCost instanceof MixtureModelCost){
			MixtureModel.save(((MixtureModelCost)probabilisticCost).mm, path);
		}else{
			throw new Exception("Wrong paser.");
		}
	}

	@Override
	public ProbabilisticCost load(String path) {
		return new MixtureModelCost(MixtureModel.load(path));
	}

}
