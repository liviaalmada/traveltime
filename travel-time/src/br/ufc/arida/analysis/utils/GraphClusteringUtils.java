package br.ufc.arida.analysis.utils;

import java.util.HashMap;

import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.Graph;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;

public class GraphClusteringUtils {
	
	public static Graph produceDualGraph(GraphImpl g){
		long edgesSize = g.getNumberOfEdges();
		HashMap<Long, Long> mapEdgeId2NodeId = new HashMap<>();
		Graph linkGraph = new GraphImpl("teste");
		
		// create a vertex to each edge in original graph
		for(long i = 0; i < edgesSize; i++){
			Node n = new NodeImpl();
			EdgeImpl edge = (EdgeImpl) g.getEdge(i);
			// TODO
			//n.setExternalId(edge.getExternalId());
			n.setLabel(edge.getLabel());
			linkGraph.addNode(n);
			mapEdgeId2NodeId.put(i, n.getId());
		}
		
		for(long i = 0; i < edgesSize; i++){
			EdgeImpl edge = (EdgeImpl) g.getEdge(i);
			// add neighbors corresponding to the edge vertex
			long to = edge.getToNode();
						
			// get reachable edges
			for (Long neighbor : g.getOutNeighbors(to)) {
				Edge linkEdge = new EdgeImpl(mapEdgeId2NodeId.get(i), mapEdgeId2NodeId.get(neighbor), 0);
				linkGraph.addEdge(linkEdge);
			}
		}
		
		return linkGraph;
	}

}
