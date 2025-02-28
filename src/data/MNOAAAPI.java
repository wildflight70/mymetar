package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.tinylog.Logger;

public class MNOAAAPI
{
	public static final String NOAA_METAR_CSV_URL = "https://aviationweather.gov/data/cache/metars.cache.csv.gz";

	private static final String LOCAL_DIR = "temp/";
	private static final String METARS_FILE = "noaa_api_metars.csv";

	public MMetar download(String _stationId)
	{
		final String NOAA_METAR_STATION_CSV_URL = "https://aviationweather.gov/api/data/dataserver?requestType=retrieve&dataSource=metars&stationString="
				+ _stationId + "&hoursBeforeNow=2&format=csv&mostRecentForEachStation=constraint";

		MMetar metar = null;

		try
		{
			URL url = URI.create(NOAA_METAR_STATION_CSV_URL).toURL();
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

	private ByteArrayOutputStream decompressGzip(byte[] compressedData) throws IOException
	{
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
				GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
		{
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = gzipInputStream.read(buffer)) != -1)
			{
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}

			return byteArrayOutputStream;
		}
	}

	public boolean download()
	{
		boolean ok = true;

		// Download
		byte[] compressedFile = null;
		HttpURLConnection conn = null;
		try
		{
			URL url = URI.create(NOAA_METAR_CSV_URL).toURL();
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			InputStream inputStream = conn.getInputStream();
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1)
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			compressedFile = byteArrayOutputStream.toByteArray();
		}
		catch (Exception e)
		{
			Logger.error(e);
			ok = false;
		}
		finally
		{
			conn.disconnect();
		}

		// Unzip
		ByteArrayOutputStream decompressedFile = null;
		try
		{
			decompressedFile = decompressGzip(compressedFile);
		}
		catch (IOException e)
		{
			Logger.error(e);
			ok = false;
		}

		// Read
		ArrayList<MMetar> metars = new ArrayList<MMetar>();
		try
		{
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(decompressedFile.toByteArray()), "UTF-8"));

			String line = reader.readLine(); // No errors
			line = reader.readLine(); // No warnings
			line = reader.readLine(); // x ms
			line = reader.readLine(); // data source=metars
			line = reader.readLine(); // x results

			String[] fields = reader.readLine().split(" ");
			while ((line = reader.readLine()) != null)
			{
				try
				{
					MMetar metar = new MMetar(fields, line);
					metars.add(metar);
				}
				catch (Exception e)
				{
					Logger.error(line + "\n" + e);
				}
			}

			reader.close();
			conn.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
			ok = false;
		}

		write(metars);

		return ok;
	}

	private boolean write(ArrayList<MMetar> _metars)
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
						String flightCategory = items.length == 3 ? items[2] : "";

						int p = rawText.indexOf(" ");
						String stationId = rawText.substring(0, p);

						MMetar metar = new MMetar(observationTime, rawText, stationId);
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

		load.download();
	}
}
