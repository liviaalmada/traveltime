package fr.david.netcutis.mapmatching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Line;
import com.vividsolutions.jts.geom.Coordinate;

import br.ufc.arida.traveltime.test.RunMapMatching;
import fr.david.netcutis.datamodel.TrajectoryAsSet;

public class MapMatchingTrajectory {

	private BufferedWriter mapMatching_file;
	private Set<Edge> edge_array;
	private Edge[] edge_array_vector;
	private RTree<Integer, Line> rtree;
	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RunMapMatching.class);

	public MapMatchingTrajectory(Set<Edge> edge_array) {
		this.edge_array = edge_array;
		rtree = RTree.create();
		edge_array_vector = new Edge[edge_array.size()];
		int j = 0;
		for (Edge edge : edge_array) {
			edge_array_vector[j] = edge;
			j++;
		}

		for (int i = 0; i < edge_array.size(); i++) {
			rtree = rtree.add(new Entry<Integer, Line>(i,
					Geometries.line(edge_array_vector[i].getSource().x, edge_array_vector[i].getSource().y,
							edge_array_vector[i].getTarget().x, edge_array_vector[i].getTarget().y)));

		}
	}

	public Edge getMatchedEgde(Coordinate c0) {

		List<Entry<Integer, Line>> edgeEntry = rtree.nearest(Geometries.point(c0.x, c0.y), 10000, 1).toList()
				.toBlocking().single();
		if (edgeEntry.size() > 0) {
			return edge_array_vector[edgeEntry.get(0).value()];
		}
		return null;
	}

	public void mapMatching(Vector<TrajectoryAsSet> objects, String file) throws IOException, SQLException {

		int length;
		Edge edge;
		mapMatching_file = new BufferedWriter(new FileWriter(file));
		double offset;

		for (TrajectoryAsSet trajectoryAsSet : objects) {
			length = trajectoryAsSet.getCoordArray().size();

			for (int i = 0; i < length; i++) {
				edge = getMatchedEgde(trajectoryAsSet.getCoordArray().get(i));
				trajectoryAsSet.getEdge_array().add(edge);

				offset = getEuclideanDistance(edge.getSource(), trajectoryAsSet.getCoordArray().get(i));

				// tid, edge_id, offset, source_id, target_id, time, x, y
				mapMatching_file.write(trajectoryAsSet.getTid() + ";" + edge.getRoadID() + ";" + offset + ";"
						+ edge.getVertex_source_id() + ";" + edge.getVertex_target_id() + ";"
						+ trajectoryAsSet.getTsArrayElement(i) + ";" + trajectoryAsSet.getCoordArray().get(i).x + ";"
						+ trajectoryAsSet.getCoordArray().get(i).y + "\n");

			}
		}
		mapMatching_file.close();
	}

	public Vector<TrajectoryAsSet> mapMatching(Vector<TrajectoryAsSet> objects) throws IOException, SQLException {

		int length;
		Edge edge;
		// double offset;

		for (TrajectoryAsSet trajectoryAsSet : objects) {
			length = trajectoryAsSet.getCoordArray().size();

			for (int i = 0; i < length; i++) {
				edge = getMatchedEgde(trajectoryAsSet.getCoordArray().get(i));
				trajectoryAsSet.getEdge_array().add(edge);
			}
		}
		return objects;

	}

	private double getEuclideanDistance(Coordinate c0, Coordinate c1) {
		return Math.sqrt(Math.pow((c1.x - c0.x), 2) + Math.pow((c1.y - c0.y), 2));
	}

	public double getPerpendicularDistance(Coordinate c0, Coordinate source, Coordinate target) {
		return Math.abs(
				(target.y - source.y) * c0.x - (target.x - source.x) * c0.y + target.x * source.y - target.y * source.x)
				/ Math.sqrt((Math.pow((target.y - source.y), 2)) + (Math.pow((target.x - source.x), 2)));
	}

}
