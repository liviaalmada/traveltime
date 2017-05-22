package br.ufc.arida.traveltime.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufc.arida.analysis.algortihms.DBScan;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class DBSCanTeste {
	public static void main(String[] args) {
		
		ProbabilisticGraph graph = new ProbabilisticGraph(
				"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new GaussianParser());
		
		graph.load();
		System.out.println(graph.getNumberOfNodes());
		System.out.println(graph.getNumberOfEdges());

		ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO(
				"/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");

		try {
			//dao.addGaussianCost((ProbabilisticGraph) graph);
			SintethicProbabilisticGraph.generateSintethicCosts(graph);
					
			DBScan alg = new DBScan(graph, new CosineDistance());
			alg.runAndSave("clusters2",0.7, 1, 5, 0);
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
	}
}
