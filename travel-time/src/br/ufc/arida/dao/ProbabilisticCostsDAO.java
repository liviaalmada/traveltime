package br.ufc.arida.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.graphast.util.ProbabilisticEdgesUtils;

import com.jmef.MixtureModel;

import br.ufc.arida.analysis.model.CostTimeSeries;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianCost;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.utils.CostClustering;
import it.unimi.dsi.fastutil.longs.LongList;

public class ProbabilisticCostsDAO {

	private int countEmptyRoads;
	private HashMap<Long, Long> mapNodes;

	public ProbabilisticCostsDAO() {
		// TODO Auto-generated constructor stub
	}

	public ProbabilisticCostsDAO(String pathStr) {
		Path path = Paths.get(pathStr);
		mapNodes = new HashMap<>();
		try {
			List<String> readAllLines = Files.readAllLines(path);
			for (String line : readAllLines) {
				String[] mapStr = line.split(",");
				mapNodes.put(Long.valueOf(mapStr[0]), Long.valueOf(mapStr[1]));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
	public void addSparseProbabilisticCosts(ProbabilisticGraph graph)
			throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		Map<Integer, CostTimeSeries> edgesToCosts = new HashMap<>();
		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			CostTimeSeries costTimeSeries = getTravelCostTimeSeries(get(id, graph));
			edgesToCosts.put(id, costTimeSeries);
			// Union of all costs in the edge (non time-dependent)
			List<Double> allCosts = costTimeSeries.getUnionOfTravelTimeCosts();
			System.out.println("Edge id " + id);
			if (allCosts.size() > 10) {
				// Generate non time-dependent gaussian mixture models
				MixtureModel gmm = ProbabilisticEdgesUtils
						.produceMixtureBregmanSoft(CostClustering.getListAsPVectorArray(allCosts), 1);
				// ProbabilisticEdgesUtils.produceMixtureKFold(CostClustering.getListAsPVectorArray(allCosts),
				// 1);
				System.out.println(gmm);
				if (gmm != null) {
					// generate temporal gaussian mixture models
					ProbabilisticCost[] temporalGMM = CostClustering.temporalGMM(costTimeSeries, gmm);
					graph.addProbabilisticCost(id, temporalGMM);

				}

			}

		}

		// countEmptyRoads = 0;
		// do {
		// for (long id = 0; id < edges; id++) {
		//
		// fillCosts(graph, id,
		// graph.getOutEdges(graph.getEdge(id).getToNode()));
		// if (graph.getProbabilisticCosts(id) == null)
		// countEmptyRoads++;
		// else if (countEmptyRoads > 0)
		// countEmptyRoads--;
		//
		// }
		// System.out.println("sem custos " + countEmptyRoads);
		// } while (countEmptyRoads > 0);
	}

	public void addTimeDependentGaussianCost(ProbabilisticGraph graph)
			throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		edges = 100;
		Map<Integer, CostTimeSeries> edgesToCosts = new HashMap<>();

		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			CostTimeSeries costTimeSeries = getTravelCostTimeSeries(get(id, graph));
			edgesToCosts.put(id, costTimeSeries);
			// double[] values =
			// costTimeSeries.getUnionOfTravelTimeCostsAsArray();
			ProbabilisticCost[] temporalCosts = new GaussianCost[graph.getNumberOfIntervals()];
			for (int i = 0; i < graph.getNumberOfIntervals(); i++) {
				double[] values = costTimeSeries.getTravelCostSet(i).getCostsArray();
				if (values != null && values.length > 0) {
					// Generate non time-dependent gaussian mixture models
					double mean = StatUtils.mean(values);
					double std = FastMath.sqrt(StatUtils.variance(values));
					if (std == 0)
						std = Math.min(mean, 5);
					GaussianCost gaussian = new GaussianCost(mean, std);
					temporalCosts[i] = gaussian;
					// graph.addProbabilisticCost(id, i, gaussian);
					// System.out.println(graph.getProbabilisticCosts(id, i));

				}

			}
			graph.addProbabilisticCost(id, temporalCosts);

		}
		printCosts(graph, 55);
		// fillCosts(graph,55);
	}

	public void addGaussianCost(ProbabilisticGraph graph) throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		edges = 100;
		Map<Integer, CostTimeSeries> edgesToCosts = new HashMap<>();

		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			CostTimeSeries costTimeSeries = getTravelCostTimeSeries(get(id, graph));
			edgesToCosts.put(id, costTimeSeries);
			graph.setNumberOfIntervals(1);
			double[] values = costTimeSeries.getUnionOfTravelTimeCostsAsArray();
			if (values != null && values.length > 0) {
				// Generate non time-dependent gaussian mixture models
				double mean = StatUtils.mean(values);
				double std = FastMath.sqrt(StatUtils.variance(values));
				if (std == 0)
					std = Math.min(mean, 1);
				GaussianCost gaussian = new GaussianCost(mean, std);
				ProbabilisticCost[] costs = new ProbabilisticCost[1];
				costs[0] = gaussian;
				graph.addProbabilisticCost(id, costs);
				// graph.addProbabilisticCost(id, i, gaussian);
				// System.out.println(graph.getProbabilisticCosts(id, i));

			}

		}
		printCosts(graph, 0);
		// fillCosts(graph,55);
	}

	public void printCosts(ProbabilisticGraph graph, int i) {
		for (int j = 0; j < graph.getNumberOfEdges(); j++) {
			ProbabilisticCost probabilisticCosts = graph.getProbabilisticCosts(j, i);
			if (probabilisticCosts != null)
				System.out.println(probabilisticCosts);
		}
	}

	public void fillCosts(ProbabilisticGraph graph, int time) {
		int conta = 0;
		long edges = graph.getNumberOfEdges();
		do {
			for (int t = time; t <= time; t++) {
				for (long id = 0; id < edges; id++) {
					fillCosts(graph, id, t, graph.getOutEdges(graph.getEdge(id).getToNode()));
					fillCosts(graph, id, t, graph.getInEdges(graph.getEdge(id).getFromNode()));
					if (graph.getProbabilisticCosts(id, t) == null)
						conta++;
					else if (conta > 0)
						conta--;
					System.out.println("sem custos " + conta);
				}
				System.out.println("sem custos " + conta);
			}
		} while (conta > 0);
	}

	private void fillCosts(ProbabilisticGraph graph, long edgeId, int time, LongList outEdges) {
		if (graph.getProbabilisticCosts(edgeId) == null) {
			countEmptyRoads++;
			System.out.println("sem custos " + countEmptyRoads);
			for (Long outId : outEdges) {
				if (graph.getProbabilisticCosts(outId, time) != null) {
					graph.addProbabilisticCost(edgeId, time, graph.getProbabilisticCosts(outId, time));
					return;
				}
			}
		}
	}

	/*
	 * Given id of graphast get the id of graph-hopper
	 */
	private Long get(long id, ProbabilisticGraph graph) {

		if (mapNodes != null) {
			return mapNodes.get(id);
		}
		return id;
	}

	// TODO: put this method in other dao
	/*
	 * Get a set of time series of speed (m/s) observations on the edge
	 */
	public CostTimeSeries getTravelCostTimeSeries(long edgeId)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection
				.prepareStatement("SELECT * FROM  compact_time_series_sintetic WHERE EDGE_ID = ?");

		prepareStatement.setLong(1, edgeId);

		ResultSet resultSet = prepareStatement.executeQuery();
		CostTimeSeries timeSeries = new CostTimeSeries(edgeId, 96);

		while (resultSet.next()) {
			double cost = resultSet.getDouble("avg_speed_ms");
			int interval = resultSet.getInt("time_interval");
			timeSeries.addTravelCost(cost, interval);
		}

		return timeSeries;
	}

}
