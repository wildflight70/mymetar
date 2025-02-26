package main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import data.MAirport;
import data.MCountry;
import data.MMetar.MRemark;
import data.MOurAirports;
import util.MTableColumnAdjuster;

@SuppressWarnings("serial")
public class MBottom extends JPanel
{
	private JLabel labelAirportValue;
	private JLabel labelCountryValue;
	private JLabel labelCityValue;

	private HashMap<String, MCountry> countries;
	private MBottomModel model;
	private MBottomTable table;

	private Font boldFont = getFont().deriveFont(Font.BOLD);

	public static MBottom instance = new MBottom();

	private MBottom()
	{
		MOurAirports ourAirports = new MOurAirports();
		countries = ourAirports.loadCountries();

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(createRemarks(), c);

		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1.0;
		panel.add(createAirport(), c);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
	}

	private JPanel createAirport()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		Border border = BorderFactory.createTitledBorder("Airport");
		panel.setBorder(border);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Airport
		JLabel labelAirport = new JLabel("Airport");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelAirport, c);

		labelAirportValue = new JLabel("");
		labelAirportValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(labelAirportValue, c);

		// Country
		JLabel labelCountry = new JLabel("Country");
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelCountry, c);

		labelCountryValue = new JLabel("");
		labelCountryValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(labelCountryValue, c);

		// City
		JLabel labelCity = new JLabel("City");
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelCity, c);

		labelCityValue = new JLabel("");
		labelCityValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(labelCityValue, c);

		return panel;
	}

	private JPanel createRemarks()
	{
		JPanel panel = new JPanel(new BorderLayout());
		Border border = BorderFactory.createTitledBorder("Remarks");
		panel.setBorder(border);
		panel.setMinimumSize(new Dimension(500, 100));
		panel.setPreferredSize(new Dimension(500, 100));

		model = new MBottomModel();
		table = new MBottomTable(model);

		JScrollPane scrollPane = new JScrollPane(table);

		panel.add(scrollPane);

		return panel;
	}

	public void update(MAirport _airport)
	{
		labelAirportValue.setText(_airport.name);
		labelCountryValue.setText(countries.get(_airport.country).toString());
		labelCityValue.setText(_airport.city);
		model.remarks = _airport.metar == null ? null : _airport.metar.remarks;
		model.fireTableDataChanged();
		new MTableColumnAdjuster(table).adjustColumns();
	}

	private class MBottomModel extends AbstractTableModel
	{
		private String[] columns = new String[] { "Field", "Explanation" };
		public ArrayList<MRemark> remarks;

		@Override
		public int getRowCount()
		{
			return remarks == null ? 0 : remarks.size();
		}

		@Override
		public int getColumnCount()
		{
			return columns.length;
		}

		@Override
		public String getColumnName(int column)
		{
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (remarks == null)
				return null;
			MRemark remark = remarks.get(rowIndex);
			switch (columnIndex)
			{
			case 0:
				return remark.field;
			case 1:
				return remark.remark;
			default:
				return null;
			}
		}

	}

	private class MBottomTable extends JTable
	{
		public MBottomTable(MBottomModel _model)
		{
			super(_model);

			getTableHeader().setReorderingAllowed(false);

			JLabel label = new JLabel("X");
			label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			setRowHeight(label.getPreferredSize().height);

			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int row, int col)
		{
			Component c = super.prepareRenderer(renderer, row, col);

			// Set background row color
			if (!isCellSelected(row, col))
				if (row % 2 == 0)
					c.setBackground(getBackground());
				else
					c.setBackground(MTable.ROW_BACKGROUND_COLOR);

			return c;
		}
	}
}
