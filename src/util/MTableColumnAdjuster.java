package util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class MTableColumnAdjuster
{
	private JTable table;
	private int spacing = 6;

	public MTableColumnAdjuster(JTable _table)
	{
		table = _table;
	}

	public void adjustColumns()
	{
		TableColumnModel tcm = table.getColumnModel();

		for (int i = 0; i < tcm.getColumnCount(); i++)
			adjustColumn(i);
	}

	public void adjustColumn(int _column)
	{
		TableColumn tableColumn = table.getColumnModel().getColumn(_column);

		if (!tableColumn.getResizable())
			return;

		int columnHeaderWidth = getColumnHeaderWidth(_column);
		int columnDataWidth = getColumnDataWidth(_column);
		int preferredWidth = Math.max(columnHeaderWidth, columnDataWidth);

		updateTableColumn(_column, preferredWidth);
	}

	private int getColumnHeaderWidth(int _column)
	{
		TableColumn tableColumn = table.getColumnModel().getColumn(_column);
		Object value = tableColumn.getHeaderValue();
		TableCellRenderer renderer = tableColumn.getHeaderRenderer();

		if (renderer == null)
			renderer = table.getTableHeader().getDefaultRenderer();

		Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, _column);
		return c.getPreferredSize().width;
	}

	private int getColumnDataWidth(int _column)
	{
		int preferredWidth = 0;
		int maxWidth = table.getColumnModel().getColumn(_column).getMaxWidth();

		for (int row = 0; row < table.getRowCount(); row++)
		{
			preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, _column));

			if (preferredWidth >= maxWidth)
				break;
		}

		return preferredWidth;
	}

	private int getCellDataWidth(int _row, int _column)
	{
		TableCellRenderer cellRenderer = table.getCellRenderer(_row, _column);
		Component c = table.prepareRenderer(cellRenderer, _row, _column);
		int width = c.getPreferredSize().width + table.getIntercellSpacing().width;

		return width;
	}

	private void updateTableColumn(int _column, int _width)
	{
		final TableColumn tableColumn = table.getColumnModel().getColumn(_column);

		if (!tableColumn.getResizable())
			return;

		_width += spacing;

		table.getTableHeader().setResizingColumn(tableColumn);
		tableColumn.setWidth(_width);
	}
}
