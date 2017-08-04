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
import br.ufc.arida.analysis.model.measures.TrafficComparatorMeasure;
import it.unimi.dsi.fastutil.booleans.BooleanBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongList;

public class LinkScan {

	protected ProbabilisticGraph graph;
	protected BooleanBigArrayBigList visited;
	protected BooleanBigArrayBigList noise;
	protected IntBigArrayBigList inCluster;
	protected TrafficComparatorMeasure<ProbabilisticCost> distance;
	private int nextCluster = 0;
	private List<Long> cluster;
	private Map<Integer, List<Long>> clusters;

	public LinkScan(ProbabilisticGraph g, TrafficComparatorMeasure<ProbabilisticCost> distance) {
		this.graph = g;
		this.distance = distance;
		clusters = new HashMap<>();
	}

	// Runs link scan algorithm, consider the parameters
	public Map<Integer, List<Long>> run(double epsSim, double epsNet, int minPts, int time) {
		// Initialize all data structures
		long graphSize = graph.getNumberOfEdges();
		clusters.clear();
		inCluster = new IntBigArrayBigList(graphSize);
		visited = new BooleanBigArrayBigList(graphSize);
		noise = new BooleanBigArrayBigList(graphSize);
		List<Long> noises = new ArrayList<>();
		// Set all edges as visited and not a noise
		for (int id = 0; id < graphSize; id++) {
			visited.add(false);
			noise.add(false);
			inCluster.add(0);
		}

		// Set the first cluster id
		nextCluster++;
		for (long id = 0; id < graphSize; id++) {
			// If the edge is already in a cluster
			if (inCluster.get(id) != 0)
				continue;

			Edge edge = graph.getEdge(id);
			ProbabilisticCost costs = graph.getProbabilisticCosts(edge.getId(), time);
			if (costs != null) {
				// Start and expand a new cluster
				cluster = new ArrayList<>();
				if (expandCluster(edge, epsSim, epsNet, minPts, time)) {
					clusters.put(nextCluster, cluster);
					nextCluster++;
				}
			}
		}
		for (long i = 0; i < noise.size64(); i++) {
			if (inCluster.get(i) == 0)
				noises.add(i);
		}
		clusters.put(0, noises);
		return clusters;
	}

	private boolean expandCluster(Edge edge, double epsSim, double epsNet, int minPoints, int time) {
		List<Edge> seeds = regionQuery(edge, epsSim, epsNet, time);

		if (seeds.size() < minPoints) {
			noise.set(edge.getId().longValue(), true);
			return false;
		}
		setCluster(edge);
		setAllSeedsCluster(seeds);
		// Verify seeds regions
		while (!seeds.isEmpty()) {
			Edge current = seeds.remove(0);
			if (!visited.get(current.getId())) {
				List<Edge> result = regionQuery(current, epsSim, epsNet, time);
				// Seed is dense
				if (result.size() < minPoints) {
					noise.set(edge.getId().longValue(), true);
					continue;
				}
				for (int i = 0; i < result.size(); i++) {
					Edge resultP = result.get(i);
					if (inCluster.get(resultP.getId().longValue()) == 0) {
						seeds.add(resultP);
						setCluster(resultP);
					}
				}

			}

		}
		return true;

	}

	private void setCluster(Edge edge) {
		noise.set(edge.getId().longValue(), false);
		inCluster.set(edge.getId().longValue(), nextCluster);
		cluster.add(edge.getId());
	}

	private void setAllSeedsCluster(List<Edge> seeds) {
		for (Edge edge : seeds) {
			if (inCluster.get(edge.getId()) == 0) {
				setCluster(edge);
			}
		}
	}

	// Get in and out neighbors until epsNet hops with distance of similarity less
	// than epsSim
	public List<Edge> regionQuery(Edge edge, double epsSim, double epsNet, int time) {
		ProbabilisticCost costs = graph.getProbabilisticCosts(edge.getId(), time);
		List<Edge> region = new ArrayList<>();
		List<Edge> neighbors = new ArrayList<>();
		Set<Long> visitedNeighbors = new HashSet<>();
		visitedNeighbors.add(edge.getId());

		// Evaluate neighborhood in the network and update neighbors list
		getEdgesInNeighborhood(edge, epsNet, neighbors, visitedNeighbors, time);

		// Filter only edges with similarity or no costs
		for (int i = 0; i < neighbors.size(); i++) {
			Edge edgeNeigh = neighbors.get(i);
			ProbabilisticCost nCost = graph.getProbabilisticCosts(edgeNeigh.getId(), time);
			try {
				if (nCost == null) {
					// Reachable edges without costs are included in the cluster
					region.add(edgeNeigh);
				} else {
					double d = distance.calculate(costs, nCost);
					if (d <= epsSim)
						region.add(edgeNeigh);
				}
			} catch (Exception e) {
				visited.set(edgeNeigh.getId().longValue(), true);
			}
		}

		return region;

	}

	protected List<Edge> getEdgesInNeighborhood(Edge originEdge, double epsNet, List<Edge> neighbors,
			Set<Long> visitedNeighbors, int time) {
		if (epsNet == 0)
			return neighbors;
		LongList nodesNeig = graph.getOutNeighbors(graph.getEdge(originEdge.getId()).getToNode());
		for (Long node : nodesNeig) {
			Edge neigh = graph.getEdge(originEdge.getToNode(), node);
			if (inCluster.get(neigh.getId()) == 0 && !visitedNeighbors.contains(neigh.getId())) {
				neighbors.add(neigh);
				visitedNeighbors.add(neigh.getId());
				if (distance instanceof NetTrafficDistance) {
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
				if (distance instanceof NetTrafficDistance) {
					((NetTrafficDistance) distance).putNetDistance(originEdge.getId(), neigh.getId(), epsNet);
				}
				getEdgesInNeighborhood(neigh, epsNet - 1, neighbors, visitedNeighbors, time);
			}
		}

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
				String costStr = null;
				ProbabilisticCost pcost = graph.getProbabilisticCosts(edge, time);
				if (pcost == null) {
					costStr = "-1;-1";
				} else {
					costStr = pcost.toString();
				}

				gravarArq.println(e.getKey() + " ;" + edge + "; " + edgeStr + " ;" + costStr);
			}
		}
		writer.close();
	}

}
