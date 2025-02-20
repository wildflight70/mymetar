package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MTop extends JPanel
{
	private JLabel labelTotalValue;
	private JLabel labelFoundValue;

	private MTable table;

	public static MTop instance = new MTop();

	public void setTable(MTable _table)
	{
		table = _table;
	}

	private MTop()
	{
		Font boldFont = getFont().deriveFont(Font.BOLD);

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// Find
		JLabel labelFind = new JLabel("Find");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelFind, c);

		JTextField textFind = new JTextField(10);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(textFind, c);

		// Total
		JLabel labelTotal = new JLabel("Total");
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelTotal, c);

		labelTotalValue = new JLabel("0");
		labelTotalValue.setFont(boldFont);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelTotalValue, c);

		// Found
		JLabel labelFound = new JLabel("Found");
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		add(labelFound, c);

		labelFoundValue = new JLabel("0");
		labelFoundValue.setFont(boldFont);
		c.gridx = 3;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelFoundValue, c);

		textFind.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					table.find(textFind.getText().trim().toUpperCase());
			}
		});
	}

	public void update(int _total, int _found)
	{
		labelTotalValue.setText(_total + "");
		labelFoundValue.setText(_found + "");
	}
}
