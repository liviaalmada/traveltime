package fr.david.netcutis.mapmatching;


import com.vividsolutions.jts.geom.Coordinate;

public class Vertex {

	private int vertex_id;
	private Coordinate vertex_coord;
	
	
	public Vertex(int vertex_id, Coordinate vertex_coord) {
		this.setVertex_coord(vertex_coord);
		this.setVertex_id(vertex_id);
	}


	public int getVertex_id() {
		return vertex_id;
	}


	public void setVertex_id(int vertex_id) {
		this.vertex_id = vertex_id;
	}


	public Coordinate getVertex_coord() {
		return vertex_coord;
	}


	public void setVertex_coord(Coordinate vertex_coord) {
		this.vertex_coord = vertex_coord;
	}
	
}
