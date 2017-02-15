package br.ufc.arida.probabilisticgraph.dao.postgis.trajectory;

import java.time.LocalDateTime;

public class TrajectoryPoint {
	
	double lat;
	double lon;
	int id_taxi;
	LocalDateTime dateTime;
	
	public TrajectoryPoint(double lat, double lon, int id_taxi, LocalDateTime dateTime) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.id_taxi = id_taxi;
		this.dateTime = dateTime;
	}
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public int getId_taxi() {
		return id_taxi;
	}
	public void setId_taxi(int id_taxi) {
		this.id_taxi = id_taxi;
	}
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	

}
