package br.uf.arida.traveltime.dao.postgis.trajectory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import br.uf.arida.traveltime.dao.postgis.ConnectionJDBC;
import br.uf.arida.traveltime.model.ConstrainedTrajectory;

public class ConstrainedTrajectoryDao {

	public void save(ArrayList<ConstrainedTrajectory> constrainedTrajectories)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionJDBC.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(
				"INSERT INTO public.trajectory_edges( traj_id, start_time, edge_id, travel_time) "
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

}
