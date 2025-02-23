package data;

public class MCountry
{
	public String code;
	public String name;

	public MCountry(String _code, String _name)
	{
		code = _code;
		name = _name;
	}

	@Override
	public String toString()
	{
		return code.isEmpty() ? "" : (name + " (" + code + ")");
	}
}
