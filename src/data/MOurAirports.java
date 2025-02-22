package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.tinylog.Logger;

public class MOurAirports
{
	private static final String AIRPORTS_URL = "https://davidmegginson.github.io/ourairports-data/airports.csv";
	private static final String AIRPORTS_FILE = "temp/ourairports_airports.csv";

	private static final String COUNTRIES_URL = "https://davidmegginson.github.io/ourairports-data/countries.csv";
	private static final String COUNTRIES_FILE = "temp/ourairports_countries.csv";

	private String[] split(String _line)
	{
		ArrayList<String> fields = new ArrayList<String>();

		try
		{
			CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setQuote('"').get();
			CSVParser parser = csvFormat.parse(new StringReader(_line));
			for (CSVRecord r : parser)
				fields.addAll(r.toList());
		}
		catch (IOException e)
		{
			Logger.error(e);
		}

		return fields.toArray(new String[0]);
	}

	public boolean downloadAirports()
	{
		boolean ok = true;

		try
		{
			URL url = URI.create(AIRPORTS_URL).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedWriter writer = new BufferedWriter(new FileWriter(AIRPORTS_FILE));
			writer.write("ident;");
			writer.write("type;");
			writer.write("name;");
			writer.write("latitude;");
			writer.write("longitude;");
			writer.write("elevation;");
			writer.write("country;");
			writer.write("city");
			writer.newLine();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			reader.readLine(); // Header
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] items = split(line);
				String ident = items[1];
				String type = items[2];
				String name = items[3];
				String latitude = items[4];
				String longitude = items[5];
				String elevation = items[6];
				String country = items[8];
				String city = items.length > 10 ? items[10] : "";

				writer.write(ident + ";");
				writer.write(type + ";");
				writer.write(name + ";");
				writer.write(latitude + ";");
				writer.write(longitude + ";");
				writer.write(elevation + ";");
				writer.write(country + ";");
				writer.write(city);

				writer.newLine();
			}

			reader.close();
			writer.close();
			connection.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
			ok = false;
		}

		return ok;
	}

	public boolean downloadCountries()
	{
		boolean ok = true;

		try
		{
			URL url = URI.create(COUNTRIES_URL).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTRIES_FILE));
			writer.write("code,");
			writer.write("name");
			writer.newLine();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			reader.readLine(); // Header
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] items = line.split(",");
				String code = items[1].replace("\"", "");
				String name = items[2].replace("\"", "");

				writer.write(code + ",");
				writer.write(name);

				writer.newLine();
			}

			reader.close();
			writer.close();
			connection.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
			ok = false;
		}

		return ok;
	}

	public ArrayList<MAirport> loadAirports()
	{
		ArrayList<MAirport> airports = new ArrayList<MAirport>();

		if (new File(AIRPORTS_FILE).exists())
			try (BufferedReader reader = new BufferedReader(new FileReader(AIRPORTS_FILE)))
			{
				reader.readLine(); // Header
				String line;
				while ((line = reader.readLine()) != null)
				{
					String[] items = line.split(";");
					MAirport airport = new MAirport(items[0]);
					airport.type = items[1];
					airport.name = items[2];
					airport.latitude = Double.parseDouble(items[3]);
					airport.longitude = Double.parseDouble(items[4]);
					airport.elevationFt = (items[5].isEmpty() || items[5].equals("NA") || items[5].equals("OC"))
							? Integer.MIN_VALUE
							: Integer.parseInt(items[5]);
					airport.country = items[6];
					airport.city = items.length == 8 ? items[7] : "";
					airports.add(airport);
				}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		else
			Logger.error(AIRPORTS_FILE + " does not exist");

		Collections.sort(airports, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				return o1.stationId.compareTo(o2.stationId);
			}
		});

		return airports;
	}

	public HashMap<String, String> loadCountries()
	{
		HashMap<String, String> countries = new HashMap<String, String>();

		if (new File(COUNTRIES_FILE).exists())
			try (BufferedReader reader = new BufferedReader(new FileReader(COUNTRIES_FILE)))
			{
				reader.readLine(); // Header
				String line;
				while ((line = reader.readLine()) != null)
				{
					String[] items = line.split(",");
					countries.put(items[0], items[1]);
				}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		else
			Logger.error(COUNTRIES_FILE + " does not exist");

		return countries;
	}

	public static void main(String[] args)
	{
		MOurAirports ourAirports = new MOurAirports();
		ourAirports.downloadAirports();
		ourAirports.downloadCountries();

		HashMap<String, String> countries = ourAirports.loadCountries();
	}
}
