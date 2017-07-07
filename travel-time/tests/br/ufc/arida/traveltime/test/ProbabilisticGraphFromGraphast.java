package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;

import org.graphast.model.GraphImpl;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.MixtureModelParser;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class ProbabilisticGraphFromGraphast {
	public static void main(String[] args) {
		try {
			GraphImpl graph = new ProbabilisticGraph(
					"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", 
					new MixtureModelParser());
			graph.load();
			System.out.println(graph.getNumberOfNodes());
			System.out.println(graph.getNumberOfEdges());

			ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO("/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");

			dao.addSparseProbabilisticCosts((ProbabilisticGraph) graph);
			//graph.setDirectory("graph-prob");
			//graph.save();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
