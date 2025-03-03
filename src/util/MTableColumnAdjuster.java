package util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class MTableColumnAdjuster
{
	private final JTable table;
	private final int spacing = 6;

	public MTableColumnAdjuster(JTable _table)
	{
		table = _table;
	}

	public void adjustColumns()
	{
		TableColumnModel tcm = table.getColumnModel();
		int columnCount = tcm.getColumnCount();

		for (int i = 0; i < columnCount; i++)
			adjustColumn(i);
	}

	public void adjustColumn(int _column)
	{
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn tableColumn = columnModel.getColumn(_column);

		if (!tableColumn.getResizable())
			return;

		int headerWidth = getColumnHeaderWidth(tableColumn);
		int dataWidth = getColumnDataWidth(_column);
		int preferredWidth = Math.max(headerWidth, dataWidth) + spacing;

		updateTableColumn(tableColumn, preferredWidth);
	}

	private int getColumnHeaderWidth(TableColumn _tableColumn)
	{
		Object value = _tableColumn.getHeaderValue();
		TableCellRenderer renderer = _tableColumn.getHeaderRenderer();

		if (renderer == null)
			renderer = table.getTableHeader().getDefaultRenderer();

		Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, _tableColumn.getModelIndex());
		return c.getPreferredSize().width;
	}

	private int getColumnDataWidth(int _column)
	{
		int preferredWidth = 0;
		TableColumn tableColumn = table.getColumnModel().getColumn(_column);
		int maxWidth = tableColumn.getMaxWidth();

		int rowCount = table.getRowCount();
		for (int row = 0; row < rowCount; row++)
		{
			preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, _column));

			if (preferredWidth >= maxWidth)
				return maxWidth;
		}
		return preferredWidth;
	}

	private int getCellDataWidth(int _row, int _column)
	{
		Component c = table.prepareRenderer(table.getCellRenderer(_row, _column), _row, _column);
		return c.getPreferredSize().width + table.getIntercellSpacing().width;
	}

	private void updateTableColumn(TableColumn _column, int _width)
	{
		table.getTableHeader().setResizingColumn(_column);
		_column.setWidth(_width);
	}
}
