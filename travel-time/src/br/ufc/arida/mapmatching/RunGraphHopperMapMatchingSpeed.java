package br.ufc.arida.mapmatching;

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
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.slf4j.LoggerFactory;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXExtension;
import com.graphhopper.matching.LocationIndexMatch;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.DistanceCalc2D;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import br.ufc.arida.analysis.dao.TrajectoryPointDAO;
import br.ufc.arida.traveltime.test.RunMapMatching;
import fr.david.netcutis.mapmatching.Edge;

public class RunGraphHopperMapMatchingSpeed {

	private static final CarFlagEncoder encoder = new CarFlagEncoder();
	private static GraphHopper hopper = new GraphHopper();
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunMapMatching.class);
	private static GeometryFactory geoFactory = JTSFactoryFinder.getGeometryFactory();

	public static void doImport() {
		hopper.setOSMFile("/media/livia/DATA/DADOS/osm-fortaleza.osm");
		hopper.setGraphHopperLocation("/home/livia/git/traveltime/travel-time/resources");
		hopper.setEncodingManager(new EncodingManager(encoder));
		hopper.setCHEnable(false);
		hopper.importOrLoad();
	}

	public static boolean isPointInLine(Point point, Point start, Point end) {
		if (start.distance(point) + point.distance(end) == start.distance(end))
			return true;

		return false;
	}

	public static boolean isPointInLine(Point point, LineString line) {
		if (line.distance(point) < 0.001)
			return true;
		return false;
	}

	public static double getDistanceInMeters(double angularDistance) {
		return angularDistance * (Math.PI / 180) * 6378137;
	}

	public static void estimateSpeed(List<EdgeMatch> matches) {
		List<EdgeMatch> linksToDistributeSpeed = new ArrayList<>();
		Map<Integer, Double> mapLinkToSpeed = new HashMap<>();
		double totalLenght = 0, startDelta = 0, endDelta = 0;
		boolean firstFound = false, lastFound = false;
		GPXEntry gpsLast = null, gpsFirst = null;
		Point last = null, first = null;
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
				gpsFirst = gpsCorrected.get(0).getEntry();
				first = geoFactory.createPoint(new Coordinate(gpsFirst.lon, gpsFirst.lat));
			}

			if (!lastFound) {
				gpsLast = gpsCorrected.get(gpsCorrected.size() - 1).getEntry();
				last = geoFactory.createPoint(new Coordinate(gpsLast.lon, gpsLast.lat));
			}

			for (int j = 0; j < coords.length - 1; j++) {
				LineString lineString = geoFactory.createLineString(Arrays.copyOfRange(coords, j, j + 2));
				if (!firstFound) {
					if (isPointInLine(first, lineString)) {
						firstFound = true;
						// start delta do inicio da linha até o ponto de gps
						startDelta += getDistanceInMeters(lineString.getStartPoint().distance(first));
						// total do gps até o fim da linha
						totalLenght += getDistanceInMeters(first.distance(lineString.getEndPoint()));
					} else {
						startDelta += getDistanceInMeters(lineString.getLength());
					}
				} else if (firstFound && !lastFound && gpsLast != null) {
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
				double speed = totalLenght / (gpsLast.getTime() - gpsFirst.getTime()) * 1000;
				System.out.println("Edge: " + edgeMatch.getEdgeState().getEdge() + " Time :"
						+ (gpsLast.getTime() - gpsFirst.getTime()) + " From, to: " + gpsFirst.getTime() + " "
						+ gpsLast.getTime() + " Length: " + totalLenght);
				mapLinkToSpeed.put(edgeMatch.getEdgeState().getEdge(), msTokmh(speed));
				for (EdgeMatch edge : linksToDistributeSpeed) {
					mapLinkToSpeed.put(edge.getEdgeState().getEdge(), msTokmh(speed));
				}
				linksToDistributeSpeed.clear();
				totalLenght = 0;
				endDelta = 0;
				startDelta = 0;
				firstFound = false;
				lastFound = false;
			}

		}

		System.out.println(mapLinkToSpeed);
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
			// Read osm file as a Graph

			doImport();

			// create MapMatching object, can and should be shared accross
			// threads
			GraphHopperStorage graph = hopper.getGraphHopperStorage();
			FlagEncoder firstEncoder = hopper.getEncodingManager().fetchEdgeEncoders().get(0);
			int gpxAccuracy = 110;
			log.info("Setup lookup index. Accuracy filter is at " + gpxAccuracy + "m");
			LocationIndexTree paramIndex = (LocationIndexTree) hopper.getLocationIndex();
			LocationIndexMatch locationIndex = new LocationIndexMatch(graph, paramIndex, gpxAccuracy);
			MapMatching mapMatching = new MapMatching(graph, locationIndex, firstEncoder);
			mapMatching.setSeparatedSearchDistance(150);
			mapMatching.setMaxNodesToVisit(1000);
			mapMatching.setForceRepair(true);

			int startDay = Integer.parseInt(args[0]);
			int endDay = Integer.parseInt(args[1]);// including start day and
													// excluding
			// end day
			int startHour = 14; // Integer.parseInt(args[2]),
			int endHour = 17; // Integer.parseInt(args[3]); // including
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
									Path path = mapMatching.calcPath(mr);
									// System.out.println(path.getDistance());
									// System.out.println(mr.getMatchLength());
									// System.out.println(mr.getMatchMillis());
									//
									// PointList points = path.calcPoints();
									// System.out.println(points);
									// System.out.println(path.calcEdges());
									// System.out.println(path.getTime());
									estimateSpeed(matches);
									PointList pointList = matches.get(0).getEdgeState().fetchWayGeometry(3);
									// pointList.
									matches.get(0).getEdgeState().getEdge();
									matches.get(0).getGpxExtensions().get(0).getEntry().getTime();

									// now do something with the edges like
									// storing
									// the edgeIds or doing
									// fetchWayGeometry etc
									count++;
									System.out.println(count);
									matches.get(0).getEdgeState();
									System.out.println(matches);
									return;

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
