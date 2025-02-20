package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tinylog.Logger;

import data.MMetar;
import main.MModel.VLColumn;
import util.MColor;

@SuppressWarnings("serial")
public class MTable extends JTable
{
	private final static Color ROW_BACKGROUND_COLOR = new Color(225, 240, 225);
	private final static Color EXTRA_COLOR = new Color(200, 255, 200);
	private final static Color FOUND_COLOR = Color.BLUE;

	private MModel model;

	private String findText = "";
	private int findRow;
	private ArrayList<Integer> findRows = new ArrayList<Integer>();

	public MTable(MModel _model)
	{
		super(_model);

		model = _model;

		getTableHeader().setReorderingAllowed(false);
		
		JLabel label = new JLabel("X");
		label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		setRowHeight(label.getPreferredSize().height);
		
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Default rendering with padding
		class PaddingRenderer extends DefaultTableCellRenderer
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
					int row, int column)
			{
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
				return label;
			}
		}
		setDefaultRenderer(Object.class, new PaddingRenderer());

		// Click on column header to sort this column
		TableColumnModel columnModel = getColumnModel();
		JTableHeader header = getTableHeader();
		header.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int col = columnModel.getColumnIndexAtX(e.getX());
				if (col >= 0 && model.canSort(col))
				{
					_model.resetColumn();
					TableColumn column = columnModel.getColumn(model.sortedColumn);
					column.setHeaderValue(_model.getColumnName(model.sortedColumn));

					if (col == model.sortedColumn)
						model.sortedAsc = !model.sortedAsc;
					else
					{
						model.sortedColumn = col;
						model.sortedAsc = true;
					}
					_model.sort();
					_model.fireTableDataChanged();

					column = columnModel.getColumn(model.sortedColumn);
					column.setHeaderValue(_model.getColumnName(model.sortedColumn));
					header.repaint();
				}
			}
		});

		// Select a row and show properties at bottom
		getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					int row = getSelectedRow();
					if (row >= 0 && row < getRowCount())
						MBottom.instance.update(model.metars.get(row));
				}
			}
		});

		// Double-click to show airport in browser
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
					doOpenGoogleMaps();
			}
		});
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col)
	{
		Component c = super.prepareRenderer(renderer, row, col);

		if (!isCellSelected(row, col))
			if (row % 2 == 0)
				c.setBackground(getBackground());
			else
				c.setBackground(ROW_BACKGROUND_COLOR);

		VLColumn column = model.columns.get(col);
		if (column.extra)
			c.setBackground(MColor.blend(c.getBackground(), EXTRA_COLOR));

		((JLabel) c).setHorizontalAlignment(column.alignment);

		MMetar metar = model.metars.get(row);
		c.setForeground(metar.found ? FOUND_COLOR : Color.BLACK);

		return c;
	}

	public void selectRow(int _row)
	{
		if (getRowCount() > 0 && _row < getRowCount())
		{
			scrollRectToVisible(getCellRect(_row, 0, true));
			setRowSelectionInterval(_row, _row);
		}
	}

	public void find(String _text)
	{
		if (!_text.equals(findText))
		{
			findText = _text;
			findRow = 0;
			for (int i = 0; i < findRows.size(); i++)
			{
				int row = findRows.get(i);
				MMetarEx metar = model.metars.get(row);
				metar.found = false;
				model.fireTableRowsUpdated(row, row);
			}
			findRows.clear();

			if (!_text.isEmpty())
				for (int i = 0; i < model.metars.size(); i++)
				{
					MMetarEx metar = model.metars.get(i);
					if (metar.stationId.contains(_text))
					{
						findRows.add(i);
						metar.found = true;
						model.fireTableRowsUpdated(i, i);
					}
				}
			MTop.instance.update(getRowCount(), findRows.size());
		}

		if (findRows.size() > 0)
		{
			selectRow(findRows.get(findRow));
			findRow++;
			if (findRow == findRows.size())
				findRow = 0;
		}
	}

	private void doOpenGoogleMaps()
	{
		MMetarEx metar = model.metars.get(getSelectedRow());
		if (metar.xPlane != null)
		{
			String url = "https://www.google.com/maps?q=" + metar.xPlane.latitude + "," + metar.xPlane.longitude;
			if (Desktop.isDesktopSupported())
			{
				try
				{
					Desktop.getDesktop().browse(new URI(url));
				}
				catch (Exception e)
				{
					Logger.error(e);
				}
			}
		}
	}
}
