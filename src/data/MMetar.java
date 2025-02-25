package data;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import util.MFormat;
import util.MUnit;

public class MMetar
{
	private static final Pattern PATTERN_WIND = Pattern.compile("(?:(VRB|\\d{3}))(\\d{2})(?:G(\\d{2}))?(KT|MPS)");
	private static final Pattern PATTERN_WIND_VARIABLE = Pattern.compile("\\d+V\\d+");
	private static final Pattern PATTERN_VISIBILITY = Pattern
			.compile("\\s(\\d+\\s*\\d*/\\d+|\\d+)SM\\s|\\s(\\d{4})(NDV)?\\s");
	private static final Pattern PATTERN_VISIBILITY_EXTRA = Pattern.compile("\\s(\\d{4})(S)\\s");
	private static final Pattern PATTERN_TEMPERATURE = Pattern.compile("(M?\\d{2})/(M?\\d{0,2}$)");
	private static final Pattern PATTERN_ALTIMETER = Pattern.compile("(A|Q)(\\d{4})");
	private static final Pattern PATTERN_CLOUDS = Pattern
			.compile("(CAVOK|CLR|SKC|NSC|NSW|NCD|FEW|SCT|BKN|OVC|VV)(\\d{2,3})?(CB|TCU)?");
	private static final Pattern PATTERN_WEATHER = Pattern
			.compile("(-|\\+|VC|RE)?(MI|BC|DR|BL|SH|TS|FZ)?(VCSH|RA|DZ|SN|SG|IC|PL|GR|GS|FG|BR|HZ|FU|VA|DU|SA|SQ|FC|SS|DS)$");
	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE = Pattern
			.compile("\\b(R(\\d{2}[LCR]?)/([PM])?(\\d{4})(V(\\d{4}))?([UND])?)\\b");
	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE_MISSING = Pattern.compile("\\b(R(\\d{2}[LCR]?)//{2,})");
	private static final Pattern PATTERN_RUNWAY_CONDITIONS = Pattern
			.compile("\\b(R(\\d{2}[LCR]?)/(\\d)(\\d)(\\d{2})(\\d{2}))\\b");

	private static final Pattern PATTERN_REMARK_AUTOMATED_STATION_TYPES = Pattern.compile("RMK.*(AO[12]A?)");
	private static final Pattern PATTERN_REMARK_SEA_LEVEL_PRESSURE = Pattern.compile("RMK.*(SLP\\d{3})");
	private static final Pattern PATTERN_REMARK_PRECISE_TEMPERATURE = Pattern.compile("RMK.*(T\\d{8})");
	private static final Pattern PATTERN_REMARK_PRESSURE_TENDENCY = Pattern.compile("RMK.*(5\\d{4})");
	private static final Pattern PATTERN_REMARK_SENSOR = Pattern.compile("(PWINO|RVRNO|VISNO|TSNO)\\s");
	private static final Pattern PATTERN_REMARK_MISSING = Pattern
			.compile("(WIND|CLD|WX|VIS|PCPN|PRES|DP|ICE|DENSITY\\sALT|T)\\sMISG");

	private static final Pattern PATTERN_NOT_DECODE = Pattern.compile("(?<=^<html>|</b>)(.*?)(?=<b>|</html>$)");

	public String rawText;
	public String stationId;
	public LocalDateTime observationTime;
	public double altimeterHpa;
	public double altimeterInHg;
	public int temperatureC;
	public int dewPointC = Integer.MIN_VALUE;
	public boolean auto;
	public boolean noSignal;
	public boolean correction;
	public double visibilitySM = -1.0;
	public boolean visibilityNonDirectionalVaration;
	public double visibilitySMExtra = -1.0;
	public String visibilityDirectionExtra = "";
	public int windDirectionDegree;
	public int windSpeedKt = -1;
	public int windGustKt;
	public boolean windVariable;
	public int windFromDegree;
	public int windToDegree;
	public String weather;
	public ArrayList<MCloud> clouds;
	public ArrayList<MRunwayVisualRange> runwayVisualRanges;
	public ArrayList<MRunwayCondition> runwayConditions;

	private String cloudsString;
	private String runwayVisualRangesString;
	private String runwayConditionsString;

	public class MRunwayVisualRange
	{
		String runway;
		String moreLess; // null, <, >
		int minVisibility = -1;
		int maxVisibility = -1;
		String trend; // null, (U)p, (D)own, (N)o change

		public MRunwayVisualRange(String _runway, String _moreLess, int _minVisibility, int _maxVisibility, String _trend)
		{
			runway = _runway;
			moreLess = _moreLess;
			minVisibility = _minVisibility;
			maxVisibility = _maxVisibility;
			trend = _trend;
		}
	}

	public class MRunwayCondition
	{
		String runway;
		String contaminationType;
		String coverage;
		int depth;
		String brakinkAction;

		public MRunwayCondition(String _runway, String _contaminationType, String _coverage, int _depth,
				String _brakingAction)
		{
			runway = _runway;
			contaminationType = _contaminationType;
			coverage = _coverage;
			depth = _depth;
			brakinkAction = _brakingAction;
		}
	}

	public class MRemark
	{
		public String field;
		public String remark;

		public MRemark(String _field, String _remark)
		{
			field = _field;
			remark = _remark;
		}

		@Override
		public String toString()
		{
			return field + ":" + remark;
		}
	}

	public ArrayList<MRemark> remarks;

	public String extraFlightCategory = "";

	public String rawTextHighlight;
	public boolean notDecoded;

	public class MCloud
	{
		public String cover;
		public int baseFeet = -1;
	}

	public MMetar(LocalDateTime _observationTime, String _raw, String _stationId)
	{
		init(_observationTime, _raw);
		stationId = _stationId;
	}

	public MMetar(LocalDateTime _observationTime, String _raw)
	{
		init(_observationTime, _raw);
	}

	public MMetar(String[] _fields, String _values)
	{
		String[] values = _values.split(",");
		init(LocalDateTime.parse(values[2].substring(0, values[2].length() - 1)), values[0]);
		stationId = values[1];
		extraFlightCategory = values[30];
	}

	private void init(LocalDateTime _observationTime, String _raw)
	{
		observationTime = _observationTime;
		rawText = _raw;
		rawTextHighlight = "<html>" + rawText + "</html>";

		clouds = new ArrayList<MCloud>();
		runwayVisualRanges = new ArrayList<MRunwayVisualRange>();
		runwayConditions = new ArrayList<MRunwayCondition>();
		remarks = new ArrayList<MRemark>();
	}

	public String cloudsToString()
	{
		if (cloudsString == null)
		{
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < clouds.size(); i++)
			{
				MCloud cloud = clouds.get(i);
				buffer.append(cloud.cover);

				if (cloud.cover.equals("CAVOK") || cloud.cover.equals("CLR"))
					continue;

				if (cloud.baseFeet >= 0)
				{
					buffer.append(" at ");
					buffer.append(MFormat.instance.numberFormatDecimal0.format(cloud.baseFeet));
				}

				if (i < clouds.size() - 1)
					buffer.append(", ");
			}
			cloudsString = buffer.toString();
		}
		return cloudsString;
	}

	public String runwayVisualRangesToString()
	{
		if (runwayVisualRangesString == null)
		{
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < runwayVisualRanges.size(); i++)
			{
				MRunwayVisualRange runwayVisualRange = runwayVisualRanges.get(i);
				buffer.append(runwayVisualRange.runway);
				buffer.append(":");

				if (runwayVisualRange.minVisibility < 0 && runwayVisualRange.maxVisibility < 0)
					buffer.append("missing");
				else
				{
					if (runwayVisualRange.moreLess != null)
						buffer.append(runwayVisualRange.moreLess);
					buffer.append(MFormat.instance.numberFormatDecimal0.format(runwayVisualRange.minVisibility) + "m");
					if (runwayVisualRange.maxVisibility >= 0)
						buffer.append(" to " + MFormat.instance.numberFormatDecimal0.format(runwayVisualRange.maxVisibility) + "m");
					if (runwayVisualRange.trend != null)
						buffer.append(" " + runwayVisualRange.trend);

					if (i < runwayVisualRanges.size() - 1)
						buffer.append(", ");
				}
			}
			runwayVisualRangesString = buffer.toString();
		}
		return runwayVisualRangesString;
	}

	public String runwayConditionsToString()
	{
		if (runwayConditionsString == null)
		{
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < runwayConditions.size(); i++)
			{
				MRunwayCondition runwayCondition = runwayConditions.get(i);
				buffer.append(runwayCondition.runway);
				buffer.append(":");
				buffer.append(runwayCondition.contaminationType);
				buffer.append(";");
				buffer.append(runwayCondition.coverage);
				buffer.append(";");
				buffer.append(runwayCondition.depth + "mm");
				buffer.append(";");
				buffer.append(runwayCondition.brakinkAction);

				if (i < runwayConditions.size() - 1)
					buffer.append(", ");
			}
			runwayConditionsString = buffer.toString();
		}
		return runwayConditionsString;
	}

	public String write()
	{
		StringBuffer buffer = new StringBuffer(observationTime.toString());
		buffer.append(",");
		buffer.append(rawText);
		return buffer.toString();
	}

	private void highLight(String _field)
	{
		rawTextHighlight = rawTextHighlight.replace(_field, "<b>" + _field + "</b>");
	}

	public void decode()
	{
		String[] items = rawText.split(" ");

		stationId = items[0];
		highLight(stationId);

		String time = items[1];
		if (time.endsWith("Z"))
			highLight(time);

		decodeAltimeter(items);
		decodeTemperature(items);
		decodeNoSignal();
		decodeAuto();
		decodeCorrection();
		decodeVisibility(items);
		decodeWind(items);
		decodeClouds(items);
		decodeWeather(items);
		decodeRunwayVisualRange();
		decodeRunwayConditions();
		decodeRemarks();

		notDecoded();
	}

	private void decodeNoSignal()
	{
		noSignal = rawText.contains("NOSIG");
		highLight("NOSIG");
	}

	private void decodeAuto()
	{
		auto = rawText.contains("AUTO");
		highLight("AUTO");
	}

	private void decodeCorrection()
	{
		correction = rawText.contains("COR");
		highLight("COR");
	}

	private void decodeWind(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			Matcher matcher = PATTERN_WIND_VARIABLE.matcher(_items[i]);
			if (matcher.matches())
			{
				windVariable = true;
				int p = _items[i].indexOf("V");
				if (p >= 0)
				{
					String from = _items[i].substring(0, p);
					windFromDegree = Integer.parseInt(from);
					String to = _items[i].substring(p + 1);
					windToDegree = Integer.parseInt(to);
				}
				highLight(_items[i]);
				break;
			}
		}

		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("/////KT"))
			{
				highLight(_items[i]);
				break;
			}

			Matcher matcher = PATTERN_WIND.matcher(_items[i]);
			if (matcher.find())
			{
				String direction = matcher.group(1);
				if (direction.equals("VRB"))
					windDirectionDegree = -1;
				else
					windDirectionDegree = Integer.parseInt(direction);

				String speedUnit = matcher.group(4);

				String speed = matcher.group(2);
				windSpeedKt = Integer.parseInt(speed);
				if (speedUnit.equals("MPS"))
					windSpeedKt = MUnit.mpsToKnots(windSpeedKt);

				String gust = matcher.group(3);
				if (gust != null)
				{
					windGustKt = Integer.parseInt(gust);
					if (speedUnit.equals("MPS"))
						windGustKt = MUnit.mpsToKnots(windGustKt);
				}
				highLight(_items[i]);
				break;
			}
		}
	}

	private double parseFractionalMiles(String _fraction)
	{
		if (_fraction.contains("/"))
		{
			String[] parts = _fraction.split("\\s+");
			double wholeNumber = 0.0;
			double fractionValue = 0.0;

			if (parts.length == 2)
			{
				wholeNumber = Double.parseDouble(parts[0]);
				_fraction = parts[1];
			}

			String[] fractionParts = _fraction.split("/");
			if (fractionParts.length == 2)
				fractionValue = Double.parseDouble(fractionParts[0]) / Double.parseDouble(fractionParts[1]);

			return wholeNumber + fractionValue;
		}
		return Double.parseDouble(_fraction);
	}

	private void decodeVisibility(String[] _items)
	{
		Matcher matcher = PATTERN_VISIBILITY.matcher(rawText);
		if (matcher.find())
		{
			String rawVisibilitySM = matcher.group(1);
			if (rawVisibilitySM != null)
			{
				visibilitySM = Math.round(10.0 * parseFractionalMiles(rawVisibilitySM)) / 10.0;
				highLight(rawVisibilitySM + "SM");
			}
			else
			{
				String rawVisibility = matcher.group(2);
				String rawVisibilityIndicator = matcher.group(3);
				if (rawVisibility != null)
				{
					visibilitySM = Integer.parseInt(rawVisibility);
					visibilitySM = Math.round(10.0 * MUnit.metersToSM(visibilitySM)) / 10.0;
					visibilityNonDirectionalVaration = rawVisibilityIndicator != null && rawVisibilityIndicator.equals("NDV");
					highLight(rawVisibility + (rawVisibilityIndicator == null ? "" : rawVisibilityIndicator));
				}
			}
		}

		matcher = PATTERN_VISIBILITY_EXTRA.matcher(rawText);
		if (matcher.find())
		{
			String rawVisibility = matcher.group(1);
			String rawVisibilityIndicator = matcher.group(2);
			if (rawVisibility != null)
			{
				visibilitySMExtra = Integer.parseInt(rawVisibility);
				visibilitySMExtra = Math.round(10.0 * MUnit.metersToSM(visibilitySMExtra)) / 10.0;
				if (rawVisibilityIndicator != null && rawVisibilityIndicator.equals("S"))
					visibilityDirectionExtra = rawVisibilityIndicator;
				highLight(rawVisibility + (rawVisibilityIndicator == null ? "" : rawVisibilityIndicator));
			}
		}
	}

	private void decodeTemperature(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			Matcher matcher = PATTERN_TEMPERATURE.matcher(_items[i]);
			if (matcher.find())
			{
				String rawTemperature = matcher.group(1);
				if (rawTemperature.startsWith("M"))
					temperatureC = -Integer.parseInt(rawTemperature.substring(1));
				else
					temperatureC = Integer.parseInt(rawTemperature);

				String rawDewPoint = matcher.group(2);
				if (!rawDewPoint.isEmpty())
					try
					{
						if (rawDewPoint.startsWith("M"))
							dewPointC = -Integer.parseInt(rawDewPoint.substring(1));
						else
							dewPointC = Integer.parseInt(rawDewPoint);
					}
					catch (Exception e)
					{
					}

				highLight(_items[i]);
				return;
			}
		}
	}

	private void decodeAltimeter(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			Matcher matcher = PATTERN_ALTIMETER.matcher(_items[i]);
			if (matcher.find())
			{
				String rawAltimeterUnit = matcher.group(1);
				String rawAltimeter = matcher.group(2);

				if (rawAltimeterUnit.equals("Q"))
				{
					altimeterHpa = Integer.parseInt(rawAltimeter);
					altimeterInHg = MUnit.hPaToInHg(altimeterHpa);
				}
				else
				{
					altimeterInHg = Integer.parseInt(rawAltimeter) / 100.0;
					altimeterHpa = MUnit.inHgToHPa(altimeterInHg);
				}
				highLight(_items[i]);
				return;
			}
		}
	}

	private void decodeClouds(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			Matcher matcher = PATTERN_CLOUDS.matcher(_items[i]);
			if (matcher.find())
			{
				String cloudType = matcher.group(1);
				String altitude = matcher.group(2);
				String type = matcher.group(3);

				MCloud cloud = new MCloud();
				cloud.cover = MMetarDefinitions.instance.covers.get(cloudType);
				if (type != null)
					cloud.cover += " " + type;
				cloud.baseFeet = altitude == null ? -1 : Integer.parseInt(altitude) * 100;
				clouds.add(cloud);
				highLight(_items[i]);
			}
		}

		Collections.sort(clouds, new Comparator<MCloud>()
		{
			@Override
			public int compare(MCloud o1, MCloud o2)
			{
				return Integer.compare(o1.baseFeet, o2.baseFeet);
			}
		});
	}

	private void decodeWeather(String[] _items)
	{
		weather = "";

		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("RMK"))
				break;

			Matcher matcher = PATTERN_WEATHER.matcher(_items[i]);
			if (matcher.find())
			{
				String rawIntensity = matcher.group(1);
				if (rawIntensity == null)
					rawIntensity = "";
				String intensity = MMetarDefinitions.instance.weathers.get(rawIntensity);
				if (intensity == null)
					intensity = "Moderate";
				if (!intensity.isEmpty())
					intensity += " ";

				String rawDescriptor = matcher.group(2);
				if (rawDescriptor == null)
					rawDescriptor = "";
				String descriptor = MMetarDefinitions.instance.weathers.get(rawDescriptor);
				if (descriptor == null)
					descriptor = "";
				if (!descriptor.isEmpty())
					descriptor += " ";

				String rawPhenomenon = matcher.group(3);
				if (rawPhenomenon == null)
					rawPhenomenon = "";
				String phenomenon = MMetarDefinitions.instance.weathers.get(rawPhenomenon);

				if (rawPhenomenon.equals("VCSH"))
					intensity = "";

				weather += intensity + descriptor + phenomenon + ", ";
				highLight(_items[i]);
			}
		}

		if (!weather.isEmpty())
			weather = weather.substring(0, weather.length() - 2);
	}

	private void decodeRunwayVisualRange()
	{
		Matcher matcher = PATTERN_RUNWAY_VISUAL_RANGE.matcher(rawText);
		while (matcher.find())
		{
			String rawMatch = matcher.group(1);
			String rawRunway = matcher.group(2);
			String rawMoreLess = matcher.group(3);
			String rawMinVisibility = matcher.group(4);
			String rawMaxVisibility = matcher.group(6);
			String rawTrend = matcher.group(7); // U, D, N

			String moreLess = null;
			if (rawMoreLess != null)
				if (rawMoreLess.equals("P"))
					moreLess = ">";
				else if (rawMoreLess.equals("M"))
					moreLess = "<";

			int minVisibility = Integer.parseInt(rawMinVisibility);
			int maxVisibility = rawMaxVisibility == null ? -1 : Integer.parseInt(rawMaxVisibility);
			String trend = MMetarDefinitions.instance.runwayTrends.get(rawTrend);

			MRunwayVisualRange runwayVisualRange = new MRunwayVisualRange(rawRunway, moreLess, minVisibility, maxVisibility,
					trend);
			runwayVisualRanges.add(runwayVisualRange);

			highLight(rawMatch);
		}

		matcher = PATTERN_RUNWAY_VISUAL_RANGE_MISSING.matcher(rawText);
		while (matcher.find())
		{
			String rawMatch = matcher.group(1);
			String rawRunway = matcher.group(2);

			MRunwayVisualRange runwayVisualRange = new MRunwayVisualRange(rawRunway, null, -1, -1, null);
			runwayVisualRanges.add(runwayVisualRange);

			highLight(rawMatch);
		}
	}

	private void decodeRunwayConditions()
	{
		Matcher matcher = PATTERN_RUNWAY_CONDITIONS.matcher(rawText);
		while (matcher.find())
		{
			String rawMatch = matcher.group(1);
			String rawRunway = matcher.group(2);
			String rawContaminationType = matcher.group(3);
			String rawCoverage = matcher.group(4);
			String rawDepth = matcher.group(5);
			String rawBrakingAction = matcher.group(6);

			String contaminationType = MMetarDefinitions.instance.runwayContaminationTypes.get(rawContaminationType);
			String coverage = MMetarDefinitions.instance.runwayCoverages.get(rawCoverage);
			int depth = Integer.parseInt(rawDepth);
			String brakingAction = MMetarDefinitions.instance.runwayBrakingActions.get(rawBrakingAction);
			if (brakingAction == null)
			{
				double brakingCoefficient = Double.parseDouble(rawBrakingAction);
				if (brakingCoefficient >= 10.0 && brakingCoefficient <= 90.0)
					brakingCoefficient /= 10.0;
				brakingAction = MFormat.instance.numberFormatDecimal1.format(brakingCoefficient);
			}

			MRunwayCondition runwayCondition = new MRunwayCondition(rawRunway, contaminationType, coverage, depth,
					brakingAction);
			runwayConditions.add(runwayCondition);

			highLight(rawMatch);
		}
	}

	private void decodeRemarks()
	{
		if (rawText.contains(" RMK"))
		{
			highLight(" RMK");

			Matcher matcher = PATTERN_REMARK_AUTOMATED_STATION_TYPES.matcher(rawText);
			if (matcher.find())
			{
				String rawStationType = matcher.group(1);
				String stationType = MMetarDefinitions.instance.automatedStationTypes.get(rawStationType);
				if (stationType != null)
				{
					remarks.add(new MRemark(rawStationType, stationType));
					highLight(rawStationType);
				}
			}

			matcher = PATTERN_REMARK_SEA_LEVEL_PRESSURE.matcher(rawText);
			if (matcher.find())
			{
				String rawSeaLevelPressure = matcher.group(1);
				double pressure = Double.parseDouble(rawSeaLevelPressure.substring(3));
				if (pressure < 200.0)
					pressure = pressure / 10.0 + 1000.0;
				else
					pressure = pressure / 10.0 + 900.0;
				remarks.add(new MRemark(rawSeaLevelPressure,
						"Sea level pressure=" + MFormat.instance.numberFormatDecimal1.format(pressure) + " hPa"));
				highLight(rawSeaLevelPressure);
			}

			matcher = PATTERN_REMARK_PRECISE_TEMPERATURE.matcher(rawText);
			if (matcher.find())
			{
				String rawPreciseTemperature = matcher.group(1);
				String temperatureSign = rawPreciseTemperature.substring(1, 2);
				double temperature = Integer.parseInt(rawPreciseTemperature.substring(2, 5)) / 10.0;
				if (temperatureSign.equals("1"))
					temperature = -temperature;
				String dewPointSign = rawPreciseTemperature.substring(5, 6);
				double dewPoint = Integer.parseInt(rawPreciseTemperature.substring(6)) / 10.0;
				if (dewPointSign.equals("1"))
					dewPoint = -dewPoint;
				remarks.add(new MRemark(rawPreciseTemperature,
						"Precise temperature=" + MFormat.instance.numberFormatDecimal1.format(temperature) + "°C" + ", dew point="
								+ MFormat.instance.numberFormatDecimal1.format(dewPoint) + "°C"));
				highLight(rawPreciseTemperature);
			}

			matcher = PATTERN_REMARK_PRESSURE_TENDENCY.matcher(rawText);
			if (matcher.find())
			{
				String rawPressureTendency = matcher.group(1);
				String trend = rawPressureTendency.substring(1, 2);
				String pressureSign = rawPressureTendency.substring(2, 3);
				double pressure = Double.parseDouble(rawPressureTendency.substring(3)) / 10.0;
				if (pressureSign.equals("1"))
					pressure = -pressure;
				remarks.add(new MRemark(rawPressureTendency, "Pressure tendency="
						+ MMetarDefinitions.instance.pressureTendencies.get(trend) + ", " + pressure + " hPa change"));
				highLight(rawPressureTendency);
			}

			matcher = PATTERN_REMARK_SENSOR.matcher(rawText);
			while (matcher.find())
			{
				String rawSensor = matcher.group(1);
				String sensor = MMetarDefinitions.instance.sensors.get(rawSensor);
				if (sensor != null)
				{
					remarks.add(new MRemark(rawSensor, sensor));
					highLight(rawSensor);
				}
			}

			matcher = PATTERN_REMARK_MISSING.matcher(rawText);
			while (matcher.find())
			{
				String rawMissing = matcher.group(1) + " MISG";
				String missing = MMetarDefinitions.instance.dataMissing.get(rawMissing);
				if (missing != null)
				{
					remarks.add(new MRemark(rawMissing, missing));
					highLight(" " + rawMissing);
				}
			}

			if (rawText.endsWith(" $"))
			{
				remarks.add(new MRemark("$", "Maintenance needed at the station"));
				highLight(" $");
			}
		}
	}

	private void notDecoded()
	{
		Matcher matcher = PATTERN_NOT_DECODE.matcher(rawTextHighlight);
		while (matcher.find())
		{
			String nonBold = matcher.group(1).trim();
			if (!nonBold.isEmpty())
			{
				notDecoded = true;
				break;
			}
		}
	}

	public static void main(String[] args)
	{
		// Create the main frame
		JFrame frame = new JFrame("JSplitPane Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 300);

		// Create two panels to put in the split pane
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.RED);
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.BLUE);

		// Create the split pane with horizontal orientation
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

		// Add the split pane to the frame
		frame.getContentPane().add(splitPane);

		// SwingUtilities.invokeLater(() ->
		{
			frame.setVisible(true);
			// Set the divider location to 50% of the total size (middle)
			splitPane.setDividerLocation(0.5);
		}

		// Display the frame
	}
}
