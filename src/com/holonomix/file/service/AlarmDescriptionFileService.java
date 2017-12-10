package com.holonomix.file.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.holonomix.hsqldb.model.Alarm;

public class AlarmDescriptionFileService {

	private static Logger log = Logger.getLogger(AlarmDescriptionFileService.class);

	
	private static AlarmDescriptionFileService mappingFile = null;
	protected Map<String, Alarm> mappingFileMap = null;
	
	
	private String fileName="";
	
	private AlarmDescriptionFileService(String fileName) {
		this.fileName=fileName;
		
		
		readFile();
	}

	public static AlarmDescriptionFileService getInstance(String fileName) {

		if (mappingFile == null)
			mappingFile = new AlarmDescriptionFileService(fileName);
		
		return mappingFile;
	}

	public void update() {
			readFile();
	}

	

	private void readFile() {
		try {
			if (log.isDebugEnabled())
			log.debug("read alarm map file "+fileName );
			mappingFileMap = new HashMap<String, Alarm>();
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				String splitarray[] = strLine.split("\t");
				if (isValidFormat(splitarray[0])){
					Alarm alarm=new Alarm();
					alarm.setOid(splitarray[0]);
					alarm.setName(splitarray[3]);
					alarm.setSeverity(splitarray[1]);
					alarm.setDescription(splitarray[5]);
					mappingFileMap.put(splitarray[0], alarm);
				}

			}
			if (log.isDebugEnabled())
			log.debug("found "+mappingFileMap.size() + " rows" );
			// Close the input stream
			in.close();

		} catch (Exception e) {// Catch exception if any
			log.error("Error: " + e.getMessage());
		}
		return;
	}

	public Alarm findValuesForKey(String key){
		return mappingFileMap.get(key);
		
	}
	
	
	private boolean isValidFormat(String row) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher matcher = pattern.matcher(row);
		if(matcher.find()){
			if(matcher.group().equalsIgnoreCase(row))
			return true;
		}
		return false;
	}
	
	
	

	

}
