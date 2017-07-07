package br.ufc.arida.analysis.algortihms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.graphast.model.Edge;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.model.measures.TrafficComparatorMeasure;
import it.unimi.dsi.fastutil.longs.LongList;

public class ProbabilisticDistanceAnalysis {

	private ProbabilisticGraph graph;
	private TrafficComparatorMeasure<ProbabilisticCost> similarity;
	private Map<Long, Set<NeighborEdge>> dissimMatrix;
	private Set<NeighborEdge> distanceSet = new HashSet<NeighborEdge>();
	private static int OUT_SEARCH = 0, IN_SEARCH = 1, BIDIRECTED_SEARCH = 2;

	public ProbabilisticDistanceAnalysis(ProbabilisticGraph graph, TrafficComparatorMeasure<ProbabilisticCost> similarity) {
		this.graph = graph;
		this.similarity = similarity;
		initSimilarityMatrix();

	}

	private void initSimilarityMatrix() {
		long numberOfEdges = graph.getNumberOfEdges();
		dissimMatrix = new HashMap<Long, Set<NeighborEdge>>();

		for (long n = 0; n < numberOfEdges; n++) {
			dissimMatrix.put(n, new HashSet<NeighborEdge>());
		}
	}

	/**
	 * This method creates a dissimilarity matrix between speed profile of
	 * adjacent edges at a specific interval time
	 * 
	 * @param intervalTime
	 * @return
	 */
	public Map<Long, Set<NeighborEdge>> similarityByTime(int intervalTime) {
		initSimilarityMatrix();
		Set<Long> edges = dissimMatrix.keySet();
		// Visit all edges and calculate the dissimilarity between the costs
		// of out edges
		for (Long edgeId : edges) {
			Edge edge = graph.getEdge(edgeId);
			distanceSet = dissimMatrix.get(edgeId);
			ProbabilisticCost costOfEdge = graph.getProbabilisticCosts(edgeId, intervalTime);
			if (costOfEdge != null) {
				LongList outNeighbors = graph.getOutNeighbors(edge.getToNode());

				for (Long outEdgeId : outNeighbors) {
					ProbabilisticCost costOfNeighbor = graph.getProbabilisticCosts(outEdgeId, intervalTime);
					compareCost(edgeId, costOfEdge, outEdgeId, costOfNeighbor);

				}
			}

		}

		return dissimMatrix;

	}

	private void compareCost(Long edgeId, ProbabilisticCost costOfEdge, Long outEdgeId,
			ProbabilisticCost costOfNeighbor) {
		try {
			if (costOfNeighbor != null && costOfEdge != null) {
				double distance = similarity.calculate(costOfEdge, costOfNeighbor);
				// ProbabilisticEdgesUtils.computeSimilarityMixtures(costOfEdge.mm,
				// costOfNeighbor.mm,
				// numberOfPoints);
				NeighborEdge neighborEdge = new NeighborEdge(edgeId, outEdgeId, distance);
				if (!distanceSet.contains(neighborEdge))
					distanceSet.add(neighborEdge);
				System.out.println(distance);
				// } else {
				// NeighborEdge neighborEdge = new NeighborEdge(edgeId,
				// outEdgeId, null);
				// if (!distanceSet.contains(neighborEdge))
				// distanceSet.add(neighborEdge);
				// System.out.println(-1);
				//
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * This method calculate the similarity between speed profiles from each
	 * edge to all edges that are reachable in at maximum of k hopes
	 * 
	 * @param k
	 * @param intervalTime
	 */
	public void similarityByTimeAndKNeighbors(int k, int intervalTime) {
		initSimilarityMatrix();
		Set<Long> edges = dissimMatrix.keySet();
		// Visit all edges and calculate the dissimilarity between the costs
		// of k neighbors
		for (Long edgeId : edges) {
			similarityByTimeAndKNeighbors(edgeId, k, intervalTime);
			dissimMatrix.put(edgeId, distanceSet);
		}
	}

	/**
	 * This method generate a list of neighbors that have at maximum k hopes
	 * from the edge. *
	 * 
	 * @param edgeId
	 * @param k
	 * @param intervalTime
	 */
	public void similarityByTimeAndKNeighbors(long edgeId, int k, int intervalTime) {

		distanceSet = new HashSet();

		similarityKNeighbors(edgeId, edgeId, k, intervalTime, BIDIRECTED_SEARCH);

	}

	/**
	 * Recursive method to calculate similarity called from
	 * {@link #similarityByTimeAndKNeighbors(int, int, int)}
	 * 
	 * @param sourceId
	 * @param edgeId
	 * @param k
	 * @param intervalTime
	 */
	private void similarityKNeighbors(long sourceId, long edgeId, int k, int intervalTime, int direction) {
		if (k == 1) {
			ProbabilisticCost sourceCost = graph.getProbabilisticCosts(sourceId, intervalTime);
			LongList outEdges = graph.getOutEdges(graph.getEdge(edgeId).getToNode());
			LongList inEdges = graph.getInEdges(graph.getEdge(edgeId).getFromNode());
			if (direction == BIDIRECTED_SEARCH || direction == OUT_SEARCH) {
				for (Long outId : outEdges) {
					ProbabilisticCost outCost = graph.getProbabilisticCosts(outId, intervalTime);
					compareCost(sourceId, sourceCost, outId, outCost);
				}
			}
			if (direction == BIDIRECTED_SEARCH || direction == IN_SEARCH) {
				for (Long inId : inEdges) {
					ProbabilisticCost inCost = graph.getProbabilisticCosts(inId, intervalTime);
					compareCost(sourceId, sourceCost, inId, inCost);
				}
			}

		} else {
			LongList outEdges = graph.getOutEdges(graph.getEdge(edgeId).getToNode());
			LongList inEdges = graph.getInEdges(graph.getEdge(edgeId).getFromNode());
			for (Long outId : outEdges) {
				similarityKNeighbors(sourceId, outId, k - 1, intervalTime, OUT_SEARCH);
			}

			for (Long inId : inEdges) {
				similarityKNeighbors(sourceId, inId, k - 1, intervalTime, IN_SEARCH);
			}

		}
	}

	public void saveDissimilarityMatrix(String outputFile) {
		try {
			FileWriter writer = new FileWriter(new File(outputFile));
			Set<Entry<Long, Set<NeighborEdge>>> entrySet = dissimMatrix.entrySet();
			for (Entry<Long, Set<NeighborEdge>> entry : entrySet) {

				for (NeighborEdge neighbor : entry.getValue()) {

					if (neighbor.getDistanceValue() != null) {
						writer.write(entry.getKey() + ", ");
						writer.write(neighbor.getIdOutEdge() + ", ");
						writer.write(neighbor.getDistanceValue() + "\n");
					}

				}
			}

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveDissimilarityKNeighbors(String outputFile) {
		try {
			FileWriter writer = new FileWriter(new File(outputFile));
			writer.write(distanceSet + "\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
