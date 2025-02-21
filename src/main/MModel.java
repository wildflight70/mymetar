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

import data.MAirport;
import data.MLoadNOAAAPI;
import data.MLoadNOAAFTP;
import data.MLoadXPlane;
import data.MMetar;
import data.MOurAirports;
import data.MXPlane;

@SuppressWarnings("serial")
public class MModel extends AbstractTableModel
{
	public ArrayList<MAirport> airports;

	private interface VLColumnValue
	{
		public Object get(MAirport _airport);
	}

	public static class VLColumn
	{
		public String name;
		public boolean extra;
		public int alignment; // SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.CENTER;
		public Comparator<MAirport> comparator;
		public VLColumnValue value;

		public VLColumn(String _name, boolean _extra, int _alignment, Comparator<MAirport> _comparator,
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

		columns.put(col++, new VLColumn("Observation time (Z)", false, SwingConstants.LEFT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = o1.metar.observationTime.compareTo(o2.metar.observationTime);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.observationTime;
			}
		}));

		columns.put(col++, new VLColumn("Station id", false, SwingConstants.LEFT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = o1.stationId.compareTo(o2.stationId);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.stationId;
			}
		}));

		columns.put(col++, new VLColumn("Elevation (ft)", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Integer.compare(o1.elevationFt, o2.elevationFt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.elevationFt != Integer.MIN_VALUE ? _airport.elevationFt : null;
			}
		}));

		columns.put(col++, new VLColumn("Latitude", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.latitude, o2.latitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return Double.isNaN(_airport.latitude) ? null : _airport.latitude;
			}
		}));

		columns.put(col++, new VLColumn("Longitude", true, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.longitude, o2.longitude);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return Double.isNaN(_airport.longitude) ? null : _airport.longitude;
			}
		}));

		columns.put(col++, new VLColumn("X Plane", true, SwingConstants.CENTER, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Boolean.compare(o1.xPlane, o2.xPlane);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.xPlane;
			}
		}));

		columns.put(col++, new VLColumn("Flight category", true, SwingConstants.CENTER, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = o1.metar.extraFlightCategory.compareTo(o2.metar.extraFlightCategory);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.extraFlightCategory;
			}
		}));

		columns.put(col++, new VLColumn("Altimeter (inHg/hPa)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.metar.altimeterHpa, o2.metar.altimeterHpa);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
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

		columns.put(col++, new VLColumn("Temperature (°C)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Integer.compare(o1.metar.temperatureC, o2.metar.temperatureC);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.temperatureC;
			}
		}));

		columns.put(col++, new VLColumn("DewPoint (°C)", false, SwingConstants.RIGHT, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.dewPointC == Integer.MIN_VALUE) ? null
						: _airport.metar.dewPointC;
			}
		}));

		columns.put(col++, new VLColumn("Visibility (SM)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Double.compare(o1.metar.visibilitySM, o2.metar.visibilitySM);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.visibilitySM < 0) ? null : _airport.metar.visibilitySM;
			}
		}));

		columns.put(col++, new VLColumn("Wind direction (°)", false, SwingConstants.RIGHT, null, new VLColumnValue()
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

		columns.put(col++, new VLColumn("Wind speed (kt)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Integer.compare(o1.metar.windSpeedKt, o2.metar.windSpeedKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.windSpeedKt;
			}
		}));

		columns.put(col++, new VLColumn("Wind gust (kt)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
		{
			@Override
			public int compare(MAirport o1, MAirport o2)
			{
				int c = Integer.compare(o1.metar.windGustKt, o2.metar.windGustKt);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return (_airport.metar == null || _airport.metar.windGustKt <= 0) ? null : _airport.metar.windGustKt;
			}
		}));

		columns.put(col++, new VLColumn("Wind variable (°)", false, SwingConstants.RIGHT, null, new VLColumnValue()
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

		columns.put(col++, new VLColumn("Clouds (ft)", false, SwingConstants.LEFT, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.cloudToString();
			}
		}));

		columns.put(col++, new VLColumn("Weather", false, SwingConstants.LEFT, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.weather;
			}
		}));

		columns.put(col++, new VLColumn("Auto", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.auto ? null : true;
			}
		}));

		columns.put(col++, new VLColumn("No signal", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.noSignal ? null : true;
			}
		}));

		columns.put(col++, new VLColumn("Correction", false, SwingConstants.CENTER, null, new VLColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.correction ? null : true;
			}
		}));

		columns.put(col++, new VLColumn("Raw", false, SwingConstants.LEFT, null, new VLColumnValue()
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
		int i = airports.size() - 1;
		while (i >= 0 && !airports.get(i).stationId.equals(_stationId))
			i--;
		return i >= 0 ? airports.get(i) : null;
	}

	public void load()
	{
		Logger.debug("load begin");

		MLoadXPlane loadXPlane = new MLoadXPlane();
		HashMap<String, MXPlane> map = loadXPlane.load();

		MLoadNOAAAPI loadAPI = new MLoadNOAAAPI();
		HashMap<String, MMetar> apiMetars = loadAPI.load();

		airports = new MOurAirports().loadAirports();

		String fileName = MLoadNOAAFTP.LOCAL_DIR + MLoadNOAAFTP.METARS_FILE;
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

						MMetar metar = new MMetar(observationTime, items[1]);
						metar.decode();

						MAirport airport = get(metar.stationId);
						if (airport != null)
						{
							airport.metar = metar;
							airport.xPlane = map.containsKey(metar.stationId);
						}

						MMetar apiMetar = apiMetars.get(metar.stationId);
						if (apiMetar != null)
							metar.extraFlightCategory = apiMetar.extraFlightCategory;
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

		Collections.sort(airports, columns.get(sortedColumn).comparator);
	}
}
