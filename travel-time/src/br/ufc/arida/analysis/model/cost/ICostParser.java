package br.ufc.arida.analysis.model.cost;

public interface ICostParser {

	void save(ProbabilisticCost probabilisticCost, String string) throws Exception;

	ProbabilisticCost load(String string);

}
