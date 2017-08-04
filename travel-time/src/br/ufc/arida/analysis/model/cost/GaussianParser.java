package br.ufc.arida.analysis.model.cost;

import com.jmef.MixtureModel;
//TODO
public class GaussianParser implements ICostParser{

	@Override
	public void save(ProbabilisticCost probabilisticCost, String path) throws Exception {
//		if(probabilisticCost instanceof GaussianCost){
//			MixtureModel.save(((GaussianCost)probabilisticCost).mm, path);
//		}else{
//			throw new Exception("Wrong paser.");
//		}
	}

	@Override
	//TODO change exception manipulation
	public ProbabilisticCost load(String path) {
		MixtureModel model = MixtureModel.load(path);
		try {
			return new GaussianCost(model);
		} catch (Exception e) {
			return null;
		}
	}

}
