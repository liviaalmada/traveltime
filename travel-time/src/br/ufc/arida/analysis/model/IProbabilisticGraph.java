package br.ufc.arida.analysis.model;

import org.graphast.model.Graph;

import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public interface IProbabilisticGraph extends Graph{

	void setNumberOfIntervals(int n);

	int getNumberOfIntervals();

	ProbabilisticCost getProbabilisticCosts(long edgeId, int timeInterval);

	ProbabilisticCost[] getProbabilisticCosts(long id);

	void addProbabilisticCost(long edgeId, ProbabilisticCost[] costs);

	void loadProbabilisticCosts();

	void saveProbabilisticCosts();

	
}
