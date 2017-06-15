package br.ufc.arida.traveltime.test;

import br.ufc.arida.analysis.algortihms.BFSClustering;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class BfsTest2 {
	public static void main(String[] args) {
		
		ProbabilisticGraph graph = new ProbabilisticGraph(
				"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new GaussianParser());
		
		graph.load();
		System.out.println(graph.getNumberOfNodes());
		System.out.println(graph.getNumberOfEdges());

		ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO(
				"/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");

		try {
			dao.addTimeDependentGaussianCost((ProbabilisticGraph) graph);
			
			BFSClustering alg = new BFSClustering();
			alg.doClustering(graph, new CosineDistance(), 49, 0.6);
			alg.saveClusters("testeRealBFS");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
	}
}
