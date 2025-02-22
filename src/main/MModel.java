package main;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.tinylog.Logger;

import data.MAirport;
import data.MMetar;
import data.MNOAAAPI;
import data.MNOAAFTP;
import data.MOurAirports;
import data.MXPlane;
import data.MXPlaneAirport;

@SuppressWarnings("serial")
public class MModel extends AbstractTableModel
{
	public ArrayList<MAirport> airports;

	private interface MColumnValue
	{
		public Object get(MAirport _airport);
	}

	public static class MColumn
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

	public HashMap<Integer, MColumn> columns;

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
		columns = new HashMap<Integer, MColumn>();

		int col = 0;

		columns.put(col++, new MColumn("Observation time (Z)", false, SwingConstants.LEFT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = o1.metar.observationTime.compareTo(o2.metar.observationTime);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.observationTime;
			}
		}));

		columns.put(col++, new MColumn("Station id", false, SwingConstants.LEFT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = o1.stationId.compareTo(o2.stationId);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.stationId;
			}
		}));

		columns.put(col++, new MColumn("Elevation (ft)", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Integer.compare(o1.elevationFt, o2.elevationFt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.elevationFt != Integer.MIN_VALUE ? _airport.elevationFt : null;
			}
		}));

		columns.put(col++, new MColumn("Latitude", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.latitude, o2.latitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return Double.isNaN(_airport.latitude) ? null : _airport.latitude;
			}
		}));

		columns.put(col++, new MColumn("Longitude", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.longitude, o2.longitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return Double.isNaN(_airport.longitude) ? null : _airport.longitude;
			}
		}));

		columns.put(col++, new MColumn("X Plane", true, SwingConstants.CENTER, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Boolean.compare(o1.xPlane, o2.xPlane);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.xPlane ? true : null;
			}
		}));

		columns.put(col++, new MColumn("Flight category", true, SwingConstants.CENTER, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = o1.metar.extraFlightCategory.compareTo(o2.metar.extraFlightCategory);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.extraFlightCategory;
			}
		}));

		columns.put(col++, new MColumn("Altimeter (inHg/hPa)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = Double.compare(o1.metar.altimeterHpa, o2.metar.altimeterHpa);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null || _airport.metar.altimeterInHg == 0)
					return null;
				else
					return numberFormatDecimal2.format(_airport.metar.altimeterInHg) + " / "
							+ numberFormatDecimal0.format(_airport.metar.altimeterHpa);
			}
		}));

		columns.put(col++, new MColumn("Temperature (°C)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = Integer.compare(o1.metar.temperatureC, o2.metar.temperatureC);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.temperatureC;
			}
		}));

		columns.put(col++, new MColumn("DewPoint (°C)", false, SwingConstants.RIGHT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.dewPointC == Integer.MIN_VALUE) ? null
						: _airport.metar.dewPointC;
			}
		}));

		columns.put(col++, new MColumn("Visibility (SM)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = Double.compare(o1.metar.visibilitySM, o2.metar.visibilitySM);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.visibilitySM < 0) ? null : _airport.metar.visibilitySM;
			}
		}));

		columns.put(col++, new MColumn("Wind direction (°)", false, SwingConstants.RIGHT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null)
					return null;
				else
					return _airport.metar.windDirectionDegree < 0 ? "variable" : _airport.metar.windDirectionDegree;
			}
		}));

		columns.put(col++, new MColumn("Wind speed (kt)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = Integer.compare(o1.metar.windSpeedKt, o2.metar.windSpeedKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.windSpeedKt;
			}
		}));

		columns.put(col++, new MColumn("Wind gust (kt)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c;
				if (o1.metar == null)
					c = -1;
				else if (o2.metar == null)
					c = 1;
				else
					c = Integer.compare(o1.metar.windGustKt, o2.metar.windGustKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.windGustKt <= 0) ? null : _airport.metar.windGustKt;
			}
		}));

		columns.put(col++, new MColumn("Wind variable (°)", false, SwingConstants.RIGHT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null)
					return null;
				else
					return _airport.metar.windVariable ? (_airport.metar.windFromDegree + " to " + _airport.metar.windToDegree)
							: null;
			}
		}));

		columns.put(col++, new MColumn("Clouds (ft)", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.cloudToString();
			}
		}));

		columns.put(col++, new MColumn("Weather", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.weather;
			}
		}));

		columns.put(col++, new MColumn("Auto", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.auto ? null : true;
			}
		}));

		columns.put(col++, new MColumn("No signal", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.noSignal ? null : true;
			}
		}));

		columns.put(col++, new MColumn("Correction", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.correction ? null : true;
			}
		}));

		columns.put(col++, new MColumn("Raw", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.rawTextHighlight;
			}
		}));
	}

	@Override
	public int getRowCount()
	{
		return airports.size();
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
		MAirport airport = airports.get(rowIndex);
		return columns.get(columnIndex).value.get(airport);
	}

	private MAirport get(String _stationId)
	{
		int i = Collections.binarySearch(airports, new MAirport(_stationId), new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				return o1.stationId.compareTo(o2.stationId);
			}
		});

		return i >= 0 ? airports.get(i) : null;
	}

	public void load()
	{
		Logger.debug("load begin");

		airports = new MOurAirports().loadAirports();
		HashMap<String, MXPlaneAirport> xPlaneMap = new MXPlane().load();
		HashMap<String, MMetar> noaaApiMetars = new MNOAAAPI().load();
		ArrayList<MMetar> noaaFtpMetars = new MNOAAFTP().read();
		
		for (MMetar metar : noaaFtpMetars)
		{
			MAirport airport = get(metar.stationId);
			if (airport != null)
			{
				airport.metar = metar;
				airport.xPlane = xPlaneMap.containsKey(metar.stationId);
			}

			MMetar apiMetar = noaaApiMetars.get(metar.stationId);
			if (apiMetar != null)
				metar.extraFlightCategory = apiMetar.extraFlightCategory;
		}

		sortedColumn = 1;
		sortedAsc = true;
		sort();

		Logger.debug("load end");
	}

	public void resetColumn()
	{
		MColumn column = columns.get(sortedColumn);
		if (column.name.endsWith("+") || column.name.endsWith("-"))
			column.name = column.name.substring(0, column.name.length() - 2);
	}

	public boolean canSort(int _col)
	{
		return columns.get(_col).comparator != null;
	}

	public void sort()
	{
		columns.get(sortedColumn).name += sortedAsc ? " +" : " -";

		Collections.sort(airports, columns.get(sortedColumn).comparator);
	}
}
