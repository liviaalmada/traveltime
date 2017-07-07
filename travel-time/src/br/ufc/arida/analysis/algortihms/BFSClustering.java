package br.ufc.arida.analysis.algortihms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import org.graphast.model.Edge;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.model.measures.TrafficComparatorMeasure;
import it.unimi.dsi.fastutil.longs.LongList;

public class BFSClustering {
	private ArrayList<Long> shuffledEdges; // array with permutation of edges
	private boolean[] visited; // indexed by edge's ids say if the edge was
								// visited
	private boolean[] inCluster; // indexed by edge's ids say if the edge is
									// already in a cluster

	private int nextRandomEdge; // index of the next edge to be
	private long notVisited;
	private ProbabilisticGraph graph;
	private int timeInterval;
	private double threshold;
	private TrafficComparatorMeasure<ProbabilisticCost> distance;
	private ArrayList<ArrayList<Long>> clusters;

	public void doClustering(ProbabilisticGraph graph, TrafficComparatorMeasure<ProbabilisticCost> distance, int timeInterval,
			double threshold) {
		this.graph = graph;
		this.timeInterval = timeInterval;
		this.distance = distance;
		this.threshold = threshold;
		this.notVisited = graph.getNumberOfEdges();
		this.clusters = new ArrayList<ArrayList<Long>>();
//		printCosts(this.graph, timeInterval);
		shuffleEdges(this.graph);
		bfsClustering(this.graph, distance);
	}

	
	public void printCosts(ProbabilisticGraph graph, int i) {
		for (int j = 0; j < graph.getNumberOfEdges(); j++) {
			ProbabilisticCost probabilisticCosts = graph.getProbabilisticCosts(j,i);
			if(probabilisticCosts!=null)System.out.println(probabilisticCosts);
		}
	}
	
	public void saveClusters(String filePath) {
		try {
			int idCluster = 0;
			FileWriter w = new FileWriter(new File(filePath));
			for (ArrayList<Long> arrayList : clusters) {
				for (Long idEdge : arrayList) {
					
					
					ProbabilisticCost cost = graph.getProbabilisticCosts(idEdge, timeInterval);
					//if(cost!=null)System.out.println(idEdge+" "+timeInterval+" "+idCluster);
					if(cost!=null)w.write(idEdge + " , " + idCluster+" "+cost+"\n");
					//else w.write(idEdge + " , " + idCluster+"\n");
				}
				idCluster++;
				//w.write("\n");
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void bfsClustering(ProbabilisticGraph graph, TrafficComparatorMeasure<ProbabilisticCost> distance) {
		printCosts(graph, timeInterval);
		PriorityQueue<Long> edgesTovisitedQueue = new PriorityQueue<>();
		int actualIndex = clusters.size();
		while (notVisited > 0) {
			clusters.add(actualIndex, new ArrayList<Long>());
			Edge edge = graph.getEdge(chooseRandomEdge());
			edgesTovisitedQueue.add(edge.getId());
			inCluster[(int) edge.getId().longValue()] = true;
			do {
				edge = graph.getEdge(edgesTovisitedQueue.poll());
				clusters.get(actualIndex).add(edge.getId());
				visit(edge, edgesTovisitedQueue);
				notVisited--;
			} while (edgesTovisitedQueue.size() > 0);
			actualIndex++;
		}
		// for (ArrayList<Long> c : clusters) {
		// System.out.println(c);
		// }

	}

	private void visit(Edge edge, PriorityQueue<Long> edgesTovisited) {
		visited[((int) edge.getId().longValue())] = true;
		LongList edges = graph.getOutEdges(edge.getToNode());
		//System.out.println("outsize " + edges.size());
		visitEdges(edge, edgesTovisited, edges);

		edges = graph.getInEdges(edge.getToNode());
		//System.out.println("insize " + edges.size());
		visitEdges(edge, edgesTovisited, edges);
	}

	private void visitEdges(Edge edge, PriorityQueue<Long> edgesTovisited, LongList edges) {

		for (Long outEdgeId : edges) {
			//System.out.println(outEdgeId);
			if (!inCluster[((int) outEdgeId.longValue())]) {
				ProbabilisticCost cost1 = graph.getProbabilisticCosts(edge.getId(), timeInterval);
				ProbabilisticCost cost2 = graph.getProbabilisticCosts(outEdgeId, timeInterval);

				if (cost1 != null && cost2 != null) {
					double dist;
					try {
						dist = distance.calculate(cost1, cost2);
						if (dist < threshold) {
							edgesTovisited.add(outEdgeId);
							inCluster[(int) outEdgeId.longValue()] = true;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(dist);

				} else {
					//System.out.println("No");
				}
			} else {
				//System.out.println("Incluster");
			}

		}
	}

	private void shuffleEdges(ProbabilisticGraph graph) {
		long numberOfEdges = graph.getNumberOfEdges();
		visited = new boolean[(int) numberOfEdges];
		inCluster = new boolean[(int) numberOfEdges];
		// visited = new boolean[100];
		shuffledEdges = new ArrayList<>((int) numberOfEdges);
		for (int i = 0; i < numberOfEdges; i++) {
			visited[i] = false;
			inCluster[i] = false;
			shuffledEdges.add((long) i);
		}
		Collections.shuffle(shuffledEdges);
		nextRandomEdge = 0;
	}

	private int chooseRandomEdge() {
		int edgeId = (int) shuffledEdges.get(nextRandomEdge).longValue();
		while (visited[edgeId]) {
			edgeId = (int) shuffledEdges.get(++nextRandomEdge).longValue();
		}
		return edgeId;
	}

}
