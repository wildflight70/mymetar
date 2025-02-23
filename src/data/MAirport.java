package data;

public class MAirport
{
	public String stationId;
	public String type;
	public String name;
	public String city;
	public String country;
	public int elevationFt = Integer.MIN_VALUE;
	public double latitude = Double.NaN;
	public double longitude = Double.NaN;

	public boolean xPlane;

	public MMetar metar;

	public boolean found;

	public MAirport(String _stationId)
	{
		stationId = _stationId;
	}
}
