package br.ufc.arida.probabilisticgraph.costs;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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

	/**
	 * 
	 * @param compTimeSeries,a
	 *            compact time cost series
	 * @param f,
	 *            paramater for f-fold evaluation
	 * @return GMM, a Gaussian Mixture Model
	 */
	public static MixtureModel produceCostClustering(TravelCostTimeSeries compTimeSeries, int f) {

		MixtureModel preGmm = null, newGmm = null;
		double preLH = Double.NEGATIVE_INFINITY, newLH = Double.NEGATIVE_INFINITY;
		int k = 0;
		List<Double> cc = compTimeSeries.getUnionOfTravelTimeCosts();
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
				
				if(i>0)train.addAll(cc.subList(0, i * n));
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
	
	private static void printMixtureModel(MixtureModel gmm, String nameFile)
	{
		// Store the points in a textual file.
		try
		{
			PrintWriter out = new PrintWriter(nameFile);
			
			for(int i = 0; i < gmm.size; i++)
			{
				out.print(gmm.weight[i]+"");
				String parametersStr = gmm.param[i].toString();				
				parametersStr = parametersStr.replace("(", "");
				parametersStr = parametersStr.replace(")", "" );
				parametersStr = parametersStr.replaceAll("(\\s+)", " , ");
				out.println(parametersStr.substring(0, parametersStr.length()-3));
				
			}
			out.flush();
			out.close();

		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	// *** PUBLIC STATIC METHODS *** //

	public static void main(String[] args) {
		// Define a set of values
		ConstrainedTrajectoryDao dao = new ConstrainedTrajectoryDao();
		try {
			TravelCostTimeSeries travelCostTimeSeries = dao.getTravelCostTimeSeries(20988);
			MixtureModel gmm = CostClustering.produceCostClustering(travelCostTimeSeries, 10);
			printMixtureModel(gmm, "./testeGMM");
			//MixtureModel.save(gmm, "./TesteSaveMM.txt");
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
