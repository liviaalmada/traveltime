package org.graphast.util;


// *** IMPORTS *** //

import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.PrintWriter;
import java.io.IOException;

import com.jmef.BregmanSoftClustering;
import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;
import com.jmef.tools.ExpectationMaximization1D;
import com.jmef.tools.KMeans;



/**
 * This class provides some static methods that help to generate edges that have associated
 * a set of probabilistic costs.
 * 
 * @author Francesco Lettich 
 *
 */
public class ProbabilisticEdgesUtils 
{
	// *** PRIVATE STATIC METHODS *** //
	
	private static void printVectorDoubles(PVector[] points, String nameFile)
	{
		// Store the points in a textual file.
		try
		{
			PrintWriter out = new PrintWriter(nameFile);
		
			for(int i = 0; i < points.length; i++)
			{
				out.println(Double.toString(points[i].array[0]));
			}
			out.flush();
			out.close();

		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Compute the mixtures from points drawn from a mixture of Gaussian distributions.
	 * 
	 * @param m Number of points drawn from the mixture.
	 * 
	 * @return Vector containing the points generated from the mixture.
	 */
	private static PVector[] testGaussian(int m)
	{	
		// TODO: move this example in some test!
		
		// Initial model : Gaussian
		MixtureModel f = new MixtureModel(3);	 // Mixture of Gaussians with 3 components.
		
		
		// Selection of the exponential family (the argument here is the variance associated with the distribution).
		f.EF        = new UnivariateGaussian();

		// Setup of the weights that we assign to each Gaussian component.
		f.weight[0] = 1.0/3.0;					
		f.weight[1] = 1.0/2.0;
		f.weight[2] = 1.0/8.0;

		// Setup of the parameters ("mu" and "sigma") associated with the components.
		PVector p1  = new PVector(2);
		PVector p2  = new PVector(2);
		PVector p3  = new PVector(2);
		p1.array[0] = 1; p1.array[1] = 10;
		p2.array[0] = 70; p2.array[1] = 10;
		p3.array[0] = 253; p3.array[1] = 18;

		// Associate the parameters to each component.
		f.param[0]  = p1;
		f.param[1]  = p2;
		f.param[2]  = p3;
		

		// *** In this part we draw a set of points from the mixture, and try to reconstruct the mixture *** //
		// *** according to the data extracted from it 							 						 *** //
		PVector[] points = f.drawRandomPoints(m);
		System.out.printf("Numero di punti: %d - %d\n", points.length, points[0].getDimension());
		System.out.printf("Test: %s\n", Double.toString(points[0].array[0]));
		printVectorDoubles(points, "./prova.txt");


		// Return
		return points;
	}
	
	
	
	// *** PUBLIC METHODS *** //
	
	/**
	 * This method returns the value that the pdf of a standardized normal distribution
	 * returns when passing x.
	 * 
	 * @param x The value given to the pdf.
	 * @return The value of the density function.
	 */
    public static double pdf(double x) 
    {
        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
    }

    /**
	 * This method returns the value that the pdf of a normal distribution
	 * returns when passing x.
	 * 
	 * @param x The value given to the pdf.
	 * @return The value of the density function.
	 */
    public static double pdf(double x, double mu, double sigma) 
    {
        return pdf((x - mu) / sigma) / sigma;
    }
	
	/**
	 * This method computes the cdf related to a standardized normal distribution. The method
	 * uses a method based on Taylor's expansion.
	 * 
	 * @param x The value.
	 * @return The cdf at "x".
	 */
	public static double cdf(double x) 
	{
        if (x < -8.0) return 0.0;
        if (x >  8.0) return 1.0;
        double sum = 0.0, term = x;
        for (int i = 3; sum + term != sum; i += 2) 
        {
            sum  = sum + term;
            term = term * x * x / i;
        }
        return 0.5 + sum * pdf(x);
    }
	
	/**
	 * This method computes the cdf related to a normal distribution.
	 * 
	 * @param x The value.
	 * @return The cdf at "x".
	 */
	public static double cdf(double x, double mu, double sigma) 
	{
        return cdf((x - mu) / sigma);
    }
	
	
	/**
	 * This method computes the cumulative distribution function for a given "x" value.
	 * 
	 * @param x the input of the CDF.
	 * @return The CDF's output value.
	 */
	public static double computeCDFMixture(MixtureModel f, double x)
	{
		// TODO: Da testare!! In teoria dovrebbe andare sulle singole componenti...da testare sulle mixture
		//		 multi-componente.
		double value = 0;
		PVector[] parametersComponent = (PVector[]) f.param;
		for(int i = 0; i < f.size; i++)
		{
			// Calculation => i_th Gaussian component's weight * cdf(x, i_th component's mu, i_th component's sigma)
			value += f.weight[i] * ProbabilisticEdgesUtils.cdf(x, parametersComponent[i].array[0], parametersComponent[i].array[1]);
		}
		
		return(value);
	}
	
	/**
	 * This method computes the probability density function for a given "x" value.
	 * 
	 * @param x the input of the PDF.
	 * @return The PDF's output value.
	 */
	public static double computePDFMixture(MixtureModel f, double x)
	{
		// TODO: Da testare!!
		PVector value = new PVector(1); value.array[0] = x;
		return(f.density(value));
	}
	
	/**
	 * Computes the log likelihood of a given mixture.
	 * @param   setPoints  Set of points.
	 * @param   f Mixture model.
	 * @return  Log-likelihood.
	 */
	public static double computeLogLikelihood(PVector[] setPoints, MixtureModel f)
	{
		// NOTE: the idea is that, the more the mixture "f" fits the data in "setPoints", the higher
		// is the ll value. Ideally, a perfect mixture achieves a score of 0.
		double ll = 0;
		for(int i = 0; i < setPoints.length; i++)
			ll += Math.log(f.density(setPoints[i]));
		
		return(ll);
	}
	
	/**
	 * This static method returns a mixture of Gaussian distribution from a set of values.
	 * The method uses the Bregman soft clustering. 
	 * 
	 * @param setValues The values from which we want to build the mixture.
	 * @param numClusters The number of clusters to be used with the internal kMeans.
	 * 
	 * @return A mixture of Gaussian distributions.
	 */
	public static MixtureModel produceMixtureBregmanSoft(PVector[] setValues, int numClusters)
	{
		// Clustering phase.
		Vector<PVector>[] clusters = KMeans.run(setValues, numClusters);
		
		// Estimation phase.
		MixtureModel mmef;
		mmef = BregmanSoftClustering.initialize(clusters, new UnivariateGaussian());
		mmef = BregmanSoftClustering.run(setValues, mmef);

		return(mmef);
	}
	
	/**
	 * This static method returns a mixture of Gaussian distribution from a set of values.
	 * The method uses the classical Expectation Maximization approach. 
	 * 
	 * @param setValues The values from which we want to build the mixture.
	 * @param numClusters The number of clusters to be used with the internal kMeans.
	 * 
	 * @return A mixture of Gaussian distributions.
	 */
	public static MixtureModel produceMixtureEM(PVector[] setValues, int numClusters)
	{
		// Clustering phase.
		Vector<PVector>[] clusters = KMeans.run(setValues, numClusters);
		
		// Estimation phase.
		MixtureModel mmc;
		mmc = ExpectationMaximization1D.initialize(clusters);
		mmc = ExpectationMaximization1D.run(setValues, mmc);

		return(mmc);
	}
	
	/**
	 * This static method returns a mixture of Gaussian distribution from a set of values.
	 * The method uses the classical Expectation Maximization approach combined with a k-fold
	 * cross-validation approach. 
	 * 
	 * @param setValues The values from which we want to build the mixture.
	 * @param k The number of partitions into which we split "setValues" to perform the
	 * 		  k-fold cross validation.
	 * 
	 * @return A mixture of Gaussian distributions.
	 */
	public static MixtureModel produceMixtureKFold(PVector[] setValues, int k)
	{	
		// *** Generate a random permutation of "setValues" *** //
		List<PVector> setValuesPermuted = new ArrayList<>(Arrays.asList(setValues));
		Collections.shuffle(setValuesPermuted);
		
		
		// *** Partition the data *** //
		int sizePartition = setValues.length / k;
		List<PVector[]> partitions = new ArrayList<>(Collections.nCopies(k, new PVector[sizePartition]));
		for(int i = 0; i < k; i++)
		{
			int offset = i * sizePartition;
			for(int j = 0; j < sizePartition; j++)
				(partitions.get(i))[j] = setValuesPermuted.get(offset + j);
		}
		setValuesPermuted = null;
		
		
		// *** Perform the k-fold cross-validation. *** //
		double llscoreNew = Double.NEGATIVE_INFINITY, llscoreOld = Double.NEGATIVE_INFINITY;
		MixtureModel mmcOld = null, mmcNew = null;
		List<PVector> tmpList = null;
		int numClusters = 0;
		do
		{
			// Perform the cross validation.
			numClusters++; 
			mmcOld = mmcNew; 
			llscoreOld = llscoreNew; llscoreNew = 0;
			for(int idxPartition = 0; idxPartition < k; idxPartition++)
			{
				// *** Generate the mixture, from the training set, by means of the Bregman soft clustering approach *** //
				mmcNew = ProbabilisticEdgesUtils.produceMixtureBregmanSoft(partitions.get(idxPartition), numClusters);
				
				// *** Test phase *** //
				// 1 - Generate the test set from the partitions.
				tmpList = new ArrayList<>();
				for(int idx = 0; idx < k; idx++) 
					if(idx != idxPartition) tmpList.addAll(Arrays.asList(partitions.get(idx)));
				
				// 2 - Accumulate the log-likelihood scores.
				llscoreNew += ProbabilisticEdgesUtils.computeLogLikelihood(tmpList.toArray(new PVector[tmpList.size()]), mmcNew);
			}
		} while(llscoreNew > llscoreOld);

		
		return(mmcOld);
	}
	
		
	
	
	// *** PUBLIC STATIC METHODS *** //
	
	/*public static void main(String[] args) 
	{
		// TODO: Read the information from which we want to build a mixture from a file.
		
		// TODO: Implement the refined approach proposed by Jensen.
		
		// Generate the mixture.
		// TODO: reactivate!
		// MixtureModel mmef = produceMixture(setPoints, 3);
	}*/
}
