package br.ufc.arida.probabilisticgraph.trajectory;

import java.util.ArrayList;
import java.util.List;

public class TravelCostSet {
	private int intervalSequence;
	private List<Double> costs;
	
	public TravelCostSet(int intervalSequence) {
		this.intervalSequence = intervalSequence;
		this.costs = new ArrayList<>();
	}
	
	public TravelCostSet(int intervalSequence, List<Double> costs) {
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

}
