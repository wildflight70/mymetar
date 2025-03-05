package data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.MFormat;
import util.MUnit;

public class MMetar
{
	public String rawText;
	public String stationId;
	public LocalDateTime observationTime;
	public double seaLevelPressureHpa = -1;
	public double altimeterInHg = -1;
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
	public String color;
	public ArrayList<MCloud> clouds;
	public ArrayList<MRunwayVisualRange> runwayVisualRanges;
	public ArrayList<MRunwayCondition> runwayConditions;
	public ArrayList<MRemark> remarks;

	public String extraFlightCategory = "";

	public String rawTextHighlight;
	public boolean notDecoded;

	private String cloudsString;
	private String runwayVisualRangesString;
	private String runwayConditionsString;

	private String rawTextBeforeRMK;
	private String rawTextAfterRMK;
	private String rawTextBeforeRMKHighlight;
	private String rawTextAfterRMKHighlight;

	public class MRunwayVisualRange
	{
		public String runway;
		public String minMoreLess; // null, <, >
		public int minVisibility = -1;
		public String maxMoreLess; // null, <, >
		public int maxVisibility = -1;
		public String trend; // null, (U)p, (D)own, (N)o change

		public MRunwayVisualRange(String _runway, String _minMoreLess, int _minVisibility, String _maxMoreLess,
				int _maxVisibility, String _trend)
		{
			runway = _runway;
			minMoreLess = _minMoreLess;
			minVisibility = _minVisibility;
			maxMoreLess = _maxMoreLess;
			maxVisibility = _maxVisibility;
			trend = _trend;
		}
	}

	public class MRunwayCondition
	{
		public String runway;
		public String contaminationType;
		public String coverage;
		public int depth;
		public String brakinkAction;

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
		rawText = _raw.trim();
		String[] raws = rawText.split("RMK");
		rawTextBeforeRMK = raws[0].trim();
		rawTextBeforeRMKHighlight = rawTextBeforeRMK;
		if (raws.length == 2)
		{
			rawTextAfterRMK = raws[1].trim();
			rawTextAfterRMKHighlight = rawTextAfterRMK;
		}

		clouds = new ArrayList<MCloud>();
		runwayVisualRanges = new ArrayList<MRunwayVisualRange>();
		runwayConditions = new ArrayList<MRunwayCondition>();
		remarks = new ArrayList<MRemark>();
	}

	private void updateRawTextHighLight()
	{
		rawTextHighlight = "<html>" + rawTextBeforeRMKHighlight;
		if (rawTextAfterRMK != null)
			rawTextHighlight += " <b>RMK</b> " + rawTextAfterRMKHighlight;
		rawTextHighlight += "</html>";
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
					if (runwayVisualRange.minMoreLess != null)
						buffer.append(runwayVisualRange.minMoreLess);

					buffer.append(MFormat.instance.numberFormatDecimal0.format(runwayVisualRange.minVisibility) + "m");

					if (runwayVisualRange.maxVisibility >= 0)
					{
						buffer.append(" to ");
						if (runwayVisualRange.maxMoreLess != null)
							buffer.append(runwayVisualRange.maxMoreLess);
						buffer.append(MFormat.instance.numberFormatDecimal0.format(runwayVisualRange.maxVisibility) + "m");
					}

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

	private String highLight(String _rawText, String _field)
	{
		String target = _field;

		String replacement = "<b>" + _field.trim() + "</b>";
		if (_field.startsWith(" "))
			replacement = " " + replacement;
		if (_field.endsWith(" "))
			replacement = replacement + " ";

		int index = _rawText.indexOf(target);
		if (index >= 0)
			return _rawText.substring(0, index) + replacement + _rawText.substring(index + target.length());
		else
			return _rawText;
	}

	private void highLightBeforeRMK(String _field)
	{
		rawTextBeforeRMKHighlight = highLight(rawTextBeforeRMKHighlight, _field);
	}

	private void highLightAfterRMK(String _field)
	{
		rawTextAfterRMKHighlight = highLight(rawTextAfterRMKHighlight, _field);
	}

	public void decode()
	{
		// 1. Decode before RMK
		String[] items = rawTextBeforeRMK.split(" ");

		decodeStationId();
		decodeObservationTime();
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
		decodeAirfieldElevation();
		decodeColor();

		// 2. Decode after RMK
		decodeRemarks();

		// 3. Update rawTextHighlight and highlight groups of slashes
		updateRawTextHighLight();
		decodeSlash();

		// 4. Check if metar is not totally decoded
		notDecoded();
	}

	private static final Pattern PATTERN_STATION_ID = Pattern.compile("^([A-Z0-9]+)\\b");

	private void decodeStationId()
	{
		Matcher matcher = PATTERN_STATION_ID.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			stationId = rawMatch;
			highLightBeforeRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_OBSERVATION_TIME = Pattern.compile("\\b(\\d{2})(\\d{2})(\\d{2})Z");

	private void decodeObservationTime()
	{
		Matcher matcher = PATTERN_OBSERVATION_TIME.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawDay = matcher.group(1);
			String rawHour = matcher.group(2);
			String rawMinute = matcher.group(3);

			int day = Integer.parseInt(rawDay);
			int hour = Integer.parseInt(rawHour);
			int minute = Integer.parseInt(rawMinute);

			if (observationTime == null)
			{
				LocalDate now = LocalDate.now();
				observationTime = LocalDateTime.of(now.getYear(), now.getMonthValue(), day, hour, minute);
			}

			highLightBeforeRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_SLASH = Pattern.compile("(?<= )/+/+(?= )|/+/+(?=$)|[AQ]/{4}");

	private void decodeSlash()
	{
		StringBuffer buffer = new StringBuffer();
		Matcher matcher = PATTERN_SLASH.matcher(rawTextHighlight);
		while (matcher.find())
		{
			String rawMatch = matcher.group();

			matcher.appendReplacement(buffer, "<b>" + rawMatch + "</b>");
		}
		matcher.appendTail(buffer);
		rawTextHighlight = buffer.toString();
	}

	private static final Pattern PATTERN_COLOR = Pattern.compile("\\b(AMB|(BLACK)?BLU|(BLACK)?GRN|RED|WHT|YLO)\\+?");

	private void decodeColor()
	{
		Matcher matcher = PATTERN_COLOR.matcher(rawTextBeforeRMKHighlight);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);

			color = MMetarDefinitions.instance.colorRemarks.get(rawMatch);

			matcher.appendReplacement(buffer, "<b>" + rawMatch + "</b>");
		}
		matcher.appendTail(buffer);
		rawTextBeforeRMKHighlight = buffer.toString();
	}

	private void decodeNoSignal()
	{
		noSignal = rawTextBeforeRMK.contains("NOSIG");
		if (noSignal)
			highLightBeforeRMK("NOSIG");
		else
		{
			noSignal = rawTextBeforeRMK.contains("RTD");
			if (noSignal)
				highLightBeforeRMK("RTD");
		}
	}

	private void decodeAuto()
	{
		auto = rawTextBeforeRMK.contains("AUTO");
		if (auto)
			highLightBeforeRMK("AUTO");
	}

	private void decodeCorrection()
	{
		correction = rawTextBeforeRMK.contains("COR");
		if (correction)
			highLightBeforeRMK("COR");
		else
		{
			correction = rawTextBeforeRMK.contains("CCA");
			if (correction)
				highLightBeforeRMK("CCA");
		}
	}

	private static final Pattern PATTERN_WIND = Pattern.compile("(?:(VRB|\\d{3}))(\\d{2})(?:G(\\d{2}))?(KT|MPS)");
	private static final Pattern PATTERN_WIND_VARIABLE = Pattern.compile("\\d+V\\d+");

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
				highLightBeforeRMK(_items[i]);
				break;
			}
		}

		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("/////KT"))
			{
				highLightBeforeRMK(_items[i]);
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
				highLightBeforeRMK(_items[i]);
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

	private static final Pattern PATTERN_VISIBILITY = Pattern
			.compile("\\s(\\d+\\s*\\d*/\\d+|\\d+)(?<unit>\\sHZSM|SM|KM)\\s|\\s(\\d{4})(NDV)?\\s");
	private static final Pattern PATTERN_VISIBILITY_EXTRA = Pattern.compile("\\s(\\d{4})(E|S|SE|N|NW|W)\\s");

	private void decodeVisibility(String[] _items)
	{
		Matcher matcher = PATTERN_VISIBILITY.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawVisibility = matcher.group(1);
			String rawVisibilityUnit = matcher.group("unit");
			if (rawVisibilityUnit != null)
			{
				double visibility = parseFractionalMiles(rawVisibility);
				visibilitySM = Math.round(10.0 * visibility) / 10.0;
				if (rawVisibilityUnit.equals("KM"))
					visibilitySM = Math.round(10.0 * MUnit.metersToSM(visibility * 1000)) / 10.0;
				highLightBeforeRMK(rawMatch);
			}
			else
			{
				rawVisibility = matcher.group(3);
				String rawVisibilityIndicator = matcher.group(4);
				if (rawVisibility != null)
				{
					visibilitySM = Integer.parseInt(rawVisibility);
					visibilitySM = Math.round(10.0 * MUnit.metersToSM(visibilitySM)) / 10.0;
					visibilityNonDirectionalVaration = rawVisibilityIndicator != null && rawVisibilityIndicator.equals("NDV");
					highLightBeforeRMK(rawMatch);
				}
			}
		}

		matcher = PATTERN_VISIBILITY_EXTRA.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawVisibility = matcher.group(1);
			String rawVisibilityIndicator = matcher.group(2);
			if (rawVisibility != null)
			{
				visibilitySMExtra = Integer.parseInt(rawVisibility);
				visibilitySMExtra = Math.round(10.0 * MUnit.metersToSM(visibilitySMExtra)) / 10.0;
				if (rawVisibilityIndicator != null)
					visibilityDirectionExtra = rawVisibilityIndicator;
				highLightBeforeRMK(rawMatch);
			}
		}
	}

	private static final Pattern PATTERN_TEMPERATURE = Pattern.compile("(M?\\d{2})/(M?\\d{0,2})$");

	private void decodeTemperature(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("RMK"))
				return;

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

				highLightBeforeRMK(_items[i]);
				return;
			}
		}
	}

	private static final Pattern PATTERN_ALTIMETER = Pattern.compile("(A|Q)(\\d{4})");

	private void decodeAltimeter(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("RMK"))
				return;

			Matcher matcher = PATTERN_ALTIMETER.matcher(_items[i]);
			if (matcher.find())
			{
				String rawAltimeterUnit = matcher.group(1);
				String rawAltimeter = matcher.group(2);

				if (rawAltimeterUnit.equals("Q"))
					seaLevelPressureHpa = Integer.parseInt(rawAltimeter);
				else // A
					altimeterInHg = Integer.parseInt(rawAltimeter) / 100.0;
				highLightBeforeRMK(_items[i]);
			}
		}
	}

	private static final Pattern PATTERN_CLOUDS = Pattern
			.compile("(CAVOK|CLR|SKC|NSC|NSW|NCD|FEW|SCT|BKN|OVC|VV)(\\d{2,3})?(CB|TCU)?");

	private void decodeClouds(String[] _items)
	{
		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("RMK"))
				return;

			Matcher matcher = PATTERN_CLOUDS.matcher(_items[i]);
			if (matcher.find())
			{
				String cloudType = matcher.group(1);
				String altitude = matcher.group(2);
				String type = matcher.group(3);

				MCloud cloud = new MCloud();
				cloud.cover = MMetarDefinitions.instance.weathers.get(cloudType);
				if (type != null)
					cloud.cover += " " + type;
				cloud.baseFeet = altitude == null ? -1 : Integer.parseInt(altitude) * 100;
				clouds.add(cloud);
				highLightBeforeRMK(_items[i]);
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

	private static final Pattern PATTERN_WEATHER = Pattern.compile(
			"(-|\\+|RE|VC)?(BC|BL|DR|FZ|MI|SH|TS)?(VCSH|RA|DZ|SN|SG|IC|PL|GR|GS|FG|BR|HZ|FU|VA|DU|SA|SQ|FC|SS|DS|TS)$");

	private void decodeWeather(String[] _items)
	{
		weather = "";

		for (int i = 2; i < _items.length; i++)
		{
			if (_items[i].equals("RMK"))
				return;

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
				highLightBeforeRMK(_items[i]);
			}
		}

		if (!weather.isEmpty())
			weather = weather.substring(0, weather.length() - 2);
	}

	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE = Pattern
			.compile("\\b(R(\\d{2}[LCR]?)/([PM])?(\\d{4})(V([PM])?(\\d{4}))?(FT)?/?([UND])?)");
	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE_MISSING = Pattern.compile("\\bR(\\d{2}[LCR]?)//{2,}");

	private void decodeRunwayVisualRange()
	{
		Matcher matcher = PATTERN_RUNWAY_VISUAL_RANGE.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawRunway = matcher.group(2);
			String rawMinMoreLess = matcher.group(3);
			String rawMinVisibility = matcher.group(4);
			String rawMaxMoreLess = matcher.group(6);
			String rawMaxVisibility = matcher.group(7);
			String rawFeet = matcher.group(8);
			String rawTrend = matcher.group(9); // U, D, N

			String minMoreLess = null;
			if (rawMinMoreLess != null)
				if (rawMinMoreLess.equals("P"))
					minMoreLess = ">";
				else if (rawMinMoreLess.equals("M"))
					minMoreLess = "<";

			String maxMoreLess = null;
			if (rawMaxMoreLess != null)
				if (rawMaxMoreLess.equals("P"))
					maxMoreLess = ">";
				else if (rawMaxMoreLess.equals("M"))
					maxMoreLess = "<";

			int minVisibility;
			if (rawFeet == null)
				minVisibility = Integer.parseInt(rawMinVisibility);
			else
				minVisibility = (int) Math.round(MUnit.feetToMeters(Integer.parseInt(rawMinVisibility)));
			int maxVisibility = rawMaxVisibility == null ? -1
					: (int) Math.round(MUnit.feetToMeters(Integer.parseInt(rawMaxVisibility)));

			String trend = MMetarDefinitions.instance.runwayTrends.get(rawTrend);

			MRunwayVisualRange runwayVisualRange = new MRunwayVisualRange(rawRunway, minMoreLess, minVisibility, maxMoreLess,
					maxVisibility, trend);
			runwayVisualRanges.add(runwayVisualRange);

			highLightBeforeRMK(rawMatch);
		}

		matcher = PATTERN_RUNWAY_VISUAL_RANGE_MISSING.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawRunway = matcher.group(1);

			MRunwayVisualRange runwayVisualRange = new MRunwayVisualRange(rawRunway, null, -1, null, -1, null);
			runwayVisualRanges.add(runwayVisualRange);

			highLightBeforeRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_RUNWAY_CONDITIONS = Pattern
			.compile("\\bR(\\d{2}[LCR]?)/(\\d)(\\d)(\\d{2})(\\d{2})");

	private void decodeRunwayConditions()
	{
		Matcher matcher = PATTERN_RUNWAY_CONDITIONS.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawRunway = matcher.group(1);
			String rawContaminationType = matcher.group(2);
			String rawCoverage = matcher.group(3);
			String rawDepth = matcher.group(4);
			String rawBrakingAction = matcher.group(5);

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

			highLightBeforeRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_AIRFIELD_ELEVATION = Pattern.compile("\\bQFE\\s(\\d+\\.\\d)");

	private void decodeAirfieldElevation()
	{
		Matcher matcher = PATTERN_AIRFIELD_ELEVATION.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawElevation = matcher.group(1);

			double elevation = Double.parseDouble(rawElevation);
			remarks.add(new MRemark(rawMatch,
					"Airfield elevation=" + MFormat.instance.numberFormatDecimal1.format(elevation) + " hPa"));
			highLightBeforeRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_AUTOMATED_STATION_TYPES = Pattern.compile("A[O0][12]A?");

	private void decodeRemarksAutomatedStationTypes()
	{
		Matcher matcher = PATTERN_REMARK_AUTOMATED_STATION_TYPES.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawStationType = matcher.group(0);
			String stationType = MMetarDefinitions.instance.automatedStationTypeRemarks.get(rawStationType);
			if (stationType != null)
			{
				remarks.add(new MRemark(rawStationType, stationType));
				highLightAfterRMK(rawStationType);
			}
		}
	}

	private static final Pattern PATTERN_REMARK_SEA_LEVEL_PRESSURE = Pattern.compile("SLP(\\d{3})");

	private void decodeRemarksSeaLevelPressure()
	{
		Matcher matcher = PATTERN_REMARK_SEA_LEVEL_PRESSURE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawSeaLevelPressure = matcher.group(1);
			double pressure = Double.parseDouble(rawSeaLevelPressure);
			if (pressure < 200.0)
				pressure = pressure / 10.0 + 1000.0;
			else
				pressure = pressure / 10.0 + 900.0;
			remarks.add(new MRemark(rawSeaLevelPressure,
					"Sea level pressure=" + MFormat.instance.numberFormatDecimal1.format(pressure) + " hPa"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_PRECISE_TEMPERATURE = Pattern
			.compile("\\bT(\\d)(\\d{3})((\\d)(\\d{3}))?");

	private void decodeRemarksPreciseTemperature()
	{
		Matcher matcher = PATTERN_REMARK_PRECISE_TEMPERATURE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			StringBuffer buffer = new StringBuffer();

			String temperatureSign = matcher.group(1);
			double temperature = Integer.parseInt(matcher.group(2)) / 10.0;
			if (temperatureSign.equals("1"))
				temperature = -temperature;
			buffer.append("Precise temperature=" + MFormat.instance.numberFormatDecimal1.format(temperature) + "°C");

			String dewPointSign = matcher.group(4);
			if (dewPointSign != null)
			{
				double dewPoint = Integer.parseInt(matcher.group(5)) / 10.0;
				if (dewPointSign.equals("1"))
					dewPoint = -dewPoint;
				buffer.append(", dew point=" + MFormat.instance.numberFormatDecimal1.format(dewPoint) + "°C");
			}

			remarks.add(new MRemark(rawMatch, buffer.toString()));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_PRESSURE_TENDENCY = Pattern.compile("\\b(PRESFR|PRESRR|5\\d{4})");

	private void decodeRemarksPressureTendency()
	{
		Matcher matcher = PATTERN_REMARK_PRESSURE_TENDENCY.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawPressureTendency = matcher.group(1);

			if (rawPressureTendency.equals("PRESFR"))
				remarks.add(new MRemark(rawPressureTendency, "Pressure falling rapidly"));
			else if (rawPressureTendency.equals("PRESRR"))
				remarks.add(new MRemark(rawPressureTendency, "Pressure rising rapidly"));
			else
			{
				String trend = rawPressureTendency.substring(1, 2);
				String pressureSign = rawPressureTendency.substring(2, 3);
				double pressure = Double.parseDouble(rawPressureTendency.substring(3)) / 10.0;
				if (pressureSign.equals("1"))
					pressure = -pressure;
				remarks.add(new MRemark(rawPressureTendency, "Pressure tendency="
						+ MMetarDefinitions.instance.pressureTendencyRemarks.get(trend) + ", " + pressure + " hPa change"));
			}
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_SENSOR = Pattern
			.compile("\\b(CHINO|PNO|PWINO|RVRNO|SLPNO|VISNO|TSNO|FZRANO|FROIN|WIND\\sSENSOR\\sOFFLINE|RTS)");

	private void decodeRemarksSensors()
	{
		Matcher matcher = PATTERN_REMARK_SENSOR.matcher(rawTextAfterRMKHighlight);

		StringBuffer buffer = new StringBuffer();
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);

			String sensor = MMetarDefinitions.instance.sensorRemarks.get(rawMatch);
			remarks.add(new MRemark(rawMatch, sensor));

			matcher.appendReplacement(buffer, "<b>" + rawMatch + "</b>");
		}
		matcher.appendTail(buffer);
		rawTextAfterRMKHighlight = buffer.toString();
	}

	private static final Pattern PATTERN_REMARK_SKY_COVERAGE = Pattern
			.compile("\\b((AC|ACC|AS|BLSN|CC|CF|CI|CS|CU|FG|HZ|IC|NS|SC|SF|SN|ST)\\d){1,}");
	private static final Pattern PATTERN_REMARK_SKY_COVERATE_ALTITUDE = Pattern.compile("\\b(\\d)(CI|CU|SC|AC)(\\d+)?");

	private void decodeRemarksSkyCoverage()
	{
		Matcher matcher = PATTERN_REMARK_SKY_COVERAGE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			StringBuffer buffer = new StringBuffer();

			int i = 0;
			while (i < rawMatch.length())
			{
				int d = i + 1;
				while (!Character.isDigit(rawMatch.charAt(d)))
					d++;

				String cloudType = MMetarDefinitions.instance.cloudRemarks.get(rawMatch.substring(i, d));
				int cover = Integer.parseInt(rawMatch.substring(d, d + 1));

				buffer.append(cloudType + "=" + cover + "/8, ");

				i = d + 1;
			}

			buffer.delete(buffer.length() - 2, buffer.length());
			remarks.add(new MRemark(rawMatch, buffer.toString()));
			highLightAfterRMK(rawMatch);
		}

		matcher = PATTERN_REMARK_SKY_COVERATE_ALTITUDE.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			StringBuffer buffer = new StringBuffer();

			String rawCover = matcher.group(1);
			String rawCloudType = matcher.group(2);
			String rawAltitude = matcher.group(3);

			int cover = Integer.parseInt(rawCover);
			String cloudType = MMetarDefinitions.instance.cloudRemarks.get(rawCloudType);
			buffer.append(cloudType + "=" + cover + "/8");

			if (rawAltitude != null)
			{
				int altitude = Integer.parseInt(rawAltitude) * 100;
				buffer.append(" at " + altitude + " ft");
			}

			remarks.add(new MRemark(rawMatch, buffer.toString()));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_WEATHER = Pattern.compile(
			"\\b(CIG|CLD(\\sEMBD)?|CVCTV|DP|(SMOKE\\s)?FU\\s(ALQDS|ALL\\sQUADS)|HALO|ICE|LGT\\sICG|PCPN|RAG|VIS|WX)(\\d{3}|\\sMISG)?\\b");

	private void decodeRemarksWeather()
	{
		HashMap<String, String> weathers = new HashMap<String, String>();
		weathers.put("SNW CVR/TRACE LOOSE", "Snow cover, trace amounts, loosely packed");
		weathers.put("SNOW COVER HARD PACK", "Snow cover is hard-packed");
		weathers.put("SNW CVR/MUCH LOOSE", "Snow cover is loose and easily lifted by the wind");
		weathers.put("SNW CVR/MEDIUM PACK", "Snow cover is medium packed");
		for (String weather : weathers.keySet())
		{
			if (rawTextAfterRMK.contains(weather))
			{
				remarks.add(new MRemark(weather, weathers.get(weather)));
				highLightAfterRMK(weather);
			}
		}

		Matcher matcher = PATTERN_REMARK_WEATHER.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawWeather = matcher.group(1);
			String rawAltitudeMissing = matcher.group(5);

			String weather = MMetarDefinitions.instance.cloudRemarks.get(rawWeather);
			if (rawAltitudeMissing == null)
				remarks.add(new MRemark(rawWeather, weather));
			else if (rawAltitudeMissing.equals(" MISG"))
				remarks.add(new MRemark(rawWeather, weather + " missing"));
			else
			{
				int altimeter = Integer.parseInt(rawAltitudeMissing) * 100;
				remarks.add(new MRemark(rawWeather, weather + " at " + altimeter + " ft"));
			}
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_WEATHER_AMOUNT = Pattern.compile("\\b(I|P)(\\d{4})");

	private void decodeRemarksWeatherAmount()
	{
		Matcher matcher = PATTERN_REMARK_WEATHER_AMOUNT.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawType = matcher.group(1);
			String rawAmount = matcher.group(2);

			String type = "";
			if (rawType.equals("I"))
				type = "Ice";
			else if (rawType.equals("P"))
				type = "Precipitation";
			double amount = Double.parseDouble(rawAmount) / 100.0;

			remarks.add(new MRemark(rawMatch, type + "=" + amount + " inches in the past hour"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_CLOUDS = Pattern.compile(
			"\\b(CF\\sTR|SOG\\sTR|ST\\sTR|CI\\sTR|SF\\sTR|SC\\sTR|SC\\sOP|SC\\sCL|AC\\sTR|ACC\\sTR|AS\\sTR|AC\\sOP\\AC\\sCUGEN|CB|TCU|OCNL\\sBLSN|BLSN\\sOCNL)");

	private void decodeRemarksClouds()
	{
		Matcher matcher = PATTERN_REMARK_CLOUDS.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String cloud = MMetarDefinitions.instance.cloudRemarks.get(rawMatch);
			remarks.add(new MRemark(rawMatch, cloud));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_ALTIMETER = Pattern.compile("A(\\d{4})");

	private void decodeRemarksAltimeter()
	{
		Matcher matcher = PATTERN_REMARK_ALTIMETER.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawAltimeter = matcher.group(1);
			double altimeter = Integer.parseInt(rawAltimeter) / 100.0;
			remarks.add(new MRemark(rawMatch, "Altimeter=" + altimeter + " inHg"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_LAST_STATIONARY_FLIGHT_DIRECTION = Pattern
			.compile("\\b((LST|LAST|LAAST)\\s?(STAFFED|STFD|STGFD)?)");

	private void decodeRemarksLastStationaryFlightDirection()
	{
		Matcher matcher = PATTERN_REMARK_LAST_STATIONARY_FLIGHT_DIRECTION.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(1);

			remarks.add(new MRemark(rawMatch, "Last stationary flight direction"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final String REMARK_NEXT_OBSERVATION_1 = "(OBS(/|\\s);?(NEXT|NXT)(/|\\s|\\sOBS)?\\s?(?<day1>\\d{2})?\\s?(?<hour1>\\d{2})(?<minute1>\\d{2})\\s?Z)";
	private static final String REMARK_NEXT_OBSERVATION_2 = "(OBS/NXT\\s(?<day2>\\d{2})@(?<hour2>\\d{2})(?<minute2>)Z)";
	private static final String REMARK_NEXT_OBSERVATION_3 = "(OBS/(?<day3>\\d{2})(?<hour3>\\d{2})(?<minute3>\\d{2})Z)";
	private static final String REMARK_NEXT_OBSERVATION_4 = "(OBS\\s?/?\\s?(NEXT|NXT)(/|\\s)(?<day4>\\d{2})(?<hour4>\\d{2})(?<minute4>\\d{2})\\s?UTC)";
	private static final String REMARK_NEXT_OBSERVATION_5 = "(OBS/\\s?(NEXT|NXT)\\s(OBS\\s)?(?<day5>\\d{2})(?<hour5>\\d{2})(?<minute5>\\d{2}))";

	private static final Pattern PATTERN_REMARK_NEXT_OBSERVATION = Pattern
			.compile("(" + REMARK_NEXT_OBSERVATION_1 + "|" + REMARK_NEXT_OBSERVATION_2 + "|" + REMARK_NEXT_OBSERVATION_3 + "|"
					+ REMARK_NEXT_OBSERVATION_4 + "|" + REMARK_NEXT_OBSERVATION_5 + ")");

	private void decodeRemarksNextObservation()
	{
		Matcher matcher = PATTERN_REMARK_NEXT_OBSERVATION.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);

			String rawDay = null;
			String rawHour = null;
			String rawMinute = null;
			for (int g = 1; g <= 5; g++)
			{
				if (rawDay == null)
					rawDay = matcher.group("day" + g);
				if (rawHour == null)
					rawHour = matcher.group("hour" + g);
				if (rawMinute == null)
					rawMinute = matcher.group("minute" + g);
			}

			int day = rawDay == null ? observationTime.getDayOfMonth() : Integer.parseInt(rawDay);
			String minute = rawMinute == null ? "" : rawMinute;

			remarks.add(new MRemark(rawMatch, "Next observation at " + day + "th " + rawHour + ":" + minute + "Z"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_DENSITY_ALTITUDE = Pattern.compile("\\bDENSITY\\sALT\\s(-?\\d+)FT");

	private void decodeRemarksDensityAltitude()
	{
		Matcher matcher = PATTERN_REMARK_DENSITY_ALTITUDE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawAltitude = matcher.group(1);
			int altitude = Integer.parseInt(rawAltitude);
			remarks.add(new MRemark(rawMatch, "Density altitude=" + altitude + " ft"));
			highLightAfterRMK(rawMatch);
		}
	}

	private void decodeRemarksColor()
	{
		Matcher matcher = PATTERN_COLOR.matcher(rawTextAfterRMKHighlight);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);

			String color = MMetarDefinitions.instance.colorRemarks.get(rawMatch);
			remarks.add(new MRemark(rawMatch, color));

			matcher.appendReplacement(buffer, "<b>" + rawMatch + "</b>");
		}
		matcher.appendTail(buffer);
		rawTextAfterRMKHighlight = buffer.toString();
	}

	private static final Pattern PATTERN_REMARK_AIRFIELD_ELEVATION = Pattern.compile("\\bQFE(\\d+)(/(\\d+))?");

	private void decodeRemarksAirfieldElevation()
	{
		Matcher matcher = PATTERN_REMARK_AIRFIELD_ELEVATION.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawFirstElevation = matcher.group(1);

			int firstElevation = Integer.parseInt(rawFirstElevation);
			remarks.add(new MRemark(rawMatch, "Airfield elevation=" + firstElevation + " mmHg + ("
					+ MFormat.instance.numberFormatDecimal0.format(MUnit.mmHgToHPa(firstElevation)) + " hPa)"));
			highLightAfterRMK(rawMatch);
		}
	}

	private static final Pattern PATTERN_REMARK_WIND = Pattern
			.compile("\\bWIND\\s(?<alt>\\d+)FT\\s(?<dir>\\d{3})(?<speed>\\d{2})(G(?<gust>\\d{2}))?KT");
	private static final Pattern PATTERN_REMARK_PEAK_WIND = Pattern
			.compile("\\bPK\\sWND\\s(?<dir>\\d{3})(?<speed>\\d{2})/(?<hour>\\d{2})(?<minute>\\d{2})");

	private void decodeRemarksWind()
	{
		if (rawTextAfterRMK.contains("WIND EST"))
		{
			remarks.add(new MRemark("WIND EST", "Wind speed estimated"));
			highLightAfterRMK("WIND EST");
		}
		else if (rawTextAfterRMK.contains("WND DATA ESTMD"))
		{
			remarks.add(new MRemark("WND DATA ESTMD", "Wind speed estimated"));
			highLightAfterRMK("WND DATA ESTMD");
		}
		else
		{
			Matcher matcher = PATTERN_REMARK_WIND.matcher(rawTextAfterRMK);
			while (matcher.find())
			{
				String rawMatch = matcher.group(0);

				String rawAltitude = matcher.group("alt");
				String rawDirection = matcher.group("dir");
				String rawSpeed = matcher.group("speed");
				String rawGust = matcher.group("gust");

				int altitude = Integer.parseInt(rawAltitude);
				int direction = rawDirection == null ? -1 : Integer.parseInt(rawDirection);
				int speed = rawSpeed == null ? -1 : Integer.parseInt(rawSpeed);

				StringBuffer buffer = new StringBuffer("Wind at " + altitude + " ft");
				if (direction >= 0)
					buffer.append(", " + direction + "° at " + speed + " kt");
				if (rawGust != null)
				{
					int gust = Integer.parseInt(rawGust);
					buffer.append(", gust at " + gust + " kt");
				}

				remarks.add(new MRemark(rawMatch, buffer.toString()));
				highLightAfterRMK(rawMatch);
			}

			matcher = PATTERN_REMARK_PEAK_WIND.matcher(rawTextAfterRMK);
			while (matcher.find())
			{
				String rawMatch = matcher.group(0);
				String rawDirection = matcher.group("dir");
				String rawSpeed = matcher.group("speed");
				String rawHour = matcher.group("hour");
				String rawMinute = matcher.group("minute");

				int direction = Integer.parseInt(rawDirection);
				int speed = Integer.parseInt(rawSpeed);
				int hour = Integer.parseInt(rawHour);
				int minute = Integer.parseInt(rawMinute);

				StringBuffer buffer = new StringBuffer(
						"Peak wind " + direction + "° at " + speed + " kt at " + hour + ":" + minute + "Z");

				remarks.add(new MRemark(rawMatch, buffer.toString()));
				highLightAfterRMK(rawMatch);
			}
		}
	}

	private static final Pattern PATTERN_REMARK_SNOW_ACCUMULATION = Pattern.compile("/S(\\d{1,2})/");

	private void decodeRemarksSnowAccumulation()
	{
		Matcher matcher = PATTERN_REMARK_SNOW_ACCUMULATION.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			String rawAccumulation = matcher.group(1);

			int accumulation = Integer.parseInt(rawAccumulation);
			remarks.add(new MRemark(rawMatch, "Possibly snow accumulation of " + accumulation + " cm per hour"));

			highLightAfterRMK(rawMatch);
		}
	}

	private static final String REMARK_PRECIPITATIONS = "(DZ|RA|SN|UP)([BE]\\d{2})(E\\d{2})?";
	private static final Pattern PATTERN_REMARK_PRECIPITATIONS = Pattern
			.compile("\\b(" + REMARK_PRECIPITATIONS + "){1,}");
	private static final Pattern PATTERN_REMARK_PRECIPITATIONS_INSIDE = Pattern.compile(REMARK_PRECIPITATIONS);

	private void decodeRemarksPrecipitations()
	{
		Matcher matcher = PATTERN_REMARK_PRECIPITATIONS.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			StringBuffer buffer = new StringBuffer();

			Matcher matcherInside = PATTERN_REMARK_PRECIPITATIONS_INSIDE.matcher(rawMatch);
			while (matcherInside.find())
			{
				String rawPrecipitationType = matcherInside.group(1);
				String rawFirst = matcherInside.group(2);
				String rawSecond = matcherInside.group(3);

				String precipitationType = "";
				if (rawPrecipitationType.equals("DZ"))
					precipitationType = "Drizzle";
				else if (rawPrecipitationType.equals("RA"))
					precipitationType = "Rain";
				else if (rawPrecipitationType.equals("SN"))
					precipitationType = "Snow";
				else if (rawPrecipitationType.equals("UP"))
					precipitationType = "Temperature rise";

				if (rawFirst.startsWith("B"))
				{
					String rawBegin = rawFirst.substring(1, 3);
					int begin = Integer.parseInt(rawBegin);

					buffer.append(precipitationType + " began " + begin + " min ");

					if (rawSecond != null && rawSecond.startsWith("E"))
					{
						String rawEnd = rawSecond.substring(1, 3);
						int end = Integer.parseInt(rawEnd);

						buffer.append("and ended " + end + " min, ");
					}
				}
				else // E
				{
					String rawEnd = rawFirst.substring(1, 3);
					int end = Integer.parseInt(rawEnd);

					buffer.append(precipitationType + " ended " + end + " min, ");
				}
			}

			if (buffer.toString().endsWith(", "))
				buffer.delete(buffer.length() - 2, buffer.length());
			remarks.add(new MRemark(rawMatch, buffer.toString()));

			highLightAfterRMK(rawMatch);
		}
	}

	private void decodeRemarksMaintenance()
	{
		if (rawTextAfterRMK.endsWith("$"))
		{
			remarks.add(new MRemark("$", "Maintenance needed at the station"));
			highLightAfterRMK("$");
		}
	}

	private void decodeRemarks()
	{
		if (rawTextAfterRMK != null)
		{
			decodeRemarksAutomatedStationTypes();
			decodeRemarksSeaLevelPressure();
			decodeRemarksPreciseTemperature();
			decodeRemarksPressureTendency();
			decodeRemarksSensors();
			decodeRemarksSkyCoverage();
			decodeRemarksAltimeter();
			decodeRemarksWeather();
			decodeRemarksWeatherAmount();
			decodeRemarksLastStationaryFlightDirection();
			decodeRemarksNextObservation();
			decodeRemarksClouds();
			decodeRemarksDensityAltitude();
			decodeRemarksColor();
			decodeRemarksAirfieldElevation();
			decodeRemarksWind();
			decodeRemarksSnowAccumulation();
			decodeRemarksPrecipitations();
			decodeRemarksMaintenance();
		}
	}

	private static final Pattern PATTERN_NOT_DECODE = Pattern.compile("(?<=^<html>|</b>)(.*?)(?=<b>|</html>$)");

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

	public String debug()
	{
		StringBuffer buffer = new StringBuffer(rawText);
		buffer.append("\n");
		buffer.append(rawTextHighlight);
		buffer.append("\n");
		buffer.append("decoded=" + !notDecoded + "\n");
		buffer.append("Station id=" + stationId + "\n");
		buffer.append("Observation time=" + observationTime + "\n");
		buffer.append("REMARKS\n");
		for (MRemark remark : remarks)
			buffer.append(remark.field + ":" + remark.remark + "\n");

		return buffer.toString();
	}

	public static void main(String[] args)
	{
		MMetar metar = new MMetar((LocalDateTime) null,
				"K7W4 281255Z AUTO 25003KT 10SM SCT060 08/M04 A2989 RMK AO1 T00751044");
		metar.decode();
		System.out.println(metar.debug());
	}
}
