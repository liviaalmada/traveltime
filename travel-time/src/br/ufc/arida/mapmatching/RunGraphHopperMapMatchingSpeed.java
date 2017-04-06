package br.ufc.arida.mapmatching;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.slf4j.LoggerFactory;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXExtension;
import com.graphhopper.matching.LocationIndexMatch;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import br.ufc.arida.analysis.dao.TrajectoryPointDAO;
import br.ufc.arida.traveltime.test.RunMapMatching;
import fr.david.netcutis.mapmatching.Edge;

public class RunGraphHopperMapMatchingSpeed {

	private static final CarFlagEncoder encoder = new CarFlagEncoder();
	private static GraphHopper hopper = new GraphHopper();
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunMapMatching.class);
	private static GeometryFactory geoFactory = JTSFactoryFinder.getGeometryFactory();

	private static class SpeedMatch {
		EdgeMatch edgeMacth;
		double speed;
		double timestamp;
		double travelTime;

	}

	public static void doImport(String osmFilePath, String graphHopperLocation) {
		hopper.setOSMFile(osmFilePath);
		hopper.setGraphHopperLocation(graphHopperLocation);
		hopper.setEncodingManager(new EncodingManager(encoder));
		hopper.setCHEnable(false);
		hopper.importOrLoad();
	}

	public static boolean isPointInLine(Point point, LineString line) {
		if (getDistanceInMeters(line.distance(point)) < 0.1)
			return true;
		return false;
	}

	public static double getDistanceInMeters(double angularDistance) {
		return angularDistance * (Math.PI / 180) * 6378137;
	}

	private static Map<Integer, SpeedMatch> estimateSpeed(List<EdgeMatch> matches) {
		List<EdgeMatch> linksToDistributeSpeed = new ArrayList<>();
		Map<Integer, SpeedMatch> mapLinkToSpeed = new HashMap<>();
		double totalLenght = 0, endDelta = 0;
		boolean firstFound = false, lastFound = false;
		GHPoint3D gpsLast = null, gpsFirst = null;
		Point last = null, first = null;
		long timeFirst = 0, timeLast = 0;
		for (int i = 0; i < matches.size(); i++) {
			EdgeMatch edgeMatch = matches.get(i);

			// Get gps points in the actual edge
			List<GPXExtension> gpsCorrected = edgeMatch.getGpxExtensions();
			// Get edge geometry
			PointList geometry = edgeMatch.getEdgeState().fetchWayGeometry(3);
			Coordinate[] coords = getCoordinates(geometry);
			linksToDistributeSpeed.add(edgeMatch);

			if (gpsCorrected.size() <= 1) {
				LineString lineString = geoFactory.createLineString(coords);
				totalLenght += getDistanceInMeters(lineString.getLength());
				continue;
			}

			if (!firstFound) {
				gpsFirst = gpsCorrected.get(0).getQueryResult().getSnappedPoint();
				timeFirst = gpsCorrected.get(0).getEntry().getTime();
				first = geoFactory.createPoint(new Coordinate(gpsFirst.lon, gpsFirst.lat));
			}

			if (!lastFound) {
				gpsLast = gpsCorrected.get(gpsCorrected.size() - 1).getQueryResult().getSnappedPoint();
				last = geoFactory.createPoint(new Coordinate(gpsLast.lon, gpsLast.lat));
				timeLast = gpsCorrected.get(gpsCorrected.size() - 1).getEntry().getTime();
			}

			for (int j = 0; j < coords.length - 1; j++) {
				LineString lineString = geoFactory.createLineString(Arrays.copyOfRange(coords, j, j + 2));
				if (!firstFound) {
					if (isPointInLine(first, lineString)) {
						firstFound = true;
						totalLenght += getDistanceInMeters(first.distance(lineString.getEndPoint()));
					}
				}

				if (firstFound && !lastFound && gpsLast != null) {
					if (isPointInLine(last, lineString)) {
						lastFound = true;
						// end delta do ponto de gps até o final da linha
						endDelta += getDistanceInMeters(last.distance(lineString.getEndPoint()));

						// total do início da linha até o ponto de gps
						totalLenght += getDistanceInMeters(lineString.getStartPoint().distance(last));

					} else {
						totalLenght += getDistanceInMeters(lineString.getLength());
					}
				} else {
					endDelta += getDistanceInMeters(lineString.getLength());
				}
			}

			if (firstFound && lastFound) {
				double speed = msTokmh(totalLenght / (timeLast - timeFirst) * 1000);
				linksToDistributeSpeed.add(edgeMatch);
				double nextTimestamp = -1;

				for (EdgeMatch edge : linksToDistributeSpeed) {
					SpeedMatch speedMatch = new SpeedMatch();
					speedMatch.edgeMacth = edgeMatch;
					speedMatch.speed = speed;

					if (nextTimestamp == -1) {
						speedMatch.timestamp = edgeMatch.getGpxExtensions().get(0).getEntry().getTime();
						GHPoint3D point = edgeMatch.getGpxExtensions().get(0).getQueryResult().getSnappedPoint();
						Point p = geoFactory.createPoint(new Coordinate(point.lat, point.lon));
						PointList pointList = edgeMatch.getEdgeState().fetchWayGeometry(3);
						double distanceInMeters = getDistanceInMeters(p.distance(
								geoFactory.createPoint(new Coordinate(pointList.getLongitude(pointList.getSize() - 1),
										pointList.getLatitude(pointList.getSize() - 1)))));
						speedMatch.travelTime = distanceInMeters / speed;
						nextTimestamp = speedMatch.timestamp + speedMatch.travelTime;
					} else {
						speedMatch.timestamp = nextTimestamp;
						PointList pointList = edgeMatch.getEdgeState().fetchWayGeometry(3);
						Point p1 = geoFactory
								.createPoint(new Coordinate(pointList.getLongitude(0), pointList.getLatitude(0)));
						Point p2 = geoFactory
								.createPoint(new Coordinate(pointList.getLongitude(pointList.getSize() - 1),
										pointList.getLatitude(pointList.getSize() - 1)));
						double distanceInMeters = getDistanceInMeters(p1.distance(p2));

						speedMatch.travelTime = distanceInMeters / speed;
						nextTimestamp = speedMatch.timestamp + speedMatch.travelTime;
					}

					mapLinkToSpeed.put(edge.getEdgeState().getEdge(), speedMatch);
				}
				linksToDistributeSpeed.clear();
				totalLenght = endDelta;
				endDelta = 0;
				firstFound = true;
				lastFound = false;
				timeFirst = timeLast;
				gpsFirst = gpsLast;
			}

		}

		return mapLinkToSpeed;
	}

	private static double msTokmh(double speed) {
		return speed * 3.6;
	}

	private static Coordinate[] getCoordinates(PointList g) {
		Coordinate[] coords = new Coordinate[g.size()];

		for (int i = 0; i < coords.length; i++) {
			coords[i] = new Coordinate(g.getLon(i), g.getLat(i));
		}

		return coords;
	}

	public static void main(String[] args) {

		if (args.length == 4) {
			int startDay = 1; // Integer.parseInt(args[0]);
			int endDay = 7;// Integer.parseInt(args[1]);// including start day
							// and
							// excluding
			// end day
			int startHour = 0; // Integer.parseInt(args[2]),
			int endHour = 23; // Integer.parseInt(args[3]); // including
								// startHour
								// and
			// Read osm file as a Graph
			String osm = "/media/livia/DATA/DADOS/osm-fortaleza.osm";
			String graphHopper = "/home/livia/git/traveltime/travel-time/resources";
			try {
				FileWriter writer = new FileWriter(new File("map-matching-output"));
				doImport(osm, graphHopper);
				List<MatchResult> doMatching = doMatching(startDay, endDay, startHour, endHour);
				for (MatchResult matchResult : doMatching) {
					Map<Integer, SpeedMatch> estimatedSpeed = estimateSpeed(matchResult.getEdgeMatches());
					doSaveMapMatching(writer, estimatedSpeed);
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static void doSaveMapMatching(FileWriter writer, Map<Integer, SpeedMatch> mapLinkToSpeed) {
		try {

			for (SpeedMatch speedMath : mapLinkToSpeed.values()) {
				writer.write(speedMath.edgeMacth.getEdgeState().getEdge() + ", " + speedMath.timestamp + ", "
						+ speedMath.speed + "\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static List<MatchResult> doMatching(int startDay, int endDay, int startHour, int endHour) {
		// create MapMatching object, can and should be shared across
		// threads
		GraphHopperStorage graph = hopper.getGraphHopperStorage();
		FlagEncoder firstEncoder = hopper.getEncodingManager().fetchEdgeEncoders().get(0);
		int gpxAccuracy = 110;
		LocationIndexTree paramIndex = (LocationIndexTree) hopper.getLocationIndex();
		LocationIndexMatch locationIndex = new LocationIndexMatch(graph, paramIndex, gpxAccuracy);
		MapMatching mapMatching = new MapMatching(graph, locationIndex, firstEncoder);
		mapMatching.setMaxNodesToVisit(1000);
		mapMatching.setForceRepair(true);
		TrajectoryPointDAO dao = new TrajectoryPointDAO();
		ZoneId zoneId = ZoneId.of("GMT-3");

		Set<Edge> edges;

		try {

			edges = dao.getEdges();
			log.info(edges.size() + " edges were retrieved...\n");
			for (int day = startDay; day < endDay; day++) {
				// for (int hour = startHour; hour < endHour; hour++) {
				LocalDate d = LocalDate.of(2016, 6, day);
				ZonedDateTime zdt = ZonedDateTime.of(d, LocalTime.of(startHour, 00, 00), zoneId);
				ZonedDateTime zdt2 = ZonedDateTime.of(d, LocalTime.of(endHour, 59, 59), zoneId);

				java.sql.Timestamp startDatetime = java.sql.Timestamp.from(zdt.toInstant());
				java.sql.Timestamp endDatetime = java.sql.Timestamp.from(zdt2.toInstant());
				log.info("Getting trajectories from " + startDatetime + " to " + endDatetime);

				try {
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
							// associated GPX entries

						} catch (Exception e) {
							// e.printStackTrace();
						}

					}

					return results;

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

			// }
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
		return null;

	}

}
