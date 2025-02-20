package main;

import java.time.LocalDateTime;

import data.MMetar;
import data.MXPlane;

public class MMetarEx extends MMetar
{
	public MXPlane xPlane;

	public MMetarEx(LocalDateTime _observationTime, String _raw)
	{
		super(_observationTime, _raw);
	}
}
