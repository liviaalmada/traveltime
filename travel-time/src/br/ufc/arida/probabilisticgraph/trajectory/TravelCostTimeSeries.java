package br.ufc.arida.probabilisticgraph.trajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.david.netcutis.mapmatching.Edge;

public class TravelCostTimeSeries {
	private Edge edge;
	private int edgeId;
	private int numberOfIntervals;
	private Map<Integer, TravelCostSet> travelCostSetMap;

	public TravelCostTimeSeries(int edgeId, int numberOfIntervals) {
		super();
		this.edgeId = edgeId;
		this.numberOfIntervals = numberOfIntervals;
		this.travelCostSetMap = new HashMap<>();
	}

	public List<Double> getUnionOfTravelTimeCosts() {
		ArrayList<Double> allCosts = new ArrayList<>();
		for (TravelCostSet travelCostSet : travelCostSetMap.values()) {
			allCosts.addAll(travelCostSet.getCosts());
		}
		return allCosts;
	}

	public void addTravelCost(Double value, int interval) {
		if (!travelCostSetMap.containsKey(interval)) {
			TravelCostSet travelCostSet = new TravelCostSet(interval);
			travelCostSetMap.put(interval, travelCostSet);
		}
	
		travelCostSetMap.get(interval).addCost(value);

	}

	public void addTravelCostSet(TravelCostSet travelCostSet) {
		travelCostSetMap.put(travelCostSet.getIntervalSequence(), travelCostSet);
	}

	public TravelCostSet getTravelCostSet(int interval) {
		return travelCostSetMap.get(interval);
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	public int getNumberOfIntervals() {
		return numberOfIntervals;
	}

	public void setNumberOfIntervals(int numberOfIntervals) {
		this.numberOfIntervals = numberOfIntervals;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(int edgeId) {
		this.edgeId = edgeId;
	}

}
