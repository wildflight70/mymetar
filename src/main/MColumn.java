package main;

import java.util.Comparator;

import data.MAirport;
import main.MModel.MColumnValue;

public class MColumn
{
	public String name;
	public boolean extra;
	public int alignment; // SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.CENTER;
	public Comparator<MAirport> comparator;
	public MColumnValue value;

	public MColumn(String _name, boolean _extra, int _alignment, Comparator<MAirport> _comparator, MColumnValue _value)
	{
		name = _name;
		extra = _extra;
		alignment = _alignment;
		comparator = _comparator;
		value = _value;
	}
}