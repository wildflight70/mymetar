package bottom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import data.MAirport;
import data.MCountry;
import data.MOurAirports;
import main.MTable;
import metar.MMetar;
import util.MTableColumnAdjuster;

@SuppressWarnings("serial")
public class MBottom extends JPanel
{
	public JLabel labelMetarValue;
	private JLabel labelAirportValue;
	private JLabel labelCountryValue;
	private JLabel labelCityValue;

	private HashMap<String, MCountry> countries;

	private MBottomItemsModel itemsModel;
	private MBottomItemsTable itemsTable;

	private MBottomRemarksModel remarksModel;
	private MBottomRemarksTable remarksTable;

	public MMetar metar;

	private Font boldFont = getFont().deriveFont(Font.BOLD);

	public static MBottom instance = new MBottom();

	private MBottom()
	{
		MOurAirports ourAirports = new MOurAirports();
		countries = ourAirports.loadCountries();

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Metar
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(createMetar(), c);
		c.gridwidth = 1;

		// Items
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(createItems(), c);

		// Remarks
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(createRemarks(), c);

		// Airport
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1.0;
		panel.add(createAirport(), c);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
	}

	private JPanel createMetar()
	{
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// METAR
		JLabel labelMetar = new JLabel("METAR");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelMetar, c);

		labelMetarValue = new JLabel("");
		labelMetarValue.setOpaque(true);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(labelMetarValue, c);

		return panel;
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

	private JPanel createItems()
	{
		JPanel panel = new JPanel(new BorderLayout());
		Border border = BorderFactory.createTitledBorder("Items");
		panel.setBorder(border);
		panel.setMinimumSize(new Dimension(500, 200));
		panel.setPreferredSize(new Dimension(500, 200));

		itemsModel = new MBottomItemsModel();
		itemsTable = new MBottomItemsTable(itemsModel);

		JScrollPane scrollPane = new JScrollPane(itemsTable);

		panel.add(scrollPane);

		return panel;
	}

	private JPanel createRemarks()
	{
		JPanel panel = new JPanel(new BorderLayout());
		Border border = BorderFactory.createTitledBorder("Remarks");
		panel.setBorder(border);
		panel.setMinimumSize(new Dimension(500, 200));
		panel.setPreferredSize(new Dimension(500, 200));

		remarksModel = new MBottomRemarksModel();
		remarksTable = new MBottomRemarksTable(remarksModel);

		JScrollPane scrollPane = new JScrollPane(remarksTable);

		panel.add(scrollPane);

		return panel;
	}

	public void update(MAirport _airport)
	{
		metar = _airport.metar;

		labelMetarValue.setText(_airport.metar == null ? "" : _airport.metar.rawTextHighlight);
		labelMetarValue
				.setBackground((_airport.metar == null || !_airport.metar.notDecoded) ? labelAirportValue.getBackground()
						: MTable.NOT_DECODED_COLOR);

		labelAirportValue.setText(_airport.name);

		MCountry country = countries.get(_airport.country);
		labelCountryValue.setText(country == null ? "" : country.toString());
		
		labelCityValue.setText(_airport.city);

		itemsModel.load(_airport.metar == null ? null : _airport.metar.items);
		itemsModel.fireTableDataChanged();
		new MTableColumnAdjuster(itemsTable).adjustColumns();

		remarksModel.load(_airport.metar == null ? null : _airport.metar.remarks);
		remarksModel.fireTableDataChanged();
		new MTableColumnAdjuster(remarksTable).adjustColumns();
	}
}
