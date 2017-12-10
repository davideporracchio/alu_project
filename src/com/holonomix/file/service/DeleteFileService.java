package com.holonomix.file.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DeleteFileService {

	private static Logger log = Logger.getLogger(DeleteFileService.class);

	
	private static DeleteFileService deleteFile = null;
	protected List< String[]> deleteFileList = null;
	
	
	private String fileName="";
	
	private DeleteFileService(String fileName) {
		this.fileName=fileName;
		
		
		readFile();
	}

	public static DeleteFileService getInstance(String fileName) {

		if (deleteFile == null)
			deleteFile = new DeleteFileService(fileName);
		
		return deleteFile;
	}

	public void update() {
			readFile();
	}

	

	private void readFile() {
		try {
			if (log.isDebugEnabled())
			log.debug("read delete file "+fileName );
			deleteFileList = new ArrayList< String[]>();
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
				
				String[] values = new String [3];
				values [0]=strLine.split(" ")[0].trim();
				values [1]=strLine.split(" ")[1].trim();
				values [2]=strLine.split(" ")[2].trim();
				
				deleteFileList.add( values);

			}
			if (log.isDebugEnabled())
			log.debug("found "+deleteFileList.size() + " rows" );
			// Close the input stream
			in.close();

		} catch (Exception e) {// Catch exception if any
			log.error("Error: " + e.getMessage());
		}
		return;
	}

	public List<String[]> getDeleteFileList() {
		return deleteFileList;
	}

	
	
	
	
	
	

	

}
