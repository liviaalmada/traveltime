package br.ufc.arida.analysis.model.cost;

import java.util.HashMap;
import java.util.Map;

import br.ufc.arida.analysis.model.measures.DistanceMeasure;

public class NetTrafficDistance implements DistanceMeasure<ProbabilisticCost> {

	private DistanceMeasure<ProbabilisticCost> pdfDistance;
	private double epsNet;
	private Map<Long, Map<Long, Double>> netDistance = new HashMap<>();

	public NetTrafficDistance(DistanceMeasure<ProbabilisticCost> pdfDistance, double epsNet) {
		this.pdfDistance = pdfDistance;
		this.epsNet = epsNet;
	}

	@Override
	public double calculate(ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		return pdfDistance.calculate(m1, m2);
	}

	public void putNetDistance(Long id1, Long id2, Double dist) {
		if (!netDistance.containsKey(id1)) {
			netDistance.put(id1, new HashMap<>());
		}
		if (!netDistance.containsKey(id2)) {
			netDistance.put(id2, new HashMap<>());
		}
		if (!netDistance.get(id1).containsKey(id2) || netDistance.get(id1).get(id2) > dist)
			netDistance.get(id1).put(id2, dist);
		if (!netDistance.get(id2).containsKey(id1) || netDistance.get(id2).get(id1) > dist)
			netDistance.get(id1).put(id2, dist);
	}
	
	public Double getNetDistance(Long id1, Long id2) {
		return netDistance.get(id1).get(id2);		
	}

	@Override
	public double calculate(Long id1, Long id2, ProbabilisticCost m1, ProbabilisticCost m2) throws Exception {
		Double netDistance = getNetDistance(id1, id2);
		if(netDistance!=null) return pdfDistance.calculate(m1, m2) + netDistance/epsNet;
		return pdfDistance.calculate(m1, m2);
	}

}
