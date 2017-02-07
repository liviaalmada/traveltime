package br.uf.arida.traveltime.dao.postgis.trajectory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;

import br.uf.arida.traveltime.dao.postgis.ConnectionJDBC;
import fr.david.netcutis.datamodel.TrajectoryAsSet;
import fr.david.netcutis.mapmatching.Edge;
import fr.david.netcutis.mapmatching.MapMatchingTrajectory;

public class TrajectoryPointDAO {
	
	
	public void insert(ArrayList<TrajectoryPoint> points){
		try {
			Connection connection = ConnectionJDBC.getConnection();
			PreparedStatement prepareStatement = connection.prepareStatement("insert into taxi_junho(id, latitude, longitude, date_time) values(?,?,?,?)");
			
			for (TrajectoryPoint p : points) {
				prepareStatement.setLong(1, p.id_taxi);
				prepareStatement.setDouble(2, p.lat);
				prepareStatement.setDouble(3, p.lon);
				prepareStatement.setTimestamp(4,Timestamp.valueOf(p.dateTime));
				
				prepareStatement.addBatch();
			}
			prepareStatement.executeBatch();
			connection.close();
			
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
	
	
	public ArrayList<TrajectoryAsSet> readTrajectoriesAsSet(String tableName, Timestamp startDate, Timestamp endDate) throws ClassNotFoundException, SQLException, IOException{
		Connection connection = ConnectionJDBC.getConnection();
		String strQuery = "select * from "+ tableName+" where date_time between ? and ?  order by id, date_time";
		PreparedStatement query = connection.prepareStatement(strQuery);
		ArrayList<Long> time_array = new ArrayList<>();
		ArrayList<Coordinate> coord_array = new ArrayList<>();
		ArrayList<TrajectoryAsSet> trajectories = new ArrayList<>();
		int idTraj = -1;
		
		query.setTimestamp(1, startDate);
		query.setTimestamp(2, endDate);
		
		ResultSet result = query.executeQuery();
		while (result.next()) {
			if(idTraj==-1){
				idTraj = result.getInt(1);
			}else{
				if(idTraj != result.getInt(1)){
					TrajectoryAsSet trajAsSet = new TrajectoryAsSet(idTraj, idTraj, time_array, coord_array);
					trajectories.add(trajAsSet);
					time_array = new ArrayList<>();
					coord_array = new ArrayList<>();
					idTraj = result.getInt(1);
				}
			}
			time_array.add(result.getTimestamp(4).getTime());
			coord_array.add(new Coordinate(result.getDouble("longitude"), result.getDouble("latitude")));
		}
		
		
		connection.close();
		return trajectories;
	}
	
	
	public void updateMapMatching(Vector<TrajectoryAsSet> objects, String trajTableName) throws ClassNotFoundException, SQLException, IOException{
		Connection connection = ConnectionJDBC.getConnection(); 
		String strQuery = "update "+ trajTableName+" set edge_id = ? where date_time = ? and id = ?";
		
		PreparedStatement preparedStatement = connection.prepareStatement(strQuery);
		
		for (TrajectoryAsSet trajectoryAsSet : objects) {
			for(int i = 0; i < trajectoryAsSet.size(); i++){
				Edge edge = trajectoryAsSet.getEdge_array().get(i);
				if(edge!=null){
					preparedStatement.setInt(1,edge.getRoadID());
					preparedStatement.setTimestamp(2, new Timestamp(trajectoryAsSet.getTsArrayElement(i)));
					preparedStatement.setInt(3, trajectoryAsSet.getTid());
					preparedStatement.addBatch();
				}
			}
			
			
		}
		preparedStatement.executeBatch();
		connection.close();
		
	}
	
	public Set<Edge> getEdges() throws ClassNotFoundException, SQLException, IOException{
		Connection connection = ConnectionJDBC.getConnection();
		Set<Edge> edgesSet = new HashSet();
		String strQuery = "select  id, class_id , source , target, x1, y1, x2, y2 from roads_experiments;";
		PreparedStatement query = connection.prepareStatement(strQuery);
		ResultSet resultSet = query.executeQuery();
		while (resultSet.next()) {
			Edge edge = new Edge(resultSet.getInt("id"), resultSet.getInt("source"), resultSet.getInt("target"), 
					new Coordinate(resultSet.getDouble("x1"), resultSet.getDouble("y1")), 
					new Coordinate(resultSet.getDouble("x2"), resultSet.getDouble("y2")));
			edgesSet.add(edge);
		}
		connection.close();
		return edgesSet;
		
	}
	
	public ArrayList<TrajectoryAsSet> readTrajectoriesAsSetWithEdges(String tableName, Timestamp startDate, Timestamp endDate) throws ClassNotFoundException, SQLException, IOException{
		Connection connection = ConnectionJDBC.getConnection();
		String strQuery = "select * from "+ tableName+" where date_time between ? and ? and edge_id is not null  order by id, date_time";
		PreparedStatement query = connection.prepareStatement(strQuery);
		ArrayList<Long> time_array = new ArrayList<>();
		ArrayList<Coordinate> coord_array = new ArrayList<>();
		ArrayList<Edge> edge_array = new ArrayList<>();
		ArrayList<TrajectoryAsSet> trajectories = new ArrayList<>();
		int idTraj = -1;
		
		query.setTimestamp(1, startDate);
		query.setTimestamp(2, endDate);
		
		ResultSet result = query.executeQuery();
		while (result.next()) {
			if(idTraj==-1){
				idTraj = result.getInt(1);
			}else{
				if(idTraj != result.getInt(1)){
					TrajectoryAsSet trajAsSet = new TrajectoryAsSet(idTraj, idTraj, time_array, coord_array, edge_array);
					trajectories.add(trajAsSet);
					time_array = new ArrayList<>();
					coord_array = new ArrayList<>();
					edge_array = new ArrayList<>();
					idTraj = result.getInt(1);
				}
			}
			time_array.add(result.getTimestamp(4).getTime());
			coord_array.add(new Coordinate(result.getDouble("longitude"), result.getDouble("latitude")));
			edge_array.add(new Edge(result.getInt("edge_id")));
		}
		
		
		connection.close();
		return trajectories;
	}
	
	// teste
	public static void main(String[] args) {
		
		TrajectoryPointDAO dao = new TrajectoryPointDAO();
		LocalDate d = LocalDate.of ( 2016 , 6 , 1 );
		ZoneId zoneId = ZoneId.of ( "GMT-3" );
		ZonedDateTime zdt = ZonedDateTime.of ( d , LocalTime.of(9, 30, 00) , zoneId );
		ZonedDateTime zdt2 = ZonedDateTime.of ( d , LocalTime.of(9, 30, 03) , zoneId );
		java.sql.Timestamp startDatetime = java.sql.Timestamp.from (zdt.toInstant() );
		System.out.println(startDatetime);
		java.sql.Timestamp endDatetime = java.sql.Timestamp.from ( zdt2.toInstant()  );
		System.out.println(endDatetime);
		try {
			ArrayList<TrajectoryAsSet> trajectoriesAsSet = dao.readTrajectoriesAsSetWithEdges("taxi_junho", startDatetime, endDatetime);
			System.out.println(trajectoriesAsSet);
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
