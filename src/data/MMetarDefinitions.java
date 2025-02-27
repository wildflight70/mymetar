package data;

import java.util.HashMap;

public class MMetarDefinitions
{
	public static MMetarDefinitions instance = new MMetarDefinitions();

	public HashMap<String, String> colors;
	public HashMap<String, String> covers;
	public HashMap<String, String> weathers;
	public HashMap<String, String> pressureTendencies;
	public HashMap<String, String> automatedStationTypes;
	public HashMap<String, String> sensorRemarks;
	public HashMap<String, String> dataMissing;
	public HashMap<String, String> runwayTrends;
	public HashMap<String, String> runwayContaminationTypes;
	public HashMap<String, String> runwayCoverages;
	public HashMap<String, String> runwayBrakingActions;
	public HashMap<String, String> cloudCoverageRemarks;
	public HashMap<String, String> cloudRemarks;
	public HashMap<String, String> weatherRemarks;

	private MMetarDefinitions()
	{
		initColors();
		initCovers();
		initWeathers();
		initPressureTendencies();
		initAutomatedStationTypes();
		initSensorRemarks();
		initDataMissing();
		initRunwayTrends();
		initRunwayContaminationTypes();
		initRunwayCoverages();
		initRunwayBrakingActions();
		initCloudCoverageRemarks();
		initCloudRemarks();
		initWeatherRemarks();
	}

	private void initColors()
	{
		colors = new HashMap<String, String>();
		colors.put("BLU", "Blue, good conditions");
		colors.put("BLU+", "Blue, optimal conditions");
		colors.put("GRN", "Green, VFR conditions");
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
		automatedStationTypes.put("A01", "Automated station without a precipitation sensor");
		automatedStationTypes.put("AO1", "Automated station without a precipitation sensor");
		automatedStationTypes.put("AO2", "Automated station with a precipitation sensor");
		automatedStationTypes.put("AO2A", "Automated station with a precipitation sensor");
	}

	private void initSensorRemarks()
	{
		sensorRemarks = new HashMap<String, String>();
		sensorRemarks.put("CHINO", "Ceiling height indicator not operational");
		sensorRemarks.put("FZRANO", "Thunderstorm sensor not operational");
		sensorRemarks.put("FROIN", "Frost on the indicator");
		sensorRemarks.put("PNO", "Precipitation sensor not operational");
		sensorRemarks.put("PWINO", "Present weather sensor not operational");
		sensorRemarks.put("RVRNO", "Runway Visual Range sensor not operational");
		sensorRemarks.put("SLPNO", "Sea level pressure not operational");
		sensorRemarks.put("TSNO", "Thunderstorm sensor not operational");
		sensorRemarks.put("VISNO", "Visibility sensor not operational");
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

	private void initRunwayTrends()
	{
		runwayTrends = new HashMap<String, String>();
		runwayTrends.put("D", "Down");
		runwayTrends.put("U", "Up");
		runwayTrends.put("N", "No change");
	}

	private void initRunwayContaminationTypes()
	{
		runwayContaminationTypes = new HashMap<String, String>();
		runwayContaminationTypes.put("0", "Clear and Dry");
		runwayContaminationTypes.put("1", "Damp");
		runwayContaminationTypes.put("2", "Wet or Water Patches");
		runwayContaminationTypes.put("3", "Rime/Frost");
		runwayContaminationTypes.put("4", "Dry Snow");
		runwayContaminationTypes.put("5", "Wet Snow");
		runwayContaminationTypes.put("6", "Slush");
		runwayContaminationTypes.put("7", "Ice");
		runwayContaminationTypes.put("8", "Compact Snow or Ice");
		runwayContaminationTypes.put("9", "Frozen Ruts or Snowdrifts");
	}

	private void initRunwayCoverages()
	{
		runwayCoverages = new HashMap<String, String>();
		runwayCoverages.put("0", "10% or less");
		runwayCoverages.put("1", "11-25%");
		runwayCoverages.put("2", "26-50%");
		runwayCoverages.put("5", "51-100%");
		runwayCoverages.put("9", "51-100%");
	}

	private void initRunwayBrakingActions()
	{
		runwayBrakingActions = new HashMap<String, String>();
		runwayBrakingActions.put("00", "Runway closed");
		runwayBrakingActions.put("91", "Poor to Medium");
		runwayBrakingActions.put("92", "Medium");
		runwayBrakingActions.put("93", "Medium to Good");
		runwayBrakingActions.put("94", "Good");
		runwayBrakingActions.put("99", "Runway not operational");
	}

	private void initCloudCoverageRemarks()
	{
		cloudCoverageRemarks = new HashMap<String, String>();
		cloudCoverageRemarks.put("AC", "Altocumulus");
		cloudCoverageRemarks.put("AS", "Altostratus");
		cloudCoverageRemarks.put("CI", "Cirrus");
		cloudCoverageRemarks.put("CS", "Cirrostratus");
		cloudCoverageRemarks.put("FG", "Fog");
		cloudCoverageRemarks.put("HZ", "Haze");
		cloudCoverageRemarks.put("NS", "Nimbostratus");
		cloudCoverageRemarks.put("SC", "Stratocumulus");
		cloudCoverageRemarks.put("SF", "Stratus fractus");
		cloudCoverageRemarks.put("SN", "Snow");
		cloudCoverageRemarks.put("ST", "Stratus");
	}

	private void initCloudRemarks()
	{
		cloudRemarks = new HashMap<String, String>();
		cloudRemarks.put("AC CUGEN", "Altocumulus castellanus generated by convection");
		cloudRemarks.put("AC OP", "Altocumulus opacus");
		cloudRemarks.put("AC TR", "Altocumulus translucidus");
		cloudRemarks.put("CB", "Cumulonimbus");
		cloudRemarks.put("CI TR", "Cirrus translucidus");
		cloudRemarks.put("OCNL BLSN", "Occasional blowing snow");
		cloudRemarks.put("SC CL", "Stratocumulus castellanus");
		cloudRemarks.put("SC OP", "Stratocumulus opacus");
		cloudRemarks.put("SC TR", "Stratocumulus translucidus");
		cloudRemarks.put("SF TR", "Stratus fractus translucidus");
		cloudRemarks.put("ST TR", "Stratus translucidus");
		cloudRemarks.put("TCU", "Towering cumulus");
	}
	
	private void initWeatherRemarks()
	{
		weatherRemarks = new HashMap<String, String>();
		weatherRemarks.put("HALO", "Halo");
		weatherRemarks.put("CIG", "Ceiling");
		weatherRemarks.put("ICE", "Ice");
		weatherRemarks.put("RAG", "Ragged clouds");
		weatherRemarks.put("SNW", "Halo");
	}
}
