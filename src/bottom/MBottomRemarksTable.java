package bottom;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import main.MTable;

class MBottomRemarksTable extends JTable
{
	public MBottomRemarksTable(MBottomRemarksModel _model)
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