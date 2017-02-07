package br.uf.arida.traveltime.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;

import fr.david.netcutis.datamodel.TrajectoryAsSet;
import fr.david.netcutis.mapmatching.Edge;

public class ConstrainedTrajectory {
	private TrajectoryAsSet trajectoryAsSet;
	private ArrayList<Edge> edgesSeq;
	private ArrayList<Timestamp> startTimesSeq;
	// private ArrayList<Timestamp> endTimesSeq;
	private ArrayList<Long> travelTimesSeq;

	public ConstrainedTrajectory(TrajectoryAsSet trajectoryAsSet) throws Exception {
		if (trajectoryAsSet != null && trajectoryAsSet.getEdge_array() != null) {
			this.trajectoryAsSet = trajectoryAsSet;
			this.edgesSeq = new ArrayList<>();
			this.startTimesSeq = new ArrayList<>();
			this.travelTimesSeq = new ArrayList<>();

			Iterator<Edge> iterator = trajectoryAsSet.getEdge_array().iterator();
			Edge last = null;
			int itArrayIdx = 0, startIdx = 0, endIdx = 0;

			while (iterator.hasNext()) {
				Edge next = iterator.next();
				if (last != null && last.getRoadID() != next.getRoadID()) {
					endIdx = itArrayIdx - 1;
					edgesSeq.add(trajectoryAsSet.getEdgeArrayElement(startIdx));
					Timestamp startTime = new Timestamp(trajectoryAsSet.getTsArrayElement(startIdx));
					Timestamp endTime = new Timestamp(trajectoryAsSet.getTsArrayElement(itArrayIdx));
					startTimesSeq.add(startTime);
					travelTimesSeq.add(endTime.getTime() - startTime.getTime());
					startIdx = itArrayIdx;
				}
				last = next;
				itArrayIdx++;
			}

			endIdx = itArrayIdx - 1;
			edgesSeq.add(trajectoryAsSet.getEdgeArrayElement(startIdx));
			Timestamp startTime = new Timestamp(trajectoryAsSet.getTsArrayElement(startIdx));
			Timestamp endTime = new Timestamp(trajectoryAsSet.getTsArrayElement(endIdx));
			startTimesSeq.add(startTime);
			travelTimesSeq.add(endTime.getTime() - startTime.getTime());
			startIdx = itArrayIdx;
		} else {
			throw new Exception("This trajectory does not have mapped edges.");
		}
	}
	
	public Edge getEdgeAt(int index){
		return edgesSeq.get(index);
	}
	
	public Timestamp getStartTimeAt(int index){
		return startTimesSeq.get(index);
	}
	
	public Long getTravelTimeAt(int index){
		return travelTimesSeq.get(index);
	}
	
	public int size(){
		return edgesSeq.size();
	}

	public String toString() {
		String ret = "";
		for (int i = 0; i < edgesSeq.size(); i++) {
			ret += "Edge " + edgesSeq.get(i).getRoadID() + "; start at " + startTimesSeq.get(i) + " spent "
					+ travelTimesSeq.get(i) + " ms \n";
		}
		return ret;
	}

	//teste
	public static void main(String[] args) {
		ArrayList<Edge> edge_arrray = new ArrayList<>();
		ArrayList<Coordinate> coord_array = new ArrayList<>();
		ArrayList<Long> time_array = new ArrayList<>();
		int tid = 1;
		int uid = 0;

		edge_arrray.add(new Edge(1, 1, 2));
		edge_arrray.add(new Edge(1, 1, 2));
		edge_arrray.add(new Edge(2, 2, 3));
		edge_arrray.add(new Edge(2, 2, 3));
		edge_arrray.add(new Edge(3, 2, 3));

		time_array.add((long) 1000);
		time_array.add((long) 1050);
		time_array.add((long) 1070);
		time_array.add((long) 1100);
		time_array.add((long) 1102);

		TrajectoryAsSet traj = new TrajectoryAsSet(uid, tid, time_array, coord_array, edge_arrray);
		System.out.println(traj);
		try {
			ConstrainedTrajectory ctraj = new ConstrainedTrajectory(traj);
			System.out.println(ctraj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public TrajectoryAsSet getTrajectoryAsSet() {
		return trajectoryAsSet;
	}

	public void setTrajectoryAsSet(TrajectoryAsSet trajectoryAsSet) {
		this.trajectoryAsSet = trajectoryAsSet;
	}

}
