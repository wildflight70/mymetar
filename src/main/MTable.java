package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.tinylog.Logger;

import data.MAirport;
import data.MCountry;
import util.MClipboard;
import util.MColor;

@SuppressWarnings("serial")
public class MTable extends JTable
{
	public final static Color ROW_BACKGROUND_COLOR = new Color(225, 240, 225);
	private final static Color EXTRA_COLOR = new Color(200, 255, 200);
	private final static Color FOUND_COLOR = Color.BLUE;
	private final static Color NOT_DECODED_COLOR = new Color(255, 200, 200);

	private MModel model;

	private String findText = "";
	private int findRow;
	private ArrayList<Integer> findRows = new ArrayList<Integer>();

	public MTable(MModel _model)
	{
		super(_model);

		model = _model;

		updateSortedColumnName(false);

		getTableHeader().setReorderingAllowed(false);

		JLabel label = new JLabel("X");
		label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
				label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				return label;
			}
		}
		setDefaultRenderer(Object.class, new PaddingRenderer());

		// Click on column header to sort this column
		JTableHeader header = getTableHeader();
		header.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int col = columnModel.getColumnIndexAtX(e.getX());
				if (col >= 0 && model.canSort(col))
				{
					updateSortedColumnName(true);

					if (col == model.sortedColumn)
						model.sortedAsc = !model.sortedAsc;
					else
					{
						model.sortedColumn = col;
						model.sortedAsc = true;
					}
					_model.sort();
					_model.fireTableDataChanged();

					updateSortedColumnName(false);
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
						MBottom.instance.update(model.visibleAirports.get(row));
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

		initMenu();
	}

	private void updateSortedColumnName(boolean _reset)
	{
		TableColumn column = columnModel.getColumn(model.sortedColumn);
		column = columnModel.getColumn(model.sortedColumn);
		String newName = model.getColumnName(model.sortedColumn);
		if (!_reset)
			newName += (model.sortedAsc ? " +" : " -");
		column.setHeaderValue(newName);
	}

	private void initMenu()
	{
		JPopupMenu popup = new JPopupMenu();

		JMenuItem menuItemCopyMetarClipboard = new JMenuItem("Copy METAR to clipboard");
		menuItemCopyMetarClipboard.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				doCopyMetarToClipboard();
			}
		});
		popup.add(menuItemCopyMetarClipboard);

		JMenuItem menuItemCopyMetarHighlightClipboard = new JMenuItem("Copy highlight METAR to clipboard");
		menuItemCopyMetarHighlightClipboard.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int selectedRow = getSelectedRow();
				MAirport selectedAirport = model.visibleAirports.get(selectedRow);
				if (selectedAirport.metar != null)
					new MClipboard().copy(selectedAirport.metar.rawTextHighlight);
			}
		});
		popup.add(menuItemCopyMetarHighlightClipboard);

		JMenuItem menuItemPrintMetar = new JMenuItem("Print METAR to output");
		menuItemPrintMetar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int selectedRow = getSelectedRow();
				MAirport selectedAirport = model.visibleAirports.get(selectedRow);
				if (selectedAirport.metar != null)
					System.out.println(selectedAirport.metar.debug());
			}
		});
		popup.add(menuItemPrintMetar);

		addMouseListener(new MouseAdapter()
		{
			private void showPopup(MouseEvent e)
			{
				int col = columnAtPoint(e.getPoint());
				if (col >= 0)
					setColumnSelectionInterval(col, col);
				int row = rowAtPoint(e.getPoint());
				if (row >= 0)
					setRowSelectionInterval(row, row);
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
					showPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
					showPopup(e);
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
				c.setBackground(ROW_BACKGROUND_COLOR);

		// Set background color to extra columns (not relative to metar)
		MColumn column = model.columns.get(col);
		if (column.extra)
			c.setBackground(MColor.blend(c.getBackground(), EXTRA_COLOR));

		// Set alignment
		((JLabel) c).setHorizontalAlignment(column.alignment);

		// Highlight search
		MAirport airport = model.visibleAirports.get(row);
		c.setForeground(airport.found ? FOUND_COLOR : Color.BLACK);

		// Highlight not decoded metars
		if (column.name.equals("Station id") || column.name.equals("Raw METAR"))
			if (airport.metar != null && airport.metar.notDecoded)
				c.setBackground(MColor.blend(c.getBackground(), NOT_DECODED_COLOR));

		return c;
	}

	/**
	 * Select a row and scroll table to it.
	 * 
	 * @param _row
	 */
	public void selectRow(int _row)
	{
		if (getRowCount() > 0 && _row < getRowCount())
		{
			scrollRectToVisible(getCellRect(_row, 0, true));
			setRowSelectionInterval(_row, _row);
		}
	}

	/**
	 * Find all rows that match a text. Press ENTER to scroll to the next row.
	 * Search applies to stationId.
	 * 
	 * @param _text
	 */
	public void find(String _text)
	{
		if (!_text.equals(findText))
		{
			findText = _text;
			findRow = 0;
			for (int i = 0; i < findRows.size(); i++)
			{
				int row = findRows.get(i);
				MAirport airport = model.visibleAirports.get(row);
				airport.found = false;
				model.fireTableRowsUpdated(row, row);
			}
			findRows.clear();

			if (!_text.isEmpty())
				for (int i = 0; i < model.visibleAirports.size(); i++)
				{
					MAirport airport = model.visibleAirports.get(i);
					if (airport.stationId.contains(_text))
					{
						findRows.add(i);
						airport.found = true;
						model.fireTableRowsUpdated(i, i);
					}
				}
			updateTop();
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
		MAirport airport = model.visibleAirports.get(getSelectedRow());
		String url = "https://www.google.com/maps?q=" + airport.latitude + "," + airport.longitude;
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

	private void doCopyMetarToClipboard()
	{
		int selectedRow = getSelectedRow();

		MAirport selectedAirport = model.visibleAirports.get(selectedRow);
		if (selectedAirport.metar != null)
			new MClipboard().copy(selectedAirport.metar.rawText);
	}

	public void updateVisible(boolean _showOnlyAirportsWithMetar, boolean _notDecodedMetars, MCountry _country,
			String _metar)
	{
		model.updateVisible(_showOnlyAirportsWithMetar, _notDecodedMetars, _country.code.isEmpty() ? null : _country,
				_metar.isEmpty() ? null : _metar);
		model.fireTableDataChanged();
		selectRow(0);
	}

	public void updateTop()
	{
		MTop.instance.update(model.getTotalAirports(), model.getVisibleAirports(), findRows.size(), model.getTotalMetars(),
				model.getTotalMetarNotDecoded());
	}

	public void clearFind()
	{
		findText = "";
		findRow = 0;
		findRows.clear();
	}
}
