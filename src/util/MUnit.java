package util;

public class MUnit
{
	public static int mpsToKnots(int _mps)
	{
		return (int) Math.round(_mps * 1.94384);
	}

	public static double metersToSM(double _meters)
	{
		return _meters * 0.000621371;
	}

	public static double metersToFeet(double _meters)
	{
		return _meters * 3.28084;
	}

	public static double hPaToInHg(double _hPa)
	{
		return Math.round(100.0 * _hPa / 33.864) / 100.0;
	}

	public static double inHgToHPa(double _inHg)
	{
		return Math.round(_inHg * 33.864);
	}
}
