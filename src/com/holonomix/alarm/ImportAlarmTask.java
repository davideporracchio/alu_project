package com.holonomix.alarm;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.alarm.service.AlarmLogicService;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.list.ActiveList;
import com.holonomix.list.ActiveListEvent;
import com.holonomix.list.ActiveListListener;
import com.holonomix.list.DefaultActiveList;
import com.holonomix.properties.PropertiesContainer;

public class ImportAlarmTask {

	private static final Logger log = Logger.getLogger(ImportAlarmTask.class);

	private final ActiveList<Alarm> activeAlarmsList = new DefaultActiveList<Alarm>();
	private final ActiveList<Alarm> listeningAlarmsList = new DefaultActiveList<Alarm>();
	private AlarmFacade alarmFacade;
	AlarmLogicService alarmLogicService;
	
	ListeningAlarmsListener listeningAlarmsListener;
	ActiveAlarmsListener activeAlarmsListener;
	private PropertiesContainer propertiesContainer;

	/**
	 * There are 2 queues one for active alarm and one for new alarm The queue
	 * for new alarm does not start until the queue for active alarm is empty
	 * Just one alarm thread can run at time.
	 * 
	 * */
	public ImportAlarmTask() {

		propertiesContainer = PropertiesContainer.getInstance();
		alarmFacade = new AlarmFacade();

	}

	/**
	 * This method initialise the process and threads for active and new alarm.
	 * 
	 * 
	 * */
	public void doImport() throws AdapterException, SmartsException {
		try {

			listeningAlarmsListener = new ListeningAlarmsListener();
			activeAlarmsListener = new ActiveAlarmsListener();
			activeAlarmsList.addActiveListListener(activeAlarmsListener);
			listeningAlarmsList.addActiveListListener(listeningAlarmsListener);

			activeAlarmsList.setActive(false);
			activeAlarmsList.clear();
			activeAlarmsList.setActive(true);

			// call AlarmFacade code
			alarmFacade.readAlarm(activeAlarmsList, listeningAlarmsList);
		} catch (AdapterException e) {
			activeAlarmsList.removeActiveListListener(activeAlarmsListener);
			listeningAlarmsList
					.removeActiveListListener(listeningAlarmsListener);

			throw e;
		}

	}

	class ActiveAlarmsListener implements ActiveListListener {

		long notificationId = 0;
		String[] domainManager = null;
		int maxAlarmsInQueue = 1000;
		ThreadPoolExecutor[] executorPool = null;
		private int count = 0;

		public ActiveAlarmsListener() {
			count = 0;
			domainManager = propertiesContainer.getProperty("AM_DOMAINS")
					.split(",");
			executorPool = new ThreadPoolExecutor[domainManager.length];
			String numberMax = PropertiesContainer.getInstance().getProperty(
					"MAX_NUMBER_ALARMS_QUEUE");
			if (numberMax != null) {
				maxAlarmsInQueue = Integer.parseInt(numberMax);
			}

			for (int i = 0; i < domainManager.length; i++) {
				BlockingQueue<Runnable> worksQueue = new ArrayBlockingQueue<Runnable>(
						maxAlarmsInQueue);
				RejectedExecutionHandler executionHandler = new MyRejectedExecutionHandelerImpl();
				ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
						150L, TimeUnit.HOURS, worksQueue, executionHandler);
				executorPool[i] = executor;
			}
		}

		public void contentsChanged(ActiveListEvent event) {

			for (Alarm alarm : activeAlarmsList) {
				try {
					count++;
					for (int i = 0; i < domainManager.length; i++) {

						AlarmCallable alarmCallable = new AlarmCallable(alarm,
								domainManager[i], "active alarm number "
										+ count);
						// create a thread for each alarm
						executorPool[i].execute(new FutureTask<Alarm>(
								alarmCallable));

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error(" error processing alarm " + alarm.toString()
							+ ", skipped it");
				}

			}
			if (ClassFactory.getEnum() != EnumAdapterName.ALUFTTH){ 
			log.debug("starting processing new alarms...");
			propertiesContainer.setProperty("ALARM_LISTENING_START", "true");
			}
			activeAlarmsList.setActive(false);
			activeAlarmsList.clear();
			activeAlarmsList.setActive(true);

			// call method to check and instantiate alarm in smart

		}
	};

	class ListeningAlarmsListener implements ActiveListListener {

		String[] domainManager = null;
		ThreadPoolExecutor executor;
		int maxAlarmsInQueue = 1000;
		ThreadPoolExecutor[] executorPool = null;
		int count = 0;

		public ListeningAlarmsListener() {

			count = 0;
			domainManager = propertiesContainer.getProperty("AM_DOMAINS")
					.split(",");
			executorPool = new ThreadPoolExecutor[domainManager.length];
			String numberMax = PropertiesContainer.getInstance().getProperty(
					"MAX_NUMBER_ALARMS_QUEUE");
			if (numberMax != null) {
				maxAlarmsInQueue = Integer.parseInt(numberMax);
			}

			for (int i = 0; i < domainManager.length; i++) {
				BlockingQueue<Runnable> worksQueue = new ArrayBlockingQueue<Runnable>(
						maxAlarmsInQueue);
				RejectedExecutionHandler executionHandler = new MyRejectedExecutionHandelerImpl();
				ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
						150L, TimeUnit.HOURS, worksQueue, executionHandler);
				executorPool[i] = executor;
			}

		}

		public void contentsChanged(ActiveListEvent event) {
			log.debug("created thread to manage a new alarm ");
			Alarm alarm = listeningAlarmsList.get(event.getX());
			alarm.setActive(false);
			count++;
			for (int i = 0; i < domainManager.length; i++) {
				
				AlarmCallable alarmCallable = new AlarmCallable(alarm,
						domainManager[i], "new alarm number " + count);
				// create a thread for each alarm
				executorPool[i].execute(new FutureTask<Alarm>(alarmCallable));
			}

			synchronized (listeningAlarmsList) {
				listeningAlarmsList.setActive(false);
				listeningAlarmsList.remove(event.getX());
				listeningAlarmsList.setActive(true);
			}

		}
	};

	class MyRejectedExecutionHandelerImpl implements RejectedExecutionHandler {
		int maxAlarmsInQueue = 1000;

		public MyRejectedExecutionHandelerImpl() {

			String numberMax = PropertiesContainer.getInstance().getProperty(
					"MAX_NUMBER_ALARMS_QUEUE");
			if (numberMax != null) {
				maxAlarmsInQueue = Integer.parseInt(numberMax);
			}
		}

		public void rejectedExecution(Runnable runnable,
				ThreadPoolExecutor executor) {
			log
					.error("there are "
							+ maxAlarmsInQueue
							+ " alarms in the queue, maximum number has been reached, this alarm is discarded ");

		}
	}

}
