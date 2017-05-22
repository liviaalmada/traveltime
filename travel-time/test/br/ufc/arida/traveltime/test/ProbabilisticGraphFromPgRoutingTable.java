package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.cost.MixtureModelParser;
import br.ufc.arida.dao.PGRountingGraphDAO;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class ProbabilisticGraphFromPgRoutingTable {
	public static void main(String[] args) {
		try {
			PGRountingGraphDAO.readFromDBAndSave("graph");
			ProbabilisticGraph graph = new ProbabilisticGraph("graph", new GaussianParser());
			graph.load();
			System.out.println(graph.getNumberOfNodes());
			System.out.println(graph.getNumberOfEdges());
			
			long edges = graph.getNumberOfEdges();
			for (int id = 0; id < edges; id++) {
				System.out.println(graph.getEdge(id));
			}
			
			ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO();
			
			dao.addTimeDependentGaussianCost((ProbabilisticGraph) graph);
			graph.setDirectory("graph-prob");
			for (int id = 0; id < edges; id++) {
				System.out.println("COST" + graph.getProbabilisticCosts(id));
			}
			graph.save();
			
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
