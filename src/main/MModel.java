package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.tinylog.Logger;

import data.MLoadFTP;
import data.MLoadXPlane;
import data.MXPlane;

@SuppressWarnings("serial")
public class MModel extends AbstractTableModel
{
	public ArrayList<MMetarEx> metars;

	private interface VLColumnValue
	{
		public Object get(MMetarEx _metar);
	}

	public static class VLColumn
	{
		public String name;
		public boolean extra;
		public int alignment; // SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.CENTER;
		public Comparator<MMetarEx> comparator;
		public VLColumnValue value;

		public VLColumn(String _name, boolean _extra, int _alignment, Comparator<MMetarEx> _comparator,
				VLColumnValue _value)
		{
			name = _name;
			extra = _extra;
			alignment = _alignment;
			comparator = _comparator;
			value = _value;
		}
	}

	public HashMap<Integer, VLColumn> columns;

	public int sortedColumn;
	public boolean sortedAsc;

	private DecimalFormat numberFormatDecimal0;
	private DecimalFormat numberFormatDecimal2;

	public MModel()
	{
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(' ');

		numberFormatDecimal0 = new DecimalFormat("###,##0", otherSymbols);
		numberFormatDecimal2 = new DecimalFormat("###,##0.00", otherSymbols);

		initColumns();
		load();
	}

	private void initColumns()
	{
		columns = new HashMap<Integer, VLColumn>();

		int col = 0;

		columns.put(col++, new VLColumn("Observation time (Z)", false, SwingConstants.LEFT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = o1.observationTime.compareTo(o2.observationTime);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.observationTime;
			}
		}));

		columns.put(col++, new VLColumn("Station id", false, SwingConstants.LEFT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = o1.stationId.compareTo(o2.stationId);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.stationId;
			}
		}));

		columns.put(col++, new VLColumn("Elevation (ft)", true, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Integer.compare(o1.extraElevationFt, o2.extraElevationFt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.extraElevationFt != Integer.MIN_VALUE ? _metar.extraElevationFt : null;
			}
		}));

		columns.put(col++, new VLColumn("Latitude", true, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Double.compare(o1.extraLatitude, o2.extraLatitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return Double.isNaN(_metar.extraLatitude) ? null : _metar.extraLatitude;
			}
		}));

		columns.put(col++, new VLColumn("Longitude", true, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Double.compare(o1.extraLongitude, o2.extraLongitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return Double.isNaN(_metar.extraLongitude) ? null : _metar.extraLongitude;
			}
		}));

		columns.put(col++, new VLColumn("X Plane", true, SwingConstants.CENTER, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Boolean.compare(o1.xPlane != null, o2.xPlane != null);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.xPlane == null ? null : true;
			}
		}));

		columns.put(col++, new VLColumn("Flight category", true, SwingConstants.CENTER, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = o1.extraFlightCategory.compareTo(o2.extraFlightCategory);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.extraFlightCategory;
			}
		}));

		columns.put(col++, new VLColumn("Altimeter (inHg/hPa)", false, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Double.compare(o1.altimeterHpa, o2.altimeterHpa);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				if (_metar.altimeterInHg == 0)
					return null;
				else
					return numberFormatDecimal2.format(_metar.altimeterInHg) + " / "
							+ numberFormatDecimal0.format(_metar.altimeterHpa);
			}
		}));

		columns.put(col++, new VLColumn("Temperature (°C)", false, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Integer.compare(o1.temperatureC, o2.temperatureC);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.temperatureC;
			}
		}));

		columns.put(col++, new VLColumn("DewPoint (°C)", false, SwingConstants.RIGHT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.dewPointC == Integer.MIN_VALUE ? null : _metar.dewPointC;
			}
		}));

		columns.put(col++, new VLColumn("Visibility (SM)", false, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Double.compare(o1.visibilitySM, o2.visibilitySM);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.visibilitySM >= 0 ? _metar.visibilitySM : null;
			}
		}));

		columns.put(col++, new VLColumn("Wind direction (°)", false, SwingConstants.RIGHT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.windDirectionDegree < 0 ? "variable" : _metar.windDirectionDegree;
			}
		}));

		columns.put(col++, new VLColumn("Wind speed (kt)", false, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Integer.compare(o1.windSpeedKt, o2.windSpeedKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.windSpeedKt;
			}
		}));

		columns.put(col++, new VLColumn("Wind gust (kt)", false, SwingConstants.RIGHT, new Comparator<MMetarEx>()
		{
			@Override
			public int compare(MMetarEx o1, MMetarEx o2)
			{
				int c = Integer.compare(o1.windGustKt, o2.windGustKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.windGustKt > 0 ? _metar.windGustKt : null;
			}
		}));

		columns.put(col++, new VLColumn("Wind variable (°)", false, SwingConstants.RIGHT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.windVariable ? (_metar.windFromDegree + " to " + _metar.windToDegree) : null;
			}
		}));

		columns.put(col++, new VLColumn("Clouds (ft)", false, SwingConstants.LEFT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.cloudToString();
			}
		}));

		columns.put(col++, new VLColumn("Weather", false, SwingConstants.LEFT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.weather;
			}
		}));

		columns.put(col++, new VLColumn("Auto", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.auto ? true : null;
			}
		}));

		columns.put(col++, new VLColumn("No signal", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.noSignal ? true : null;
			}
		}));

		columns.put(col++, new VLColumn("Correction", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.correction ? true : null;
			}
		}));

		columns.put(col++, new VLColumn("Raw", false, SwingConstants.LEFT, null, new VLColumnValue()
		{
			@Override
			public Object get(MMetarEx _metar)
			{
				return _metar.rawTextHighlight;
			}
		}));
	}

	@Override
	public int getRowCount()
	{
		return metars.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.size();
	}

	@Override
	public String getColumnName(int column)
	{
		return columns.get(column).name;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		MMetarEx metar = metars.get(rowIndex);
		return columns.get(columnIndex).value.get(metar);
	}

	public void load()
	{
		Logger.debug("load begin");

		MLoadXPlane loadXPlane = new MLoadXPlane();
		HashMap<String, MXPlane> map = loadXPlane.load();

		metars = new ArrayList<MMetarEx>();

		String fileName = MLoadFTP.LOCAL_DIR + MLoadFTP.METARS_FILE;
		if (new File(fileName).exists())
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
			{
				String line;
				while ((line = reader.readLine()) != null)
					if (!line.isEmpty())
					{
						String[] items = line.split(",");

						LocalDateTime observationTime = LocalDateTime.parse(items[0]);

						MMetarEx metar = new MMetarEx(observationTime, items[1]);
						metar.xPlane = map.get(metar.stationId);
						if (metar.xPlane != null)
						{
							metar.extraElevationFt = metar.xPlane.elevationFeet;
							metar.extraLatitude = metar.xPlane.latitude;
							metar.extraLongitude = metar.xPlane.longitude;
						}
						metars.add(metar);
					}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		}
		else
			Logger.error(fileName + " does not exist");

		sortedColumn = 1;
		sortedAsc = true;
		sort();

		Logger.debug("load end");
	}

	public void resetColumn()
	{
		if (columns.get(sortedColumn).name.endsWith("+") || columns.get(sortedColumn).name.endsWith("-"))
			columns.get(sortedColumn).name = columns.get(sortedColumn).name.substring(0,
					columns.get(sortedColumn).name.length() - 2);
	}

	public boolean canSort(int _col)
	{
		return columns.get(_col).comparator != null;
	}

	public void sort()
	{
		columns.get(sortedColumn).name += sortedAsc ? " +" : " -";

		Collections.sort(metars, columns.get(sortedColumn).comparator);
	}
}
