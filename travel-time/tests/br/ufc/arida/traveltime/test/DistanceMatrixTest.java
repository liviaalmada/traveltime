package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;

import br.ufc.arida.analysis.algortihms.ProbabilisticDistanceAnalysis;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class DistanceMatrixTest {
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
//			System.out.println("PRINT COSTS...");
//			for (int i = 0; i < graph.getNumberOfEdges(); i++) {
//				for (int j = 0; j < graph.getNumberOfIntervals(); j++) {
//					ProbabilisticCost costs = graph.getProbabilisticCosts(i, j);
//					if (costs != null)
//						System.out.println(graph.getProbabilisticCosts(i, j));
//				}
//
//			}
			ProbabilisticDistanceAnalysis m = new ProbabilisticDistanceAnalysis((ProbabilisticGraph) graph,
					new CosineDistance());
			m.similarityByTime(65);
			m.saveDissimilarityMatrix("matrix-teste");
			m.similarityByTimeAndKNeighbors(2, 65);
			m.saveDissimilarityMatrix("2-neighbors-matrix");
			m.similarityByTimeAndKNeighbors(3, 65);
			m.saveDissimilarityMatrix("3-neighbors-matrix");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
