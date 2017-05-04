package br.ufc.arida.analysis.algortihms;

public class NeighborEdge {
	private long idEdge;
	private long idOutEdge;
	private Double distanceValue;

	public NeighborEdge(long idEdge, long idOutEdge, Double distanceValue) {
		super();
		this.idEdge = idEdge;
		this.idOutEdge = idOutEdge;
		this.distanceValue = distanceValue;
	}

	public long getIdEdge() {
		return idEdge;
	}

	public void setIdEdge(long idEdge) {
		this.idEdge = idEdge;
	}

	public long getIdOutEdge() {
		return idOutEdge;
	}

	public void setIdOutEdge(long idOutEdge) {
		this.idOutEdge = idOutEdge;
	}

	public Double getDistanceValue() {
		return distanceValue;
	}

	public void setDistanceValue(double distanceMeasure) {
		this.distanceValue = distanceMeasure;
	}

	@Override
	public String toString() {
		return "[" + idEdge + "," + idOutEdge + "," + distanceValue + "]";

	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return arg0 instanceof NeighborEdge && ((NeighborEdge) arg0).idEdge == idEdge
				&& ((NeighborEdge) arg0).idOutEdge == idOutEdge;
	}

}