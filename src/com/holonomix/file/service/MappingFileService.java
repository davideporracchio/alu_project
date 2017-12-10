package com.holonomix.file.service;

import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MappingFileService {

	private static Logger log = Logger.getLogger(MappingFileService.class);

	
	private static MappingFileService mappingFile = null;
	private Map<String, String[]> mappingFileMap = null;
	private Map<String, Set<String>> deviceIpamMap = null;
	private List<String> listKeys = new ArrayList<String>();
	private String[] listIpam;
	private String fileName="";
	
	private MappingFileService(String fileName,String listIpam) {
		this.fileName=fileName;
		this.listIpam=listIpam.split(",");
		deviceIpamMap=new HashMap<String, Set<String>>();
		
		//readFile();
	}

	public static MappingFileService getInstance(String fileName,String listIpam) {
		
		
		if (mappingFile == null)
			mappingFile = new MappingFileService(fileName,listIpam);
		
		return mappingFile;
	}

	public void update() {
			readFile();
	}

	

	private void readFile() {
		try {
			if (log.isDebugEnabled())
			log.debug("read map file "+fileName );
			mappingFileMap = new HashMap<String, String[]>();
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				try{
				String key = strLine.split("=")[0].trim();
				String[] values = strLine.split("=")[1].trim().split(",");
				listKeys.add(key);
				mappingFileMap.put(key, values);
				log.debug("reading mapping file : found device "+key + " for ipam "+values );
				} catch (Exception e) {// Catch exception if any
					if (strLine.equalsIgnoreCase("")){
							log.error("syntax error line is empty in seed file." );
					}
					else{
					log.error("syntax error in line: " +strLine);
					}
					
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
	
	public Set<String> findIpamsForDevice(String name){
		
		if (deviceIpamMap.containsKey(name)){
			return deviceIpamMap.get(name);
		}
		else{
			return findIpamsForDeviceLogic(name);
		}
	}
	

	private Set<String> findIpamsForDeviceLogic(String name){
		
		Set<String> result=null;
		for (String patternStr:listKeys){
		
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(name);
		if(matcher.find()){
			
			if(matcher.group().equalsIgnoreCase(name)){
			result = matchIpams(mappingFileMap.get(patternStr));
			break;
			}
		}
		}
		if (log.isDebugEnabled() && result!=null){
		log.debug("device "+name +" is  in ipams: "+result.toString() );
		deviceIpamMap.put(name,result);
		}else {
			
			log.debug("device "+name + " not found in mapping memory array");
		}
		return result;
	}
	
	private Set<String> matchIpams(String[] listRegexIpam){
		Set<String> matchedIpams=new HashSet<String>();
		for (String regexIpam:listRegexIpam){
			Pattern pattern = Pattern.compile(regexIpam);
			for (String ipam:listIpam){
				Matcher matcher = pattern.matcher(ipam);
				if (matcher.find()) {
					if(matcher.group().equalsIgnoreCase(ipam)){
						matchedIpams.add(ipam);
					}
			}
			}
		}
		return  matchedIpams;
		
	}
	

	

}
