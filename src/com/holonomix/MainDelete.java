package com.holonomix;

import org.apache.log4j.Logger;




public class MainDelete {

	private static final Logger log = Logger.getLogger(MainDelete.class);
	
	public static void main(String args[]) {

		try {
			DeleteAdapter deleteAdapter = new DeleteAdapter();
			deleteAdapter.start();
		
			} catch (Exception e) {
				
				log.error(e.getMessage());
			}
			
	}
		
	
}
