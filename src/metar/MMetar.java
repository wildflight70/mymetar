package metar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.MFormat;
import util.MUnit;

public class MMetar
{
	public static final int INTEGER_NO_VALUE = Integer.MIN_VALUE;
	public static final double DOUBLE_NO_VALUE = -1.0;

	public String rawText;
	public String stationId;
	public LocalDateTime observationTime;
	public double seaLevelPressureHpa = INTEGER_NO_VALUE;
	public double altimeterInHg = INTEGER_NO_VALUE;
	public int temperatureC;
	public int dewPointC = INTEGER_NO_VALUE;
	public boolean auto;
	public boolean noSignal;
	public boolean correction;
	public double visibilitySM = DOUBLE_NO_VALUE;
	public boolean visibilityNonDirectionalVariation;
	public double visibilitySMExtra = DOUBLE_NO_VALUE;
	public String visibilityDirectionExtra = "";
	public int windDirectionDegree = INTEGER_NO_VALUE;
	public int windSpeedKt = INTEGER_NO_VALUE;
	public int windGustKt = INTEGER_NO_VALUE;
	public boolean windVariable;
	public int windFromDegree;
	public int windToDegree;
	public String weather;
	public String color;
	public ArrayList<MCover> covers;
	public ArrayList<MRunwayVisualRange> runwayVisualRanges;
	public ArrayList<MRunwayCondition> runwayConditions;
	public String temporary = "";
	public String becoming = "";

	public ArrayList<MItem> items;
	public ArrayList<MRemark> remarks;

	public String extraFlightCategory = "";

	public String rawTextHighlight;
	public boolean notDecoded;

	private String coversString;
	private String runwayVisualRangesString;
	private String runwayConditionsString;

	private String rawTextBeforeRMK;
	private String rawTextAfterRMK;

	private int posTempoBeforeRMK = -1;
	private int posBecomingBeforeRMK = -1;

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
		if (raws.length == 2)
			rawTextAfterRMK = raws[1].trim();

		covers = new ArrayList<MCover>();
		runwayVisualRanges = new ArrayList<MRunwayVisualRange>();
		runwayConditions = new ArrayList<MRunwayCondition>();
		remarks = new ArrayList<MRemark>();
		items = new ArrayList<MItem>();

		posTempoBeforeRMK = rawTextBeforeRMK.indexOf("TEMPO");
		posBecomingBeforeRMK = rawTextBeforeRMK.indexOf("BECMG");
	}

	private String highlight()
	{
		StringBuffer buffer = new StringBuffer("<html>");

		buffer.append(highlight(items, rawTextBeforeRMK));

		if (rawTextAfterRMK != null)
		{
			buffer.append(" <b>RMK</b> ");
			buffer.append(highlight(remarks, rawTextAfterRMK));
		}

		buffer.append("</html>");

		String text = buffer.toString().replace("TEMPO", "<b>TEMPO</b>");
		text = text.replace("BECMG", "<b>BECMG</b>");

		return text;
	}

	private String highlight(ArrayList<? extends MItem> _items, String _text)
	{
		StringBuffer buffer = new StringBuffer();
		int posText = 0;
		for (MItem item : _items)
		{
			buffer.append(_text.substring(posText, item.begin));
			buffer.append("<b>");
			buffer.append(item.field);
			buffer.append("</b>");
			posText = item.end;
		}
		buffer.append(_text.substring(posText));
		return buffer.toString();
	}

	public String coversToString()
	{
		if (coversString == null)
		{
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < covers.size(); i++)
			{
				MCover cover = covers.get(i);
				buffer.append(cover.toString());

				if (i < covers.size() - 1)
					buffer.append(", ");
			}
			coversString = buffer.toString();
		}
		return coversString;
	}

	public String runwayVisualRangesToString()
	{
		if (runwayVisualRangesString == null)
		{
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < runwayVisualRanges.size(); i++)
			{
				MRunwayVisualRange runwayVisualRange = runwayVisualRanges.get(i);
				buffer.append(runwayVisualRange.toString());

				if (i < runwayVisualRanges.size() - 1)
					buffer.append(", ");
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
				buffer.append(runwayCondition.toString());

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

	public void decode()
	{
		System.out.println(rawText);

		// 1. Decode before RMK
		decodeStationId();
		decodeObservationTime();
		decodeAltimeter();
		decodeTemperature();
		decodeNoSignal();
		decodeAuto();
		decodeCorrection();
		decodeVisibility();
		decodeWind();
		decodeCovers();
		decodeWeather();
		decodeRunwayVisualRange();
		decodeRunwayConditions();
		decodeAirfieldElevation();
		decodeColor();
		decodeBecomingTime();

		// 2. Decode after RMK
		decodeRemarks();

		// 3. Sort items and remarks by begin
		Collections.sort(items, new Comparator<MItem>()
		{
			@Override
			public int compare(MItem o1, MItem o2)
			{
				return Integer.compare(o1.begin, o2.begin);
			}
		});

		Collections.sort(remarks, new Comparator<MItem>()
		{
			@Override
			public int compare(MItem o1, MItem o2)
			{
				return Integer.compare(o1.begin, o2.begin);
			}
		});

		// 4. Update rawTextHighlight and highlight groups of slashes
		rawTextHighlight = highlight();
		decodeSlash();

		// 5. Check if metar is not totally decoded
		notDecoded();
	}

	private static final Pattern PATTERN_STATION_ID = Pattern.compile("^([A-Z0-9]+)\\b");

	private void decodeStationId()
	{
		Matcher matcher = PATTERN_STATION_ID.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			stationId = rawMatch;

			items.add(new MItem(rawMatch, "Station id=" + stationId, begin, end));
		}
	}

	private static final Pattern PATTERN_OBSERVATION_TIME = Pattern.compile("\\b(\\d{2})(\\d{2})(\\d{2})Z");

	private void decodeObservationTime()
	{
		Matcher matcher = PATTERN_OBSERVATION_TIME.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			items.add(new MItem(rawMatch, "Observation time=" + observationTime.toString(), begin, end));
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

	private static final Pattern PATTERN_COLOR = Pattern.compile("\\b(AMB|(BLACK)?BLU|(BLACK)?GRN|RED|WHT|YLO[12]?)\\+?");

	private void decodeColor()
	{
		Matcher matcher = PATTERN_COLOR.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String colorRemark = MMetarDefinitions.instance.colorRemarks.get(rawMatch);
			if (colorRemark != null)
			{
				color += colorRemark + ", ";

				items.add(new MItem(rawMatch, "Color=" + colorRemark, begin, end));
			}
		}
	}

	private void decodeNoSignal()
	{
		String rawMatch = "NOSIG";
		int begin = rawTextBeforeRMK.indexOf(rawMatch);
		noSignal = begin >= 0;
		if (!noSignal)
		{
			rawMatch = "RTD";
			begin = rawTextBeforeRMK.indexOf(rawMatch);
			noSignal = begin >= 0;
		}
		if (noSignal)
			items.add(new MItem(rawMatch, "No signal", begin, begin + rawMatch.length()));
	}

	private void decodeAuto()
	{
		String rawMatch = "AUTO";
		int begin = rawTextBeforeRMK.indexOf(rawMatch);
		auto = begin >= 0;
		if (auto)
			items.add(new MItem(rawMatch, "Automated station", begin, begin + rawMatch.length()));
	}

	private void decodeCorrection()
	{
		String rawMatch = " COR ";
		int begin = rawTextBeforeRMK.indexOf(rawMatch);
		correction = begin >= 0;
		if (!correction)
		{
			rawMatch = " CCA ";
			begin = rawTextBeforeRMK.indexOf(rawMatch);
			correction = begin >= 0;
		}
		if (correction)
			items.add(new MItem(rawMatch, "Correction", begin + 1, begin + 1 + rawMatch.length() - 1));
	}

	private static final Pattern PATTERN_WIND = Pattern.compile("\\b(VRB|\\d{3})(\\d{2})?(G(\\d{2}))?(KT|MPS)");
	private static final Pattern PATTERN_WIND_VARIABLE = Pattern.compile("\\b(\\d+)V(\\d+)\\b");

	private void decodeWind()
	{
		Matcher matcher = PATTERN_WIND_VARIABLE.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawFrom = matcher.group(1);
			String rawTo = matcher.group(2);

			windVariable = true;
			windFromDegree = Integer.parseInt(rawFrom);
			windToDegree = Integer.parseInt(rawTo);

			items.add(new MItem(rawMatch, "Wind variable from " + windFromDegree + "° to " + windToDegree + "°", begin, end));
		}

		String rawMatch = "/////KT";
		int p = rawTextBeforeRMK.indexOf(rawMatch);
		if (p >= 0)
			items.add(new MItem(rawMatch, "Wind undefined", p, p + rawMatch.length()));

		matcher = PATTERN_WIND.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawDirection = matcher.group(1);
			String rawSpeed = matcher.group(2);
			String rawGust = matcher.group(4);
			String rawSpeedUnit = matcher.group(5);

			boolean isTempo = isTempoBeforeRMK(begin);

			int windDirectionDegree = MMetar.INTEGER_NO_VALUE;
			if (rawDirection.equals("VRB"))
				windDirectionDegree = -1;
			else
				windDirectionDegree = Integer.parseInt(rawDirection);

			StringBuffer buffer = new StringBuffer();
			if (isTempo)
				buffer.append("Temporary wind ");
			else
				buffer.append("Wind ");

			if (rawDirection.equals("VRB"))
				buffer.append("variable ");
			else if (!rawDirection.equals("/////"))
			{
				windDirectionDegree = Integer.parseInt(rawDirection);
				buffer.append(windDirectionDegree + "° ");
			}

			int windSpeedKt = rawSpeed == null ? MMetar.INTEGER_NO_VALUE : Integer.parseInt(rawSpeed);
			if (rawSpeed != null && rawSpeedUnit.equals("MPS"))
				windSpeedKt = MUnit.mpsToKnots(windSpeedKt);

			if (rawSpeed != null)
				buffer.append("at " + windSpeedKt + " kt");

			int windGustKt = MMetar.INTEGER_NO_VALUE;
			if (rawGust != null)
			{
				windGustKt = Integer.parseInt(rawGust);
				if (rawSpeedUnit.equals("MPS"))
					windGustKt = MUnit.mpsToKnots(windGustKt);
				buffer.append(" with gust at " + windGustKt + " kt");
			}

			if (isTempo)
				temporary += buffer.toString() + ", ";
			else
			{
				this.windDirectionDegree = windDirectionDegree;
				this.windSpeedKt = windSpeedKt;
				this.windGustKt = windGustKt;
			}

			items.add(new MItem(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_BECOMING_TIME = Pattern.compile("\\bTL(\\d{2})(\\d{2})");

	private void decodeBecomingTime()
	{
		Matcher matcher = PATTERN_BECOMING_TIME.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawHour = matcher.group(1);
			String rawMinute = matcher.group(2);

			if (isBecomingBeforeRMK(begin))
			{
				String buffer = "Becoming by " + Integer.parseInt(rawHour) + ":" + Integer.parseInt(rawMinute) + "Z";

				items.add(new MItem(rawMatch, buffer, begin, end));
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
			.compile("\\b(\\d+\\s*\\d*/\\d+|\\d+)(?<unit>\\sHZSM|SM|KM)\\b|\\b(\\d{4})(NDV)?\\b");
	private static final Pattern PATTERN_VISIBILITY_EXTRA = Pattern.compile("\\s(\\d{4})(E|S|SE|N|NW|W)\\b");

	private void decodeVisibility()
	{
		Matcher matcher = PATTERN_VISIBILITY.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			boolean isTempo = isTempoBeforeRMK(begin);
			boolean isBecoming = isBecomingBeforeRMK(begin);

			String rawVisibility = matcher.group(1);
			String rawVisibilityUnit = matcher.group("unit");
			if (rawVisibilityUnit != null)
			{
				double visibility = parseFractionalMiles(rawVisibility);
				if (rawVisibilityUnit.equals("KM"))
					visibility = MUnit.metersToSM(visibility * 1000);

				double visibilitySM = Math.round(10.0 * visibility) / 10.0;

				String buffer = "Visibility=" + visibilitySM + " SM";

				if (isTempo)
					temporary += buffer + ", ";
				else if (isBecoming)
					becoming += buffer + ", ";
				else
					this.visibilitySM = visibilitySM;

				if (isTempo)
					buffer = "Temporary " + buffer;
				else if (isBecoming)
					buffer = "Becoming " + buffer;
				items.add(new MItem(rawMatch, buffer, begin, end));
			}
			else
			{
				rawVisibility = matcher.group(3);
				String rawVisibilityIndicator = matcher.group(4);
				if (rawVisibility != null)
				{
					double visibilitySM = Integer.parseInt(rawVisibility);
					visibilitySM = Math.round(10.0 * MUnit.metersToSM(visibilitySM)) / 10.0;
					boolean visibilityNonDirectionalVariation = rawVisibilityIndicator != null;

					StringBuffer buffer = new StringBuffer("Visibility=" + visibilitySM + " SM");
					if (visibilityNonDirectionalVariation)
						buffer.append(" non directional variation");

					if (isTempo)
						temporary += buffer.toString() + ", ";
					else if (isBecoming)
						becoming += buffer.toString() + ", ";
					else
					{
						this.visibilitySM = visibilitySM;
						this.visibilityNonDirectionalVariation = visibilityNonDirectionalVariation;
					}

					if (isTempo)
						buffer.insert(0, "Temporary ");
					else if (isBecoming)
						buffer.insert(0, "Becoming ");
					items.add(new MItem(rawMatch, buffer.toString(), begin, end));
				}
			}
		}

		matcher = PATTERN_VISIBILITY_EXTRA.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawVisibility = matcher.group(1);
			String rawVisibilityIndicator = matcher.group(2);
			if (rawVisibility != null)
			{
				visibilitySMExtra = Integer.parseInt(rawVisibility);
				visibilitySMExtra = Math.round(10.0 * MUnit.metersToSM(visibilitySMExtra)) / 10.0;
				if (rawVisibilityIndicator != null)
					visibilityDirectionExtra = rawVisibilityIndicator;
				items.add(new MItem(rawMatch, "Visibility=" + visibilitySM + " " + visibilityDirectionExtra, begin, end));
			}
		}
	}

	private static final Pattern PATTERN_TEMPERATURE = Pattern.compile("\\b(M?\\d{2})/((M|-)?\\d{0,2})");

	private void decodeTemperature()
	{
		Matcher matcher = PATTERN_TEMPERATURE.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawTemperature = matcher.group(1);
			if (rawTemperature.startsWith("M"))
				temperatureC = -Integer.parseInt(rawTemperature.substring(1));
			else
				temperatureC = Integer.parseInt(rawTemperature);

			String rawDewPoint = matcher.group(2);
			if (!rawDewPoint.isEmpty())
				try
				{
					if (rawDewPoint.startsWith("M") || rawDewPoint.startsWith("-"))
						dewPointC = -Integer.parseInt(rawDewPoint.substring(1));
					else
						dewPointC = Integer.parseInt(rawDewPoint);
				}
				catch (Exception e)
				{
				}

			StringBuffer buffer = new StringBuffer();
			buffer.append("Temperature=" + temperatureC + "°C");
			if (dewPointC != Integer.MIN_VALUE)
				buffer.append(", dew point=" + dewPointC + "°C");
			items.add(new MItem(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_ALTIMETER = Pattern.compile("\\b(A|Q)(\\d{4})");

	private void decodeAltimeter()
	{
		Matcher matcher = PATTERN_ALTIMETER.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawAltimeterUnit = matcher.group(1);
			String rawAltimeter = matcher.group(2);

			StringBuffer buffer = new StringBuffer();

			if (rawAltimeterUnit.equals("Q"))
			{
				seaLevelPressureHpa = Integer.parseInt(rawAltimeter);
				buffer.append("Sea level pressure=" + seaLevelPressureHpa + " hPa");
			}
			else // A
			{
				altimeterInHg = Integer.parseInt(rawAltimeter) / 100.0;
				buffer.append("Altimeter=" + altimeterInHg + " inHg");
			}

			items.add(new MItem(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_COVERS = Pattern
			.compile("\\b(CAVOK|CLR|SKC|NSC|NSW|NCD|FEW|SCT|BKN|OVC|VV)(\\d{2,3})?(CB|SC|TCU)?\\b");

	private void decodeCovers(String _pattern)
	{
		String rawMatch = _pattern;
		int begin = rawTextBeforeRMK.indexOf(rawMatch);
		if (begin >= 0)
		{
			rawMatch = rawMatch.trim();
			begin++;
			int end = begin + rawMatch.length();
			String type = MMetarDefinitions.instance.cloudRemarks.get(rawMatch.substring(3));
			items.add(new MItem(rawMatch, type, begin, end));
		}
	}

	private void decodeCovers()
	{
		decodeCovers(" ///TCU ");
		decodeCovers(" ///CB ");

		Matcher matcher = PATTERN_COVERS.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawCoverType = matcher.group(1);
			String rawAltitude = matcher.group(2);
			String rawType = matcher.group(3);

			if (end < rawTextBeforeRMK.length() && rawTextBeforeRMK.substring(end, end + 3).equals("///"))
			{
				end += 3;
				int pos = end;
				while (end < rawTextBeforeRMK.length() && rawTextBeforeRMK.charAt(end) != ' ')
					end++;
				rawMatch = rawTextBeforeRMK.substring(begin, end);
				rawType = rawTextBeforeRMK.substring(pos, end);
				if (rawType.isEmpty())
					rawType = null;
			}

			MCover cover = new MCover();
			cover.type = MMetarDefinitions.instance.weathers.get(rawCoverType);
			if (rawType != null)
			{
				String type = MMetarDefinitions.instance.cloudRemarks.get(rawType);
				cover.type += " " + type;
			}
			cover.baseFeet = rawAltitude == null ? MMetar.INTEGER_NO_VALUE : Integer.parseInt(rawAltitude) * 100;

			StringBuffer buffer = new StringBuffer();
			if (isTempoBeforeRMK(begin))
			{
				temporary += cover.toString() + ", ";
				buffer.append("Temporary " + cover.toString());
			}
			else if (isBecomingBeforeRMK(begin))
			{
				becoming += cover.toString() + ", ";
				buffer.append("Becoming " + cover.toString());
			}
			else
			{
				covers.add(cover);
				buffer.append(cover.toString());
			}

			items.add(new MItem(rawMatch, buffer.toString(), begin, end));
		}

		Collections.sort(covers, new Comparator<MCover>()
		{
			@Override
			public int compare(MCover o1, MCover o2)
			{
				return Integer.compare(o1.baseFeet, o2.baseFeet);
			}
		});
	}

	private static final Pattern PATTERN_WEATHER = Pattern.compile(
			"(?<=\\s)(-|\\+|RE|VC)?(BC|BL|DR|FZ|MI|SH|TS)?(SN|VCSH|RA|RADZ|RASN|DZ|SG|IC|PL|GR|GS|FG|BR|HZ|FU|VA|DU|SA|SQ|FC|SS|DS|TS)\\b");

	private void decodeWeather()
	{
		weather = "";

		Matcher matcher = PATTERN_WEATHER.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			String value = intensity + descriptor + phenomenon;
			if (isTempoBeforeRMK(begin))
				temporary += value + ", ";
			else
				weather += value + ", ";

			items.add(new MItem(rawMatch, value, begin, end));
		}

		if (!weather.isEmpty())
			weather = weather.substring(0, weather.length() - 2);
	}

	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE = Pattern
			.compile("\\b(R(\\d{2}[LCR]?)/([PM])?(\\d{4})(V([PM])?(\\d{4}))?(FT)?/?([UND])?)\\b");
	private static final Pattern PATTERN_RUNWAY_VISUAL_RANGE_MISSING = Pattern.compile("\\bR(\\d{2}[LCR]?)//{2,}\\b");

	private void decodeRunwayVisualRange()
	{
		Matcher matcher = PATTERN_RUNWAY_VISUAL_RANGE.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			items.add(new MItem(rawMatch, runwayVisualRange.toString(), begin, end));
		}

		matcher = PATTERN_RUNWAY_VISUAL_RANGE_MISSING.matcher(rawTextBeforeRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawRunway = matcher.group(1);

			MRunwayVisualRange runwayVisualRange = new MRunwayVisualRange(rawRunway, null, -1, null, -1, null);
			runwayVisualRanges.add(runwayVisualRange);

			items.add(new MItem(rawMatch, runwayVisualRange.toString(), begin, end));
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			items.add(new MItem(rawMatch, runwayCondition.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_AIRFIELD_ELEVATION = Pattern.compile("\\bQFE\\s(\\d+\\.\\d)");

	private void decodeAirfieldElevation()
	{
		Matcher matcher = PATTERN_AIRFIELD_ELEVATION.matcher(rawTextBeforeRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawElevation = matcher.group(1);

			double elevation = Double.parseDouble(rawElevation);
			items.add(new MItem(rawMatch,
					"Airfield elevation=" + MFormat.instance.numberFormatDecimal1.format(elevation) + " hPa", begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_AUTOMATED_STATION_TYPES = Pattern.compile("\\bA[O0][12]A?");

	private void decodeRemarksAutomatedStationTypes()
	{
		Matcher matcher = PATTERN_REMARK_AUTOMATED_STATION_TYPES.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String stationType = MMetarDefinitions.instance.automatedStationTypeRemarks.get(rawMatch);
			if (stationType != null)
			{
				remarks.add(new MRemark(rawMatch, stationType, begin, end));
			}
		}
	}

	private static final Pattern PATTERN_REMARK_SEA_LEVEL_PRESSURE = Pattern.compile("\\bSLP(\\d{3})");

	private void decodeRemarksSeaLevelPressure()
	{
		Matcher matcher = PATTERN_REMARK_SEA_LEVEL_PRESSURE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawSeaLevelPressure = matcher.group(1);
			double pressure = Double.parseDouble(rawSeaLevelPressure);
			if (pressure < 200.0)
				pressure = pressure / 10.0 + 1000.0;
			else
				pressure = pressure / 10.0 + 900.0;

			remarks.add(new MRemark(rawMatch,
					"Sea level pressure=" + MFormat.instance.numberFormatDecimal1.format(pressure) + " hPa", begin, end));
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_PRESSURE_TENDENCY = Pattern.compile("\\b(PRESFR|PRESRR|5\\d{4})");

	private void decodeRemarksPressureTendency()
	{
		Matcher matcher = PATTERN_REMARK_PRESSURE_TENDENCY.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawPressureTendency = matcher.group(1);

			if (rawPressureTendency.equals("PRESFR"))
				remarks.add(new MRemark(rawMatch, "Pressure falling rapidly", begin, end));
			else if (rawPressureTendency.equals("PRESRR"))
				remarks.add(new MRemark(rawMatch, "Pressure rising rapidly", begin, end));
			else
			{
				String trend = rawPressureTendency.substring(1, 2);
				String pressureSign = rawPressureTendency.substring(2, 3);
				double pressure = Double.parseDouble(rawPressureTendency.substring(3)) / 10.0;
				if (pressureSign.equals("1"))
					pressure = -pressure;
				remarks.add(
						new MRemark(rawMatch, "Pressure tendency=" + MMetarDefinitions.instance.pressureTendencyRemarks.get(trend)
								+ ", " + pressure + " hPa change", begin, end));
			}
		}
	}

	private static final Pattern PATTERN_REMARK_SENSOR = Pattern
			.compile("\\b(CHINO|PNO|PWINO|RVRNO|SLPNO|VISNO|TSNO|FZRANO|FROIN|WIND\\sSENSOR\\sOFFLINE|RTS)");

	private void decodeRemarksSensors()
	{
		Matcher matcher = PATTERN_REMARK_SENSOR.matcher(rawTextAfterRMK);

		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String sensor = MMetarDefinitions.instance.sensorRemarks.get(rawMatch);
			if (sensor != null)
				remarks.add(new MRemark(rawMatch, sensor, begin, end));
		}
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

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
			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}

		matcher = PATTERN_REMARK_SKY_COVERATE_ALTITUDE.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_WEATHER = Pattern.compile(
			"\\b(CIG|CLD(\\sEMBD)?|CVCTV|DP|(SMOKE\\s)?FU\\s(ALQDS|ALL\\sQUADS)|HALO|ICE|LGT\\sICG|PCPN|RAG|VIS|WX)(\\d{3}|\\sMISG)?\\b");
	private static final Pattern PATTERN_REMARK_WEATHER_2 = Pattern.compile(
			"\\b(AFT\\s(\\d{2})UTC\\s)?(SNW\\sCVR/TRACE\\sLOOSE|SNOW\\sCOVER\\sHARD\\sPACK|SNW\\sCVR/MUCH\\sLOOSE|SNW\\sCOV/MUCH\\sLOOSE|SNW\\sCVR/MEDIUM\\sPACK)");

	private void decodeRemarksWeather()
	{
		Matcher matcher = PATTERN_REMARK_WEATHER_2.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawAfterHour = matcher.group(2);
			String rawWeather = matcher.group(3);

			String weather = MMetarDefinitions.instance.weathers.get(rawWeather);

			StringBuffer buffer = new StringBuffer(weather);
			if (rawAfterHour != null)
			{
				int afterHour = Integer.parseInt(rawAfterHour);
				buffer.append(" after " + afterHour + ":00Z");
			}

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}

		matcher = PATTERN_REMARK_WEATHER.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawWeather = matcher.group(1);
			String rawAltitudeMissing = matcher.group(5);

			String weather = MMetarDefinitions.instance.cloudRemarks.get(rawWeather);
			if (rawAltitudeMissing == null)
				remarks.add(new MRemark(rawMatch, weather, begin, end));
			else if (rawAltitudeMissing.equals(" MISG"))
				remarks.add(new MRemark(rawMatch, weather + " missing", begin, end));
			else
			{
				int altimeter = Integer.parseInt(rawAltitudeMissing) * 100;
				remarks.add(new MRemark(rawMatch, weather + " at " + altimeter + " ft", begin, end));
			}
		}
	}

	private static final Pattern PATTERN_REMARK_WEATHER_AMOUNT = Pattern.compile("\\b(I|P)(\\d{4})");

	private void decodeRemarksWeatherAmount()
	{
		Matcher matcher = PATTERN_REMARK_WEATHER_AMOUNT.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawType = matcher.group(1);
			String rawAmount = matcher.group(2);

			String type = "";
			if (rawType.equals("I"))
				type = "Ice";
			else if (rawType.equals("P"))
				type = "Precipitation";
			double amount = Double.parseDouble(rawAmount) / 100.0;

			remarks.add(new MRemark(rawMatch, type + "=" + amount + " inches in the past hour", begin, end));
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String cloud = MMetarDefinitions.instance.cloudRemarks.get(rawMatch);

			remarks.add(new MRemark(rawMatch, cloud, begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_ALTIMETER = Pattern.compile("A(\\d{4})");

	private void decodeRemarksAltimeter()
	{
		Matcher matcher = PATTERN_REMARK_ALTIMETER.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawAltimeter = matcher.group(1);
			double altimeter = Integer.parseInt(rawAltimeter) / 100.0;

			remarks.add(new MRemark(rawMatch, "Altimeter=" + altimeter + " inHg", begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_LAST_STATIONARY_FLIGHT_DIRECTION = Pattern
			.compile("\\b(LST|LAST|LAAST)\\s?(STAFFED|STFD|STGFD)?");

	private void decodeRemarksLastStationaryFlightDirection()
	{
		Matcher matcher = PATTERN_REMARK_LAST_STATIONARY_FLIGHT_DIRECTION.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			remarks.add(new MRemark(rawMatch, "Last stationary flight direction", begin, end));
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			remarks
					.add(new MRemark(rawMatch, "Next observation at " + day + "th " + rawHour + ":" + minute + "Z", begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_DENSITY_ALTITUDE = Pattern.compile("\\bDENSITY\\sALT\\s(-?\\d+)FT");

	private void decodeRemarksDensityAltitude()
	{
		Matcher matcher = PATTERN_REMARK_DENSITY_ALTITUDE.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawAltitude = matcher.group(1);
			int altitude = Integer.parseInt(rawAltitude);

			remarks.add(new MRemark(rawMatch, "Density altitude=" + altitude + " ft", begin, end));
		}
	}

	private void decodeRemarksColor()
	{
		Matcher matcher = PATTERN_COLOR.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String color = MMetarDefinitions.instance.colorRemarks.get(rawMatch);
			if (color != null)
				remarks.add(new MRemark(rawMatch, color, begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_AIRFIELD_ELEVATION = Pattern.compile("\\bQFE(\\d+)(/(\\d+))?");

	private void decodeRemarksAirfieldElevation()
	{
		Matcher matcher = PATTERN_REMARK_AIRFIELD_ELEVATION.matcher(rawTextAfterRMK);
		if (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawFirstElevation = matcher.group(1);

			int firstElevation = Integer.parseInt(rawFirstElevation);
			remarks
					.add(new MRemark(rawMatch,
							"Airfield elevation=" + firstElevation + " mmHg + ("
									+ MFormat.instance.numberFormatDecimal0.format(MUnit.mmHgToHPa(firstElevation)) + " hPa)",
							begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_WIND = Pattern
			.compile("\\bWIND\\s(?<alt>\\d+)FT\\s(?<dir>\\d{3})(?<speed>\\d{2})(G(?<gust>\\d{2}))?KT");
	private static final Pattern PATTERN_REMARK_WIND_ESTIMATED = Pattern.compile("\\b(WIND EST|WND DATA ESTMD)");
	private static final Pattern PATTERN_REMARK_PEAK_WIND = Pattern
			.compile("\\bPK\\sWND\\s(?<dir>\\d{3})(?<speed>\\d{2})/(?<hour>\\d{2})(?<minute>\\d{2})");

	private void decodeRemarksWind()
	{
		Matcher matcher = PATTERN_REMARK_WIND_ESTIMATED.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			remarks.add(new MRemark(rawMatch, "Wind speed estimated", begin, end));
		}

		matcher = PATTERN_REMARK_WIND.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}

		matcher = PATTERN_REMARK_PEAK_WIND.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

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

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}
	}

	private static final Pattern PATTERN_REMARK_SNOW_ACCUMULATION = Pattern.compile("/S(\\d{1,2})/");

	private void decodeRemarksSnowAccumulation()
	{
		Matcher matcher = PATTERN_REMARK_SNOW_ACCUMULATION.matcher(rawTextAfterRMK);
		while (matcher.find())
		{
			String rawMatch = matcher.group(0);
			int begin = matcher.start(0);
			int end = matcher.end(0);

			String rawAccumulation = matcher.group(1);

			int accumulation = Integer.parseInt(rawAccumulation);
			remarks.add(new MRemark(rawMatch, "Possibly snow accumulation of " + accumulation + " cm per hour", begin, end));
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
			int begin = matcher.start(0);
			int end = matcher.end(0);

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
					int beginTime = Integer.parseInt(rawBegin);

					buffer.append(precipitationType + " began " + beginTime + " min ");

					if (rawSecond != null && rawSecond.startsWith("E"))
					{
						String rawEnd = rawSecond.substring(1, 3);
						int endTime = Integer.parseInt(rawEnd);

						buffer.append("and ended " + endTime + " min, ");
					}
				}
				else // E
				{
					String rawEnd = rawFirst.substring(1, 3);
					int endTime = Integer.parseInt(rawEnd);

					buffer.append(precipitationType + " ended " + endTime + " min, ");
				}
			}

			if (buffer.toString().endsWith(", "))
				buffer.delete(buffer.length() - 2, buffer.length());

			remarks.add(new MRemark(rawMatch, buffer.toString(), begin, end));
		}
	}

	private void decodeRemarksOther()
	{
		// Maintenance
		String rawMatch = "$";
		int begin = rawTextAfterRMK.indexOf(rawMatch);
		if (begin >= 0 && begin == rawTextAfterRMK.length() - 1)
			remarks.add(new MRemark(rawMatch, "Maintenance needed at the station", begin, begin + rawMatch.length()));

		// First observation
		rawMatch = "FIRST";
		begin = rawTextAfterRMK.indexOf(rawMatch);
		if (begin >= 0)
			remarks.add(new MRemark(rawMatch, "First observation of the day", begin, begin + rawMatch.length()));
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
			decodeRemarksOther();
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

	private boolean isTempoBeforeRMK(int _begin)
	{
		return posTempoBeforeRMK >= 0 && _begin > posTempoBeforeRMK;
	}

	private boolean isBecomingBeforeRMK(int _begin)
	{
		return posBecomingBeforeRMK >= 0 && _begin > posBecomingBeforeRMK;
	}

	public String debug()
	{
		StringBuffer buffer = new StringBuffer("metar:" + rawText);
		buffer.append("\n");
		buffer.append("metar highlight:" + rawTextHighlight);
		buffer.append("\n");
		buffer.append("decoded=" + !notDecoded + "\n");
		buffer.append("ITEMS\n");
		for (MItem item : items)
			buffer.append(item + "\n");
		buffer.append("REMARKS\n");
		for (MRemark remark : remarks)
			buffer.append(remark + "\n");

		return buffer.toString();
	}

	public static void main(String[] args)
	{
		MMetar metar = new MMetar((LocalDateTime) null, "KCCA 281255Z AUTO 00000KT 10SM CLR M01/M01 A3008 RMK AO2");
		metar.decode();
		System.out.println(metar.debug());
	}
}
