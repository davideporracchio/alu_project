package com.holonomix.alarm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.ActiveAlarmInterface;
import com.holonomix.commoninterface.ListeningAlarmInterface;
import com.holonomix.exception.AdapterException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.list.ActiveList;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

/**
 * this class starts two threads: ListeningAlarmInterface for new alarms
 * ActiveAlarmInterface for active alarms
 * */
public class AlarmFacade {

	private static final Logger log = Logger.getLogger(AlarmFacade.class);

	public AlarmFacade() {

	}

	public void readAlarm(ActiveList<Alarm> activeList,
			ActiveList<Alarm> listeningList) throws AdapterException {

		final ActiveList<Alarm> myActiveList = activeList;
		final ActiveList<Alarm> myListeningList = listeningList;
		final ActiveAlarmInterface activeAlarmClient = ClassFactory
				.getActiveAlarmInstance();

		activeList.setActive(false);
		activeList.clear();
		activeList.setActive(true);
		listeningList.setActive(true);
		// start active alarm

		PropertiesContainer.getInstance().setProperty("ALARM_LISTENING_START",
				"false");
		log.debug("start listening alarm");
		ListeningAlarmInterface listeningAlarmClient = ClassFactory
				.getListeningAlarmInstance(myListeningList);
		listeningAlarmClient.clearTimestamp();
		ListeningAlarmThread listeningAlarmThread = new ListeningAlarmThread(
				listeningAlarmClient);

		FutureTask future = new FutureTask(listeningAlarmThread);
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future f = es.submit(future);
		AlarmMonitor.clean();
		log.debug("start active alarm");
		try {
			activeAlarmClient.connect(myActiveList);
			//log.debug("checkpoint117a");
			activeAlarmClient.disconnect();
			//log.debug("checkpoint117b");
		} catch (AdapterException e) {
			//log.debug("checkpoint118");
			es.shutdownNow();
			PropertiesContainer.getInstance()
					.setProperty("STOP_THREAD", "TRUE");
			throw e;
		}

		while (true) {

			try {
				//log.debug("checkpoint119");
				Thread.sleep(2000);
				//davide 2309 
				//f.get();
				if (MapMonitor.getInstance().get("LISTENINGHEARTBEAT") != null) {
					if (MapMonitor.getInstance().get("LISTENINGHEARTBEAT")
							.equalsIgnoreCase("failed")) {
						MapMonitor.getInstance()
								.put("LISTENINGHEARTBEAT", null);
						PropertiesContainer.getInstance().setProperty(
								"STOP_THREAD", "TRUE");
						log.info("listening heartbeat failed. Closing session.");
						es.shutdownNow();
						if (PropertiesContainer.getInstance()
								.getProperty("ADAPTER_NAME")
								.equalsIgnoreCase("ZTE-METE") ||(PropertiesContainer.getInstance()
										.getProperty("ADAPTER_NAME")
										.equalsIgnoreCase("ZTE-MSAN"))) {
							listeningAlarmClient = null;
							ClassFactory.setListeningAlarmInterface(null);
						}
						throw new AdapterException(1);
					}
				}
			} catch (InterruptedException e) {

				PropertiesContainer.getInstance().setProperty("STOP_THREAD",
						"TRUE");
				es.shutdownNow();
				throw new AdapterException(1);
			/*} catch (ExecutionException ee) {
				System.out.println("Execution failed " + ee.getMessage());
				PropertiesContainer.getInstance().setProperty("STOP_THREAD",
						"TRUE");
				es.shutdownNow();*/
			}
		}

	}

}
