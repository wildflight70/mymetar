package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import data.MLoadNOAAAPI;
import data.MLoadNOAAFTP;
import data.MMetar;
import util.MTableColumnAdjuster;

@SuppressWarnings("serial")
public class MMainWindow extends JFrame
{
	private MModel model;
	private MTable table;

	public MMainWindow()
	{
		super("METAR");

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		initTable();
		initMenu();

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setSize((size.width * 85 / 100), (size.height * 60) / 100);
		setLocationRelativeTo(null);
	}

	private void initTable()
	{
		model = new MModel();
		table = new MTable(model);

		new MTableColumnAdjuster(table).adjustColumns();

		JScrollPane scrollPane = new JScrollPane(table);

		MTop.instance.setTable(table);
		MTop.instance.update(table.getRowCount(), 0);

		table.selectRow(0);

		setLayout(new BorderLayout());
		add(MTop.instance, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(MBottom.instance, BorderLayout.SOUTH);
	}

	private void initMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		// Menu File
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		// Menu File > Load FTP
		JMenuItem menuFileLoad = new JMenuItem("Load NOAA FTP");
		menuFileLoad.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				doLoad();
			}
		});
		menuFile.add(menuFileLoad);

		// Menu File > Exit
		JMenuItem menuFileExit = new JMenuItem("Exit");
		menuFileExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});
		menuFile.add(menuFileExit);

		setJMenuBar(menuBar);
	}

	private void doLoad()
	{
		// FTP
		MLoadNOAAFTP loadFTP = new MLoadNOAAFTP();
		loadFTP.download();
		ArrayList<MMetar> metars = loadFTP.loadAll();
		loadFTP.write(metars);

		// API
		MLoadNOAAAPI loadAPI = new MLoadNOAAAPI();
		metars = loadAPI.downloadAllCSV();
		loadAPI.write(metars);

		// Update table
		model.resetColumn();
		model.load();
		model.fireTableDataChanged();
		new MTableColumnAdjuster(table).adjustColumns();
		table.selectRow(0);
		MTop.instance.update(table.getRowCount(), 0);
	}
}
