package br.ufc.arida.probabilisticgraph.costs;

import com.jmef.MixtureModel;
import com.jmef.PVector;

public class PDFSimilarityUtils {

	public static void getHistogramRepresentation() {

	}

	public static void teste(MixtureModel q, MixtureModel p, int n) {

		PVector parMaxQ = (PVector) q.param[0];
		PVector parMaxP = (PVector) p.param[0];

		double maxQ = parMaxQ.array[0] + parMaxQ.array[1];
		double maxP = parMaxP.array[0] + parMaxP.array[1];

		for (int i = 1; i < n; i++) {

			parMaxQ = (PVector) q.param[i];
			parMaxP = (PVector) p.param[i];

			double valueQ = parMaxQ.array[0] + parMaxQ.array[1];
			double valueP = parMaxP.array[0] + parMaxP.array[1];

			if (valueP > maxP)
				maxP = valueP;
			if (valueQ > maxQ)
				maxQ = valueQ;

		}

		System.out.println(maxQ);
		System.out.println(maxP);
	}

	public static double calculateCorrelation(MixtureModel q, MixtureModel p, int n) {
		PVector[] points = q.drawRandomPoints(n);
		return correlation(q, p, points);
	}

	public static double correlation(MixtureModel q, MixtureModel p, PVector[] points) {

		double meanQ = 0, meanP = 0, frac1 = 0, frac21 = 0, frac22 = 0;

		for (PVector pVector : points) {
			meanQ += q.density(pVector);
			meanP += p.density(pVector);
		}

		meanQ = meanQ / points.length;
		meanP = meanP / points.length;

		for (PVector pVector : points) {
			frac1 += (q.density(pVector) - meanQ) * (p.density(pVector) - meanP);
			frac21 += Math.pow(q.density(pVector) - meanQ, 2);
			frac22 += Math.pow(p.density(pVector) - meanP, 2);
		}

		return frac1 / Math.sqrt(frac21 * frac22);
	}

	public static double calculateKLDivergence(MixtureModel q, MixtureModel p, int n) {
		return MixtureModel.KLDMC(q, p, n);
	}

}
