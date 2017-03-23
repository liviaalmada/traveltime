package br.ufc.arida.mapmatching;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXFile;
import com.graphhopper.matching.LocationIndexMatch;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;

import br.ufc.arida.analysis.dao.TrajectoryPointDAO;
import br.ufc.arida.probabilisticgraph.dao.postgis.trajectory.TrajectoryReader;
import br.ufc.arida.traveltime.test.RunMapMatching;
import fr.david.netcutis.datamodel.TrajectoryAsSet;
import fr.david.netcutis.mapmatching.Edge;
import fr.david.netcutis.mapmatching.MapMatchingTrajectory;

public class RunGraphHopperMapMatching {
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunMapMatching.class);

	public static void main(String[] args) {

		if (args.length == 4) {
			System.out.println(args.length);

			GraphHopper hopper = new GraphHopperOSM();
			hopper.setDataReaderFile("/media/livia/DATA/DADOS/osm-fortaleza.osm");
			hopper.setGraphHopperLocation("/home/livia/git/traveltime/travel-time/resources");
			CarFlagEncoder encoder = new CarFlagEncoder();
			hopper.setEncodingManager(new EncodingManager(encoder));
			hopper.getCHFactoryDecorator().setEnabled(false);
			hopper.importOrLoad();

			// create MapMatching object, can and should be shared accross
			// threads

			GraphHopperStorage graph = hopper.getGraphHopperStorage();
			// LocationIndexMatch locationIndex = new LocationIndexMatch(graph,
			// (LocationIndexTree) hopper.getLocationIndex());
			AlgorithmOptions chOpts = AlgorithmOptions.start().maxVisitedNodes(3000)
					.hints(new PMap().put(Parameters.CH.DISABLE, false)).build();

			MapMatching mapMatching = new MapMatching(hopper, chOpts);
			// mapMatching.setTransitionProbabilityBeta(0.009);
			mapMatching.setMeasurementErrorSigma(150);

			int startDay = Integer.parseInt(args[0]);
			int endDay = Integer.parseInt(args[1]);// including start day and
													// excluding
			// end day
			int startHour = Integer.parseInt(args[2]), endHour = Integer.parseInt(args[3]); // including
																							// startHour
																							// and
			// excluding endHour
			TrajectoryPointDAO dao = new TrajectoryPointDAO();
			ZoneId zoneId = ZoneId.of("GMT-3");
			Set<Edge> edges;

			try {

				edges = dao.getEdges();
				int count = 0;
				log.info(edges.size() + " edges were retrieved...\n");
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
							// TODO LER TRAJETORIAS NO FORMATO GRAPH HOPPER
							Map<Integer, List<GPXEntry>> trajectories = dao.readTrajectoriesAsGPXEntries("taxi_junho",
									startDatetime, endDatetime);
							List<MatchResult> results = new ArrayList<>();

							for (List<GPXEntry> trajs : trajectories.values()) {
								// do the actual matching, get the GPX entries
								// from a file or via stream
								try {
									log.info("started!");
									MatchResult mr = mapMatching.doWork(trajs);
									results.add(mr);
									// return GraphHopper edges with all
									// associated
									// GPX entries

									List<EdgeMatch> matches = mr.getEdgeMatches();
									System.out.println(matches);
									// now do something with the edges like
									// storing
									// the edgeIds or doing
									// fetchWayGeometry etc
									count++;
									System.out.println(count);
									matches.get(0).getEdgeState();
								} catch (Exception e) {
									e.printStackTrace();
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
