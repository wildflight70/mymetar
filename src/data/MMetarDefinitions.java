package data;

import java.util.HashMap;

public class MMetarDefinitions
{
	public static MMetarDefinitions instance = new MMetarDefinitions();

	public HashMap<String, String> weathers;
	public HashMap<String, String> runwayCoverages;
	public HashMap<String, String> runwayBrakingActions;
	public HashMap<String, String> runwayTrends;
	public HashMap<String, String> runwayContaminationTypes;
	
	public HashMap<String, String> colorRemarks;
	public HashMap<String, String> pressureTendencyRemarks;
	public HashMap<String, String> automatedStationTypeRemarks;
	public HashMap<String, String> sensorRemarks;
	public HashMap<String, String> cloudRemarks;

	private MMetarDefinitions()
	{
		initWeathers();
		initRunwayCoverages();
		initRunwayBrakingActions();
		initRunwayTrends();
		initRunwayContaminationTypes();
		
		initColorRemarks();
		initPressureTendencyRemarks();
		initAutomatedStationTypeRemarks();
		initSensorRemarks();
		initCloudRemarks();
	}

	private void initColorRemarks()
	{
		colorRemarks = new HashMap<String, String>();

		colorRemarks.put("BLACK", "Black, Operations restricted or closed");
		
		colorRemarks.put("YLO", "Yellow, IFR conditions");
		colorRemarks.put("AMB", "Amber, IFR conditions");
		colorRemarks.put("RED", "Red, low IFR conditions");
	
		colorRemarks.put("BLACKBLU", "Black blue, excellent VFR conditions");
		colorRemarks.put("BLACKGRN", "Black blue, good VFR conditions");
		colorRemarks.put("BLU", "Blue, good VFR conditions");
		colorRemarks.put("BLU+", "Blue, optimal VFR conditions");
		colorRemarks.put("GRN", "Green, VFR conditions");
		colorRemarks.put("WHT", "White, low VFR conditions");
	}	
	
	private void initWeathers()
	{
		weathers = new HashMap<String, String>();
		weathers.put("-", "Light");
		weathers.put("+", "Heavy");
		weathers.put("RE", "Recent");
		weathers.put("VC", "In vicinity");

		weathers.put("BC", "Patches");
		weathers.put("BL", "Blowing");
		weathers.put("DR", "Low drifting");
		weathers.put("FZ", "Freezing");
		weathers.put("MI", "Shallow");
		weathers.put("SH", "Showers");
		weathers.put("TS", "Thunderstorm");

		weathers.put("BR", "Mist");
		weathers.put("DS", "Dust storm");
		weathers.put("DU", "Widespread dust");
		weathers.put("DZ", "Drizzle");
		weathers.put("FC", "Funnel cloud");
		weathers.put("FG", "Fog");
		weathers.put("FU", "Smoke");
		weathers.put("GR", "Hail");
		weathers.put("GS", "Small hail");
		weathers.put("HZ", "Haze");
		weathers.put("RA", "Rain");
		weathers.put("SA", "Sand");
		weathers.put("SQ", "Squall");
		weathers.put("IC", "Ice crystals");
		weathers.put("PL", "Ice pellets");
		weathers.put("SG", "Snow grains");
		weathers.put("SN", "Snow");
		weathers.put("SS", "Sandstorm");
		weathers.put("VA", "Volcanic ash");
		weathers.put("VCSH", "Vicinity showers");

		weathers.put("CAVOK", "CAVOK");
		weathers.put("BKN", "Broken");
		weathers.put("CLR", "Clear");
		weathers.put("FEW", "Few");
		weathers.put("NCD", "No cloud detected");
		weathers.put("NSC", "No signifiant cloud");
		weathers.put("NSW", "No signifiant weather");
		weathers.put("OVC", "Overcast");
		weathers.put("SCT", "Scattered");
		weathers.put("SKC", "Clear");
		weathers.put("VV", "Sky obscured");
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
		sensorRemarks.put("WIND SENSOR OFFLINE", "Wind sensor not operational");
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

	private void initCloudRemarks()
	{
		cloudRemarks = new HashMap<String, String>();
		cloudRemarks.put("AC", "Altocumulus");
		cloudRemarks.put("AC CUGEN", "Altocumulus cumulus generated");
		cloudRemarks.put("AC OP", "Altocumulus opacus");
		cloudRemarks.put("AC TR", "Altocumulus translucidus");
		cloudRemarks.put("ACC", "Altocumulus castellanus");
		cloudRemarks.put("ACC TR", "Altocumulus castellanus translucidus");
		cloudRemarks.put("AS", "Altostratus");
		cloudRemarks.put("AS TR", "Altostratus translucidus");
		cloudRemarks.put("BLSN", "Blowing snow");
		cloudRemarks.put("BLSN OCNL", "Occasional blowing snow");
		cloudRemarks.put("CB", "Cumulonimbus");
		cloudRemarks.put("CC", "Cirro-cumulus");
		cloudRemarks.put("CF", "Cumulus fractus");
		cloudRemarks.put("CF TR", "Cumulus fractus translucidus");
		cloudRemarks.put("CI", "Cirrus");
		cloudRemarks.put("CI TR", "Cirrus translucidus");
		cloudRemarks.put("CIG", "Ceiling");
		cloudRemarks.put("CLD", "Clouds");
		cloudRemarks.put("CLD EMBD", "Embedded clouds");
		cloudRemarks.put("CS", "Cirrostratus");
		cloudRemarks.put("CU", "Cumulus");
		cloudRemarks.put("CVCTV", "Convective clouds");
		cloudRemarks.put("FG", "Fog");
		cloudRemarks.put("FU ALQDS", "Smoke in all quadrants");
		cloudRemarks.put("HALO", "Halo");
		cloudRemarks.put("HZ", "Haze");
		cloudRemarks.put("IC", "Ice crystals");
		cloudRemarks.put("ICE", "Ice");
		cloudRemarks.put("LGT ICG", "Light ice crystals");
		cloudRemarks.put("NS", "Nimbostratus");
		cloudRemarks.put("OCNL BLSN", "Occasional blowing snow");
		cloudRemarks.put("RAG", "Ragged clouds");
		cloudRemarks.put("SC", "Stratocumulus");
		cloudRemarks.put("SC CL", "Stratocumulus castellanus");
		cloudRemarks.put("SC OP", "Stratocumulus opacus");
		cloudRemarks.put("SC TR", "Stratocumulus translucidus");
		cloudRemarks.put("SF", "Stratus fractus");
		cloudRemarks.put("SF TR", "Stratus fractus translucidus");
		cloudRemarks.put("SMOKE FU ALL QUADS", "Smoke in all quadrants");
		cloudRemarks.put("SN", "Snow");
		cloudRemarks.put("SNW", "Snow");
		cloudRemarks.put("SOG TR", "Snow on the ground translucidus");
		cloudRemarks.put("ST", "Stratus");
		cloudRemarks.put("ST TR", "Stratus translucidus");
		cloudRemarks.put("TCU", "Towering cumulus");
		cloudRemarks.put("WX", "Weather");
	}
}
