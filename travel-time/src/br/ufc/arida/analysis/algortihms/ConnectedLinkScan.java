package br.ufc.arida.analysis.algortihms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.graphast.model.Edge;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.cost.NetTrafficDistance;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.analysis.model.measures.TrafficComparatorMeasure;
import br.ufc.arida.dao.ProbabilisticCostsDAO;
import it.unimi.dsi.fastutil.longs.LongList;

public class ConnectedLinkScan extends LinkScan {

	private double epsPenalty;

	public ConnectedLinkScan(ProbabilisticGraph g, TrafficComparatorMeasure<ProbabilisticCost> distance,
			double epsPenalty) {
		super(g, distance);
		this.epsPenalty = epsPenalty;
	}

	// TODO REFAZER PARA RETORNAR APENAS REGIOES CONEXAS => TALVEZ NÃO FAÇA
	// SENTIDO PQ OS DADOS SÃO ESPARSOS
	@Override
	public List<Edge> regionQuery(Edge edge, double epsSim, double epsNet, int time) {
		List<Edge> region = new ArrayList<Edge>();
		ProbabilisticCost costs = super.graph.getProbabilisticCosts(edge.getId(), time);
		HashMap<Long, Double> penalty = new HashMap<Long, Double>();
		Set<Long> visitedNeighbors = new HashSet<>();
		visitedNeighbors.add(edge.getId());
		List<Edge> neighbors = new ArrayList<>();
		getEdgesInConnectedNeighborhood(edge, epsNet, neighbors, visitedNeighbors, time, penalty);

		for (Edge neig : neighbors) {
			ProbabilisticCost neigCost = graph.getProbabilisticCosts(neig.getId(), time);
			try {
				if (neigCost != null && costs != null) {
					double d = distance.calculate(costs, neigCost);
					if (d < epsSim) {
						region.add(neig);
					}
				}else{
					region.add(neig);
				}

			} catch (Exception e) {
				visited.set(neig.getId().longValue(), true);
				e.printStackTrace();
			}
		}

		return region;
	}

	private boolean checkPenalty(Edge edge, List<Edge> region, HashMap<Long, Double> penalty, Edge neig, int time) {
		ProbabilisticCost neigCost = graph.getProbabilisticCosts(neig.getId(), time);

		if (neigCost == null) {
			Double edgePenalty = penalty.get(edge.getId());
			if (edgePenalty != null) {
				penalty.put(neig.getId(), edgePenalty + 1);
			} else {
				penalty.put(neig.getId(), 1.);
				edgePenalty = 1.;
			}
			if (edgePenalty + 1. < epsPenalty) {
				return true;
			}
			return false;
		}

		return true;
	}

	protected List<Edge> getEdgesInConnectedNeighborhood(Edge originEdge, double epsNet, List<Edge> neighbors,
			Set<Long> visitedNeighbors, int time, HashMap<Long, Double> penalty) {
		// maximum number of hops is found
		if (epsNet == 0)
			return neighbors;
		LongList nodesNeig = graph.getOutNeighbors(graph.getEdge(originEdge.getId()).getToNode());
		for (Long node : nodesNeig) {
			Edge neigh = graph.getEdge(originEdge.getToNode(), node);
			if (inCluster.get(neigh.getId()) == 0 && !visitedNeighbors.contains(neigh.getId())) {
				if (checkPenalty(originEdge, neighbors, penalty, neigh, time)) {
					neighbors.add(neigh);
					if (distance instanceof NetTrafficDistance) {
						((NetTrafficDistance) distance).putNetDistance(originEdge.getId(), neigh.getId(), epsNet);
					}
					getEdgesInConnectedNeighborhood(neigh, epsNet - 1, neighbors, visitedNeighbors, time, penalty);
				}
				visitedNeighbors.add(neigh.getId());

			}
		}

		nodesNeig = graph.getInNeighbors(graph.getEdge(originEdge.getId()).getFromNode());
		for (Long node : nodesNeig) {
			Edge neigh = graph.getEdge(node, originEdge.getFromNode());
			if (inCluster.get(neigh.getId()) == 0 && !visitedNeighbors.contains(neigh.getId())) {
				if (checkPenalty(originEdge, neighbors, penalty, neigh, time)) {
					neighbors.add(neigh);
					if (distance instanceof NetTrafficDistance) {
						((NetTrafficDistance) distance).putNetDistance(originEdge.getId(), neigh.getId(), epsNet);
					}
					getEdgesInNeighborhood(neigh, epsNet - 1, neighbors, visitedNeighbors, time);
				}
				visitedNeighbors.add(neigh.getId());
			}
		}

		return neighbors;
	}

	public static void main(String[] args) {
		ProbabilisticGraph graph = new ProbabilisticGraph(
				"/home/livia/git/graph-import/graph-import/graph/fortaleza-graphast", new GaussianParser());
		graph.load();
		ProbabilisticCostsDAO dao = new ProbabilisticCostsDAO(
				"/home/livia/git/graph-import/graph-import/graphast-to-graphhopper-map");

		try {
			int numIntervals = 4;
			dao.addGaussianCost((ProbabilisticGraph) graph, false, numIntervals);
			graph.setNumberOfIntervals(numIntervals);
			ConnectedLinkScan alg = new ConnectedLinkScan(graph, new CosineDistance(), 3);

			String file = "con-clusters" + String.format("%.1f", 0.2) + "-" + 10 + "-" + 100 + " - " + 3;
			System.out.println("Processing " + file);
			alg.runAndSave(file, 0.2, 10, 100, 3);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
