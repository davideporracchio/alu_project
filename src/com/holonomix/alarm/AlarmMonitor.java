package com.holonomix.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.holonomix.alarm.service.AlarmLogicService;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.log.alarm.AlarmLog;
import com.holonomix.properties.PropertiesContainer;

public class AlarmMonitor {
	private static AlarmMonitor alarmMonitor;

	private static final Logger log = Logger.getLogger(AlarmMonitor.class);

	private Map<String, List<Alarm>> mapIdAlarmAction;

	private int numberIpams;
	private BrokerManager brokerManager;
	private PropertiesContainer propertiesContainer;

	private AlarmMonitor() {
		mapIdAlarmAction = new HashMap<String, List<Alarm>>();
		propertiesContainer = PropertiesContainer.getInstance();
		numberIpams = propertiesContainer.getProperty("AM_DOMAINS").split(",").length;

	}

	public static AlarmMonitor getInstance() {
		if (alarmMonitor == null)
			alarmMonitor = new AlarmMonitor();
		return alarmMonitor;
	}

	public static void clean() {
		if (alarmMonitor != null){
			alarmMonitor.mapIdAlarmAction.clear();	
		alarmMonitor = null;
		}
	}

	public synchronized void sendInfoAlarm(String alarmId,String ipam, Alarm alarm)
			throws  SmartsException {
		
		log.debug("Ipam ="+ipam +" sent " + alarmId
				+ " with probableCauseQualifier ="+alarm.getProbableCauseQualifier() +" and process status =" + alarm.getId().intValue());
		List<Alarm> listStatus = new ArrayList<Alarm>();

		if (this.mapIdAlarmAction.containsKey(alarmId)) {
			// check if exist in the map
			listStatus = this.mapIdAlarmAction.get(alarmId);
		} else {
			this.mapIdAlarmAction.put(alarmId, listStatus);
		}
		// add new status
		listStatus.add(alarm);
		log.debug("key "+alarmId + " size list = "+listStatus.size());
		
		// check if number of status is equal number ipams
		if (listStatus.size() == numberIpams) {

			log.debug("ready to compare status for id "+alarmId);
			
			if(isFoundInOneIpam(listStatus)){
				mapIdAlarmAction.remove(alarmId);
				return;
			}else  if(isDeviceInIpamButNotTheCardOrInterface(listStatus)){
				
					//send to OI
				log.debug("ready to send message to OI for alarm "+alarmId);
				Alarm alarmToSend = messageToOI(listStatus);
				if (alarmToSend!=null){
					sendMessageToOI(alarmToSend,alarmId);
					
				}
				mapIdAlarmAction.remove(alarmId);
				return;
			}
			else {
			
				//log
				log.debug("no device found, print message to file for alarm "+alarmId);
				AlarmLog.printNoDeviceFound(alarm,alarmId);
				mapIdAlarmAction.remove(alarmId);
				return;
			}
			
			
			
			
		}else if (listStatus.size() > numberIpams){
			log.debug("list is bigger than number of ipams! ERROR");
			
		}
	}

	// i) if status includes a 2 (i.e. Alarm is for OI & Object exists in at
	// least 1 IPAM) then send Alarm to OI
	// ii) if status is anything else then drop the Alarm
	private Alarm messageToOI(List<Alarm> listStatus) {
		for (Alarm alarm : listStatus) {

			if (alarm.getId().intValue() == 3 || alarm.getId().intValue() ==0)
				return alarm;
			
		}
		return null;
	
	}
	
	private boolean isFoundInOneIpam(List<Alarm> listStatus) {
		
		boolean flag=false;
		for (Alarm alarm : listStatus) {

			if (alarm.getId().intValue() == 1 )
				flag= true;
			
		}
		return flag;
	
	}
	
	private boolean isNoDeviceFoundAnyWhere(List<Alarm> listStatus) {
		// if true skip everything
		boolean flag=false;
		for (Alarm alarm : listStatus) {

			if (alarm.getId().intValue() != 2 )
				flag= true;
			
		}
		return flag;
	
	}
	
	private boolean isNoDeviceFoundAndNotRCAAlarm(List<Alarm> listStatus) {
		
		boolean flag=false;
		for (Alarm alarm : listStatus) {

			if (alarm.getId().intValue() != 4 )
				flag= true;
			
		}
		return flag;
	
	}
	
	
	private boolean isDeviceInIpamButNotTheCardOrInterface(List<Alarm> listStatus) {
		// status 0 send to OI
		boolean flag=false;
		for (Alarm alarm : listStatus) {

			if (alarm.getId().intValue() == 0 || alarm.getId().intValue() == 3)
				flag= true;
			
		}
		return flag;
	
	}

	private void sendMessageToOI(Alarm alarm,String alarmId) throws SmartsException {
		String domainManagerOI[] = propertiesContainer.getProperty(
				"OI_INSTANCES").split(",");
		RemoteDomainManager[] remoteDomainManagerOI = new RemoteDomainManager[domainManagerOI.length];
		AlarmLogicService[] alarmLogicServiceOI = new AlarmLogicService[domainManagerOI.length];
		boolean[] foundInIpamOI = new boolean[domainManagerOI.length];
		brokerManager = BrokerManager.getInstance();
		for (int i = 0; i < domainManagerOI.length; i++) {

			remoteDomainManagerOI[i] = brokerManager.getDomainManagerForAlarm(
					domainManagerOI[i], false);
			if (remoteDomainManagerOI[i] != null) {
				alarmLogicServiceOI[i] = new AlarmLogicService(
						remoteDomainManagerOI[i]);
				// push event in OI

				foundInIpamOI[i] = alarmLogicServiceOI[i].pushEvent(alarm);

				log.info("sent notification "+alarmId +", "
						+ alarm.getProbableCauseQualifier() + " "
						+ alarm.getName() + " " + alarm.getDeviceName() + " "
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
			
		}



	}

}
