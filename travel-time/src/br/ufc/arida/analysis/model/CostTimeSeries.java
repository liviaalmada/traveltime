package br.ufc.arida.analysis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostTimeSeries {
	
	private long edgeId;
	private double edgeMaxSpeedMS;
	private double edgeLenghtMtr;
	private int numberOfIntervals;
	private Map<Integer, CostSet> travelCostSetMap;
	
	public CostTimeSeries(long edgeId, int numberOfIntervals, double maxSpeed, double leght) {
		super();
		this.edgeMaxSpeedMS = maxSpeed;
		this.edgeLenghtMtr = leght;
		this.edgeId = edgeId;
		this.numberOfIntervals = numberOfIntervals;
		this.travelCostSetMap = new HashMap<>();
	}

	public CostTimeSeries(long edgeId, int numberOfIntervals) {
		super();
		this.edgeId = edgeId;
		this.numberOfIntervals = numberOfIntervals;
		this.travelCostSetMap = new HashMap<>();
	}

	public ArrayList<Double> getUnionOfTravelTimeCosts() {
		ArrayList<Double> allCosts = new ArrayList<>();
		for (CostSet travelCostSet : travelCostSetMap.values()) {
			allCosts.addAll(travelCostSet.getCosts());
		}
		return allCosts;
	}
	
	public double[] getUnionOfTravelTimeCostsAsArray() {
		ArrayList<Double> allCosts = new ArrayList<>();
		for (CostSet travelCostSet : travelCostSetMap.values()) {
			allCosts.addAll(travelCostSet.getCosts());
		}
		
		double[] array = allCosts.stream().mapToDouble(Double::doubleValue).toArray();
		return array;
	}
	
	public void addTravelCost(Double value, int interval) {
		if (!travelCostSetMap.containsKey(interval)) {
			CostSet travelCostSet = new CostSet(interval);
			travelCostSetMap.put(interval, travelCostSet);
		}
	
		travelCostSetMap.get(interval).addCost(value);

	}

	public void addTravelCostSet(CostSet travelCostSet) {
		travelCostSetMap.put(travelCostSet.getIntervalSequence(), travelCostSet);
	}

	public CostSet getTravelCostSet(int interval) {
		if(!travelCostSetMap.containsKey(interval))
			travelCostSetMap.put(interval, new CostSet(interval));
		return travelCostSetMap.get(interval);
	}
	
	public Collection<CostSet> getTravellCostSetList() {
		return travelCostSetMap.values();
	}
	
	public double[] arrayOfCosts(int interval) {
		if( !travelCostSetMap.containsKey(interval)){
			travelCostSetMap.put(interval, new CostSet(interval));
		}		
		return travelCostSetMap.get(interval).getCostsArray(); 
	}

	public int getNumberOfIntervals() {
		return numberOfIntervals;
	}

	public void setNumberOfIntervals(int numberOfIntervals) {
		this.numberOfIntervals = numberOfIntervals;
	}

	public long getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(int edgeId) {
		this.edgeId = edgeId;
	}

	public double getEdgeLenghtMtr() {
		return edgeLenghtMtr;
	}

	public void setEdgeLenghtMtr(double edgeLenghtMtr) {
		this.edgeLenghtMtr = edgeLenghtMtr;
	}

	public double getEdgeMaxSpeedMS() {
		return edgeMaxSpeedMS;
	}

	public void setEdgeMaxSpeedMS(double edgeMaxSpeedMS) {
		this.edgeMaxSpeedMS = edgeMaxSpeedMS;
	}

}
