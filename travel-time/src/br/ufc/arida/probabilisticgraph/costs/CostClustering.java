package br.ufc.arida.probabilisticgraph.costs;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graphast.util.ProbabilisticEdgesUtils;

import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;

import br.ufc.arida.probabilisticgraph.dao.postgis.trajectory.ConstrainedTrajectoryDao;
import br.ufc.arida.probabilisticgraph.trajectory.TravelCostTimeSeries;

/**
 * This class implements the ClostClustering algorithm proposed by Yang, Bin,
 * Chenjuan Guo, and Christian S. Jensen in the paper "Travel cost inference
 * from sparse, spatio temporally correlated time series using Markov models."
 * on Proceedings of the VLDB Endowment 6.9 (2013): 769-780.
 * 
 * @author livia
 *
 */
public class CostClustering {

	public static int mCounts = 50;

	/**
	 * 
	 * @param compTimeSeries, compact time cost series
	 * @param f, paramater for f-fold evaluation
	 * @return GMM, a Gaussian Mixture Model
	 */
	public static MixtureModel produceCostClustering(TravelCostTimeSeries compTimeSeries, int f) {

		MixtureModel preGmm = null, newGmm = null;
		double preLH = Double.NEGATIVE_INFINITY, newLH = Double.NEGATIVE_INFINITY;
		int k = 0;
		List<Double> cc = compTimeSeries.getUnionOfTravelTimeCosts();
		Collections.shuffle(cc);
		int n = cc.size() / f;

		do {
			preGmm = newGmm;
			preLH = newLH;
			newLH = 0;
			k++;

			// f-fold likelihood evaluation
			for (int i = 0; i < f; i++) {
				List<Double> test = cc.subList(i * n, (i + 1) * n);
				List<Double> train = new ArrayList<Double>();

				if (i > 0)
					train.addAll(cc.subList(0, i * n));
				train.addAll(cc.subList((i + 1) * n, cc.size()));
				double betterLH = Double.NEGATIVE_INFINITY, actualLH = Double.NEGATIVE_INFINITY;
				MixtureModel gmmCandidate;
				PVector[] trainPVector = getListAsPVectorArray(train);
				PVector[] testPVector = getListAsPVectorArray(test);

				// take the best model in three times
				for (int j = 0; j < 3; j++) {

					gmmCandidate = ProbabilisticEdgesUtils.produceMixtureBregmanSoft(trainPVector, k);
					actualLH = ProbabilisticEdgesUtils.computeLogLikelihood(testPVector, gmmCandidate);
					if (actualLH > betterLH) {
						betterLH = actualLH;
						newGmm = gmmCandidate;
					}
				}
				newLH = newLH + betterLH;
			}
		} while (newLH > preLH);

		return preGmm;

	}

	private static PVector[] getListAsPVectorArray(List<Double> values) {
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
	 * Produce a sequence of temporal Gaussian Mixture Models, given a time series ts and a Gaussian Mixture Model
	 * for the compact (aggregated) time series. The number of inervals is the same of ts.
	 * @param ts
	 * @param gmm
	 * @return
	 */
	public static MixtureModel[] temporalGMM(TravelCostTimeSeries ts, MixtureModel gmm) {

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
	 * Extracts a list of weights for the temporal GMM model according to the time interval
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

	// *** PUBLIC STATIC METHODS *** //

	public static void main(String[] args) {
		// Define a set of values
		ConstrainedTrajectoryDao dao = new ConstrainedTrajectoryDao();
		try {
			TravelCostTimeSeries ts1 = dao.getTravelCostTimeSeries(20988);
			
			
			TravelCostTimeSeries ts2 = dao.getTravelCostTimeSeries(41218);

			//List<Double> unionOfTravelTimeCosts = travelCostTimeSeries.getUnionOfTravelTimeCosts();
			
			MixtureModel model1 = CostClustering.produceCostClustering(ts1, 10);
			MixtureModel model2 = CostClustering.produceCostClustering(ts2, 10);
			
			printValues(ts1.getTravelCostSet(0).getCosts(), "values20988T0");
			printValues(ts2.getTravelCostSet(0).getCosts(), "values41218T0");
			
			MixtureModel[] temporalGMM1 = CostClustering.temporalGMM(ts1, model1);
			MixtureModel[] temporalGMM2 = CostClustering.temporalGMM(ts2, model2);
			
			CostClustering.printMixtureModel(temporalGMM1[0], "./gmm01");
			CostClustering.printMixtureModel(temporalGMM2[0], "./gmm02");
			
			double correlation = PDFSimilarityUtils.calculateCorrelation(temporalGMM1[0], temporalGMM2[0], 100);
			double kld = PDFSimilarityUtils.calculateKLDivergence(temporalGMM1[0], temporalGMM2[0], 100);
			System.out.println(correlation);
			System.out.println(kld);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
