package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.tinylog.Logger;

import util.MProperties;

public class MLoadXPlane
{
	public HashMap<String, MXPlane> load()
	{
		HashMap<String, MXPlane> map = new HashMap<String, MXPlane>();

		String xPlanePath = MProperties.instance.properties.getProperty("XPLANE_PATH");

		String fileName = xPlanePath + "Global Scenery/Global Airports/Earth nav data/apt.dat";
		if (new File(fileName).exists())
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
			{
				MXPlane xPlane = null;
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("1 ") || line.startsWith("16 ") || line.startsWith("17 "))
					{
						String[] items = line.split("\\s+");

						String stationId = items[4];

						String airportname = items[5];
						for (int i = 6; i < items.length; i++)
							airportname += " " + items[i];

						xPlane = new MXPlane();
						xPlane.airportName = airportname;
						xPlane.elevationFeet = Integer.parseInt(items[1]);

						map.put(stationId, xPlane);
					}
					else if (line.startsWith("1302 "))
					{
						String[] items = line.split("\\s+");
						if (items[1].equals("country"))
						{
							xPlane.country = items[2];
							for (int i = 3; i < items.length; i++)
								xPlane.country += " " + items[i];
						}
						else if (items[1].equals("city"))
						{
							xPlane.city = items[2];
							for (int i = 3; i < items.length; i++)
								xPlane.city += " " + items[i];
						}
						else if (items[1].equals("datum_lat"))
							xPlane.latitude = Double.parseDouble(items[2]);
						else if (items[1].equals("datum_lon"))
							xPlane.longitude = Double.parseDouble(items[2]);
					}
				}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		}
		else
			Logger.error(fileName + " does not exist");

		return map;
	}
}
