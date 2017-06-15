package br.ufc.arida.traveltime.test;

import br.ufc.arida.analysis.algortihms.DBScan;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class DBSCanCosineTeste {
	public static void main(String[] args) {

		ProbabilisticGraph graph = new ProbabilisticGraph(
				"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new GaussianParser());
		graph.load();
		System.out.println(graph.getNumberOfNodes());
		System.out.println(graph.getNumberOfEdges());

		ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO(
				"/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");

		try {
			int minPts, EpsNet, numIntervals = 4;
			dao.addGaussianCost((ProbabilisticGraph) graph, false, numIntervals);
			graph.setNumberOfIntervals(numIntervals);
			// SintethicProbabilisticGraph.generateSintethicCosts(graph);

			// DBScan alg = new DBScan(graph, new CosineDistance());
			double EpsSim;

			// String file = "clusters-turnos/clusters" + String.format("%.1f",
			// 0.3) + "-" + 10
			// + "-" + 15 +" - "+2;
			// System.out.println("Processing " + file);
			// alg.runAndSave(file, 0.3, 10, 15, 2);

			for (int interval = 0; interval < numIntervals; interval++) {
				for (minPts = 15; minPts <= 30; minPts += 5) {
					for (EpsSim = 0.2; EpsSim < 1; EpsSim += 0.1) {
						for (EpsNet = 10; EpsNet <= 15; EpsNet += 15) {
							String file = "clusters-turnos/clusters" + String.format("%.1f", EpsSim) + "-" + EpsNet
									+ "-" + minPts + " - " + interval;
							System.out.println("Processing " + file);
							DBScan alg = new DBScan(graph, new CosineDistance());
							alg.runAndSave(file, EpsSim, EpsNet, minPts, interval);
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
