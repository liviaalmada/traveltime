package br.ufc.arida.analysis.model.measures;

import org.graphast.query.route.shortestpath.ShortestPathService;
import org.graphast.query.route.shortestpath.astar.AStarConstantWeight;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
// TODO
public class SpatialKLDivergence implements DistanceMeasure {
	
	private ProbabilisticGraph graph;
	private ShortestPathService pathService; 
	
	public SpatialKLDivergence(ProbabilisticGraph graph) {
		this.graph = graph;
		pathService = new AStarConstantWeight(graph);
	}

	@Override
	public double calculate(Object m1, Object m2){// throws CostNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

}
