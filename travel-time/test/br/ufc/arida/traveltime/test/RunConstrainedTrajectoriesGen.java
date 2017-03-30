package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import br.ufc.arida.analysis.dao.ConstrainedTrajectoryDao;
import br.ufc.arida.analysis.dao.TrajectoryPointDAO;
import fr.david.netcutis.datamodel.TrajectoryAsSet;
import fr.david.netcutis.mapmatching.ConstrainedTrajectory;

public class RunConstrainedTrajectoriesGen {
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunConstrainedTrajectoriesGen.class);

	public static void main(String[] args) {
		TrajectoryPointDAO dao = new TrajectoryPointDAO();
		int startDay=8, endDay=10;
		for (int i = startDay; i < endDay; i++) {
			LocalDate d = LocalDate.of(2016, 6, i);
			ZoneId zoneId = ZoneId.of("GMT-3");
			int start = 0;
			while (start < 24) {
				ZonedDateTime zdt = ZonedDateTime.of(d, LocalTime.of(start, 00, 00), zoneId);
				ZonedDateTime zdt2 = ZonedDateTime.of(d, LocalTime.of(start, 59, 00), zoneId);
				java.sql.Timestamp startDatetime = java.sql.Timestamp.from(zdt.toInstant());
				java.sql.Timestamp endDatetime = java.sql.Timestamp.from(zdt2.toInstant());
				ArrayList<TrajectoryAsSet> trajectoriesAsSetList;
				ArrayList<ConstrainedTrajectory> constTrajectoryList = new ArrayList<>();
				ConstrainedTrajectoryDao cDao = new ConstrainedTrajectoryDao();
				try {
					log.debug("Reading trajectories from " + startDatetime + " to " + endDatetime);
					trajectoriesAsSetList = dao.readTrajectoriesAsSetWithEdges("taxi_junho", startDatetime, endDatetime);
					log.debug(trajectoriesAsSetList.size() + " trajectories were retrieved...\n");
					for (TrajectoryAsSet trajectoryAsSet : trajectoriesAsSetList) {
						try {
							constTrajectoryList.add(new ConstrainedTrajectory(trajectoryAsSet));

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					cDao.save(constTrajectoryList);
					

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
				start++;
			}

			
		}
		
	}
}
