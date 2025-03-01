package data;

import java.util.HashMap;

public class MMetarDefinitions
{
	public static MMetarDefinitions instance = new MMetarDefinitions();

	public HashMap<String, String> covers;
	public HashMap<String, String> weathers;
	public HashMap<String, String> runwayCoverages;
	public HashMap<String, String> runwayBrakingActions;
	public HashMap<String, String> runwayTrends;
	public HashMap<String, String> runwayContaminationTypes;
	
	public HashMap<String, String> colorRemarks;
	public HashMap<String, String> pressureTendencyRemarks;
	public HashMap<String, String> automatedStationTypeRemarks;
	public HashMap<String, String> sensorRemarks;
	public HashMap<String, String> dataMissingRemarks;
	public HashMap<String, String> skyCoverageRemarks;
	public HashMap<String, String> cloudRemarks;
	public HashMap<String, String> weatherRemarks;

	private MMetarDefinitions()
	{
		initCovers();
		initWeathers();
		initRunwayCoverages();
		initRunwayBrakingActions();
		initRunwayTrends();
		initRunwayContaminationTypes();
		
		initColorRemarks();
		initPressureTendencyRemarks();
		initAutomatedStationTypeRemarks();
		initSensorRemarks();
		initDataMissingRemarks();
		initSkyCoverageRemarks();
		initCloudRemarks();
		initWeatherRemarks();
	}

	private void initColorRemarks()
	{
		colorRemarks = new HashMap<String, String>();
		colorRemarks.put("BLU", "Blue, good conditions");
		colorRemarks.put("BLU+", "Blue, optimal conditions");
		colorRemarks.put("GRN", "Green, VFR conditions");
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

	private void initPressureTendencyRemarks()
	{
		pressureTendencyRemarks = new HashMap<String, String>();
		pressureTendencyRemarks.put("0", "No change");
		pressureTendencyRemarks.put("1", "Rising then falling");
		pressureTendencyRemarks.put("2", "Rising then steady");
		pressureTendencyRemarks.put("3", "Rising steadily");
		pressureTendencyRemarks.put("4", "Rising rapidly");
		pressureTendencyRemarks.put("5", "Falling then rising");
		pressureTendencyRemarks.put("6", "Falling then steady");
		pressureTendencyRemarks.put("7", "Falling steadily");
		pressureTendencyRemarks.put("8", "Falling rapidly");
	}

	private void initAutomatedStationTypeRemarks()
	{
		automatedStationTypeRemarks = new HashMap<String, String>();
		automatedStationTypeRemarks.put("A01", "Automated station without a precipitation sensor");
		automatedStationTypeRemarks.put("AO1", "Automated station without a precipitation sensor");
		automatedStationTypeRemarks.put("A02", "Automated station with a precipitation sensor");
		automatedStationTypeRemarks.put("AO2", "Automated station with a precipitation sensor");
		automatedStationTypeRemarks.put("AO2A", "Automated station with a precipitation sensor");
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

	private void initDataMissingRemarks()
	{
		dataMissingRemarks = new HashMap<String, String>();
		dataMissingRemarks.put("WIND MISG", "Wind missing");
		dataMissingRemarks.put("CLD MISG", "Clouds missing");
		dataMissingRemarks.put("WX MISG", "Weather missing");
		dataMissingRemarks.put("VIS MISG", "Visibility missing");
		dataMissingRemarks.put("PCPN MISG", "Precipitation missing");
		dataMissingRemarks.put("PRES MISG", "Pressure missing");
		dataMissingRemarks.put("T MISG", "Temperature missing");
		dataMissingRemarks.put("DP MISG", "Dew point missing");
		dataMissingRemarks.put("ICE MISG", "Icing missing");
		dataMissingRemarks.put("DENSITY ALT MISG", "Density altitude missing");
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

	private void initSkyCoverageRemarks()
	{
		skyCoverageRemarks = new HashMap<String, String>();
		skyCoverageRemarks.put("AC", "Altocumulus");
		skyCoverageRemarks.put("AS", "Altostratus");
		skyCoverageRemarks.put("CC", "Cirro-cumulus");
		skyCoverageRemarks.put("CF", "Cumulus fractus");
		skyCoverageRemarks.put("CI", "Cirrus");
		skyCoverageRemarks.put("CS", "Cirrostratus");
		skyCoverageRemarks.put("CU", "Cumulus");
		skyCoverageRemarks.put("FG", "Fog");
		skyCoverageRemarks.put("HZ", "Haze");
		skyCoverageRemarks.put("IC", "Ice crystals");
		skyCoverageRemarks.put("NS", "Nimbostratus");
		skyCoverageRemarks.put("SC", "Stratocumulus");
		skyCoverageRemarks.put("SF", "Stratus fractus");
		skyCoverageRemarks.put("SN", "Snow");
		skyCoverageRemarks.put("ST", "Stratus");
	}

	private void initCloudRemarks()
	{
		cloudRemarks = new HashMap<String, String>();
		cloudRemarks.put("AC CUGEN", "Altocumulus castellanus generated by convection");
		cloudRemarks.put("AC OP", "Altocumulus opacus");
		cloudRemarks.put("AC TR", "Altocumulus translucidus");
		cloudRemarks.put("AS TR", "Altostratus translucidus");
		cloudRemarks.put("CB", "Cumulonimbus");
		cloudRemarks.put("CF TR", "Cumulus fractus translucidus");
		cloudRemarks.put("CI TR", "Cirrus translucidus");
		cloudRemarks.put("OCNL BLSN", "Occasional blowing snow");
		cloudRemarks.put("SC CL", "Stratocumulus castellanus");
		cloudRemarks.put("SC OP", "Stratocumulus opacus");
		cloudRemarks.put("SC TR", "Stratocumulus translucidus");
		cloudRemarks.put("SF TR", "Stratus fractus translucidus");
		cloudRemarks.put("SOG TR", "Snow on the ground translucidus");
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
