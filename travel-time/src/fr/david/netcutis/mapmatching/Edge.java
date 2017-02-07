package fr.david.netcutis.mapmatching;


import com.vividsolutions.jts.geom.Coordinate;

public class Edge {

	private int id;
	private int road_id;
	private double offset;
	private int vertex_source_id;
	private int vertex_target_id;
	private Coordinate source;
	private Coordinate target;
	
	
	public Edge(int road_id) {
		this.setRoadID(road_id);
	}
	
	
	
	public Edge(int road_id, int vertex_source_id, int vertex_target_id){
		this.setRoadID(road_id);
		this.setVertex_source_id(vertex_source_id);
		this.setVertex_target_id(vertex_target_id);
	}
	
	public Edge(int road_id, int vertex_source_id, int vertex_target_id, Coordinate c_source, Coordinate c_target){
		this.setRoadID(road_id);
		this.setVertex_source_id(vertex_source_id);
		this.setVertex_target_id(vertex_target_id);
		this.setTarget(c_target);
		this.setSource(c_source);
	}
	
	
	public void set(int road_id, int vertex_source_id, int vertex_target_id){
		this.setRoadID(road_id);
		this.setVertex_source_id(vertex_source_id);
		this.setVertex_target_id(vertex_target_id);
	}
	
	public void set(int road_id, int vertex_source_id, int vertex_target_id, Coordinate c){
		this.setRoadID(road_id);
		this.setVertex_source_id(vertex_source_id);
		this.setVertex_target_id(vertex_target_id);
		this.setSource(c);
	}
	
	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public Coordinate getSource() {
		return source;
	}

	public void setSource(Coordinate source) {
		this.source = source;
	}

	public Coordinate getTarget() {
		return target;
	}

	public void setTarget(Coordinate target) {
		this.target = target;
	}

	public int getRoadID() {
		return road_id;
	}

	public void setRoadID(int road_id) {
		this.road_id = road_id;
	}

	public int getVertex_source_id() {
		return vertex_source_id;
	}

	public void setVertex_source_id(int vertex_source_id) {
		this.vertex_source_id = vertex_source_id;
	}

	public int getVertex_target_id() {
		return vertex_target_id;
	}

	public void setVertex_target_id(int vertex_target_id) {
		this.vertex_target_id = vertex_target_id;
	}
	
	
}
