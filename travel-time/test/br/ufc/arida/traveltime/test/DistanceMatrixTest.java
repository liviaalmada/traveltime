package br.ufc.arida.traveltime.test;

import com.jmef.MixtureModel;

import br.ufc.arida.analysis.model.ProbabilisticDistanceAnalysis;
import br.ufc.arida.analysis.model.ProbabilisticGraph;

public class DistanceMatrixTest {
	public static void main(String[] args) {
		ProbabilisticGraph graph = new ProbabilisticGraph("graph-prob");
		graph.load();
		ProbabilisticDistanceAnalysis m = new ProbabilisticDistanceAnalysis(graph, graph.getDirectory()+"/outputDistance");
		m.calculateForIntervalTime(48);
		m.saveDissimilarityMatrix();
	}

}
