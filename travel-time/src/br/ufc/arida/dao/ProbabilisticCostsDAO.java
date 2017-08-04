package br.ufc.arida.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.graphast.model.Edge;
import org.graphast.util.ProbabilisticEdgesUtils;

import com.jmef.MixtureModel;
import com.jmef.PVector;

import br.ufc.arida.analysis.model.CostSet;
import br.ufc.arida.analysis.model.CostTimeSeries;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianCost;
import br.ufc.arida.analysis.model.cost.MixtureModelCost;
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

			if (allCosts.size() > 10) {
				// Generate non time-dependent gaussian mixture models
				MixtureModel gmm = ProbabilisticEdgesUtils
						.produceMixtureKFold(CostClustering.getListAsPVectorArray(allCosts), 2);
				if (gmm != null) {
					// generate temporal gaussian mixture models
					ProbabilisticCost[] temporalGMM = CostClustering.temporalGMM(costTimeSeries, gmm);
					graph.addProbabilisticCost(id, temporalGMM);

				}

			}

		}

	}

	@Deprecated
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
	}

	/**
	 * Attribute a Gaussian cost to edges in each interval time
	 * 
	 * @param graph
	 * @param fill
	 * @param numIntervals
	 *            number of intervals that the day is divided equally
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addGaussianCost(ProbabilisticGraph graph, boolean fill, int numIntervals)
			throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		graph.setNumberOfIntervals(numIntervals);
		int count = 0;
		Map<Long, CostTimeSeries> map = getTravelCostTimeSeries(numIntervals);
		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			Long dbId = get(id, graph);
			CostTimeSeries costTimeSeries = map.get(dbId);
			if (costTimeSeries == null) {
				costTimeSeries = new CostTimeSeries(id, numIntervals);
			}
			graph.setNumberOfIntervals(numIntervals);
			int interval = 0;
			for (interval = 0; interval < numIntervals; interval++) {
				double[] values = costTimeSeries.getTravelCostSet(interval).getCostsArray();
				if (values != null && values.length > 0) {
					count++;
					// Generate non time-dependent gaussian mixture models
					double mean = StatUtils.mean(values);
					double std = FastMath.sqrt(StatUtils.variance(values));
					if (std == 0)
						std = 0.5;
					GaussianCost gaussian = new GaussianCost(mean, std);
					graph.addProbabilisticCost(id, interval, gaussian);

				}
			}

		}
		printCosts(graph, 0);
		if (fill)
			fillCosts(graph, 0);
		System.out.println(count);
	}

	/**
	 * Attribute a Gaussian cost to edges in each interval time
	 * 
	 * @param graph
	 * @param fill
	 * @param numIntervals
	 *            number of intervals that the day is divided equally
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addMixtureGaussianCost(ProbabilisticGraph graph, boolean fill, int numIntervals, int k)
			throws ClassNotFoundException, SQLException, IOException {
		long edges = graph.getNumberOfEdges();
		Map<Long, CostTimeSeries> map = getTravelCostTimeSeries(numIntervals);
		for (int id = 0; id < edges; id++) {
			// Get time series of observations in the edge
			Long dbId = get(id, graph);
			CostTimeSeries costTimeSeries = map.get(dbId);
			if (costTimeSeries == null) {
				costTimeSeries = new CostTimeSeries(id, numIntervals);
			}
			graph.setNumberOfIntervals(numIntervals);
			int interval = 0;
			for (interval = 0; interval < numIntervals; interval++) {
				CostSet values = costTimeSeries.getTravelCostSet(interval);
				if(values!=null && values.size()>k){
					PVector[] pValues = CostClustering.getListAsPVectorArray(values.getCosts());
					MixtureModel gmm = ProbabilisticEdgesUtils
							.produceMixtureKFold(pValues, k);
					System.out.println(gmm);
					if (gmm != null) {
						graph.addProbabilisticCost(id, interval, new MixtureModelCost(gmm));
						System.out.println(gmm);
					}
				}
			}
		}
	}

	public void printCosts(ProbabilisticGraph graph, int i) {
		for (int j = 0; j < graph.getNumberOfEdges(); j++) {
			ProbabilisticCost probabilisticCosts = graph.getProbabilisticCosts(j, i);
			if (probabilisticCosts != null)
				System.out.println(probabilisticCosts);
		}
	}

	public void fillCosts(ProbabilisticGraph graph, int time) {
		long edges = graph.getNumberOfEdges();
		List<Long> edgesToFill = new ArrayList<>();

		for (long id = 0; id < edges; id++) {
			ProbabilisticCost costs = graph.getProbabilisticCosts(id, time);
			if (costs == null)
				edgesToFill.add(id);
		}
		int count = edgesToFill.size();
		int rounds = 100;
		System.out.println(count);
		while (count > 0 && rounds > 0) {
			for (Long id : edgesToFill) {
				LongList neighboors = getNeighboors(graph, id);
				count = fillCosts(graph, id, time, neighboors, count);
			}
			edgesToFill = new ArrayList<>();

			for (long id = 0; id < edges; id++) {
				ProbabilisticCost costs = graph.getProbabilisticCosts(id, time);
				if (costs == null)
					edgesToFill.add(id);
			}

			System.out.println(rounds);
			System.out.println(count);
			rounds--;
		}

		System.out.println(count);
	}

	private LongList getNeighboors(ProbabilisticGraph graph, Long id) {
		LongList neighboors = graph.getOutEdges(graph.getEdge(id).getToNode());
		neighboors.addAll(graph.getInEdges(graph.getEdge(id).getFromNode()));
		return neighboors;
	}

	private int fillCosts(ProbabilisticGraph graph, long edgeId, int time, LongList neigboors, int count) {
		for (Long neighboor : neigboors) {
			ProbabilisticCost costs = graph.getProbabilisticCosts(neighboor, time);
			if (costs != null) {
				graph.addProbabilisticCost(edgeId, time, costs);
				count--;
				return count;
			}
		}
		return count;
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
				.prepareStatement("SELECT * FROM  compact_time_series_turnos WHERE EDGE_ID = ?");

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

	// TODO: put this method in other dao
	/*
	 * Get a set of time series of speed (km/h) observations on the edge
	 */
	public Map<Long, CostTimeSeries> getTravelCostTimeSeries(int numIntervals)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(
				"SELECT * FROM  compact_time_series_turnos  where avg_speed_ms < ? order by edge_id, time_interval");
		prepareStatement.setDouble(1, 120);
		ResultSet resultSet = prepareStatement.executeQuery();
		Map<Long, CostTimeSeries> map = new HashMap<>();
		CostTimeSeries timeSeries = null;
		while (resultSet.next()) {

			Long edge = resultSet.getLong("edge_id");

			if (!map.containsKey(edge)) {
				timeSeries = new CostTimeSeries(edge, numIntervals);
				map.put(edge, timeSeries);
			}

			double cost = resultSet.getDouble("avg_speed_ms");
			int interval = resultSet.getInt("time_interval");
			timeSeries.addTravelCost(cost, interval);
		}

		return map;
	}
}
