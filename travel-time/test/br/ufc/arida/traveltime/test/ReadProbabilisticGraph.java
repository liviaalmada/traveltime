package br.ufc.arida.traveltime.test;

import com.jmef.MixtureModel;

import br.ufc.arida.analysis.model.ProbabilisticGraph;

public class ReadProbabilisticGraph {
	public static void main(String[] args) {
		ProbabilisticGraph graph = new ProbabilisticGraph("graph-prob");
		graph.load();
		long edges = graph.getNumberOfEdges();
		
		for (int i = 0; i < edges; i++) {
			MixtureModel[] probabilisticCosts = graph.getProbabilisticCosts(i);			
			for (MixtureModel mixtureModel : probabilisticCosts) {
				System.out.println(mixtureModel);
			}
		}
		
		
	}
}
