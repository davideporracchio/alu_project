package com.holonomix.topology;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.holonomix.properties.PropertiesContainer;

public class ImportTopologyThread implements Runnable {

	boolean isCompleted = false;
	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger
			.getLogger(ImportTopologyThread.class);

	public ImportTopologyThread() {

		propertiesContainer = PropertiesContainer.getInstance();
		propertiesContainer.setProperty("THREAD_FLAG_DISCOVERY", "false");
	}

	public int getFrequency() {

		String frequency = propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_FREQUENCY").trim();
		return Integer.parseInt(frequency) * 3600;
	}

	private DateTime getStartTime() {

		String startTime = propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_STARTIME").trim();
		DateTime dt = new DateTime();
		int hours = Integer.parseInt(startTime.split(":")[0]);
		int minutes = Integer.parseInt(startTime.split(":")[1]);
		DateTime dt2 = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt
				.getDayOfMonth(), hours, minutes, 0, 0);

		
		
		if (propertiesContainer.getProperty("TOPOLOGY_IMPORT_STARTDAY") != null
				&& !propertiesContainer.getProperty("TOPOLOGY_IMPORT_STARTDAY")
						.equalsIgnoreCase("")) {
			while (!dt2.dayOfWeek().getAsText().equalsIgnoreCase(
					propertiesContainer.getProperty("TOPOLOGY_IMPORT_STARTDAY")
							.trim())) {
				dt2 = dt2.plusDays(1);

			}
			//if the date is in the past i add one week
			if (dt2.isBeforeNow())
				dt2 = dt2.plusDays(7);
		} else {
			if (dt2.isBeforeNow())
				dt2 = dt2.plusDays(1);

		}
		return dt2;
	}

	

	public void run() {

		final ImportTopologyTask importLogic = new ImportTopologyTask();
		
		// wait correct time to run the first import
		Boolean updateNow = new Boolean(propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_NOW").trim());

		Runnable callReadBuffer = new Runnable() {
			public void run() {
				do{
					synchronized (propertiesContainer) {
				if(propertiesContainer.getProperty("THREAD_FLAG_PARTIAL_DISCOVERY")==null || propertiesContainer.getProperty("THREAD_FLAG_PARTIAL_DISCOVERY").equalsIgnoreCase("false")){
					propertiesContainer.setProperty("THREAD_FLAG_DISCOVERY", "true");
				}}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.debug("shut down application. Exit code: 20");
					System.exit(20);
				}
				}while(propertiesContainer.getProperty("THREAD_FLAG_DISCOVERY").equalsIgnoreCase("false"));
			
				log.info("START IMPORT "
						+ (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));

				// main code
				importLogic.doImport();

				log.info("FINISHED IMPORT "
						+ (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));
				synchronized (propertiesContainer) {
					propertiesContainer.setProperty("THREAD_FLAG_DISCOVERY", "false");
				
			}
				isCompleted = true;

			}
		};

		DateTime startDate = getStartTime();
		DateTime nowDate = new DateTime();
		int delay = 0;
		if (!updateNow) {
			delay = Seconds.secondsBetween(nowDate, startDate).getSeconds();
		}
		ScheduledExecutorService scheduler = Executors
				.newSingleThreadScheduledExecutor();

		// Get a handle, starting now, with a 10 second delay
		final ScheduledFuture<?> timeHandle = scheduler.scheduleAtFixedRate(
				callReadBuffer, delay, getFrequency(), TimeUnit.SECONDS);

	}

	public boolean isCompleted() {
		return isCompleted;
	}

}
