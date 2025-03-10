package bottom;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import metar.MRemark;

@SuppressWarnings("serial")
class MBottomRemarksModel extends AbstractTableModel
{
	private String[] columns = new String[] { "Field", "Explanation" };
	public ArrayList<MRemark> remarks;

	@Override
	public int getRowCount()
	{
		return remarks == null ? 0 : remarks.size();
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
		if (remarks == null)
			return null;
		MRemark remark = remarks.get(rowIndex);
		switch (columnIndex)
		{
		case 0:
			return remark.field;
		case 1:
			return remark.value;
		default:
			return null;
		}
	}
}