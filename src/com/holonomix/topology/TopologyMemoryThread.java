package com.holonomix.topology;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.holonomix.properties.PropertiesContainer;

public class TopologyMemoryThread implements Runnable {

	boolean isCompleted = false;
	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger
			.getLogger(TopologyMemoryThread.class);
	
	public TopologyMemoryThread() {
		
		propertiesContainer = PropertiesContainer.getInstance();
	}

	private int getFrequency() {

		String frequency = propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_MEMORY_FREQUENCY").trim();
		return Integer.parseInt(frequency) * 3600;
	}

	private DateTime getStartTime() {

		String startTime = propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_MEMORY_STARTIME").trim();
		DateTime dt = new DateTime();
		int hours = Integer.parseInt(startTime.split(":")[0]);
		int minutes = Integer.parseInt(startTime.split(":")[1]);
		DateTime dt2 = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt
				.getDayOfMonth(), hours, minutes, 0, 0);

		
		
		if (propertiesContainer.getProperty("TOPOLOGY_IMPORT_MEMORY_STARTDAY") != null
				&& !propertiesContainer.getProperty("TOPOLOGY_IMPORT_MEMORY_STARTDAY")
						.equalsIgnoreCase("")) {
			while (!dt2.dayOfWeek().getAsText().equalsIgnoreCase(
					propertiesContainer.getProperty("TOPOLOGY_IMPORT_MEMORY_STARTDAY")
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

		final TopologyMemoryTask topologyMemoryTask =TopologyMemoryTask.getInstance();
		// wait correct time to run the first import
		Boolean updateNow = new Boolean(propertiesContainer.getProperty(
				"TOPOLOGY_IMPORT_MEMORY_NOW").trim());

		Runnable callReadBuffer = new Runnable() {
			public void run() {

				log.info("START MEMORY IMPORT "
						+ (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));

				// main code
				topologyMemoryTask.doImport();
				log.info("FINISHED MEMORY IMPORT "
						+ (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));

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
