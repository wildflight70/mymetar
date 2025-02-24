package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.tinylog.Logger;

public class MMain
{
	public static void main(String[] args)
	{
		Logger.info("myMetar");

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (Exception e)
				{
					Logger.error(e);
				}

				new MMainWindow().setVisible(true);
			}
		});
	}
}
