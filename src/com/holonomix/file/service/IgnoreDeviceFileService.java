package com.holonomix.file.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.properties.PropertiesContainer;

public class IgnoreDeviceFileService {

	private static Logger log = Logger.getLogger(IgnoreDeviceFileService.class);
	private static IgnoreDeviceFileService ignoreFile = null;
	protected Set<Pattern> ignoreFileList = new HashSet<Pattern>();
	protected Set<String> ignoreFileListString = new HashSet<String>();
	private String fileName = "";

	private IgnoreDeviceFileService(String fileName) {

		this.fileName = fileName;

	}

	private void readFile() {
		try {

			// if (log.isDebugEnabled())
			// log.debug("read ignore file "+fileName );
			Set<String> ignoreFileListStringTemp = new HashSet<String>();

			File file = new File(fileName);
			if (file.exists()) {

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
					ignoreFileListStringTemp.add(key);
					// ignoreFileListStringTemp.add(Pattern.compile(key));
				}
				/*
				 * if (log.isDebugEnabled()){ if (ignoreFileList.size()==0)
				 * log.debug("no row found" ); else
				 * log.debug("found "+ignoreFileList.size() + " rows" );
				 * 
				 * }
				 */
				// Close the input stream
				in.close();

			} else {
				log.debug("file  " + fileName + " does not exist");
			}
			if (!areBothTheSame(ignoreFileListStringTemp, ignoreFileListString)) {
				ignoreFileListString = ignoreFileListStringTemp;
				ignoreFileList.clear();
				Iterator<String> it = ignoreFileListString.iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					log.debug("Pattern to ignore:" + key);
					ignoreFileList.add(Pattern.compile(key));
				}
				log.debug("Found "+ ignoreFileList.size() +" patterns for devices to ignore.");
				
				
			}

		} catch (Exception e) {// Catch exception if any
			log.error("Error: " + e.getMessage());
		}
		return;
	}

	private boolean areBothTheSame(Set<String> set1, Set<String> set2) {
		if (set1.containsAll(set2) && set2.containsAll(set1))
			return true;

		return false;
	}

	public static IgnoreDeviceFileService getInstance(String fileName) {

		if (ignoreFile == null) {
			ignoreFile = new IgnoreDeviceFileService(fileName);
			ignoreFile.readFile();
		}
		return ignoreFile;
	}

	public synchronized void updateList() {
		readFile();

	}

	public boolean isValidDevice(String device) {
		if (ignoreFileList == null || ignoreFileList.size() == 0)
			return true;
		for (Pattern pattern : ignoreFileList) {
			if (matchesPattern(pattern, device)) {
				return false;
			}
		}
		return true;

	}

	private boolean matchesPattern(Pattern p, String device) {
		Matcher m = p.matcher(device);

		if (m.matches()) {
			log.debug("Excluding device with name " + device);
			return true;
		}

		return false;
	}
}
