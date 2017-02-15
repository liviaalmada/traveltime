package br.ufc.arida.probabilisticgraph.dao.postgis.trajectory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufc.arida.probabilisticgraph.dao.postgis.ConnectionJDBC;
import br.ufc.arida.probabilisticgraph.trajectory.TravelCostTimeSeries;
import fr.david.netcutis.mapmatching.ConstrainedTrajectory;

public class ConstrainedTrajectoryDao {

	public void save(ArrayList<ConstrainedTrajectory> constrainedTrajectories)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection
				.prepareStatement("INSERT INTO public.trajectory_edges( traj_id, start_time, edge_id, travel_time) "
						+ "VALUES (?, ?, ?, ?);");
		for (ConstrainedTrajectory constrainedTrajectory : constrainedTrajectories) {
			int size = constrainedTrajectory.size();
			for (int i = 0; i < size; i++) {
				prepareStatement.setInt(1, constrainedTrajectory.getTrajectoryAsSet().getTid());
				prepareStatement.setTimestamp(2, constrainedTrajectory.getStartTimeAt(i));
				prepareStatement.setInt(3, constrainedTrajectory.getEdgeAt(i).getRoadID());
				prepareStatement.setDouble(4, constrainedTrajectory.getTravelTimeAt(i));
				prepareStatement.addBatch();
			}
		}
		prepareStatement.executeBatch();
		connection.close();

	}

	public ArrayList<Double> listPointsGroupedByEdge(int startHour, int endHour, int edgeId)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(
				"select travel_time " + "from trajectory_edges " + "where date_part('hour', start_time) >= ? "
						+ "and date_part('hour', start_time) <? " + "and edge_id = ?");
		prepareStatement.setInt(1, startHour);
		prepareStatement.setInt(2, endHour);
		prepareStatement.setInt(3, edgeId);

		ResultSet resultSet = prepareStatement.executeQuery();
		ArrayList<Double> travelTimes = new ArrayList<>();

		while (resultSet.next()) {
			travelTimes.add(resultSet.getDouble("travel_time"));
		}
		return travelTimes;
	}
	
	// TODO: put thie method in other dao 
	public TravelCostTimeSeries getTravelCostTimeSeries(int edgeId)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(
				"SELECT * FROM  COMPACT_TIME_SERIES WHERE EDGE_ID = ?");
		prepareStatement.setInt(1, edgeId);

		ResultSet resultSet = prepareStatement.executeQuery();
		TravelCostTimeSeries timeSeries = new TravelCostTimeSeries(edgeId, 96);
		
		while (resultSet.next()) {
			double cost = resultSet.getInt("travel_time");
			int interval =resultSet.getInt("time_interval");
			timeSeries.addTravelCost(cost, interval);;
		}
		return timeSeries;
	}
	
	
	public static void main(String[] args) {
		ConstrainedTrajectoryDao dao = new ConstrainedTrajectoryDao();
		try {
			//ArrayList<Double> listPointsGroupedByEdge = dao.listPointsGroupedByEdge(0, 12, 44894);
			TravelCostTimeSeries travelCostTimeSeries = dao.getTravelCostTimeSeries(20988);
			List<Double> unionOfTravelTimeCosts = travelCostTimeSeries.getUnionOfTravelTimeCosts();
			System.out.println(unionOfTravelTimeCosts.size());
			System.out.println(unionOfTravelTimeCosts);
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