package br.ufc.arida.analysis.model;

import java.util.HashMap;
import java.util.Map;

import org.graphast.exception.GraphastException;
import org.graphast.model.GraphImpl;
import org.graphast.util.FileUtils;

import br.ufc.arida.analysis.model.cost.ICostParser;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class ProbabilisticGraph extends GraphImpl implements IProbabilisticGraph{

	private static final long serialVersionUID = 1L;
	
	private int numberOfIntervals = 96;

	private Map<Long, ProbabilisticCost[]> probabilisticCosts;
	
	private ICostParser parser;

	public ProbabilisticGraph(String directory, ICostParser parser) {
		super(directory);
		probabilisticCosts = new HashMap<>();
		this.parser = parser;
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		super.load();
		loadProbabilisticCosts();
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		super.save();
		saveProbabilisticCosts();

	}
	
	@Override
	public void saveProbabilisticCosts() {
		// TODO Auto-generated method stub
		// TODO save the number of intervals 
		
		for (long idEdge : probabilisticCosts.keySet()) {
			System.out.println("saving...");
			String directoryRoot = absoluteDirectory + "/probabilistic_costs/";
			FileUtils.createDir(directoryRoot);
			ProbabilisticCost[] costs = probabilisticCosts.get(idEdge);
			for (int i = 0; i < numberOfIntervals; i++) {
				
				//MixtureModel.save(mixtureModels[i].mm, directoryRoot + String.valueOf(i));
				try {
					parser.save(costs[i], directoryRoot + String.valueOf(idEdge+"_"+i));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}

	@Override
	public void loadProbabilisticCosts( ) {
		// TODO como fazer para ler os custos?
		long edges = getNumberOfEdges();
		for (long id = 0; id < edges; id++) {
			String directoryRoot = absoluteDirectory + "/probabilistic_costs/";
			ProbabilisticCost[] costs = new ProbabilisticCost[numberOfIntervals];
			for(int i = 0; i<numberOfIntervals; i++){
				ProbabilisticCost cost = parser.load(directoryRoot+id+"_"+i);
				costs[i]= cost;
			}
			addProbabilisticCost(id, costs);	
		}
	}

	@Override
	public void addProbabilisticCost(long edgeId, ProbabilisticCost[] costs) {
		if(edgeId < getNumberOfEdges())
			probabilisticCosts.put(edgeId, costs);
		else throw new GraphastException("Edge not found.");
	}
	
	@Override
	public ProbabilisticCost[] getProbabilisticCosts(long id){
		return probabilisticCosts.get(id);
	}
	
	/**
	 * The probabilistic distribution function in a edge 
	 * @param edgeId
	 * @param timeInterval
	 * @return
	 */
	@Override
	public ProbabilisticCost getProbabilisticCosts(long edgeId, int timeInterval){
		ProbabilisticCost[] costs = probabilisticCosts.get(edgeId);
		if(costs!=null && timeInterval<costs.length)
			return costs[timeInterval];
		return null;
	}

	@Override
	public int getNumberOfIntervals() {
		return numberOfIntervals;
	}

	@Override
	public void setNumberOfIntervals(int n) {
		this.numberOfIntervals = n;
	}

	public void addProbabilisticCost(long edgeId, int time, ProbabilisticCost newCost) {
		if(edgeId < getNumberOfEdges()&& time <= maxTime) {
			ProbabilisticCost[] pCosts = getProbabilisticCosts(edgeId);
			if(pCosts==null){
				pCosts = new ProbabilisticCost[numberOfIntervals];
				probabilisticCosts.put(edgeId, pCosts);
			}
			pCosts[time]=newCost;
		} else throw new GraphastException("Edge not found.");
	}
	
	

}
