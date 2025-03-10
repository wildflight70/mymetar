package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import bottom.MBottom;
import util.MTableColumnAdjuster;

@SuppressWarnings("serial")
public class MMainWindow extends JFrame
{
	private MModel model;
	private MTable table;

	public MMainWindow()
	{
		super("myMetar");

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
		table.updateTop();
		table.selectRow(0);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollPane, MBottom.instance);

		setLayout(new BorderLayout());
		add(MTop.instance, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);

		split.setDividerLocation(300);
	}

	private void initMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		// Menu File
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		// Menu File > Download
		JMenuItem menuFileLoad = new JMenuItem("Download");
		menuFileLoad.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				doDownload();
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

	private void doDownload()
	{
		MDownload download = new MDownload();
		download.setVisible(true);

		// Update table
		model.load();
		model.fireTableDataChanged();
		new MTableColumnAdjuster(table).adjustColumns();
		table.selectRow(0);
		table.updateTop();
	}
}
