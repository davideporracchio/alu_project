package com.holonomix.alarm;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.alarm.service.AlarmLogicService;
import com.holonomix.commoninterface.AlarmMappingFileInterface;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.IgnoreDeviceFileService;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.TopologyMemoryTask;

public class AlarmCallable implements Callable<Alarm> {
	private static final Logger log = Logger.getLogger(AlarmCallable.class);

	Alarm alarm;
	private BrokerManager brokerManager;
	private String domain;
	private PropertiesContainer propertiesContainer;
	private boolean isIpamDown = false;

	private String count;

	public AlarmCallable(Alarm alarm, String domain, String count) {
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
					log.debug("a new alarm (specificNumber " + alarm.getSpecificNumber()
							+ ") is waiting to be processed...");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.error("interrupted thread alarm");
				}
			}
			try {
				int status = 0;
				String[] instruction = null;
				log.debug("----------------domain "
						+ domain + "  count " + count );
				if(IgnoreDeviceFileService.getInstance(
						propertiesContainer.getProperty("SMARTS_DEVICES_TO_IGNORE")).isValidDevice(alarm.getDeviceName())) 
				{	
				AlarmMappingFileInterface alarmMappingFileInterface = ClassFactory
						.getAlarmMappingFileInstance();
				// find instruction for this alarm

				instruction = alarmMappingFileInterface
						.findInstructionForAlarm(alarm);
				//alarm.setDeviceName(");
				brokerManager = BrokerManager.getInstance();
				RemoteDomainManager remoteDomainManager = null;
				log.debug(count+" domain "+domain +" requesting reference from broker");
				while (remoteDomainManager == null) {
					try{
					remoteDomainManager = brokerManager
							.getDomainManagerForAlarm(domain, true);
					}catch(java.lang.SecurityException e){
						
						try {
						isIpamDown = true;
						MapMonitor.getInstance().put("IPAM",
								"down, " + domain);
						Thread.sleep(Integer.parseInt(propertiesContainer
								.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
						} catch (InterruptedException e1) {

							log.error(count+" interrupted thread alarm");
						}
					}
					
					try {
						if (remoteDomainManager == null) {
							isIpamDown = true;
							MapMonitor.getInstance().put("IPAM",
									"down, " + domain);
							Thread.sleep(Integer.parseInt(propertiesContainer
									.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
						}
					} catch (InterruptedException e1) {

						log.error(count+" interrupted thread alarm");
					}

				}
				log.debug(count+" domain "+domain +" completed reference from broker");
				if (remoteDomainManager != null) {
					if (isIpamDown) {
						MapMonitor.getInstance().put("IPAM", "up, " + domain);
					}

					AlarmLogicService alarmLogicService = new AlarmLogicService(
							remoteDomainManager);

					// alarm for ipam
					if (instruction != null) {
						log.debug(count+" domain "+domain +" RCA alarm ");	
						//new code 
						List<String> ipamList =TopologyMemoryTask.getInstance().getIpamsForDevice(alarm.getDeviceName());
						if(ipamList.contains(domain)){
							log.debug(count+" domain "+domain +" device found in lookup table or ipam ");	
							status = alarmLogicService
							.sendAlarmTimeout(alarm, instruction);
							if(status==2){
								String condition = alarm.getCondition()+"-"+alarm.getSeverity();
								log.warn(count+" device: "+alarm.getDeviceName()+" is not present in ipam: "+domain+ " but is present in lookup table, condition= "+condition +" component "+alarm.getComponent());
							}else{
								String condition = alarm.getCondition()+"-"+alarm.getSeverity();
								log.warn(count+" device: "+alarm.getDeviceName()+" is present in ipam: "+domain+ " and present in lookup table, condition= "+condition +" component "+alarm.getComponent());
				
								
							}
						}
						else if(ipamList.size()==0){
							String condition = alarm.getCondition()+"-"+alarm.getSeverity();
							log.warn(count+" device: "+alarm.getDeviceName()+" is not present in lookup table, condition= "+condition +" component "+alarm.getComponent());
							status = alarmLogicService
							.sendAlarmTimeout(alarm, instruction);
							
						
						
						}else {
								
							String condition = alarm.getCondition()+"-"+alarm.getSeverity();
							log.warn(count+" device: "+alarm.getDeviceName()+" is not present in lookup table for ipam: "+domain +" condition= "+condition +" component "+alarm.getComponent());
							status = 2;
						}
					} else {
						log.debug(count+" domain "+domain +" OI alarm");
						Device device = new Device();
						device.setName(alarm.getDeviceName());
						try{
							log.debug(count+" checking info device in ionix");
						alarmLogicService.findInfoDevice(device);
						log.debug(count+" completed checking info device in ionix");
						if (device==null || device.getCreationClassName().equalsIgnoreCase("Unknown")){
							
							String condition = alarm.getCondition()+"-"+alarm.getSeverity();
							log.debug(count+" domain "+domain +" OI alarm, device is not present in IPAM, condition= "+condition +" component "+alarm.getComponent());
						status =4;
						}
						else if (device != null ) {
							
							String condition = alarm.getCondition()+"-"+alarm.getSeverity();
							log.debug(count+" domain "+domain +" OI alarm, device is present in IPAM, condition= "+condition +" component "+alarm.getComponent());
							status=3;
							alarm.setDeviceType(device.getCreationClassName());
						}
						}catch(SmartsException e){
							log.error(count+" error checking info device in ionix "+e.getMessage());
							if (e.getTypeError()==3){
								status =  Integer.parseInt(propertiesContainer
										.getProperty("IPAM_CHECKOBJECT_DEFAULTSTATUS"));
							
							}else {
								throw e;
							
							}
						}
						

					}
				}
				}else{
					status =5;
				}
				// sentToIpam can be:
				// 0 = IPAM card/interface/port object not found
				// 1 = IPAM card/interface/port object found
				// 2 = IPAM device is not present in this ipam
				// 3 = OI device found 
				// 4 = OI device not found 
				// 5 = device to ignore

				// a) Alarm is for IPAM & Object exists in IPAM - change IPAM
				// object status, send Alarm ID, thread ID & status=0 to Master
				// thread
				// b) Alarm is for IPAM & Object does not exist in IPAM - send
				// Alarm ID, thread ID & status=1 to Master thread
				// c) Alarm is for OI & Object exists in IPAM - send Alarm ID,
				// thread ID & status=2 to Master thread
				// d) Alarm is for OI & Object does not exist in IPAM - send
				// Alarm ID, thread ID & status=3 to Master thread
				if (status == 1) {
					log.debug(" injected alarm "
							+ alarm.getProbableCauseQualifier() + " "
							+ alarm.getName() + " " + alarm.getDeviceName()
							+ " " + alarm.getComponent() + " to ipam " + domain);
					Alarm alarmTemp = alarm.clone();
					alarmTemp.setId(1L);
					AlarmMonitor.getInstance().sendInfoAlarm(count,domain, alarmTemp);
					return null;
				} else if (status == 2) {
					log.warn(" device " + alarm.getDeviceName()
							+ " is not present in ipam " + domain);
					Alarm alarmTemp = alarm.clone();
					alarmTemp.setId(2L);
					AlarmMonitor.getInstance().sendInfoAlarm(count,domain, alarmTemp);
					return null;

				} else if (status == 0 || status ==4){
					if (status==4){
					log.warn(" device " + alarm.getDeviceName()
							+ " is not present in ipam " + domain);
					}else {
						log.warn(" component " + alarm.getComponent()
								+ " is not present in ipam " + domain);
						
					}
					// object not found push event in OI
					alarm.setSpecificNumber("1");
					if (instruction != null
							&& instruction[1].equalsIgnoreCase("UP"))
						alarm.setSpecificNumber("2");
					else if (instruction == null) {
						if (alarm.getSeverity().equalsIgnoreCase("5"))
							alarm.setSpecificNumber("2");
					}
					Alarm alarmTemp = alarm.clone();
					alarmTemp.setId(new Long(status));
					AlarmMonitor.getInstance().sendInfoAlarm(count,domain, alarmTemp);
					return null;
				}else if (status == 3  ){
					log.warn(" device found " + alarm.getDeviceName()
							+ " in ipam " + domain);
					
					// no instruction but device in ipam
					alarm.setSpecificNumber("1");
					if (alarm.getSeverity().equalsIgnoreCase("5")){
							alarm.setSpecificNumber("2");
					}
					Alarm alarmTemp = alarm.clone();
					alarmTemp.setId(new Long(status));
				
					AlarmMonitor.getInstance().sendInfoAlarm(count,domain, alarmTemp);
					return null;
				}
				else if (status == 5  ){
					log.warn(" device found " + alarm.getDeviceName()
							+ " in the ignore list");
					return null;
				}
			} catch (SmartsException e) {
				log.debug("error in AlarmCallable " + alarm.toString() +", error: "+e.getMessage());
				//log.error("error in AlarmCallable " + e.getMessage());
				return null;

			}
		}
		return null;
	}

}
