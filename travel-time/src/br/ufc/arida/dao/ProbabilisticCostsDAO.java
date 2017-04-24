package br.ufc.arida.analysis.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphast.util.ProbabilisticEdgesUtils;

import com.jmef.MixtureModel;

import br.ufc.arida.analysis.timeseries.model.CostTimeSeries;
import br.ufc.arida.analysis.timeseries.model.ProbabilisticGraph;
import br.ufc.arida.analysis.utils.CostClustering;
import it.unimi.dsi.fastutil.longs.LongList;

public class ProbabilisticCostsDAO {

	/**
	 * This method read time-dependent edges costs from the database and build
	 * the Gaussian Mixture Model for the edges in each time interval. To deal
	 * with sparseness on an edge the method use the approach propose by Yang,
	 * Bin, Chenjuan Guo, and Christian S. Jensen in the paper "Travel cost
	 * inference from sparse, spatio temporally correlated time series using
	 * Markov models." on Proceedings of the VLDB Endowment 6.9 (2013): 769-780.
	 * 
	 * @param graph
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addProbabilisticCosts(ProbabilisticGraph graph)
			throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		Map<Integer, CostTimeSeries> edgesToCosts = new HashMap<>();
		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			CostTimeSeries costTimeSeries = getTravelCostTimeSeries(id);
			edgesToCosts.put(id, costTimeSeries);
			// Union of all costs in the edge (non time-dependent)
			List<Double> allCosts = costTimeSeries.getUnionOfTravelTimeCosts();
			System.out.println("Edge id " + id);
			if (allCosts.size() > 10) {
				// Generate non time-dependent gaussian mixture models
				MixtureModel gmm = ProbabilisticEdgesUtils
						.produceMixtureKFold(CostClustering.getListAsPVectorArray(allCosts), 5);
				System.out.println(gmm);
				if (gmm != null) {
					// generate temporal gaussian mixture models
					MixtureModel[] temporalGMM = CostClustering.temporalGMM(costTimeSeries, gmm);
					graph.addProbabilisticCost(id, temporalGMM);
					// for (int i = 0; i < temporalGMM.length; i++) {
					// System.out.println("On time "+ i );
					// System.out.println(temporalGMM[i]);
					// }

				}

			}

		}

		// TODO percorrer novamente array e verificar quais arestas não
		// tiveram custos atribuídos
		int conta = 0;
		do{
			for (long id = 0; id < edges; id++) {
	
				fillCosts(graph, id, graph.getOutEdges(graph.getEdge(id).getToNode()));
				if (graph.getProbabilisticCosts(id) == null) conta++;
				else if(conta>0)conta--;
				System.out.println("sem custos " + conta);
			}
			System.out.println("sem custos " + conta);
		}while(conta>0);
	}

	// TODO ajeitar esse método
	private void fillCosts(ProbabilisticGraph graph, long id, LongList outEdges) {

		if (graph.getProbabilisticCosts(id) == null) {
			for (Long outId : outEdges) {
				if (graph.getProbabilisticCosts(outId) != null) {
					graph.addProbabilisticCost(id, graph.getProbabilisticCosts(outId));
					return;
				}
			}
			
		}

	}

	// TODO: put this method in other dao
	/*
	 * Get a set of time series of speed (m/s) observations on the edge
	 */
	public CostTimeSeries getTravelCostTimeSeries(int edgeId) throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection
				.prepareStatement("SELECT * FROM  COMPACT_TIME_SERIES WHERE EDGE_ID = ? ORDER BY EDGE_ID");
		PreparedStatement statementEdge = connection
				.prepareStatement("SELECT cost_s, length_m FROM  ROADS_EXPERIMENTS WHERE ID = ?");
		statementEdge.setInt(1, edgeId);
		ResultSet edge = statementEdge.executeQuery();
		double lenght = 0, minCost = 0;
		if (edge.next()) {
			lenght = edge.getDouble(2);
			minCost = edge.getDouble(1);
		}

		prepareStatement.setInt(1, edgeId);

		ResultSet resultSet = prepareStatement.executeQuery();
		CostTimeSeries timeSeries = new CostTimeSeries(edgeId, 96, minCost, lenght);

		while (resultSet.next()) {
			// convert travel time in milliseconds to seconds and then get the
			// speed
			double cost = lenght / (resultSet.getDouble("travel_time") / 1000);
			// System.out.println("Cost " + cost);
			// System.out.println("minCost " + minCost);
			int interval = resultSet.getInt("time_interval");
			timeSeries.addTravelCost(cost, interval);
		}
		// if (timeSeries.getUnionOfTravelTimeCosts().size() == 0) {
		// System.out.println("No costs");
		// } else {
		// System.out.println(timeSeries.getUnionOfTravelTimeCosts().size());
		// }
		return timeSeries;
	}

}
