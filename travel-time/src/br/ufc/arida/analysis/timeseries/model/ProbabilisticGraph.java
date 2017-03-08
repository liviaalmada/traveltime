package br.ufc.arida.analysis.timeseries.model;

import java.util.HashMap;
import java.util.Map;

import org.graphast.model.GraphImpl;
import org.graphast.util.FileUtils;

import com.jmef.MixtureModel;

import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

public class ProbabilisticGraph extends GraphImpl {

	private static final long serialVersionUID = 1L;
	
	private int numberOfIntervals = 96;

	private Map<Long, MixtureModel[]> probabilisticCosts;

	public ProbabilisticGraph(String directory) {
		super(directory);
		probabilisticCosts = new HashMap<>();
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

	private void saveProbabilisticCosts() {
		// TODO Auto-generated method stub
		// TODO save the number of intervals 
		for (long idEdge : probabilisticCosts.keySet()) {
			String directoryRoot = absoluteDirectory + "/probabilistic_costs/" + idEdge + "/";
			FileUtils.createDir(directoryRoot);
			MixtureModel[] mixtureModels = probabilisticCosts.get(idEdge);
			for (int i = 0; i < mixtureModels.length; i++) {
				MixtureModel.save(mixtureModels[i], directoryRoot + String.valueOf(i));
			}
		}
	}

	private void loadProbabilisticCosts() {
		// TODO como fazer para ler os custos?
		IntBigArrayBigList edges = getEdges();
		for (Integer id : edges) {
			String directoryRoot = absoluteDirectory + "/probabilistic_costs/" + id + "/";
			MixtureModel[] mixtureModels = new MixtureModel[numberOfIntervals];
			for(int i = 0; i<numberOfIntervals; i++){
				MixtureModel gmm = MixtureModel.load(directoryRoot+i);
				mixtureModels[i]= gmm;
			}
			addProbabilisticCost(id, mixtureModels);
		}
	}

	public void addProbabilisticCost(long edgeId, MixtureModel[] costs) {
		probabilisticCosts.put(edgeId, costs);
	}
	
	public MixtureModel[] getProbabilisticCosts(long id){
		return probabilisticCosts.get(id);
	}

	public int getNumberOfIntervals() {
		return numberOfIntervals;
	}

	public void setNumberOfIntervals(int n) {
		this.numberOfIntervals = n;
	}
	
	

}
