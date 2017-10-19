package br.ufc.arida.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.Graph;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;

import br.ufc.arida.analysis.utils.GraphClusteringUtils;

public class PGRountingGraphDAO {

	private static String TABLE = "roads";

	private static String QUERY_GRAPH_STR = "select id, source, target," + " x1, y1, x2, y2, cost, reverse_cost, "
			+ " ST_Length(geom_way::geography)  as distance " + "from "+TABLE;
	
	private static String QUERY_GRAPH_STR_DEBUG = "select id, source, target," + " x1, y1, x2, y2, cost, reverse_cost, name, "
			+ " ST_Length(geom_way::geography)  as distance "  + "from "+TABLE;
	//where id in (select edge_id from MOST_USED_TIME_SERIES) order by id;";

	/**
	 * This method load a network from the pgrouting database schema and save as
	 * a graphast file on an specific directory
	 * 
	 * @param directory
	 * @return 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static GraphImpl readFromDBAndSave(String directory) throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		System.out.println(QUERY_GRAPH_STR);
		PreparedStatement prepareStatement = connection.prepareStatement(QUERY_GRAPH_STR);
		ResultSet result = prepareStatement.executeQuery();

		GraphImpl graph = new GraphImpl(directory);
		HashMap<Integer, Node> nodes = new HashMap<>();

		int count = 0;
		while (result.next()) {

			Node n1 = null;
			Node n2 = null;
			int source = result.getInt("source");
			int target = result.getInt("target");

			if (!nodes.containsKey(source)) {
				n1 = new NodeImpl(source, result.getDouble("x1"), result.getDouble("y1"));
				graph.addNode(n1);
				nodes.put(result.getInt("source"), n1);
			} else {
				n1 = nodes.get(source);
			}

			if (!nodes.containsKey(target)) {
				n2 = new NodeImpl(target, result.getDouble("x2"), result.getDouble("y2"));
				graph.addNode(n2);
				nodes.put(target, n2);
			} else {
				n2 = nodes.get(target);
			}
			// Distance in (mm)
			Edge e1 = new EdgeImpl(result.getInt("id"), n1.getId(), n2.getId(),
					(int) (result.getDouble("distance") ));
			graph.addEdge(e1);
			System.out.println(e1);
			count++;

			if (result.getDouble("reverse_cost") == result.getDouble("cost")) {
				Edge e2 = new EdgeImpl(result.getInt("id"), n2.getId(), n1.getId(),
						(int) (result.getDouble("distance") ));
				graph.addEdge(e2);
				System.out.println(e2);
				count++;
			}
			
			if(count==500) break;
		}
		graph.save();
		connection.close();
		return graph;
	}

	public static void main(String[] args) {
		try {
			PGRountingGraphDAO.TABLE = "roads";
			readFromDBAndSave("/Users/liviaalmada/git/beijing-graph");
			GraphImpl g = new GraphImpl("graph");
			g.load();
			System.out.println(g.getNumberOfNodes());
			System.out.println(g.getNumberOfEdges());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
