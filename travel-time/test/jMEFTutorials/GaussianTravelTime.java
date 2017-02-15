package jMEFTutorials;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import com.jmef.MixtureModel;
import com.jmef.PVector;
import com.jmef.UnivariateGaussian;
import com.jmef.tools.ExpectationMaximization1D;
import com.jmef.tools.KMeans;

import br.ufc.arida.probabilisticgraph.dao.postgis.trajectory.ConstrainedTrajectoryDao;

public class GaussianTravelTime {
	public static void main(String[] args) {

		ConstrainedTrajectoryDao dao = new ConstrainedTrajectoryDao();
		try {
			ArrayList<Double> listPointsGroupedByEdge = dao.listPointsGroupedByEdge(7, 12, 51165);
			int size = listPointsGroupedByEdge.size();	
			System.out.println(size);
			
			if(size > 10){
				PVector[] points = new PVector[size];

				for (int i = 0; i < points.length; i++) {
					PVector v = new PVector(1);
					v.array[0] = listPointsGroupedByEdge.get(i);
					points[i] = v;
				}

				Vector<PVector>[] clusters = KMeans.run(points, 10);

				// Classical EM
				MixtureModel mmc;
				mmc = ExpectationMaximization1D.initialize(clusters);
				mmc = ExpectationMaximization1D.run(points, mmc);
				mmc.EF = new UnivariateGaussian();
				System.out.println("Mixure model estimated using classical EM \n" + mmc+ "\n");
				
				for (int i = 0; i < mmc.param.length; i++) {
					
					System.out.println(mmc.param[i].toString());
				}
			}
		
			
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
