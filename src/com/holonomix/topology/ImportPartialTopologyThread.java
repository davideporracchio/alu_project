package com.holonomix.topology;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.ClassFactory;
import com.holonomix.alarm.AlarmCallable;
import com.holonomix.commoninterface.ActiveAlarmInterface;
import com.holonomix.exception.AdapterException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.list.ActiveList;
import com.holonomix.list.DefaultActiveList;
import com.holonomix.listener.DeviceListener;
import com.holonomix.properties.PropertiesContainer;

public class ImportPartialTopologyThread implements Runnable {

	boolean isCompleted = false;
	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger.getLogger(ImportPartialTopologyThread.class);

	public ImportPartialTopologyThread() {

		propertiesContainer = PropertiesContainer.getInstance();
		propertiesContainer.setProperty("THREAD_FLAG_PARTIAL_DISCOVERY", "false");
	}

	public int getFrequency() {

		String frequency = propertiesContainer.getProperty("TOPOLOGY_PARTIAL_IMPORT_FREQUENCY").trim();
		return Integer.parseInt(frequency) * 3600;
	}

	public void run() {

		final ImportPartialTopologyTask importLogic = new ImportPartialTopologyTask();

		Runnable callReadBuffer = new Runnable() {
			public void run() {

				do {
					synchronized (propertiesContainer) {
						if (propertiesContainer.getProperty("THREAD_FLAG_DISCOVERY") == null
								|| propertiesContainer.getProperty("THREAD_FLAG_DISCOVERY").equalsIgnoreCase("false")) {
							DeviceListener.clear();
							propertiesContainer.setProperty("THREAD_FLAG_PARTIAL_DISCOVERY", "true");
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.debug("shut down application. Exit code: 20");
						System.exit(20);
					}
				} while (propertiesContainer.getProperty("THREAD_FLAG_PARTIAL_DISCOVERY").equalsIgnoreCase("false"));

				log.info("START PARTIAL IMPORT " + (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));

				// main code
				importLogic.doImport();

				log.info("FINISHED PARTIAL IMPORT " + (new DateTime()).toString("dd/MM/YYYY HH:mm:ss"));

				isCompleted = true;
				synchronized (propertiesContainer) {
					propertiesContainer.setProperty("THREAD_FLAG_PARTIAL_DISCOVERY", "false");

				}
				final ActiveAlarmInterface activeAlarmClient = ClassFactory.getActiveAlarmInstance();
				ActiveList<Alarm> activeAlarmsList = new DefaultActiveList<Alarm>();
				try {
					if (DeviceListener.size()==0) return;
					log.debug("Get list active alarm");
					activeAlarmClient.connect(activeAlarmsList);
					int count = 0;
					for (Alarm alarm : activeAlarmsList) {

						if (DeviceListener.isDeviceNew(alarm.getDeviceName())) {
							log.debug("Alarm for a new device");
							propertiesContainer.setProperty("ALARM_LISTENING_START", "false");
							try {
								String[] domainManager = null;

								domainManager = propertiesContainer.getProperty("AM_DOMAINS").split(",");
								ExecutorService pool = Executors.newFixedThreadPool(domainManager.length);
								for (int i = 0; i < domainManager.length; i++) {
									AlarmCallable alarmCallable = new AlarmCallable(alarm, domainManager[i], "active alarm number " + count);
									pool.submit(alarmCallable);

								}
								pool.shutdown();
								while (!pool.isTerminated()) {
								}
								log.debug("Completed process active alarm ");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								log.error(" error processing alarm " + alarm.toString() + ", skipped it");
							}

						}
					}
					propertiesContainer.setProperty("ALARM_LISTENING_START", "true");
				} catch (AdapterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		int delay = 10;

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		// Get a handle, starting now, with a 10 second delay
		final ScheduledFuture<?> timeHandle = scheduler.scheduleWithFixedDelay(callReadBuffer, delay, getFrequency(), TimeUnit.SECONDS);

	}

	public boolean isCompleted() {
		return isCompleted;
	}

}
