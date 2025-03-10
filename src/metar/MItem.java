package metar;

public class MItem
{
	public String field;
	public String value;

	public int begin;
	public int end;

	public MItem(String _field, String _value, int _begin, int _end)
	{
		field = _field;
		value = _value;
		begin = _begin;
		end = _end;
	}

	@Override
	public String toString()
	{
		return field + ":" + value + " [" + begin + "-" + end + "]";
	}

	@Override
	public boolean equals(Object _object)
	{
		MItem other = (MItem) _object;
		return begin == other.begin && end == other.end;
	}
}