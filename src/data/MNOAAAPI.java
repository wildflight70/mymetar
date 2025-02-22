package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.tinylog.Logger;

public class MNOAAAPI
{
	public static final String LOCAL_DIR = "temp/";
	public static final String METARS_FILE = "noaa_api_metars.csv";

	public MMetar downloadCSV(String _stationId)
	{
		final String NOAA_METAR_CSV_URL = "https://aviationweather.gov/api/data/dataserver?requestType=retrieve&dataSource=metars&stationString="
				+ _stationId + "&hoursBeforeNow=2&format=csv&mostRecentForEachStation=constraint";

		MMetar metar = null;

		try
		{
			URL url = URI.create(NOAA_METAR_CSV_URL).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			reader.readLine(); // No errors
			reader.readLine(); // No warnings
			reader.readLine(); // x ms
			reader.readLine(); // data source=metars
			reader.readLine(); // x results

			String[] fields = reader.readLine().split(" ");
			String values = reader.readLine();
			if (values != null)
				metar = new MMetar(fields, values);

			reader.close();
			conn.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
		}

		return metar;
	}

	public ArrayList<MMetar> downloadCSV(int _latitudeFrom, int _latitudeTo, int _longitudeFrom, int _longitudeTo)
	{
		final String NOAA_METAR_CSV_URL = "https://aviationweather.gov/api/data/dataserver?requestType=retrieve&dataSource=metars&hoursBeforeNow=2&format=csv&mostRecentForEachStation=constraint&boundingBox="
				+ _latitudeFrom + "%2C" + _longitudeFrom + "%2C" + _latitudeTo + "%2C" + _longitudeTo;

		ArrayList<MMetar> metars = new ArrayList<MMetar>();

		try
		{
			URL url = URI.create(NOAA_METAR_CSV_URL).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			reader.readLine(); // No errors
			reader.readLine(); // No warnings
			reader.readLine(); // x ms
			reader.readLine(); // data source=metars
			reader.readLine(); // x results

			String[] fields = reader.readLine().split(" ");
			String values;
			while ((values = reader.readLine()) != null)
			{
				MMetar metar = new MMetar(fields, values);
				metars.add(metar);
			}

			reader.close();
			conn.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
		}

		return metars;
	}

	public ArrayList<MMetar> downloadAllCSV()
	{
		Logger.debug("loadAllCSV begin");

		ArrayList<MMetar> allMetars = new ArrayList<MMetar>();

		int processors = Runtime.getRuntime().availableProcessors();

		ExecutorService executor = Executors.newFixedThreadPool(processors);
		List<Future<ArrayList<MMetar>>> futures = new ArrayList<>();

		int deltaLatitude = 10;
		int deltaLongitude = 20;
		for (int latitude = -90; latitude < 90; latitude += deltaLatitude)
			for (int longitude = -180; longitude < 180; longitude += deltaLongitude)
				futures.add(
						executor.submit(new TaskLoad(latitude, latitude + deltaLatitude, longitude, longitude + deltaLongitude)));

		for (Future<ArrayList<MMetar>> future : futures)
			try
			{
				ArrayList<MMetar> metars = future.get();
				allMetars.addAll(metars);
			}
			catch (Exception e)
			{
				Logger.error(e);
			}

		executor.shutdown();

		Logger.debug("loadAllCSV end");

		return allMetars;
	}

	private class TaskLoad implements Callable<ArrayList<MMetar>>
	{
		private int latitudeFrom;
		private int latitudeTo;
		private int longitudeFrom;
		private int longitudeTo;

		public TaskLoad(int _latitudeFrom, int _latitudeTo, int _longitudeFrom, int _longitudeTo)
		{
			latitudeFrom = _latitudeFrom;
			latitudeTo = _latitudeTo;
			longitudeFrom = _longitudeFrom;
			longitudeTo = _longitudeTo;
		}

		public ArrayList<MMetar> call() throws Exception
		{
			return downloadCSV(latitudeFrom, latitudeTo, longitudeFrom, longitudeTo);
		}
	}

	public boolean write(ArrayList<MMetar> _metars)
	{
		boolean ok = true;

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOCAL_DIR + METARS_FILE)))
		{
			for (MMetar metar : _metars)
			{
				writer.write(metar.observationTime + ",");
				writer.write(metar.rawText + ",");
				writer.write(metar.extraFlightCategory);
				writer.newLine();
			}
		}
		catch (IOException e)
		{
			Logger.error(e);
			ok = false;
		}
		return ok;
	}

	public HashMap<String, MMetar> load()
	{
		HashMap<String, MMetar> metars = new HashMap<String, MMetar>();

		String fileName = LOCAL_DIR + METARS_FILE;
		if (new File(fileName).exists())
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
			{
				String line;
				while ((line = reader.readLine()) != null)
					if (!line.isEmpty())
					{
						String[] items = line.split(",");

						LocalDateTime observationTime = LocalDateTime.parse(items[0]);
						String rawText = items[1];
						String flightCategory = items.length == 5 ? items[4] : "";

						int p = rawText.indexOf(" ");
						String stationId = rawText.substring(0, p);

						MMetar metar = new MMetar(observationTime, stationId);
						metar.extraFlightCategory = flightCategory;

						metars.put(stationId, metar);
					}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		}
		else
			Logger.error(fileName + " does not exist");

		return metars;
	}

	public static void main(String[] args)
	{
		MNOAAAPI load = new MNOAAAPI();
//		MMetar metarStation = load.loadCSV("LFPG");

//		ArrayList<MMetar> metars = load.loadCSV(40, 50, 0, 10);
//		System.out.println(metars.size());
//		for (MMetar metar : metars)
//			System.out.println(metar.rawText);

		ArrayList<MMetar> metars = load.downloadAllCSV();
		System.out.println(metars.size());
	}
}
