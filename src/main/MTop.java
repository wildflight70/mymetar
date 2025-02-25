package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import data.MCountry;
import data.MOurAirports;
import util.MFormat;

@SuppressWarnings("serial")
public class MTop extends JPanel
{
	private JLabel labelTotalAirportsValue;
	private JLabel labelVisibleAirportsValue;
	private JLabel labelFoundAirportsValue;
	private JLabel labelTotalMetarsValue;
	private JLabel labelMetarNotDecodedValue;

	private Font boldFont;

	private MTable table;

	public static MTop instance = new MTop();

	public void setTable(MTable _table)
	{
		table = _table;
	}

	private MTop()
	{
		boldFont = getFont().deriveFont(Font.BOLD);

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Filter
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		add(createFilter(), c);

		// Search
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		add(createSearch(), c);

		// Count
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		add(createCount(), c);
	}

	private JPanel createFilter()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		Border boder = BorderFactory.createTitledBorder("Filter");
		panel.setBorder(boder);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Airports with METAR
		JLabel labelAirportsWithMetar = new JLabel("Airports with METAR");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelAirportsWithMetar, c);

		JCheckBox checkAirportsWithMetar = new JCheckBox();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(checkAirportsWithMetar, c);

		// Not decoded METARs
		JLabel labelNotDecodedMetars = new JLabel("Not decoded METARs");
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelNotDecodedMetars, c);

		JCheckBox checkNotDecodedMetars = new JCheckBox();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(checkNotDecodedMetars, c);

		// Country
		JLabel labelCountry = new JLabel("Country");
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelCountry, c);

		Vector<MCountry> countries = new Vector<MCountry>();
		countries.add(new MCountry("", ""));
		countries.addAll(new MOurAirports().loadCountries().values());
		Collections.sort(countries, new Comparator<MCountry>()
		{
			@Override
			public int compare(MCountry o1, MCountry o2)
			{
				return o1.name.compareTo(o2.name);
			}
		});
		JComboBox<MCountry> comboCountry = new JComboBox<MCountry>(countries);
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(comboCountry, c);

		// Listeners
		ActionListener actionListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				table.updateVisible(checkAirportsWithMetar.isSelected(), checkNotDecodedMetars.isSelected(),
						(MCountry) comboCountry.getSelectedItem());
				table.updateTop();
			}
		};
		comboCountry.addActionListener(actionListener);
		checkAirportsWithMetar.addActionListener(actionListener);
		checkNotDecodedMetars.addActionListener(actionListener);

		return panel;
	}

	private JPanel createSearch()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		Border boder = BorderFactory.createTitledBorder("Search");
		panel.setBorder(boder);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Find
		JLabel labelFind = new JLabel("Find");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelFind, c);

		JTextField textFind = new JTextField(10);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(textFind, c);

		textFind.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					table.find(textFind.getText().trim().toUpperCase());
			}
		});

		return panel;
	}

	private JPanel createCount()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		Border boder = BorderFactory.createTitledBorder("Count");
		panel.setBorder(boder);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Total airports
		JLabel labelTotal = new JLabel("Total airports");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotal, c);

		labelTotalAirportsValue = new JLabel("0");
		labelTotalAirportsValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotalAirportsValue, c);

		// Visible airports
		JLabel labelVisible = new JLabel("Visible airports");
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelVisible, c);

		labelVisibleAirportsValue = new JLabel("0");
		labelVisibleAirportsValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelVisibleAirportsValue, c);

		// Found airports
		JLabel labelFound = new JLabel("Found airports");
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelFound, c);

		labelFoundAirportsValue = new JLabel("0");
		labelFoundAirportsValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelFoundAirportsValue, c);

		// Total METARs
		JLabel labelTotalMetars = new JLabel("Total METARs");
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotalMetars, c);

		labelTotalMetarsValue = new JLabel("0");
		labelTotalMetarsValue.setFont(boldFont);
		c.gridx = 3;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotalMetarsValue, c);

		// Not decoded METARs
		JLabel labelTotalMetarNotDecoded = new JLabel("Not decoded METARs");
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotalMetarNotDecoded, c);

		labelMetarNotDecodedValue = new JLabel("0");
		labelMetarNotDecodedValue.setFont(boldFont);
		c.gridx = 3;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelMetarNotDecodedValue, c);

		return panel;
	}

	public void update(int _totalAirports, int _visibleAirports, int _foundAirports, int _totalMetars,
			int _totalMetarNotDecoded)
	{
		labelTotalAirportsValue.setText(MFormat.instance.numberFormatDecimal0.format(_totalAirports));
		labelVisibleAirportsValue.setText(MFormat.instance.numberFormatDecimal0.format(_visibleAirports));
		labelFoundAirportsValue.setText(MFormat.instance.numberFormatDecimal0.format(_foundAirports));
		labelTotalMetarsValue.setText(MFormat.instance.numberFormatDecimal0.format(_totalMetars));
		labelMetarNotDecodedValue.setText(MFormat.instance.numberFormatDecimal0.format(_totalMetarNotDecoded));
	}
}
