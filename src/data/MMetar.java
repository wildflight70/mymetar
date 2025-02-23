package data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.MFormat;

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
	public int windSpeedKt;
	public int windGustKt;
	public boolean windVariable;
	public int windFromDegree;
	public int windToDegree;
	public String weather;
	public ArrayList<VLMetarCloud> clouds;

	public String extraFlightCategory = "";

	public String rawTextHighlight;
	public boolean notDecoded;

	public class VLMetarCloud
	{
		public String cover;
		public int baseFeet = -1;
	}

	private static final HashMap<String, String> COVERS = initCovers();
	private static final HashMap<String, String> WEATHERS = initWeathers();

	private static HashMap<String, String> initCovers()
	{
		HashMap<String, String> covers = new HashMap<String, String>();
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
		return covers;
	}

	private static HashMap<String, String> initWeathers()
	{
		HashMap<String, String> weathers = new HashMap<String, String>();
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

		return weathers;
	}

	public MMetar(LocalDateTime _observationTime, String _raw, String _stationId)
	{
		observationTime = _observationTime;
		rawText = _raw;
		rawTextHighlight = "<html>" + rawText + "</html>";
		stationId = _stationId;

		clouds = new ArrayList<MMetar.VLMetarCloud>();
	}

	public MMetar(LocalDateTime _observationTime, String _raw)
	{
		observationTime = _observationTime;
		rawText = _raw;
		rawTextHighlight = "<html>" + rawText + "</html>";

		clouds = new ArrayList<MMetar.VLMetarCloud>();
	}

	public MMetar(String[] _fields, String _values)
	{
		String[] values = _values.split(",");

		rawText = values[0];
		rawTextHighlight = "<html>" + rawText + "</html>";
		stationId = values[1];
		observationTime = LocalDateTime.parse(values[2].substring(0, values[2].length() - 1));
		extraFlightCategory = values[30];

		clouds = new ArrayList<MMetar.VLMetarCloud>();
	}

	public String cloudToString()
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < clouds.size(); i++)
		{
			VLMetarCloud cloud = clouds.get(i);
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
		return buffer.toString();
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
					windSpeedKt = mpsToKnots(windSpeedKt);

				String gust = matcher.group(3);
				if (gust != null)
				{
					windGustKt = Integer.parseInt(gust);
					if (speedUnit.equals("MPS"))
						windGustKt = mpsToKnots(windGustKt);
				}
				highLight(_items[i]);
				break;
			}
		}
	}

	public static double parseFractionalMiles(String _fraction)
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
					visibilitySM = Math.round(10.0 * metersToSM(visibilitySM)) / 10.0;
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
				visibilitySMExtra = Math.round(10.0 * metersToSM(visibilitySMExtra)) / 10.0;
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
					altimeterInHg = hPaToInHg(altimeterHpa);
				}
				else
				{
					altimeterInHg = Integer.parseInt(rawAltimeter) / 100.0;
					altimeterHpa = inHgToHPa(altimeterInHg);
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

				VLMetarCloud cloud = new VLMetarCloud();
				cloud.cover = COVERS.get(cloudType);
				if (type != null)
					cloud.cover += " " + type;
				cloud.baseFeet = altitude == null ? -1 : Integer.parseInt(altitude) * 100;
				clouds.add(cloud);
				highLight(_items[i]);
			}
		}

		Collections.sort(clouds, new Comparator<VLMetarCloud>()
		{
			@Override
			public int compare(VLMetarCloud o1, VLMetarCloud o2)
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
			Matcher matcher = PATTERN_WEATHER.matcher(_items[i]);
			if (matcher.find())
			{
				String rawIntensity = matcher.group(1);
				if (rawIntensity == null)
					rawIntensity = "";
				String intensity = WEATHERS.get(rawIntensity);
				if (intensity == null)
					intensity = "Moderate";
				if (!intensity.isEmpty())
					intensity += " ";

				String rawDescriptor = matcher.group(2);
				if (rawDescriptor == null)
					rawDescriptor = "";
				String descriptor = WEATHERS.get(rawDescriptor);
				if (descriptor == null)
					descriptor = "";
				if (!descriptor.isEmpty())
					descriptor += " ";

				String rawPhenomenon = matcher.group(3);
				if (rawPhenomenon == null)
					rawPhenomenon = "";
				String phenomenon = WEATHERS.get(rawPhenomenon);

				if (rawPhenomenon.equals("VCSH"))
					intensity = "";

				weather += intensity + descriptor + phenomenon + ", ";
				highLight(_items[i]);
			}
		}

		if (!weather.isEmpty())
			weather = weather.substring(0, weather.length() - 2);
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

	private int mpsToKnots(int _mps)
	{
		return (int) Math.round(_mps * 1.94384);
	}

	private double metersToSM(double _meters)
	{
		return _meters * 0.000621371;
	}

	static public double metersToFeet(double _meters)
	{
		return _meters * 3.28084;
	}

	private double hPaToInHg(double _hPa)
	{
		return Math.round(100.0 * _hPa / 33.864) / 100.0;
	}

	private double inHgToHPa(double _inHg)
	{
		return Math.round(_inHg * 33.864);
	}
}
