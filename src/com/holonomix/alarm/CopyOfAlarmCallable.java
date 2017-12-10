package com.holonomix.alarm;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.alarm.service.AlarmLogicService;
import com.holonomix.commoninterface.AlarmMappingFileInterface;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.log.alarm.AlarmLog;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class CopyOfAlarmCallable implements Callable<Alarm> {
	private static final Logger log = Logger.getLogger(CopyOfAlarmCallable.class);

	Alarm alarm;
	private BrokerManager brokerManager;
	private String domain;
	private PropertiesContainer propertiesContainer;
	private boolean isIpamDown = false;

	private String count;

	public CopyOfAlarmCallable(Alarm alarm, String domain, String count) {
		this.count = count;
		this.alarm = alarm;
		this.domain = domain;
		propertiesContainer = PropertiesContainer.getInstance();

	}

	public Alarm call() throws AdapterException {

		Thread.currentThread().setName("" + domain);
		while (alarm != null) {
			// this loop is done to wait that all active alarms are processed
			// before new alarms
			while (!alarm.isActive()
					&& (propertiesContainer
							.getProperty("ALARM_LISTENING_START") == null || propertiesContainer
							.getProperty("ALARM_LISTENING_START")
							.equalsIgnoreCase("false"))) {
				try {
					log.debug("a  new alarm (id "+alarm.getName()+") is waiting to be preocessed...");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.error("interrupted thread alarm");
				}
			}
			try {

				log.debug("-------------------------------------domain "
						+ domain + "  count " + count + "  alarm id ="
						+ alarm.getName());
				AlarmLog.printAlarmStatus(alarm);
				AlarmMappingFileInterface alarmMappingFileInterface = ClassFactory
						.getAlarmMappingFileInstance();
				// find instruction for this alarm
				String[] instruction = alarmMappingFileInterface
						.findInstructionForAlarm(alarm);
				int sentToIpam = 0;
				brokerManager = BrokerManager.getInstance();
				if (instruction != null) {

					RemoteDomainManager remoteDomainManager = null;

					while (remoteDomainManager == null) {
						remoteDomainManager = brokerManager
								.getDomainManagerForAlarm(domain, true);
						try {
							if (remoteDomainManager == null) {
								isIpamDown = true;
								MapMonitor.getInstance().put("IPAM",
										"down, " + domain);
								Thread.sleep(Integer.parseInt(propertiesContainer
										.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
							}
						} catch (InterruptedException e1) {

							log.error("interrupted thread alarm");
						}

					}

					if (remoteDomainManager != null) {
						if (isIpamDown) {
							MapMonitor.getInstance().put("IPAM",
									"up, " + domain);
						}
						AlarmLogicService alarmLogicService = new AlarmLogicService(
								remoteDomainManager);

						//sentToIpam = alarmLogicService.sendAlarm(alarm,
						//		instruction);

					}
				} else {
					// if we have not recursive alarm we need to understand the
					// device type to send the OI notif
					if (ClassFactory.getEnum() == EnumAdapterName.HWZTE
							|| ClassFactory.getEnum() == EnumAdapterName.HWMETE) {
						RemoteDomainManager remoteDomainManager = null;

						while (remoteDomainManager == null) {
							remoteDomainManager = brokerManager
									.getDomainManagerForAlarm(domain, true);
							try {
								if (remoteDomainManager == null) {
									isIpamDown = true;
									MapMonitor.getInstance().put("IPAM",
											"down, " + domain);
									Thread.sleep(Integer.parseInt(propertiesContainer
											.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
								}
							} catch (InterruptedException e1) {

								log.error("interrupted thread alarm");
							}

						}

						if (remoteDomainManager != null) {
							if (isIpamDown) {
								MapMonitor.getInstance().put("IPAM",
										"up, " + domain);
							}
							AlarmLogicService alarmLogicService = new AlarmLogicService(
									remoteDomainManager);
							Device device = new Device();
							device.setName(alarm.getDeviceName());
							alarmLogicService.findInfoDevice(device);
							if (device != null) {
								alarm.setDeviceType(device
										.getCreationClassName());
							}

						}
					}

				}
				// sentToIpam can be:
				// 0 = object not found
				// 1 = object found
				// 2 = device is not present in this ipam

				if (sentToIpam == 1) {
					log.debug(" injected alarm "
							+ alarm.getProbableCauseQualifier() + " "
							+ alarm.getName() + " " + alarm.getDeviceName()
							+ " " + alarm.getComponent() + " to ipam " + domain);
					return null;
				} else if (sentToIpam == 2) {
					log.warn(" device " + alarm.getDeviceName()
							+ " is not present in ipam " + domain);
					return null;

				} else {
					// object not found push event in OI
					alarm.setSpecificNumber("1");
					if (instruction != null
							&& instruction[1].equalsIgnoreCase("UP"))
						alarm.setSpecificNumber("2");
					else if (instruction == null) {
						if (alarm.getSeverity().equalsIgnoreCase("5"))
							alarm.setSpecificNumber("2");
					}

					String domainManagerOI[] = propertiesContainer.getProperty(
							"OI_INSTANCES").split(",");
					RemoteDomainManager[] remoteDomainManagerOI = new RemoteDomainManager[domainManagerOI.length];
					AlarmLogicService[] alarmLogicServiceOI = new AlarmLogicService[domainManagerOI.length];
					boolean[] foundInIpamOI = new boolean[domainManagerOI.length];
					for (int i = 0; i < domainManagerOI.length; i++) {

						remoteDomainManagerOI[i] = brokerManager
								.getDomainManagerForAlarm(domainManagerOI[i],
										false);
						if (remoteDomainManagerOI[i] != null) {
							alarmLogicServiceOI[i] = new AlarmLogicService(
									remoteDomainManagerOI[i]);
							// push event in OI

							foundInIpamOI[i] = alarmLogicServiceOI[i]
									.pushEvent(alarm);

							log.info("sent notification "
									+ alarm.getProbableCauseQualifier() + " "
									+ alarm.getName() + " "
									+ alarm.getDeviceName() + " "
									+ alarm.getComponent() + " to OI system "
									+ domainManagerOI[i]);
						}
					}
					int countAlarmCreated = 0;
					for (int i = 0; i < domainManagerOI.length; i++) {
						if (foundInIpamOI[i] == true) {
							countAlarmCreated++;
						}
					}
					if (countAlarmCreated == 0) {
						log.error("error in sending  massage in both OI system "
								+ alarm.toString());
					} else {
						// alarm created
						return null;
					}

				}

			} catch (SmartsException e) {

				log.error("error in AlarmCallable " + e.getMessage());

			}
		}
		return null;
	}

}
