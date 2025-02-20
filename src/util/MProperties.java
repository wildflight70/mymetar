package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.tinylog.Logger;

public class MProperties
{
	public static MProperties instance = new MProperties();

	public Properties properties;

	private MProperties()
	{
		properties = new Properties();
		try (FileInputStream fis = new FileInputStream("mymetar.ini"))
		{
			properties.load(fis);
		}
		catch (IOException e)
		{
			Logger.error(e);
		}
	}
}
