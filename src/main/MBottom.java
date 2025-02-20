package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MBottom extends JPanel
{
	private JLabel labelAirportValue;
	private JLabel labelCountryValue;
	private JLabel labelCityValue;

	public static MBottom instance = new MBottom();

	private MBottom()
	{
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

	public void update(MMetarEx _metar)
	{
		if (_metar.xPlane != null)
		{
			labelAirportValue.setText(_metar.xPlane.airportName);
			labelCountryValue.setText(_metar.xPlane.country);
			labelCityValue.setText(_metar.xPlane.city);
		}
		else
		{
			labelAirportValue.setText("");
			labelCountryValue.setText("");
			labelCityValue.setText("");
		}
	}
}
