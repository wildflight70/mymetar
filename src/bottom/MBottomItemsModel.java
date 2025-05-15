package bottom;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import metar.MItem;

@SuppressWarnings("serial")
class MBottomItemsModel extends AbstractTableModel
{
	private String[] columns = new String[] { "Field", "Explanation" };
	public ArrayList<MItem> items;

	@Override
	public int getRowCount()
	{
		return items == null ? 0 : items.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return columns[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (items == null)
			return null;
		MItem item = items.get(rowIndex);
		switch (columnIndex)
		{
		case 0:
			return item.field;
		case 1:
			return item.value;
		default:
			return null;
		}
	}

	public void load(ArrayList<MItem> _items)
	{
		if (_items != null)
		{
			items = new ArrayList<MItem>();
			for (MItem item : _items)
				if (item.value != null)
					items.add(item);
		}
	}
}