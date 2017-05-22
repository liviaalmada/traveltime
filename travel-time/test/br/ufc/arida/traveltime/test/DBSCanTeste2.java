package br.ufc.arida.traveltime.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufc.arida.analysis.algortihms.DBScan;
import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.measures.CosineDistance;
import br.ufc.arida.dao.ProbabilisticCostsDAO;

public class DBSCanTeste2 {
	public static void main(String[] args) {

		try {
			ProbabilisticGraph graph = SintethicProbabilisticGraph.generate(10, 10, 1);

			DBScan alg = new DBScan(graph, new CosineDistance());
			Map<Integer, List<Long>> map = alg.run(0.5, 1, 2, 0);
			for (Entry<Integer, List<Long>> e : map.entrySet()) {
				for (Long l : e.getValue()) {
					System.out.println(e.getKey() + " , " + l + " " + graph.getProbabilisticCosts(l, 0));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
