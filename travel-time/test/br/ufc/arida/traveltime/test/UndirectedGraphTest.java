package br.ufc.arida.traveltime.test;

import org.graphast.model.EdgeImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.graphast.query.route.shortestpath.ShortestPathService;
import org.graphast.query.route.shortestpath.astar.AStarConstantWeight;
import org.graphast.query.route.shortestpath.model.Path;

import br.ufc.arida.analysis.timeseries.model.UndirectedGraphImpl;

public class UndirectedGraphTest {

	public static void main(String[] args) {
		UndirectedGraphImpl graph = new UndirectedGraphImpl("TesteGrafoNaoDirecionado");
		Node n1 = new NodeImpl(1, 1.1, 1.2);
		Node n2 = new NodeImpl(2, 2.1, 1.2);
		Node n3 = new NodeImpl(3, 2.5, 1.2);
		Node n4 = new NodeImpl(4, 2.9, 1.2);
		Node n5 = new NodeImpl(5, 1.0, 2.2);
		Node n6 = new NodeImpl(6, 1.1, 1.2);

		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
		graph.addNode(n4);
		graph.addNode(n5);
		graph.addNode(n6);

		System.out.println(graph.getNodes());
// TODO
//		graph.addEdge(new EdgeImpl(1, n1.getId(), n2.getId(), 1, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(2, n1.getId(), n3.getId(), 2, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(3, n2.getId(), n3.getId(), 3, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(4, n4.getId(), n2.getId(), 4, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(5, n4.getId(), n3.getId(), 3, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(6, n4.getId(), n5.getId(), 2, new int[] { 1, 2, 3 }));
//		graph.addEdge(new EdgeImpl(7, n6.getId(), n5.getId(), 1, new int[] { 1, 2, 3 }));

		System.out.println(graph.getOutEdges(n1.getId()));
		System.out.println(graph.getInEdges(n1.getId()));

		System.out.println(graph.getInNeighbors(n2.getId()));
		System.out.println(graph.getOutNeighbors(n2.getId()));

		// getOutNeighborsAndCosts

		System.out.println(graph.accessNeighborhood(n2));
		System.out.println(graph.getOutNeighborsAndCosts(n2.getId(), 20));
		
		ShortestPathService sps = new AStarConstantWeight(graph);
		Path shortestPath = sps.shortestPath(n1, n5);
		System.out.println(shortestPath.getEdges());
	}

}
