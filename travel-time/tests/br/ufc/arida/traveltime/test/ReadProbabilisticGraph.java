package br.ufc.arida.traveltime.test;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.MixtureModelCost;
import br.ufc.arida.analysis.model.cost.MixtureModelParser;

public class ReadProbabilisticGraph {
	public static void main(String[] args) {
		ProbabilisticGraph graph = new ProbabilisticGraph("/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new MixtureModelParser());
		graph.load();
		long edges = graph.getNumberOfEdges();
		
		for (int i = 0; i < edges; i++) {
			MixtureModelCost[] probabilisticCosts = (MixtureModelCost[]) graph.getProbabilisticCosts(i);			
			for (MixtureModelCost mixtureModel : probabilisticCosts) {
				System.out.println(mixtureModel.mm);
			}
		}
		
		
	}
}
