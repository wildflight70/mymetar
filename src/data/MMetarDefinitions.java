package data;

import java.util.HashMap;

public class MMetarDefinitions
{
	public static MMetarDefinitions instance = new MMetarDefinitions();

	public HashMap<String, String> covers;
	public HashMap<String, String> weathers;
	public HashMap<String, String> pressureTendencies;
	public HashMap<String, String> automatedStationTypes;
	public HashMap<String, String> sensors;
	public HashMap<String, String> dataMissing;

	private MMetarDefinitions()
	{
		initCovers();
		initWeathers();
		initPressureTendencies();
		initAutomatedStationTypes();
		initSensors();
		initDataMissing();
	}

	private void initCovers()
	{
		covers = new HashMap<String, String>();
		covers.put("CAVOK", "CAVOK");
		covers.put("CLR", "Clear");
		covers.put("SKC", "Clear");
		covers.put("NSC", "No signifiant cloud");
		covers.put("NSW", "No signifiant weather");
		covers.put("NCD", "No cloud detected");
		covers.put("FEW", "Few");
		covers.put("SCT", "Scattered");
		covers.put("BKN", "Broken");
		covers.put("OVC", "Overcast");
		covers.put("VV", "Sky obscured");
	}

	private void initWeathers()
	{
		weathers = new HashMap<String, String>();
		weathers.put("-", "Light");
		weathers.put("+", "Heavy");
		weathers.put("VC", "In vicinity");
		weathers.put("RE", "Recent");

		weathers.put("MI", "Shallow");
		weathers.put("BC", "Patches");
		weathers.put("DR", "Low drifting");
		weathers.put("BL", "Blowing");
		weathers.put("SH", "Showers");
		weathers.put("TS", "Thunderstorm");
		weathers.put("FZ", "Freezing");

		weathers.put("VCSH", "Vicinity showers");
		weathers.put("RA", "Rain");
		weathers.put("DZ", "Drizzle");
		weathers.put("SN", "Snow");
		weathers.put("SG", "Snow grains");
		weathers.put("IC", "Ice crystals");
		weathers.put("PL", "Ice pellets");
		weathers.put("GR", "Hail");
		weathers.put("GS", "Small hail");
		weathers.put("FG", "Fog");
		weathers.put("BR", "Mist");
		weathers.put("HZ", "Haze");
		weathers.put("FU", "Smoke");
		weathers.put("VA", "Volcanic ash");
		weathers.put("DU", "Widespread dust");
		weathers.put("SA", "Sand");
		weathers.put("SQ", "Squall");
		weathers.put("FC", "Funnel cloud");
		weathers.put("SS", "Sandstorm");
		weathers.put("DS", "Dust storm");
	}

	private void initPressureTendencies()
	{
		pressureTendencies = new HashMap<String, String>();
		pressureTendencies.put("0", "No change");
		pressureTendencies.put("1", "Rising then falling");
		pressureTendencies.put("2", "Rising then steady");
		pressureTendencies.put("3", "Rising steadily");
		pressureTendencies.put("4", "Rising rapidly");
		pressureTendencies.put("5", "Falling then rising");
		pressureTendencies.put("6", "Falling then steady");
		pressureTendencies.put("7", "Falling steadily");
		pressureTendencies.put("8", "Falling rapidly");
	}

	private void initAutomatedStationTypes()
	{
		automatedStationTypes = new HashMap<String, String>();
		automatedStationTypes.put("AO1", "Automated station without a precipitation sensor");
		automatedStationTypes.put("AO2", "Automated station with a precipitation sensor");
	}

	private void initSensors()
	{
		sensors = new HashMap<String, String>();
		sensors.put("PWINO", "Precipitation sensor not operational");
		sensors.put("RVRNO", "Runway Visual Range sensor not operational");
		sensors.put("VISNO", "Visibility sensor not operational");
		sensors.put("TSNO", "Thunderstorm sensor not operational");
	}
	
	private void initDataMissing()
	{
		dataMissing = new HashMap<String, String>();
		dataMissing.put("WIND MISG", "Wind missing");
		dataMissing.put("CLD MISG", "Clouds missing");
		dataMissing.put("WX MISG", "Weather missing");
		dataMissing.put("VIS MISG", "Visibility missing");
		dataMissing.put("PCPN MISG", "Precipitation missing");
		dataMissing.put("PRES MISG", "Pressure missing");
		dataMissing.put("T MISG", "Temperature missing");
		dataMissing.put("DP MISG", "Dew point missing");
		dataMissing.put("ICE MISG", "Icing missing");
		dataMissing.put("DENSITY ALT MISG", "Density altitude missing");
	}
}
