package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MFormat
{
	public static MFormat instance = new MFormat();

	public DecimalFormat numberFormatDecimal0;
	public DecimalFormat numberFormatDecimal2;
	public DecimalFormat numberFormatDecimal5;

	private MFormat()
	{
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(' ');

		numberFormatDecimal0 = new DecimalFormat("###,##0", otherSymbols);
		numberFormatDecimal2 = new DecimalFormat("###,##0.00", otherSymbols);
		numberFormatDecimal5 = new DecimalFormat("###,##0.00000", otherSymbols);
	}
}
