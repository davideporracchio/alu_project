package com.holonomix.alarm;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.ListeningAlarmInterface;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Timestamp;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class ListeningAlarmThread implements Callable<Object> {

	static int numberThread = 0;
	private static final Logger log = Logger
			.getLogger(ListeningAlarmThread.class);

	private ListeningAlarmInterface listeningAlarmClient;

	public ListeningAlarmThread(ListeningAlarmInterface listeningAlarmClient)
			throws AdapterException {
		this.listeningAlarmClient = listeningAlarmClient;

	}

	public Object call() throws AdapterException,SmartsException {
		numberThread++;
		while (numberThread > 1) {
			try {
				Thread.sleep(10000);
				//log.debug("checkpoint waiting 10");
			} catch (InterruptedException e) {

				log.error(e.getMessage());
			}
		}
		Thread.currentThread().setName("ListeningAlarmThread");
		final Timestamp timestamp = listeningAlarmClient.getTimestamp();

		Callable<String> monitorHeartBeat = new Callable<String>() {
			public String call() throws AdapterException, SmartsException{

				BrokerManager br = BrokerManager.getInstance();
				Thread.currentThread().setName("InnerThread_MonitorHeartBeat");
				boolean sendClearNotificationAlarm = true;
				// each time application starts it clears alarm notification
				if (sendClearNotificationAlarm) {
					sendClearNotificationAlarm = false;
					MapMonitor.getInstance().put("LISTENINGALARM", "clear");
				}
				PropertiesContainer.getInstance().setProperty("STOP_THREAD",
						"false");
				// until we have heartbeat from ems we are inside this loop
				while (PropertiesContainer.getInstance().getProperty(
						"STOP_THREAD").equalsIgnoreCase("false")) {
					try {
						// calculate seconds from last heartbeat and alarm
						int secondHeartbeat = Seconds.secondsBetween(
								timestamp.getDataTimeHeartbeat(),
								new DateTime()).getSeconds();
						int secondAlarm = Seconds.secondsBetween(
								timestamp.getDateTimeAlarm(), new DateTime())
								.getSeconds();

						log.debug("seconds from last heartbeat "
								+ secondHeartbeat);
						int timeoutHeartbeat = Integer
								.parseInt(PropertiesContainer.getInstance()
										.getProperty("HEARTBEAT_TIMEOUT"));
						int timeoutAlarm = Integer.parseInt(PropertiesContainer
								.getInstance().getProperty("ALARM_TIMEOUT"));
						// stop thread if we missed heartbeat
						if (secondHeartbeat > timeoutHeartbeat) {
							log
									.debug("missed HEARTBEAT message try to connect again");
							MapMonitor.getInstance().put("LISTENINGHEARTBEAT",
									"failed");
							listeningAlarmClient.disconnect();
							numberThread--;
							//davide
							//PropertiesContainer.getInstance().setProperty(
							//		"HEARTBEAT_TIMEOUT", "300");
							PropertiesContainer.getInstance().setProperty(
									"STOP_THREAD", "true");
							break;
						}
						// send notif after a while we do not see alarm
						if (secondAlarm > timeoutAlarm
								&& sendClearNotificationAlarm == false) {
							if (MapMonitor.getInstance().get("LISTENINGALARM") != null
									&& MapMonitor.getInstance().get(
											"LISTENINGALARM").equalsIgnoreCase(
											"clear")) {
								log.debug("no Alarms seen for " + secondAlarm
										+ " seconds");
								MapMonitor.getInstance().put("LISTENINGALARM",
										"failed");

								sendClearNotificationAlarm = true;
							}

						} else if (secondAlarm < timeoutAlarm
								&& sendClearNotificationAlarm == true) {
							log.debug("seen Alarms...");
							MapMonitor.getInstance().put("LISTENINGALARM",
									"clear");
							sendClearNotificationAlarm=false;

						}

						// check if ipam is still up and running

						String[] ipams = PropertiesContainer.getInstance()
								.getProperty("AM_DOMAINS").split(",");

						for (String ipam : ipams) {
							RemoteDomainManager remoteDomainManager = null;

							try {
								
								remoteDomainManager = br
										.getDomainManagerForAlarm(ipam, false);
								if (remoteDomainManager == null) {
									log.error("ipam "+ipam+" reported down by broker ");
									MapMonitor.getInstance().put("IPAM",
											"down, " + ipam);
								}

							} catch (SmartsException e) {
								log.error("error check ipam status "
										+ e.getMessage());
								
								
								MapMonitor.getInstance().put("IPAM",
										"down, " + ipam);
							}
						}
						//check OI status
						ipams = PropertiesContainer.getInstance().getProperty(
								"OI_INSTANCES").split(",");
						for (String oi : ipams) {
							RemoteDomainManager remoteDomainManager = null;

							try {
								remoteDomainManager = br
										.getDomainManagerForAlarm(oi, false);
								if (remoteDomainManager == null) {
									MapMonitor.getInstance().put("OI",
											"down, " + oi);
								}

							} catch (SmartsException e) {
								log.error("error check ipam status "
										+ e.getMessage());
								MapMonitor.getInstance().put("OI",
										"down, " + oi);
								
							}
						}

						// sleep 30 seconds
						Thread.sleep(30000);

					} catch (InterruptedException e) {
						log.error("Interrupted timestamp check thread");

					}
				}
				//stop thread
				if (PropertiesContainer.getInstance()
						.getProperty("STOP_THREAD").equalsIgnoreCase("TRUE")) {
					
					listeningAlarmClient.disconnect();
					//log.debug("checkpoint99");
					//davide1509
					listeningAlarmClient=null;
					numberThread--;
					//throw new AdapterException(1);
					if (PropertiesContainer.getInstance().getProperty("ADAPTER_NAME").equalsIgnoreCase(
							"ALU-FTTH")||PropertiesContainer.getInstance().getProperty("ADAPTER_NAME").equalsIgnoreCase(
									"ZTE-MSAN")){
						
							ClassFactory.deleteListeningAlarmInstance();
							
							//throw new AdapterException(1);
					}
				}
				return null;
			}

		};

		FutureTask future = new FutureTask(monitorHeartBeat);
		ExecutorService es = Executors.newSingleThreadExecutor();
		//log.debug("checkpoint100");
		try {
			Future f = es.submit(future);
			listeningAlarmClient.connect();
			//davide 2309
			//log.debug("checkpoint101");
			//f.get();
		} catch (AdapterException e) {
			es.shutdownNow();
			//log.debug("checkpoint102");
			numberThread--;
			throw e;
		/*} catch (InterruptedException e) {
			es.shutdownNow();
			numberThread--;
			throw new AdapterException();
		} catch (ExecutionException e) {
			es.shutdownNow();
			numberThread--;
			throw new AdapterException();
		}*/
		}
		//log.debug("checkpoint103");
		return null;
	}

}
