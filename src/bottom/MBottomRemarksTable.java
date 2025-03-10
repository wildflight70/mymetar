package bottom;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import main.MTable;
import metar.MRemark;

@SuppressWarnings("serial")
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

		getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					int row = getSelectedRow();
					if (row >= 0 && row < getRowCount())
						doHighlight(_model.remarks.get(row));
				}
			}
		});
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

	private void doHighlight(MRemark _remark)
	{
		String text = MBottom.instance.metar.highlight(null, _remark);
		MBottom.instance.labelMetarValue.setText(text);
	}
}