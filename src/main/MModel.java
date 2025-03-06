package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.tinylog.Logger;

import data.MAirport;
import data.MCountry;
import data.MMetar;
import data.MNOAAAPI;
import data.MNOAAFTP;
import data.MOurAirports;
import data.MXPlane;
import data.MXPlaneAirport;
import main.MColumn.MColumnValue;
import util.MFormat;

@SuppressWarnings("serial")
public class MModel extends AbstractTableModel
{
	private ArrayList<MAirport> airports;
	public List<MAirport> visibleAirports;

	public HashMap<Integer, MColumn> columns;

	public int sortedColumn;
	public boolean sortedAsc;
	public boolean filterShowOnlyAirportsWithMetar;
	public boolean filterNotDecodedMetars;
	public MCountry filterCountry;
	public String filterMetar;

	private int totalMetars;
	private int totalMetarNotDecoded;

	public MModel()
	{
		visibleAirports = new ArrayList<MAirport>();

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
				if (o1.metar == null || o1.metar.observationTime == null)
					c = -1;
				else if (o2.metar == null || o2.metar.observationTime == null)
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
				return _airport.elevationFt == Integer.MIN_VALUE ? null
						: MFormat.instance.numberFormatDecimal0.format(_airport.elevationFt);
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
				return Double.isNaN(_airport.latitude) ? null : MFormat.instance.numberFormatDecimal5.format(_airport.latitude);
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
				return Double.isNaN(_airport.longitude) ? null
						: MFormat.instance.numberFormatDecimal5.format(_airport.longitude);
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

		columns.put(col++, new MColumn("Sea level pressure (hPa)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
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
					c = Double.compare(o1.metar.seaLevelPressureHpa, o2.metar.seaLevelPressureHpa);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null || _airport.metar.seaLevelPressureHpa < 0)
					return null;
				else
					return MFormat.instance.numberFormatDecimal0.format(_airport.metar.seaLevelPressureHpa);
			}
		}));

		columns.put(col++, new MColumn("Altimeter (inHg)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
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
					c = Double.compare(o1.metar.altimeterInHg, o2.metar.altimeterInHg);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null || _airport.metar.altimeterInHg < 0)
					return null;
				else
					return MFormat.instance.numberFormatDecimal2.format(_airport.metar.altimeterInHg);
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
				if (_airport.metar == null || _airport.metar.visibilitySM < 0)
					return null;
				else
					return _airport.metar.visibilityNonDirectionalVaration ? (_airport.metar.visibilitySM + " NDV")
							: _airport.metar.visibilitySM;
			}
		}));

		columns.put(col++, new MColumn("Visibility extra (SM)", false, SwingConstants.RIGHT, new Comparator<MAirport>()
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
					c = Double.compare(o1.metar.visibilitySMExtra, o2.metar.visibilitySMExtra);
				if (!sortedAsc)
					c = -c;
				return c;
			}
		}, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				if (_airport.metar == null || _airport.metar.visibilitySMExtra < 0)
					return null;
				else
					return _airport.metar.visibilitySMExtra + (" " + _airport.metar.visibilityDirectionExtra).trim();
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
				return (_airport.metar == null || _airport.metar.windSpeedKt < 0) ? null : _airport.metar.windSpeedKt;
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

		columns.put(col++, new MColumn("Cover (ft)", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.coversToString();
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

		columns.put(col++, new MColumn("Runway visual ranges", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.runwayVisualRangesToString();
			}
		}));

		columns.put(col++, new MColumn("Runway conditions", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.runwayConditionsToString();
			}
		}));

		columns.put(col++, new MColumn("Color", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.color;
			}
		}));

		columns.put(col++, new MColumn("Temporary", false, SwingConstants.LEFT, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null ? null : _airport.metar.temporary;
			}
		}));

		columns.put(col++, new MColumn("Auto", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.auto ? null : "o";
			}
		}));

		columns.put(col++, new MColumn("No signal", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.noSignal ? null : "o";
			}
		}));

		columns.put(col++, new MColumn("Correction", false, SwingConstants.CENTER, null, new MColumnValue()
		{
			@Override
			public Object get(MAirport _airport)
			{
				return _airport.metar == null || !_airport.metar.correction ? null : "o";
			}
		}));

		columns.put(col++, new MColumn("Raw METAR", false, SwingConstants.LEFT, null, new MColumnValue()
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
		return visibleAirports.size();
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
		MAirport airport = visibleAirports.get(rowIndex);
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
		Logger.info("load begin");

		airports = new MOurAirports().loadAirports();
		HashMap<String, MXPlaneAirport> xPlaneMap = new MXPlane().load();
		HashMap<String, MMetar> noaaApiMetars = new MNOAAAPI().load();
		ArrayList<MMetar> noaaFtpMetars = new MNOAAFTP().load();

		totalMetars = 0;
		totalMetarNotDecoded = 0;

		for (MMetar metar : noaaFtpMetars)
		{
			MAirport airport = get(metar.stationId);
			if (airport != null)
			{
				airport.metar = metar;
				airport.xPlane = xPlaneMap.containsKey(metar.stationId);

				totalMetars++;
				if (metar.notDecoded)
					totalMetarNotDecoded++;
			}

			MMetar apiMetar = noaaApiMetars.get(metar.stationId);
			if (apiMetar != null)
				metar.extraFlightCategory = apiMetar.extraFlightCategory;
		}

		updateVisible();

		sortedColumn = 1;
		sortedAsc = true;
		sort();

		Logger.info("load end");
	}

	public boolean canSort(int _col)
	{
		return columns.get(_col).comparator != null;
	}

	public void sort()
	{
		Collections.sort(visibleAirports, columns.get(sortedColumn).comparator);
	}

	public void updateVisible()
	{
		visibleAirports.clear();

		visibleAirports = airports.stream().filter(airport -> !filterShowOnlyAirportsWithMetar || airport.metar != null)
				.filter(airport -> !filterNotDecodedMetars || (airport.metar != null && airport.metar.notDecoded))
				.filter(airport -> filterCountry == null || airport.country.equals(filterCountry.code))
				.filter(
						airport -> filterMetar == null || (airport.metar != null && airport.metar.rawText.contains(filterMetar)))
				.collect(Collectors.toList());
	}

	public int getTotalAirports()
	{
		return airports.size();
	}

	public int getVisibleAirports()
	{
		return visibleAirports.size();
	}

	public int getTotalMetars()
	{
		return totalMetars;
	}

	public int getTotalMetarNotDecoded()
	{
		return totalMetarNotDecoded;
	}
}
