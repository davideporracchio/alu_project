package com.holonomix.file.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.properties.PropertiesContainer;

public class SeedFileService {

	private static Logger log = Logger.getLogger(SeedFileService.class);
	private static SeedFileService seedFile = null;
	protected Set<String> seedFileList = new HashSet<String>();
	private String fileName="";
	Pattern pattern;
	
	private SeedFileService(String fileName) {
		pattern = Pattern.compile(PropertiesContainer.getInstance().getProperty("SEEDFILE_REGEX"));
		this.fileName=fileName;
		
	}

	

	private void readFile() {
		try {
			if (log.isDebugEnabled())
			log.debug("read seed file "+fileName );
			seedFileList = new HashSet<String>();
			File file = new File(fileName);
			if (file.exists()){
			
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(file);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				String key = strLine.trim();
				if (isValidFormat(key))
					seedFileList.add(key);
				else 
					log.error("this row has incorrect format: "+key);
				

			}
			if (log.isDebugEnabled()){
				if (seedFileList.size()==0)
					log.debug("no row found, application executes full import" );
				else
					log.debug("found "+seedFileList.size() + " rows" );
			
			}
			// Close the input stream
			in.close();
			DateTime d1= new DateTime();
			//davide
			file.renameTo(new File(fileName+"."+d1.toString("ddMMYYYY")+".done"));
			}
			else{
				log.debug("file  "+fileName+ " does not exist, application executes import" );
			}

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return ;
	}
	
	public static SeedFileService getInstance(String fileName) {

		if (seedFile == null)
			seedFile = new SeedFileService(fileName);

		return seedFile;
	}

	public Set<String> getSeedFileList() {
		readFile();
		return seedFileList;
	}
	
	private boolean isValidFormat(String row) {
		
		Matcher matcher = pattern.matcher(row);
		if(matcher.find()){
			if(matcher.group().equalsIgnoreCase(row))
			return true;
		}
		return false;
	}
}
