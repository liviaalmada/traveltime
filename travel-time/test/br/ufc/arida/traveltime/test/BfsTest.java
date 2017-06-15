package br.ufc.arida.traveltime.test;

import br.ufc.arida.analysis.algortihms.BFSClustering;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.measures.CosineDistance;

public class BfsTest {
	public static void main(String[] args) {
		ProbabilisticGraph graph = SintethicProbabilisticGraph.generate(100, 100, 1);
		BFSClustering alg = new BFSClustering();
		alg.doClustering(graph, new CosineDistance(), 0, 0.6);
		alg.saveClusters("testBFS");
//		
//		ProbabilisticGraph graph = new ProbabilisticGraph(
//				"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new GaussianParser());
//		
//		graph.load();
//		System.out.println(graph.getNumberOfNodes());
//		System.out.println(graph.getNumberOfEdges());
//
//		ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO(
//				"/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");
//
//		try {
//			dao.addGaussianCost((ProbabilisticGraph) graph);
//			BFSClustering alg = new BFSClustering();
//			alg.doClustering(graph, new CosineDistance(), 65, 1);
//			alg.saveClusters("testeRealBFS");
//		}catch (Exception e) {
//			// TODO: handle exception
//		}	
	}
}
