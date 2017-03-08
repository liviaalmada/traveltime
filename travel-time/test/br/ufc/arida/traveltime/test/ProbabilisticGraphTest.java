package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;

import org.graphast.model.GraphImpl;

import br.ufc.arida.analysis.dao.PGRountingGraphDAO;
import br.ufc.arida.analysis.dao.ProbabilisticCostsDAO;
import br.ufc.arida.analysis.timeseries.model.ProbabilisticGraph;

public class ProbabilisticGraphTest {
	public static void main(String[] args) {
		try {
			PGRountingGraphDAO.readFromDBAndSave("graph");
			GraphImpl graph = new ProbabilisticGraph("graph");
			graph.load();
			System.out.println(graph.getNumberOfNodes());
			System.out.println(graph.getNumberOfEdges());
			
			long edges = graph.getNumberOfEdges();
			for (int id = 0; id < edges; id++) {
				System.out.println(graph.getEdge(id));
			}
			
			ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO();
			
			dao.addProbabilisticCosts((ProbabilisticGraph) graph);
			graph.setDirectory("graph-prob");
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
