package br.ufc.arida.probabilisticgraph.dao.postgis.trajectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.varia.StringMatchFilter;

public class TrajectoryReader {

	private static final String SEPARATOR = ";";
	private static final int ID_TAXI_IDX = 0;
	private static final int LAT_IDX = 1;
	private static final int LON_IDX = 2;
	private static final int TIMESTAMP_IDX = 7;
	

	public ArrayList<TrajectoryPoint> loadFilesInDatabase(String directoryPath) {
		String strLine = null;
		FileInputStream fstream;
		File directory = new File(directoryPath);
		File[] listFiles = directory.listFiles();
		TrajectoryPointDAO dao = new TrajectoryPointDAO();
		ArrayList<TrajectoryPoint> points = new ArrayList<>();

		for (File file : listFiles) {
			if (file.getName().endsWith(".txt")) {
				try {
					fstream = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
					System.out.println("Reading file " + file.getName());
					while ((strLine = br.readLine()) != null) {

						String[] split = strLine.split(SEPARATOR);
						int idTaxi = Integer.parseInt(split[ID_TAXI_IDX]);
						double lat = Double.parseDouble(split[LAT_IDX]);
						double lon = Double.parseDouble(split[LON_IDX]);
						LocalDateTime timestamp = getDateFromString(split[TIMESTAMP_IDX]);
						TrajectoryPoint p = new TrajectoryPoint(lat, lon, idTaxi, timestamp);
						points.add(p);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dao.insert(points);
				points.clear();
			}

		}

		return points;

	}

	public LocalDateTime getDateFromString(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd uuuu HH:mm:ss");
		int indexOf = date.indexOf(" GMT");
		LocalDateTime dateTime = LocalDateTime.parse(date.substring(0, indexOf), formatter.withLocale(Locale.ENGLISH));

		return dateTime;
	}

	public static void main(String[] args) {

		TrajectoryReader reader = new TrajectoryReader();
		reader.loadFilesInDatabase("/media/livia/DATA/DADOS/trajetorias-taxi-simples/junho 2016/");

	}

}
