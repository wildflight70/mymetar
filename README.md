The purpose of <b>myMetar</b> is to read and decode all METARs provided by airports worldwide and present them in a table to allow sorting, searching, and filtering.

Currently:
- Airports are retrieved from https://ourairports.com.
- METARs are retrieved from https://aviationweather.gov.

This project is developed in Java with Eclipse.

It uses the following libraries:
- tinylog (https://tinylog.org)
- common-net (https://commons.apache.org)
- common-csv (dependencies: common-io and common-codec)

The <code>build.xml</code> file allows generating a distribution using Ant (https://ant.apache.org). The generated distribution is located in the <code>dist</code> folder and includes the launchers <code>mymetar.cmd</code> for Windows and <code>mymetar.sh</code> for Linux.

The <code>mymetar.ini</code> configuration file contains:
- The location of X-Plane
- The NOAA server coordinates

When running the application, a <code>mymetar.log</code> file is generated.

Downloading data generates a <code>temp</code> folder containing:
- <code>*.TXT</code>: Raw NOAA files
- <code>noaa_ftp_metars.csv</code>: Aggregation of raw NOAA files, keeping only the latest METARs
- <code>noaa_api_metars.csv</code>: Latest METARs
- <code>ourairports_airports.csv</code>: Airports
- <code>ourairports_countries.csv</code>: Countries

Improvements:
- Complete the decoding of METARs
- Display progress during data download
- Better warn the user in case of errors
- Read airports from Microsoft Flight Simulator 2020 & 2024
