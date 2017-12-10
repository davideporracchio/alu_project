package com.holonomix.alarm;

import org.apache.log4j.Logger;

import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;


/**
 * This class manages the number of time we do a connection with ems before we do the fail over
 * 
 */
public class ImportAlarmThread implements Runnable {
	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger.getLogger(ImportAlarmThread.class);
	public ImportAlarmThread() {
		
		 propertiesContainer = PropertiesContainer.getInstance();
	}

	
	

	public void run() {

		
		int numConnection = 0;
		int maxConnection = Integer.parseInt(propertiesContainer
				.getProperty("ALARM_RETRY_NUMBER"));
		int holtTime = Integer.parseInt(propertiesContainer.getProperty("ALARM_RETRY_HOLDTIME")) * 1000;

		try {
			while (numConnection < maxConnection) {
				if(numConnection>0){
					log.debug(" try again to open session to get alarms");
				}
				try {
					numConnection++;
					
					ImportAlarmTask importLogic = new ImportAlarmTask();
					importLogic.doImport();
					
					
				} catch (AdapterException ei) {
					Thread.sleep(holtTime);
				} catch (SmartsException ei) {
					Thread.sleep(holtTime);
			}
			}		
			
		} catch (InterruptedException ei) {
			log.error("interrupted Exception "+ei.getMessage());
		}

		if (numConnection == maxConnection) {
			log.error("FAIL OVER");
			MapMonitor.getInstance().put("ALARM", "failed");
		}

	}

}
