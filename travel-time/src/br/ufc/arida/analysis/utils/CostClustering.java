package br.ufc.arida.analysis.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graphast.util.ProbabilisticEdgesUtils;

import com.jmef.ExponentialFamily;
import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;

import br.ufc.arida.analysis.dao.ConstrainedTrajectoryDao;
import br.ufc.arida.analysis.dao.ProbabilisticCostsDAO;
import br.ufc.arida.analysis.timeseries.model.CostTimeSeries;

/**
 * This class implements the CostClustering algorithm proposed by Yang, Bin,
 * Chenjuan Guo, and Christian S. Jensen in the paper "Travel cost inference
 * from sparse, spatio temporally correlated time series using Markov models."
 * on Proceedings of the VLDB Endowment 6.9 (2013): 769-780.
 * 
 * @author livia
 *
 */
public class CostClustering {

	public static int mCounts = 15;

	public static PVector[] getListAsPVectorArray(List<Double> values) {
		PVector[] pVector = new PVector[values.size()];
		for (int i = 0; i < pVector.length; i++) {
			pVector[i] = new PVector(1);
			pVector[i].array[0] = values.get(i);
		}
		return pVector;
	}

	public static void printMixtureModel(MixtureModel gmm, String nameFile) {
		// Store the points in a textual file.
		try {
			PrintWriter out = new PrintWriter(nameFile);
			out.println("# mean  variance  weight");

			for (int i = 0; i < gmm.size; i++) {

				String parametersStr = gmm.param[i].toString();
				parametersStr = parametersStr.replace("(", "");
				parametersStr = parametersStr.replace(")", "");
				parametersStr = parametersStr.replaceAll("(\\s+)", " ");
				out.print(parametersStr.substring(0, parametersStr.length()));
				out.println(" " + gmm.weight[i] + "");

			}
			out.flush();
			out.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void printValues(List<Double> values, String nameFile) {
		// Store the points in a textual file.
		try {
			PrintWriter out = new PrintWriter(nameFile);
			for (int i = 0; i < values.size(); i++) {

				out.println(values.get(i));

			}
			out.flush();
			out.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Produce a sequence of temporal Gaussian Mixture Models, given a time
	 * series ts and a Gaussian Mixture Model for the compact (aggregated) time
	 * series. The number of inervals is the same of ts.
	 * 
	 * @param ts
	 * @param gmm
	 * @return
	 */
	public static MixtureModel[] temporalGMM(CostTimeSeries ts, MixtureModel gmm) {

		int m = ts.getNumberOfIntervals();
		MixtureModel[] temporalGMM = new MixtureModel[m];
		UnivariateGaussian gaussian = new UnivariateGaussian();

		// Produce new weights for the gaussian mixture model according to the
		// interval x
		for (int x = 0; x < m; x++) {
			temporalGMM[x] = gmm.clone();

			// If the number of observations is enough, otherwise the weights
			// will be the original
			if (ts.getTravelCostSet(x).size() > mCounts) {
				boolean converge = false;
				PVector[] costSet = getListAsPVectorArray(ts.getTravelCostSet(x).getCosts());
				do {
					System.out.println("Não convergiu...");
					double[] sum = new double[gmm.size];

					for (PVector cost : costSet) {
						double[] lh = new double[gmm.size];
						double sumLh = 0;
						for (int k = 0; k < gmm.size; k++) {
							lh[k] = temporalGMM[x].weight[k] * gaussian.density(cost, (PVector) gmm.param[k]);
							sumLh = sumLh + lh[k];
						}
						for (int k = 0; k < gmm.size; k++) {
							lh[k] = lh[k] / sumLh;
							sum[k] = sum[k] + lh[k];
						}
						converge = true;
						for (int k = 0; k < gmm.size; k++) {
							double previsouWeight = temporalGMM[x].weight[k];
							temporalGMM[x].weight[k] = sum[k] / costSet.length;
							if (Math.abs(previsouWeight - temporalGMM[x].weight[k]) > 0.1) {
								converge = false;
							}
						}
					}

				} while (!converge);
			}
		}

		return temporalGMM;
	}

	/**
	 * Extracts a list of weights for the temporal GMM model according to the
	 * time interval
	 * 
	 * @param temporalGMM
	 * @return
	 */
	public static List<double[]> compactToPoints(MixtureModel[] temporalGMM) {
		double[] kl = new double[temporalGMM.length];
		int numOfComponentes = temporalGMM[0].weight.length;
		double[] fx = new double[numOfComponentes + 1];
		List<double[]> f = new ArrayList<>(temporalGMM.length);

		for (int x = 0; x < temporalGMM.length; x++) {
			kl[x] = MixtureModel.KLDMC(temporalGMM[x - 1], temporalGMM[x], 100);
			if (x == 0)
				kl[x] = MixtureModel.KLDMC(temporalGMM[x], temporalGMM[temporalGMM.length - 1], 100);
			for (int i = 0; i < temporalGMM[x].weight.length; i++) {
				fx[i] = temporalGMM[x].weight[i];
			}
			fx[numOfComponentes] = kl[x];
			f.add(1, fx);
		}
		return f;
	}

}
