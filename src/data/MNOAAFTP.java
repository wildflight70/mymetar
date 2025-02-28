package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.tinylog.Logger;

import util.MProperties;

public class MNOAAFTP
{
	private static final String LOCAL_DIR = "temp/";
	private static final String METARS_FILE = "noaa_ftp_metars.csv";

	private String server;
	private int port;
	private String user;
	private String password;
	private String remoteDir;

	public boolean download(TaskDownloadUpdate _update)
	{
		Logger.info("download begin");

		boolean ok = true;

		File localDir = new File(LOCAL_DIR);
		if (!localDir.exists())
			localDir.mkdir();

		int processors = Math.min(24, Runtime.getRuntime().availableProcessors());
		Logger.info(processors + " processors");

		ExecutorService executor = Executors.newFixedThreadPool(processors);
		List<Future<Void>> futures = new ArrayList<>();

		try
		{
			server = MProperties.instance.properties.getProperty("NOAA_FTP_SERVER");
			port = Integer.parseInt(MProperties.instance.properties.getProperty("NOAA_FTP_PORT"));
			user = MProperties.instance.properties.getProperty("NOAA_FTP_USER");
			password = MProperties.instance.properties.getProperty("NOAA_FTP_PASSWORD");
			remoteDir = MProperties.instance.properties.getProperty("NOAA_FTP_REMOTE_DIR");

			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(server, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();

			ftpClient.changeWorkingDirectory(remoteDir);
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile file : files)
				if (file.isFile())
					futures.add(executor.submit(new TaskDownload(_update, file)));

			ftpClient.logout();
			ftpClient.disconnect();
		}
		catch (Exception e)
		{
			Logger.error(e);
		}

		for (Future<Void> future : futures)
			try
			{
				future.get();
			}
			catch (Exception e)
			{
				Logger.error(e);
			}

		executor.shutdown();

		ArrayList<MMetar> metars = read();
		write(metars);

		Logger.info("download end");

		return ok;
	}

	public static interface TaskDownloadUpdate
	{
		public void run(String _file, boolean _status);
	}

	private class TaskDownload implements Callable<Void>
	{
		private TaskDownloadUpdate update;
		private FTPFile file;

		public TaskDownload(TaskDownloadUpdate _update, FTPFile _file)
		{
			update = _update;
			file = _file;
		}

		public Void call() throws Exception
		{
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(server, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			File downloadFile = new File(LOCAL_DIR + file.getName());
			OutputStream outputStream = new FileOutputStream(downloadFile);
			boolean success = ftpClient.retrieveFile(remoteDir + file.getName(), outputStream);

			ftpClient.logout();
			ftpClient.disconnect();

			if (update != null)
				update.run(server + "/" + file.getName(), success);

			Logger.info("Downloaded " + file + " : " + success);

			return null;
		}
	}

	private ArrayList<MMetar> read()
	{
		Logger.info("read begin");

		int processors = Runtime.getRuntime().availableProcessors();

		ExecutorService executor = Executors.newFixedThreadPool(processors);
		List<Future<ArrayList<MMetar>>> futures = new ArrayList<>();

		File directory = new File(LOCAL_DIR);
		File[] textFiles = directory.listFiles((dir, name) -> name.endsWith(".TXT"));
		for (File file : textFiles)
			futures.add(executor.submit(new TaskRead(file.toString())));

		HashMap<String, MMetar> metarsMap = new HashMap<String, MMetar>();

		for (Future<ArrayList<MMetar>> future : futures)
			try
			{
				ArrayList<MMetar> metars = future.get();
				for (MMetar metar : metars)
				{
					MMetar metarMap = metarsMap.get(metar.stationId);
					if (metarMap == null || metar.observationTime.isAfter(metarMap.observationTime))
						metarsMap.put(metar.stationId, metar);
				}
			}
			catch (Exception e)
			{
				Logger.error(e);
			}

		executor.shutdown();

		ArrayList<MMetar> metars = new ArrayList<MMetar>();
		metarsMap.forEach((stationId, metar) ->
		{
			metars.add(metar);
		});

		Logger.info("read end");

		return metars;
	}

	private class TaskRead implements Callable<ArrayList<MMetar>>
	{
		private String file;

		public TaskRead(String _file)
		{
			file = _file;
		}

		public ArrayList<MMetar> call() throws Exception
		{
			return read(file);
		}
	}

	private ArrayList<MMetar> read(String _file)
	{
		HashMap<String, MMetar> metarsMap = new HashMap<String, MMetar>();

		try (BufferedReader reader = new BufferedReader(new FileReader(_file)))
		{
			LocalDateTime observationTime = null;
			String line;
			while ((line = reader.readLine()) != null)
				if (!line.isEmpty())
				{
					String[] items = line.split(" ");
					if (items.length == 2)
					{
						String[] date = items[0].split("/");
						int year = Integer.parseInt(date[0]);
						int month = Integer.parseInt(date[1]);
						int day = Integer.parseInt(date[2]);
						String[] time = items[1].split(":");
						int hour = Integer.parseInt(time[0]);
						int minute = Integer.parseInt(time[1]);
						if (minute >= 60)
						{
							hour++;
							minute = minute % 60;
						}
						try
						{
							observationTime = LocalDateTime.of(year, month, day, hour, minute, 0);
						}
						catch (DateTimeException ex)
						{
							observationTime = null;
						}
					}
					else if (items.length > 2 && observationTime != null)
					{
						MMetar metar = new MMetar(observationTime, line, items[0]);

						MMetar metarMap = metarsMap.get(metar.stationId);
						if (metarMap == null || metar.observationTime.isAfter(metarMap.observationTime))
							metarsMap.put(metar.stationId, metar);
					}
				}
		}
		catch (IOException e)
		{
			Logger.error(e);
		}

		ArrayList<MMetar> metars = new ArrayList<MMetar>();
		metarsMap.forEach((stationId, metar) ->
		{
			metars.add(metar);
		});

		return metars;
	}

	private boolean write(ArrayList<MMetar> _metars)
	{
		boolean ok = true;

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOCAL_DIR + METARS_FILE)))
		{
			for (MMetar metar : _metars)
			{
				writer.write(metar.write());
				writer.newLine();
			}
		}
		catch (IOException e)
		{
			Logger.error(e);
			ok = false;
		}
		return ok;
	}

	public ArrayList<MMetar> load()
	{
		ArrayList<MMetar> metars = new ArrayList<MMetar>();

		String fileName = LOCAL_DIR + METARS_FILE;
		if (new File(fileName).exists())
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
			{
				String line;
				while ((line = reader.readLine()) != null)
					if (!line.isEmpty())
					{
						String[] items = line.split(",");

						LocalDateTime observationTime = LocalDateTime.parse(items[0]);

						MMetar metar = new MMetar(observationTime, items[1]);
						metar.decode();
						metars.add(metar);
					}
			}
			catch (IOException e)
			{
				Logger.error(e);
			}
		}
		else
			Logger.error(fileName + " does not exist");

		return metars;
	}
}
