package br.ufc.arida.analysis.timeseries.model;

import java.util.HashMap;

import org.graphast.model.Edge;
import org.graphast.model.Graph;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;

public class UndirectedGraphImpl extends GraphImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UndirectedGraphImpl(String directory) {
		super(directory);
	}

	@Override
	// TODO Should this method be public?
	public void setEdge(Edge edge, long pos) {
		super.setEdge(edge, pos);
	}

	@Override
	public void addEdge(Edge edge) {
		// TODO How to verify if the edge (or reverse) already exists
		super.addEdge(edge);
	}

	// This method should be public? What is your meaning?
	// @Override
	// public void updateNeighborhood(Edge edge) {
	// // TODO It is probably different
	// super.updateNeighborhood(edge);
	// }
	//
	// @Override
	// public void updateNodeNeighborhood(Node node, long eid) {
	// // TODO It is probably different
	// }

	@Override
	// TODO Should be replaced by getEdges, outEdges and inEdges do not have
	// difference in an undirected graph
	public LongList getOutEdges(long nodeId) {
		// in an undirected graph in and out edges are the same
		LongList outEdges = super.getOutEdges(nodeId);
		LongList inEdges = super.getInEdges(nodeId);
		outEdges.addAll(inEdges);
		return super.getOutEdges(nodeId);
	}

	// TODO Should be replaced by getEdges, outEdges and inEdges do not have
	// difference in an undirected graph
	@Override
	public LongList getInEdges(long nodeId) {
		// in an undirected graph in and out edges are the same
		LongList outEdges = super.getOutEdges(nodeId);
		LongList inEdges = super.getInEdges(nodeId);
		outEdges.addAll(inEdges);
		return outEdges;
	}

	// @Override
	// public int[] getEdgesCosts(LongList edges, int time) {
	// // TODO Auto-generated method stub
	// return getEdgesCosts(edges, time);
	// }
	//
	// @Override
	// public int[] getNodeCosts(long id) {
	// // TODO Auto-generated method stub
	// return graph.getNodeCosts(id);
	// }

	// TODO Should be replaced by getNeighbors, getOutNeighbors and
	// getInNeighbors do not have
	// difference in an undirected graph
	@Override
	public LongList getOutNeighbors(long vid) {
		// in an undirected graph in and out neighbors are the same
		LongList out = super.getOutNeighbors(vid);
		LongList in = super.getInNeighbors(vid);
		out.addAll(in);
		return out;
	}

	@Override
	// TODO Should be replaced by getNeighbors, getOutNeighbors and
	// getInNeighbors do not have
	// difference in an undirected graph
	public LongList getOutNeighborsAndCosts(long vid, int time) {
		LongList out = super.getOutNeighborsAndCosts(vid, time);
		LongList in = super.getInNeighborsAndCosts(vid, time);
		out.addAll(in);
		return out;
	}

	@Override
	// TODO Should be replaced by getNeighbors, getOutNeighbors and
	// getInNeighbors do not have
	// difference in an undirected graph
	public LongList getInNeighbors(long vid) {
		// in an undirected graph in and out neighbors are the same
		LongList out = super.getOutNeighbors(vid);
		LongList in = super.getInNeighbors(vid);
		out.addAll(in);
		return out;
	}

	@Override
	// TODO Should be replaced by getNeighbors, getOutNeighbors and
	// getInNeighbors do not have
	// difference in an undirected graph
	public LongList getInNeighborsAndCosts(long vid, int time) {
		LongList out = super.getOutNeighborsAndCosts(vid, time);
		LongList in = super.getInNeighborsAndCosts(vid, time);
		out.addAll(in);
		return out;
	}

	@Override
	public Edge getEdge(long originNodeId, long destinationNodeId) {
		// TODO It is the same of the reverse
		Edge edge = super.getEdge(originNodeId, destinationNodeId);
		if (edge.getId() == -1) {
			edge = super.getEdge(destinationNodeId, originNodeId);
		}
		return edge;
	}

	@Override
	// TODO It returns only the out edges, modify to do for both, out and in
	// TODO document the meaning of this method => node and spatial cost?
	public Long2IntMap accessNeighborhood(Node v) {
		
		Long2IntMap neighbors = new Long2IntOpenHashMap();

		for (Long e : super.getOutEdges(v.getId())) {

			Edge edge = this.getEdge(e);
			long neighborNodeId = edge.getToNode();
			int cost = edge.getDistance();
			// TODO What means this verification in the original method?
			if (!neighbors.containsKey(neighborNodeId)) {
				// Is it possible do not have this key?
				neighbors.put(neighborNodeId, cost);
			} else {
				if (neighbors.get(neighborNodeId) > cost) {
					neighbors.put(neighborNodeId, cost);
				}
			}
		}
		
		for (Long e : super.getInEdges(v.getId())) {

			Edge edge = this.getEdge(e);
			long neighborNodeId = edge.getFromNode();
			int cost = edge.getDistance();
			// TODO What means this verification in the original method?
			if (!neighbors.containsKey(neighborNodeId)) {
				neighbors.put(neighborNodeId, cost);
			} else {
				if (neighbors.get(neighborNodeId) > cost) {
					neighbors.put(neighborNodeId, cost);
				}
			}
		}

		return neighbors;
		//return super.accessNeighborhood(v);

	}

	@Override
	// TODO It returns only the out edges, modify to do for both, out and in
	// TODO document the meaning of this method => node and temporal cost?
	public HashMap<Node, Integer> accessNeighborhood(Node v, int time) {
		// TODO Auto-generated method stub
		return super.accessNeighborhood(v, time);
	}
	
	@Override
	public boolean equals(Graph graph) {
		// TODO Depends on the implementation, maybe it is different for
		// undirected graphs
		return false;
	}

	// @Override
	// public void save() {
	// }
	//
	// @Override
	// public void load() {
	// }
	//
	// @Override
	// public void addNode(Node node) {
	// }
	//
	// @Override
	// public void updateNodeInfo(Node node) {
	// TODO See what this method do
	//
	// }
	//
	// @Override
	// public Node getNode(long id) {
	// }
	//

	// @Override
	// public Edge getEdge(long id) {
	// }
	//
	// @Override
	// public int[] getEdgeCosts(long id) {

	// return graph.getEdgeCosts(id);
	// }
	//
	// @Override
	// public Integer getEdgeCost(Edge edge, int time) {

	// return graph.getEdgeCost(edge, time);
	// }
	//
	// @Override
	// public List<Point> getGeometry(long id) {

	// return grap;
	// }

	// @Override
	// public Long getNodeId(double latitude, double longitude) {

	// return null;
	// }
	//
	// @Override
	// public String getNodeLabel(long id) {

	// return null;
	// }
	//
	// @Override
	// public String getEdgeLabel(long id) {

	// return null;
	// }

	// @Override
	// public IntBigArrayBigList getNodes() {

	// return null;
	// }

	// @Override
	// public IntBigArrayBigList getEdges() {

	// return null;
	// }
	//
	// @Override
	// public void logNodes() {

	//
	// }
	//
	// @Override
	// public void logEdges() {

	//
	// }

	// @Override
	// public long getNumberOfNodes() {

	// return 0;
	// }
	//
	// @Override
	// public long getNumberOfEdges() {

	// return 0;
	// }

	// @Override
	// public boolean hasNode(long id) {
	// return false;
	// }

	// @Override
	// public boolean hasNode(double lat, double lon) {
	// return false;
	// }

	// @Override
	// public Node addPoi(long id, double lat, double lon, int category,
	// LinearFunction[] costs) {
	// return null;
	// }
	//
	// @Override
	// public Node addPoi(long id, double lat, double lon, int category) {
	// return null;
	// }
	//
	// @Override
	// public boolean isPoi(long vid) {
	// return false;
	// }
	//
	// @Override
	// public Node getPoi(long vid) {
	// return null;
	// }

	// @Override
	// public int poiGetCost(long vid, int time) {
	// return 0;
	// }
	//
	// @Override
	// public int poiGetCost(long vid) {
	// return 0;
	// }
	//
	// @Override
	// public int[] getPoiCost(long vid) {
	// return null;
	// }
	//
	// @Override
	// public LinearFunction[] convertToLinearFunction(int[] costs) {
	// return null;
	// }

	// @Override
	// public int getMaxTime() {
	// return 0;
	// }
	//
	// @Override
	// public void setMaxTime(int maxTime) {
	//
	// }

	// @Override
	// public int getArrival(int dt, int tt) {
	// // TODO Documentation: what is dt? what is tt?
	// return super.getArrival(dt, tt);
	// }

	// @Override
	// public CompressionType getCompressionType() {
	// return null;
	// }
	//
	// @Override
	// public void setCompressionType(CompressionType compressionType) {
	//
	// }

	// @Override
	// public TimeType getTimeType() {
	// return null;
	// }
	//
	// @Override
	// public void setTimeType(TimeType timeType) {
	//
	// }
	//
	// @Override
	// public IntSet getCategories() {
	// return null;
	// }

	// @Override
	// TODO It does not make sense a reverse graph in a undirected graph
	// public void reverseGraph() {
	//
	// }

	// @Override
	// public void setEdgeCosts(long edgeId, int[] costs) {
	//
	// }

	// TODO In documentation be clear that is the nearest in Euclidean space
	// @Override
	// public Node getNearestNode(double latitude, double longitude) {
	// return null;
	// }

	// @Override
	// public void setNodeCategory(long nodeId, int category) {
	//
	// }
	//
	// @Override
	// public void setEdgeGeometry(long edgeId, List<Point> geometry) {
	//
	// }

	// @Override
	// public BBox getBBox() {
	// return null;
	// }
	//
	// @Override
	// public void setBBox(BBox bBox) {
	//
	// }

	// @Override
	// public List<PoI> getPOIs() {
	// return null;
	// }
	//
	// @Override
	// public List<PoI> getPOIs(Integer categoryId) {
	// return null;
	// }

	// @Override
	// public List<Integer> getPOICategories() {
	// return null;
	// }
	//
	// @Override
	// public String getDirectory() {
	// return null;
	// }
	//
	// @Override
	// public String getAbsoluteDirectory() {
	// return null;
	// }
	//
	// @Override
	// public void setDirectory(String directory) {
	//
	// }

}
