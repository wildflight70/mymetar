package metar;

import util.MFormat;

public class MRunwayVisualRange
{
	public String runway;
	public String minMoreLess; // null, <, >
	public int minVisibility = Integer.MIN_VALUE;
	public String maxMoreLess; // null, <, >
	public int maxVisibility = Integer.MIN_VALUE;
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

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(runway);
		buffer.append("=");

		if (minVisibility == Integer.MIN_VALUE && maxVisibility == Integer.MIN_VALUE)
			buffer.append("missing");
		else
		{
			if (minMoreLess != null)
				buffer.append(minMoreLess);

			buffer.append(MFormat.instance.numberFormatDecimal0.format(minVisibility) + "m");

			if (maxVisibility != Integer.MIN_VALUE)
			{
				buffer.append(" to ");
				if (maxMoreLess != null)
					buffer.append(maxMoreLess);
				buffer.append(MFormat.instance.numberFormatDecimal0.format(maxVisibility) + "m");
			}

			if (trend != null)
				buffer.append(" " + trend);
		}
		return buffer.toString();
	}
}