package main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import data.MNOAAAPI;
import data.MNOAAFTP;
import data.MNOAAFTP.TaskDownloadUpdate;
import data.MOurAirports;
import util.MTableColumnAdjuster;

@SuppressWarnings("serial")
public class MDownload extends JDialog
{
	private JButton buttonStart;
	private JButton buttonClose;

	private MDownloadModel model;
	private MDownloadTable table;

	public MDownload()
	{
		super((Frame) null, "Download", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		buttonStart = new JButton("Start");
		buttonStart.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				buttonStart.setEnabled(false);

				buttonClose.setText("Running");
				buttonClose.setEnabled(false);

				download();
			}
		});

		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		model = new MDownloadModel();
		table = new MDownloadTable(model);

		JScrollPane scrollPane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(buttonStart, BorderLayout.NORTH);
		add(scrollPane);
		add(buttonClose, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	private void update(String _file, boolean _status)
	{
		model.data.add(new MDownloadItem(_file, _status));
		model.fireTableDataChanged();
		new MTableColumnAdjuster(table).adjustColumns();

		int row = table.getRowCount() - 1;
		table.scrollRectToVisible(table.getCellRect(row, 0, true));
		table.setRowSelectionInterval(row, row);
	}

	private void download()
	{
		class MWorker extends SwingWorker<Void, MDownloadItem>
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				// NOAA FTP
				MNOAAFTP noaaFTP = new MNOAAFTP();
				noaaFTP.download(new TaskDownloadUpdate()
				{
					@Override
					public void run(String _file, boolean _status)
					{
						publish(new MDownloadItem(_file, _status));
					}
				});

				// NOAA API
				MNOAAAPI noaaAPI = new MNOAAAPI();
				noaaAPI.downloadAll(new TaskDownloadUpdate()
				{
					@Override
					public void run(String _file, boolean _status)
					{
						publish(new MDownloadItem(_file, _status));
					}
				});

				// OurAirports
				MOurAirports ourAirports = new MOurAirports();

				boolean status = ourAirports.downloadAirports();
				publish(new MDownloadItem(MOurAirports.AIRPORTS_URL, status));

				status = ourAirports.downloadCountries();
				publish(new MDownloadItem(MOurAirports.COUNTRIES_URL, status));

				return null;
			}

			@Override
			protected void process(List<MDownloadItem> chunks)
			{
				for (MDownloadItem item : chunks)
					update(item.file, item.status);
			}

			@Override
			protected void done()
			{
				buttonStart.setEnabled(true);

				buttonClose.setText("Close");
				buttonClose.setEnabled(true);
			}
		}

		new MWorker().execute();
	}

	private class MDownloadItem
	{
		public String file;
		public boolean status;

		public MDownloadItem(String _file, boolean _status)
		{
			file = _file;
			status = _status;
		}
	}

	private class MDownloadModel extends AbstractTableModel
	{
		private String[] columns = new String[] { "File", "Status" };
		public ArrayList<MDownloadItem> data;

		public MDownloadModel()
		{
			data = new ArrayList<MDownloadItem>();
		}

		@Override
		public int getRowCount()
		{
			return data.size();
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
			MDownloadItem item = data.get(rowIndex);
			switch (columnIndex)
			{
			case 0:
				return item.file;
			case 1:
				return item.status;
			default:
				return null;
			}
		}
	}

	private class MDownloadTable extends JTable
	{
		public MDownloadTable(MDownloadModel _model)
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
}
