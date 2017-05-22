package br.ufc.arida.analysis.algortihms;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.graphast.geometry.Point;
import org.graphast.model.Edge;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;
import br.ufc.arida.analysis.model.measures.DistanceMeasure;
import it.unimi.dsi.fastutil.booleans.BooleanBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongList;

public class DBScan {

	private ProbabilisticGraph graph;
	private BooleanBigArrayBigList visited;
	private BooleanBigArrayBigList reachable;
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
		this.minPts = minPts;
		inCluster = new IntBigArrayBigList(graphSize);
		visited = new BooleanBigArrayBigList(graphSize);
		reachable = new BooleanBigArrayBigList(graphSize);
		noise = new BooleanBigArrayBigList(graphSize);
		noises = new ArrayList<>();

		for (int id = 0; id < graphSize; id++) {
			visited.add(false);
			noise.add(false);
			inCluster.add(0);
		}
		for (long id = 0; id < graphSize; id++) {
			if (visited.get(id)) {
				System.out.println("visited: " + id);
				continue;
			}
			visited.set(id, true);
			Edge edge = graph.getEdge(id);
			

			List<Edge> neighborsPts = regionQuery(edge, epsSim, epsNet, time);
			System.out.println("end region: " + id + " " + getIdsAsString(neighborsPts));
			if (neighborsPts.size() < minPts) {
				noise.set(id, true);
				noises.add(id);
				System.out.println("noise: " + id);
			} else {
				nextCluster++;
				cluster = new ArrayList<>();
				cluster.add((long) id);
				System.out.println("expand: " + id);
				for (int i = 0; i < neighborsPts.size(); i++) {
					cluster.add((long) i);
					expandCluster(neighborsPts.get(i).getId(), neighborsPts, epsSim, epsNet, time);
				}
				clusters.put(nextCluster, cluster);
			}

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

	private void expandCluster(long id, List<Edge> neighborsPts, double epsSim,  double epsNet, int time) {
		for (int i = 0; i < neighborsPts.size(); i++) {
			Edge neighbor = neighborsPts.get(i);
			if (visited.get(neighbor.getId()))
				continue;
			visited.set(neighbor.getId().longValue(), true);
			List<Edge> neighborsPts2 = regionQuery(neighbor, epsSim, epsNet, time);
			System.out.println("expansion " + getIdsAsString(neighborsPts2));
			if (neighborsPts2.size() > 0) {
				neighborsPts.addAll(neighborsPts.size(), neighborsPts2);
			}

			System.out.println("size ="+(neighborsPts.size()-i));	
		}
		
		// expandCluster(id, newPts, epsSim, minPts, time);
		
	}

	public List<Edge> regionQuery(Edge edge, double epsSim, double epsNet, int time) {
		List<Edge> region = new ArrayList<Edge>();
		ProbabilisticCost costs = graph.getProbabilisticCosts(edge.getId(), time);
		List<Edge> neighbors = getEdgesInNeighborhood(edge);

		for (int i = 0; i < neighbors.size(); i++) {
			Edge edgeNeigh = neighbors.get(i);
			if (visited.get(edgeNeigh.getId()))
				continue;
			visited.set(edgeNeigh.getId().longValue(), true);
			ProbabilisticCost outCosts = graph.getProbabilisticCosts(edgeNeigh.getId(), time);
			double d = distance.calculate(costs, outCosts);
			if (d < epsSim) {
				System.out.println("distance: " + d);
				region.add(edgeNeigh);
				List<Edge> edgesInNeighborhood = getEdgesInNeighborhood(edgeNeigh);
				for (Edge edge2 : edgesInNeighborhood) {
					if(!visited.get(edge2.getId()))neighbors.add(edge2);
				}
				//neighbors.addAll();
			} else {
				System.out.println("distance: " + d);
			}
		}

		return region;
	}

	private List<Edge> getEdgesInNeighborhood(Edge edge) {
		List<Edge> neighbors = new ArrayList<>();
		LongList nodesNeig = graph.getOutNeighbors(graph.getEdge(edge.getId()).getToNode());
		for (Long node : nodesNeig) {
			neighbors.add(graph.getEdge(edge.getToNode(), node));
		}
		System.out.println("region: " + edge.getId() + " out " + getIdsAsString(neighbors));
		nodesNeig = graph.getInNeighbors(graph.getEdge(edge.getId()).getFromNode());
		for (Long node : nodesNeig) {
			neighbors.add(graph.getEdge(node, edge.getFromNode()));
		}
		System.out.println("region: " + edge.getId() + " in and out " + getIdsAsString(neighbors));
		return neighbors;
	}
	
	public void runAndSave(String filePath, double epsSim, double epsNet, int minPts, int time) throws IOException{
		FileWriter writer = new FileWriter(filePath);
		Map<Integer, List<Long>> map = this.run(epsSim, epsNet, minPts, time);
		for (Entry<Integer, List<Long>> e : map.entrySet()) {
			for (Long edge : e.getValue()) {
				List<Point> geometry = this.graph.getEdge(edge.longValue()).getGeometry();
				String edgeStr = geometry.get(0).getLatitude()+", "
								+geometry.get(0).getLongitude()+", "
								+geometry.get(geometry.size()-1).getLatitude()+", "
										+geometry.get(geometry.size()-1).getLongitude()+", ";
				writer.write(e.getKey()+" , "+edge+", "+edgeStr+", "+graph.getProbabilisticCosts(edge,0)+"\n");
			}
		}
	}

}
