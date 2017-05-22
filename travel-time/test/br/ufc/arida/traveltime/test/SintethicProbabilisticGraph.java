package br.ufc.arida.traveltime.test;

import org.graphast.model.EdgeImpl;
import org.graphast.model.NodeImpl;

import br.ufc.arida.analysis.model.ProbabilisticGraph;
import br.ufc.arida.analysis.model.cost.GaussianCost;
import br.ufc.arida.analysis.model.cost.GaussianParser;
import br.ufc.arida.analysis.model.cost.ProbabilisticCost;

public class SintethicProbabilisticGraph {

	public static ProbabilisticGraph generate(long nLines, long nNodesByLine, int numIntervals) {
		ProbabilisticGraph g = new ProbabilisticGraph("/home/livia/dados/", new GaussianParser());

		for (int i = 0; i < nNodesByLine * nLines; i++) {
			NodeImpl n = new NodeImpl(i, 0.001, 0.002);
			g.addNode(n);
		}

		for (int i = 0; i < nLines; i++) {
			for (int j = 0; j < nNodesByLine; j++) {
				if (i < nLines - 1) {
					long fromNode = (i * nNodesByLine) + j;
					long toNode = ((i + 1) * nNodesByLine) + j;
					System.out.println("edge: "+fromNode+" "+toNode);
					g.addEdge(new EdgeImpl(fromNode, toNode, 1));
					System.out.println("edge: "+toNode+" "+fromNode);
					g.addEdge(new EdgeImpl(toNode, fromNode, 1));
				}
				if (j < nNodesByLine - 1) {
					long fromNode = (i * nNodesByLine) + j;
					long toNode = fromNode + 1;
					System.out.println("edge: "+fromNode+" "+toNode);
					g.addEdge(new EdgeImpl(fromNode, toNode, 1));
					System.out.println("edge: "+toNode+" "+fromNode);
					g.addEdge(new EdgeImpl(toNode, fromNode, 1));
				}
			}
		}

		generateSintethicCosts(g);

		return g;
	}

	public static void generateSintethicCosts(ProbabilisticGraph g) {
		for (int i = 0; i < g.getNumberOfEdges(); i++) {
			System.out.println("Gen to edge "+i);
			g.addProbabilisticCost(i, generateSintethicCosts(g.getNumberOfIntervals()));
		}
	}

	private static ProbabilisticCost[] generateSintethicCosts(int i) {
		GaussianCost[] costs = new GaussianCost[i];
		for (int j = 0; j < costs.length; j++) {
			double mean = 20.+ Math.random()*15.;
			costs[j] = new GaussianCost(mean,  Math.random()*3.);
		}
		return costs;
	}

	public static void main(String[] args) {
		ProbabilisticGraph graph = SintethicProbabilisticGraph.generate(100, 100, 1);
	}
}
