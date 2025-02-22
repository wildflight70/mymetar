The purpose of <b>myMetar</b> is to read and decode all METARs provided by airports worldwide and present them in a table to allow sorting, searching, and filtering.

Currently:
- Airports are retrieved from https://ourairports.com.
- METARs are retrieved from https://aviationweather.gov.

This project is developed in Java with Eclipse.

It uses the following libraries:
- tinylog (https://tinylog.org)
- common-net (https://commons.apache.org)
- common-csv (dependencies: common-io and common-codec)

The build.xml file allows generating a distribution using Ant (https://ant.apache.org). The generated distribution is located in the dist folder and includes the launchers mymetar.cmd for Windows and mymetar.sh for Linux.

The mymetar.ini configuration file contains:
- The location of X-Plane
- The NOAA server coordinates

When running the application, a mymetar.log file is generated.

Downloading data generates a temp folder containing:
- *.TXT: Raw NOAA files
- noaa_ftp_metars.csv: Aggregation of raw NOAA files, keeping only the latest METARs
- noaa_api_metars.csv: Latest METARs
- ourairports_airports.csv: Airports
- ourairports_countries.csv: Countries
