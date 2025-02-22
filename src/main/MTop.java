package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import util.MFormat;

@SuppressWarnings("serial")
public class MTop extends JPanel
{
	private JLabel labelTotalValue;
	private JLabel labelVisibleValue;
	private JLabel labelFoundValue;

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
		c.anchor = GridBagConstraints.LINE_START;
		add(createFilter(), c);

		// Search
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(createSearch(), c);

		// Count
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(createCount(), c);
	}

	private JPanel createFilter()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		Border boder = BorderFactory.createTitledBorder("Filter");
		panel.setBorder(boder);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		JCheckBox checkShowOnlyMetars = new JCheckBox("Show only airports with METAR");
		checkShowOnlyMetars.setHorizontalTextPosition(SwingConstants.LEFT);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(checkShowOnlyMetars, c);

		checkShowOnlyMetars.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				table.updateVisible(checkShowOnlyMetars.isSelected());
				table.updateTop();
			}
		});

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

		// Total
		JLabel labelTotal = new JLabel("Total airports");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotal, c);

		labelTotalValue = new JLabel("0");
		labelTotalValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelTotalValue, c);

		// Visible
		JLabel labelVisible = new JLabel("Visible airports");
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelVisible, c);

		labelVisibleValue = new JLabel("0");
		labelVisibleValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelVisibleValue, c);

		// Found
		JLabel labelFound = new JLabel("Found airports");
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelFound, c);

		labelFoundValue = new JLabel("0");
		labelFoundValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(labelFoundValue, c);

		return panel;
	}

	public void update(int _total, int _visible, int _found)
	{
		labelTotalValue.setText(MFormat.instance.numberFormatDecimal0.format(_total));
		labelVisibleValue.setText(MFormat.instance.numberFormatDecimal0.format(_visible));
		labelFoundValue.setText(MFormat.instance.numberFormatDecimal0.format(_found));
	}
}
