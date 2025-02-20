package util;

import java.awt.Color;

public class MColor
{
	public static Color blend(Color _color1, Color _color2)
	{
		double totalAlpha = _color1.getAlpha() + _color2.getAlpha();
		double weight0 = _color1.getAlpha() / totalAlpha;
		double weight1 = _color2.getAlpha() / totalAlpha;

		double r = weight0 * _color1.getRed() + weight1 * _color2.getRed();
		double g = weight0 * _color1.getGreen() + weight1 * _color2.getGreen();
		double b = weight0 * _color1.getBlue() + weight1 * _color2.getBlue();
		double a = Math.max(_color1.getAlpha(), _color2.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}
}
