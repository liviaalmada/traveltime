package br.ufc.arida.analysis.algortihms;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.graphast.geometry.Point;
import org.graphast.model.Edge;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.NetTrafficDistance;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.model.measures.DistanceMeasure;
import it.unimi.dsi.fastutil.booleans.BooleanBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongList;

public class DBScan {

	private ProbabilisticGraph graph;
	private BooleanBigArrayBigList visited;
	private BooleanBigArrayBigList noise;
	private IntBigArrayBigList inCluster;
	private DistanceMeasure<ProbabilisticCost> distance;
	private int nextCluster = 0;
	private List<Long> cluster;
	private List<Long> noises;
	private Map<Integer, List<Long>> clusters;
	private int minPts;

	public DBScan(ProbabilisticGraph g, DistanceMeasure<ProbabilisticCost> distance) {
		this.graph = g;
		this.distance = distance;
		clusters = new HashMap<>();
	}

	public Map<Integer, List<Long>> run(double epsSim, double epsNet, int minPts, int time) {
		long graphSize = graph.getNumberOfEdges();
		clusters.clear();
		this.minPts = minPts;
		inCluster = new IntBigArrayBigList(graphSize);
		visited = new BooleanBigArrayBigList(graphSize);
		noise = new BooleanBigArrayBigList(graphSize);
		noises = new ArrayList<>();

		for (int id = 0; id < graphSize; id++) {
			visited.add(false);
			noise.add(false);
			inCluster.add(nextCluster);
		}
		nextCluster++;
		for (long id = 0; id < graphSize; id++) {
			if (inCluster.get(id) != 0)
				continue;

			Edge edge = graph.getEdge(id);

			// if(graph.getProbabilisticCosts(edge.getId(), time)==null)
			// continue;

			cluster = new ArrayList<>();
			if (expandCluster(edge, epsSim, epsNet, minPts, time)) {
				clusters.put(nextCluster, cluster);
				nextCluster++;
			}
		}
		for (long i = 0; i < noise.size64(); i++) {
			if (inCluster.get(i) == 0)
				noises.add(i);
		}
		clusters.put(0, noises);
		return clusters;
	}

	private String getIdsAsString(List<Edge> neighborsPts) {
		String pts = " ";
		for (Edge edge : neighborsPts) {
			pts += edge.getId() + " ";
		}
		return pts;
	}

	private boolean expandCluster(Edge edge, double epsSim, double epsNet, int minPoints, int time) {
		List<Edge> seeds = disconnectedRegionQuery(edge, epsSim, epsNet, time);

		if (seeds.size() < minPoints) {
			noise.set(edge.getId().longValue(), true);
			return false;
		}

		inCluster.set(edge.getId().longValue(), nextCluster);
		cluster.add(edge.getId());
		setAllSeedsCluster(seeds);
		while (!seeds.isEmpty()) {
			Edge current = seeds.get(0);
			seeds.remove(0);
			List<Edge> result = disconnectedRegionQuery(current, epsSim, epsNet, time);

			if (result.size() >= minPoints) {
				for (int i = 0; i < result.size(); i++) {
					Edge resultP = result.get(i);
					if (inCluster.get(resultP.getId().longValue()) == 0) {
						seeds.add(resultP);
						noise.set(resultP.getId().longValue(), false);
						inCluster.set(resultP.getId().longValue(), nextCluster);
						cluster.add(resultP.getId());
					}

				}
			}
		}
		return true;

		// expandCluster(id, newPts, epsSim, minPts, time);

	}

	private void setAllSeedsCluster(List<Edge> seeds) {
		for (Edge edge : seeds) {
			if (inCluster.get(edge.getId()) == 0) {
				inCluster.set(edge.getId().longValue(), nextCluster);
				noise.set(edge.getId().longValue(), false);
				cluster.add(edge.getId());
			}
		}
	}

	// TODO REFAZER PARA RETORNAR APENAS REGIOES CONEXAS => TALVEZ NÃO FAÇA
	// SENTIDO PQ OS DADOS SÃO ESPARSOS
	public List<Edge> connectedRegionQuery(Edge edge, double epsSim, double epsNet, int time) {
		List<Edge> region = new ArrayList<Edge>();
		ProbabilisticCost costs = graph.getProbabilisticCosts(edge.getId(), time);
		List<Edge> neighbors = new ArrayList<>();
		Set<Long> visitedNeighbors = new HashSet<>();
		visitedNeighbors.add(edge.getId());
		getEdgesInNeighborhood(edge, epsNet, neighbors, visitedNeighbors, time);

		for (int i = 0; i < neighbors.size(); i++) {
			Edge edgeNeigh = neighbors.get(i);
			ProbabilisticCost outCosts = graph.getProbabilisticCosts(edgeNeigh.getId(), time);

			try {
				double d = distance.calculate(costs, outCosts);
				if (d < epsSim)
					region.add(edgeNeigh);
				// System.out.println("distance: " + d);

			} catch (Exception e) {
				visited.set(edgeNeigh.getId().longValue(), true);
				e.printStackTrace();
			}
		}

		return region;
	}

	public List<Edge> disconnectedRegionQuery(Edge edge, double epsSim, double epsNet, int time) {
		List<Edge> region = new ArrayList<Edge>();
		ProbabilisticCost costs = graph.getProbabilisticCosts(edge.getId(), time);
		List<Edge> neighbors = new ArrayList<>();
		Set<Long> visitedNeighbors = new HashSet<>();
		visitedNeighbors.add(edge.getId());
		getEdgesInNeighborhood(edge, epsNet, neighbors, visitedNeighbors, time);

		for (int i = 0; i < neighbors.size(); i++) {
			Edge edgeNeigh = neighbors.get(i);
			ProbabilisticCost outCosts = graph.getProbabilisticCosts(edgeNeigh.getId(), time);
			try {
				double d = distance.calculate(costs, outCosts);
				if (outCosts == null || d < epsSim) {
					region.add(edgeNeigh);
				}

				// System.out.println("distance: " + d);

			} catch (Exception e) {
				visited.set(edgeNeigh.getId().longValue(), true);
				// e.printStackTrace();
			}
		}

		return region;

	}

	private List<Edge> getEdgesInNeighborhood(Edge originEdge, double epsNet, List<Edge> neighbors,
			Set<Long> visitedNeighbors, int time) {
		if (epsNet == 0)
			return neighbors;
		LongList nodesNeig = graph.getOutNeighbors(graph.getEdge(originEdge.getId()).getToNode());
		for (Long node : nodesNeig) {
			Edge neigh = graph.getEdge(originEdge.getToNode(), node);
			if (inCluster.get(neigh.getId()) == 0 && !visitedNeighbors.contains(neigh.getId())) {
				neighbors.add(neigh);
				visitedNeighbors.add(neigh.getId());
				if(distance instanceof NetTrafficDistance){
					((NetTrafficDistance) distance).putNetDistance(originEdge.getId(), neigh.getId(), epsNet);
				}
				getEdgesInNeighborhood(neigh, epsNet - 1, neighbors, visitedNeighbors, time);
			}
		}

		nodesNeig = graph.getInNeighbors(graph.getEdge(originEdge.getId()).getFromNode());
		for (Long node : nodesNeig) {
			Edge neigh = graph.getEdge(node, originEdge.getFromNode());
			if (inCluster.get(neigh.getId()) == 0 && !visitedNeighbors.contains(neigh.getId())) {
				neighbors.add(neigh);
				visitedNeighbors.add(neigh.getId());
				if(distance instanceof NetTrafficDistance){
					((NetTrafficDistance) distance).putNetDistance(originEdge.getId(), neigh.getId(), epsNet);
				}
				getEdgesInNeighborhood(neigh, epsNet - 1, neighbors, visitedNeighbors, time);
			}
		}

		return neighbors;
	}

	private List<Edge> getEdgesInNeighborhood(Edge edge) {
		List<Edge> neighbors = new ArrayList<>();
		LongList nodesNeig = graph.getOutNeighbors(graph.getEdge(edge.getId()).getToNode());
		for (Long node : nodesNeig) {
			neighbors.add(graph.getEdge(edge.getToNode(), node));
		}
		// System.out.println("region: " + edge.getId() + " out " +
		// getIdsAsString(neighbors));
		nodesNeig = graph.getInNeighbors(graph.getEdge(edge.getId()).getFromNode());
		for (Long node : nodesNeig) {
			neighbors.add(graph.getEdge(node, edge.getFromNode()));
		}
		// System.out.println("region: " + edge.getId() + " in and out " +
		// getIdsAsString(neighbors));
		return neighbors;
	}

	public void runAndSave(String filePath, double epsSim, double epsNet, int minPts, int time) throws IOException {
		FileWriter writer = new FileWriter(filePath);
		PrintWriter gravarArq = new PrintWriter(writer);
		Map<Integer, List<Long>> map = this.run(epsSim, epsNet, minPts, time);
		for (Entry<Integer, List<Long>> e : map.entrySet()) {
			for (Long edge : e.getValue()) {
				List<Point> geometry = this.graph.getEdge(edge.longValue()).getGeometry();
				String edgeStr = "LINESTRING(" + geometry.get(0).getLongitude() + " " + geometry.get(0).getLatitude()
						+ ", " + geometry.get(geometry.size() - 1).getLongitude() + " "
						+ geometry.get(geometry.size() - 1).getLatitude() + ");";
				gravarArq.println(
						e.getKey() + " ;" + edge + "; " + edgeStr + " ;" + graph.getProbabilisticCosts(edge, time));
			}
		}
		writer.close();
	}

}
