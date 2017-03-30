package br.ufc.arida.traveltime.test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import br.ufc.arida.analysis.dao.TrajectoryPointDAO;
import fr.david.netcutis.datamodel.TrajectoryAsSet;
import fr.david.netcutis.mapmatching.Edge;
import fr.david.netcutis.mapmatching.MapMatchingTrajectory;

public class RunMapMatching {
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunMapMatching.class);

	public static void main(String[] args) {
		// args: startDay, endDay, startHour, endHour

		if (args.length == 4) {
			System.out.println(args.length);

			int startDay = Integer.parseInt(args[0]);
			int endDay = Integer.parseInt(args[1]);// including start day and excluding
											// end day
			int startHour = Integer.parseInt(args[2]), endHour = Integer.parseInt(args[3]); // including startHour and
												// excluding endHour
			TrajectoryPointDAO dao = new TrajectoryPointDAO();
			ZoneId zoneId = ZoneId.of("GMT-3");
			Set<Edge> edges;

			try {

				edges = dao.getEdges();
				
				log.info(edges.size() + " edges were retrieved...\n");
				MapMatchingTrajectory mapmatching = new MapMatchingTrajectory(edges);
				for (int day = startDay; day < endDay; day++) {
					for (int hour = startHour; hour < endHour; hour++) {
						LocalDate d = LocalDate.of(2016, 6, day);
						ZonedDateTime zdt = ZonedDateTime.of(d, LocalTime.of(hour, 00, 00), zoneId);
						ZonedDateTime zdt2 = ZonedDateTime.of(d, LocalTime.of(hour, 59, 59), zoneId);

						java.sql.Timestamp startDatetime = java.sql.Timestamp.from(zdt.toInstant());
						System.out.println(startDatetime.toString());
						java.sql.Timestamp endDatetime = java.sql.Timestamp.from(zdt2.toInstant());
						System.out.println(endDatetime.toString());
						try {
							ArrayList<TrajectoryAsSet> trajectoriesAsSet = dao.readTrajectoriesAsSet("taxi_junho",
									startDatetime, endDatetime);
							log.info(trajectoriesAsSet.size() + " trajectories were retrieved...\n");
							Vector<TrajectoryAsSet> trajectoriesAsSetVector = new Vector<>(trajectoriesAsSet);
							System.out.println("starting map-matching...\n");
							Vector<TrajectoryAsSet> mapMatching2 = mapmatching.mapMatching(trajectoriesAsSetVector);
							System.out.println("update database...\n");
							dao.updateMapMatching(mapMatching2, "taxi_junho");
							System.out.println("finished!\n");
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
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}
}
