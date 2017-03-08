package br.ufc.arida.analysis.timeseries.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jmef.PVector;

public class CostSet {
	private int intervalSequence;
	private ArrayList<Double> costs;
	private double[] costsArray;
	
	
	public CostSet(int intervalSequence) {
		this.intervalSequence = intervalSequence;
		this.costs = new ArrayList<>();
		
		
	}
	
	public CostSet(int intervalSequence, List<Double> costs) {
		this.intervalSequence = intervalSequence;
		this.costs = new ArrayList<>(costs.size());
		for (Double value : costs) {
			this.costs.add(value);
		}
	}
	
	public void addCost(Double value){
		this.costs.add(value);
	}

	public int getIntervalSequence() {
		return intervalSequence;
	}

	public void setIntervalSequence(int intervalSequence) {
		this.intervalSequence = intervalSequence;
	}

	public List<Double> getCosts() {
		return costs;
	}
	
	public List<Double> getCostsSorted(){
		Collections.sort(costs);
		return costs;
	}

	public int size() {
		// TODO Auto-generated method stub
		return this.costs.size();
	}

	public double[] getCostsArray() {
		if(costsArray==null || costsArray.length != costs.size())
			costsArray = costs.stream().mapToDouble(Double::doubleValue).toArray();
		return costsArray;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return costs.toString();
	}

}
