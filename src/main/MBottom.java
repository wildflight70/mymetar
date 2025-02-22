package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import data.MAirport;
import data.MOurAirports;

@SuppressWarnings("serial")
public class MBottom extends JPanel
{
	private JLabel labelAirportValue;
	private JLabel labelCountryValue;
	private JLabel labelCityValue;

	private HashMap<String, String> countries;

	public static MBottom instance = new MBottom();

	private MBottom()
	{
		MOurAirports ourAirports = new MOurAirports();
		countries = ourAirports.loadCountries();

		Font boldFont = getFont().deriveFont(Font.BOLD);

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		int col = 0;

		// Airport
		JLabel labelAirport = new JLabel("Airport");
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelAirport, c);

		labelAirportValue = new JLabel("");
		labelAirportValue.setFont(boldFont);
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelAirportValue, c);

		// Country
		JLabel labelCountry = new JLabel("Country");
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelCountry, c);

		labelCountryValue = new JLabel("");
		labelCountryValue.setFont(boldFont);
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelCountryValue, c);

		// City
		JLabel labelCity = new JLabel("City");
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelCity, c);

		labelCityValue = new JLabel("");
		labelCityValue.setFont(boldFont);
		c.gridx = col++;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelCityValue, c);
	}

	public void update(MAirport _airport)
	{
		labelAirportValue.setText(_airport.name);
		labelCountryValue.setText(countries.get(_airport.country) + " (" + _airport.country + ")");
		labelCityValue.setText(_airport.city);
	}
}
