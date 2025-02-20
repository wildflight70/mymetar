package data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.tinylog.Logger;

public class MLoadAPI
{
	public MMetar loadCSV(String _stationId)
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

			String fields = reader.readLine();
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
	
	public static void main(String[] args)
	{
		MLoadAPI load = new MLoadAPI();
		MMetar metar = load.loadCSV("LFPG");
	}
}
