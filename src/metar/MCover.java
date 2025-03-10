package metar;

import util.MFormat;

public class MCover
{
	public String type;
	public int baseFeet = Integer.MIN_VALUE;

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(type);
		if (baseFeet != Integer.MIN_VALUE)
		{
			buffer.append(" at ");
			buffer.append(MFormat.instance.numberFormatDecimal0.format(baseFeet));
			buffer.append(" ft");
		}
		return buffer.toString();
	}
}