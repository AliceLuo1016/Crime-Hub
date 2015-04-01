package com.google.gwt.sample.crimehub.server;

import java.io.FileNotFoundException;
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;  
import java.util.List;    

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.gwt.sample.crimehub.client.Crime;
import com.google.gwt.sample.crimehub.client.ParserService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ParserServiceImpl extends RemoteServiceServlet implements ParserService {

	private boolean isLoaded = false;
	private List<Crime> crimeList = new ArrayList<Crime>();

	public ParserServiceImpl() {}

	public List<Crime> getParsedCrimes(){
		if (isLoaded == true) {
			System.out.println("Parsed crimes already retrieved.");
			return null;
		}
		else {
			System.out.println("New data added.");
			setToLoaded();
			tryCsvToJava();
			return crimeList;
		}
	}

	private void setToLoaded() {
		isLoaded = true;
	}

	private void tryCsvToJava() {
		try {
			convertCsvToJava();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertCsvToJava() throws MalformedURLException, IOException {  
		// Data hosted on Jimmy's ugrad account
		InputStream input2012 = new URL("http://www.ugrad.cs.ubc.ca/~p9u8/ToBeParsedCrimeFile2012.csv").openStream();
		InputStream input2013 = new URL("http://www.ugrad.cs.ubc.ca/~p9u8/ToBeParsedCrimeFile2013.csv").openStream();
		InputStream input2014 = new URL("http://www.ugrad.cs.ubc.ca/~p9u8/ToBeParsedCrimeFile2014.csv").openStream();

		// InputStream as a Reader using InputStreamReader which you can then feed to the CSV parser
		Reader reader2012 = new InputStreamReader(input2012, "UTF-8");
		Reader reader2013 = new InputStreamReader(input2013, "UTF-8");
		Reader reader2014 = new InputStreamReader(input2014, "UTF-8");

		try {  

			parseCrimeYear(reader2012);

			parseCrimeYear(reader2013);

			parseCrimeYear(reader2014);

			// Create the CSVFormat object
			CSVFormat format = CSVFormat.EXCEL.withDelimiter(',').withHeader("TYPE", "YEAR", "MONTH", "HUNDRED_BLOCK", "INDEX", "LAT", "LON");

//			// Initialize CSVParser object
//			CSVParser parser1 = new CSVParser(reader2012, format);
//			// 2012
//			for (CSVRecord record: parser1) {
//				Crime crimeObject = new Crime();
//				crimeObject.setType(record.get("TYPE"));
//				crimeObject.setYear(record.get("YEAR"));
//				crimeObject.setMonth(record.get("MONTH"));
//				crimeObject.setAddress(record.get("HUNDRED_BLOCK"));
//				crimeObject.setLat(record.get("LAT"));
//				crimeObject.setLon(record.get("LON"));
//				if (crimeIsValid(crimeObject)) {
//					crimeList.add(crimeObject);
//				}
//			}
//			parser1.close();
//
			// Initialize CSVParser object
			CSVParser parser2 = new CSVParser(reader2013, format);
			// 2013
			for (CSVRecord record: parser2) {
				Crime crimeObject = new Crime();
				crimeObject.setType(record.get("TYPE"));
				crimeObject.setYear(record.get("YEAR"));
				crimeObject.setMonth(record.get("MONTH"));
				crimeObject.setAddress(record.get("HUNDRED_BLOCK"));
				crimeObject.setLat(record.get("LAT"));
				crimeObject.setLon(record.get("LON"));
				if (crimeIsValid(crimeObject)) {
					crimeList.add(crimeObject);
				}
			}
			parser2.close();

//			// Initialize CSVParser object
//			CSVParser parser3 = new CSVParser(reader2014, format);
//			// 2014
//			for (CSVRecord record: parser3) {
//				Crime crimeObject = new Crime();
//				crimeObject.setType(record.get("TYPE"));
//				crimeObject.setYear(record.get("YEAR"));
//				crimeObject.setMonth(record.get("MONTH"));
//				crimeObject.setAddress(record.get("HUNDRED_BLOCK"));
//				crimeObject.setLat(record.get("LAT"));
//				crimeObject.setLon(record.get("LON"));
//				if (crimeIsValid(crimeObject)) {
//					crimeList.add(crimeObject);
//				}
//			}
//			parser3.close();
			
		} catch (FileNotFoundException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}

	}

	public List<Crime> parseCrimeYear(Reader reader)
			throws IOException {
		if (reader == null) {
			return crimeList;
		}
		// Create the CSVFormat object
		CSVFormat format = CSVFormat.EXCEL.withDelimiter(',').withHeader("TYPE", "YEAR", "MONTH", "HUNDRED_BLOCK", "INDEX", "LAT", "LON");
		// Initialize CSVParser object
		CSVParser parser1 = new CSVParser(reader, format);
		// For demo use, the daily quota is our enemy.
		int NUM_ITERATIONS = 0;
		// Get rid of the header row
		boolean firstIteration = true;
		for (CSVRecord record: parser1) {
			if (firstIteration) {
				firstIteration = false;
				continue;
			}
			if (NUM_ITERATIONS > 3000) {
				break;
			}
			Crime crimeObject = new Crime();
			crimeObject.setType(record.get("TYPE"));
			crimeObject.setYear(record.get("YEAR"));
			crimeObject.setMonth(record.get("MONTH"));
			crimeObject.setAddress(record.get("HUNDRED_BLOCK"));
			crimeObject.setLat(record.get("LAT"));
			crimeObject.setLon(record.get("LON"));
			if (crimeIsValid(crimeObject)) {
				crimeList.add(crimeObject);
			}
			NUM_ITERATIONS++;
		}
		parser1.close();
		return crimeList;
	}

	public boolean crimeIsValid(Crime c) {
		String lat = c.getLat();
		String lon = c.getLon();
		if (lat.equals("-") || lat.equals("LAT") || lon.equals("-") || lon.equals("LON")) {
			return false;
		}
		else return true;
	}


}  
