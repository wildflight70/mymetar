package metar;

public class MRunwayCondition
{
	public String runway;
	public String contaminationType;
	public String coverage;
	public int depth;
	public String brakinkAction;

	public MRunwayCondition(String _runway, String _contaminationType, String _coverage, int _depth,
			String _brakingAction)
	{
		runway = _runway;
		contaminationType = _contaminationType;
		coverage = _coverage;
		depth = _depth;
		brakinkAction = _brakingAction;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(runway);
		buffer.append("=");
		buffer.append(contaminationType);
		buffer.append(";");
		buffer.append(coverage);
		buffer.append(";");
		buffer.append(depth + "mm");
		buffer.append(";");
		buffer.append(brakinkAction);
		return buffer.toString();
	}
}