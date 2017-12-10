package com.holonomix.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.alarm.service.AlarmLogicService;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.monitor.ActiveMapEvent;
import com.holonomix.monitor.ActiveMapListener;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class MonitorListener implements ActiveMapListener {

	MapMonitor mapMonitor;

	
	private static final Logger log = Logger.getLogger(MonitorListener.class);
	private PropertiesContainer propertiesContainer;

	public MonitorListener(MapMonitor mapMonitor) {
		this.mapMonitor = mapMonitor;
		propertiesContainer = PropertiesContainer.getInstance();
	}

	public void contentsChanged(ActiveMapEvent event) {
	
		List<Alarm> listAlarm = new ArrayList<Alarm>();
		// create the alarm to send to OI
		//and return the exit number to do fail over
		int exitNumber = createAlarm(event.getEventName(), listAlarm);
		for(Alarm alarm: listAlarm){
		if (alarm != null) {
			int countAlarmCreated = 0;
			BrokerManager brokerManager = BrokerManager.getInstance();
			try {
				String domainManagerOI[] = propertiesContainer.getProperty(
						"OI_INSTANCES").split(",");
				RemoteDomainManager[] remoteDomainManagerOI = new RemoteDomainManager[domainManagerOI.length];
				AlarmLogicService[] alarmLogicServiceOI = new AlarmLogicService[domainManagerOI.length];
				boolean[] foundInIpamOI = new boolean[domainManagerOI.length];
				for (int i = 0; i < domainManagerOI.length; i++) {

					remoteDomainManagerOI[i] = brokerManager
							.getDomainManagerForAlarm(domainManagerOI[i],false);
					if(remoteDomainManagerOI[i]!=null){
					alarmLogicServiceOI[i] = new AlarmLogicService(
							remoteDomainManagerOI[i]);
					foundInIpamOI[i] = alarmLogicServiceOI[i].pushEventFailOver(alarm);
					}
				}

				countAlarmCreated = 0;
				for (int i = 0; i < domainManagerOI.length; i++) {
					if (foundInIpamOI[i] == true) {
						countAlarmCreated++;
					}
				}

			} catch (SmartsException e) {
				log.error(" error pushing message to OI");
			}
		}
		}
		//if exit number >0 shut down the adapter
		//10 is ems error
		//20 is smarts error
		if (exitNumber>0){
			
			final int number = exitNumber;
			 Runnable r=new Runnable()  {
			    public void run()
			    {
			    	try {
						Thread.sleep(10000);
						log.debug("shut down application. Exit code: "+number);
						System.exit(number);
					} catch (InterruptedException e) {
						
						log.error("thread interrupted");
					}
			   
			    }
			    };
			    Thread t=new Thread(r);
			    t.start();
			
		}

	}

	private int createAlarm(String eventName,List<Alarm> listAlarm) {
		
		//10 is ems error
		//20 is smarts error
		int exitNumber = 0;
		
		if (eventName.equalsIgnoreCase("IPAM")){
			String value = mapMonitor.get("IPAM");
			String arrayValue[] = value.split(",");
			Alarm alarm = new Alarm();
			
			alarm.setEms(arrayValue[1].trim());
			alarm.setName("");
			alarm.setDescription("Primary_OR_Secondary");
			if (arrayValue[0].equalsIgnoreCase("up")){
				alarm.setSpecificNumber("7");
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 7 to OI [Primary_OR_Secondary-up]");
			}
			else {
				alarm.setSpecificNumber("6");
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 6 to OI [Primary_OR_Secondary-down]");
				exitNumber=20;
			}
			/* removed to fix issue 312
			Alarm alarm2 = new Alarm();
			alarm2.setEms(arrayValue[1].trim());
			alarm2.setName("");
			alarm2.setDescription("SECONDARY");
			if (arrayValue[0].equalsIgnoreCase("up")){
				alarm2.setSpecificNumber("7");
				log.debug("send 'session ipam "+alarm2.getEms()+"' with specific number 7 to OI [secondary-up]");
			}
			else {
				alarm2.setSpecificNumber("6");
				log.debug("send 'session ipam "+alarm2.getName()+"' with specific number 6 to OI [secondary-down]");
				exitNumber=20;
			}
			listAlarm.add(alarm2);*/
			listAlarm.add(alarm);
			
		}
		if (eventName.equalsIgnoreCase("OI")){
			String value = mapMonitor.get("OI");
			String arrayValue[] = value.split(",");
			Alarm alarm = new Alarm();
			
			alarm.setEms(arrayValue[1].trim());
			alarm.setName("");
			alarm.setDescription("PRIMARY");
			if (arrayValue[0].equalsIgnoreCase("up")){
				alarm.setSpecificNumber("7");
				log.info("OI up");
			}
			else {
				alarm.setSpecificNumber("6");
				log.debug("OI down");
				exitNumber=20;
			}
			listAlarm.add(alarm);
			
			
		}
		else if (eventName.equalsIgnoreCase("IPAM_PRIMARY")){
			String value = mapMonitor.get("IPAM_PRIMARY");
			String arrayValue[] = value.split(",");
			Alarm alarm = new Alarm();
			alarm.setName(arrayValue[1].split(":")[0]);
			alarm.setEms(arrayValue[1].split(":")[1]);
			alarm.setDescription("PRIMARY");
			if (arrayValue[0].equalsIgnoreCase("up")){
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 7 to OI [primary-up]");
				alarm.setSpecificNumber("7");
			}
			else {
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 6 to OI [primary-down]");
				alarm.setSpecificNumber("6");
				
			}
				
			listAlarm.add(alarm);
		}
		else if (eventName.equalsIgnoreCase("IPAM_SECONDARY")){
			String value = mapMonitor.get("IPAM_SECONDARY");
			String arrayValue[] = value.split(",");
			Alarm alarm = new Alarm();
			alarm.setName(arrayValue[1].split(":")[0]);
			alarm.setEms(arrayValue[1].split(":")[1]);
			
			alarm.setDescription("SECONDARY");
			if (arrayValue[0].equalsIgnoreCase("up")){
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 7 to OI [secondary-up]");
				alarm.setSpecificNumber("7");
			}
			else {
				
				log.debug("send 'session ipam "+alarm.getName()+"' with specific number 6 to OI [secondary-down]");
				alarm.setSpecificNumber("6");
				
				}
			listAlarm.add(alarm);
				
		}
		else if (eventName.equalsIgnoreCase("ALARM")){
		String value = mapMonitor.get("ALARM");
		Alarm alarm = new Alarm();
		alarm.setEms(PropertiesContainer.getInstance().getProperty("EMS_IP"));
		alarm.setProtocol(ClassFactory.getProtocol());
		alarm.setDescription(PropertiesContainer.getInstance().getProperty("EMS_DESC"));
		alarm.setSpecificNumber("4");
		if (value != null && value.equalsIgnoreCase("failed")) {
			log.debug("send 'session ems "+alarm.getEms()+"' with specific number 4 to OI [EMS-failed]");
			alarm.setSpecificNumber("4");
			exitNumber=10;
			
		}
		else if (value != null && value.equalsIgnoreCase("clear")) {
			log.debug("send 'session ems "+alarm.getEms()+"' with specific number 5 to OI [EMS-clear]");
			alarm.setSpecificNumber("5");
		}
		listAlarm.add(alarm);
		
		}
		else if (eventName.equalsIgnoreCase("LISTENINGALARM")){
			String value = mapMonitor.get("LISTENINGALARM");
			int timeoutAlarm=(Integer.parseInt(PropertiesContainer.getInstance().getProperty("ALARM_TIMEOUT")))/60;
			Alarm alarm = new Alarm();
			alarm.setEms(PropertiesContainer.getInstance().getProperty("EMS_IP"));
			alarm.setSpecificNumber("9");
			alarm.setDescription(timeoutAlarm+"");
			if (value != null && value.equalsIgnoreCase("failed")) {
				log.debug("send message to OI no Alarm seen for "+timeoutAlarm+" minutes [failed]");
				alarm.setSpecificNumber("8");
				
				
			}
			else if (value != null && value.equalsIgnoreCase("clear")) {
				log.debug("send message to OI no Alarm seen for "+timeoutAlarm+" minutes [clear]");
				alarm.setSpecificNumber("9");
			}
			listAlarm.add(alarm);
			
			}
		
		else if (eventName.equalsIgnoreCase("NODEVICE")){
			String value = mapMonitor.get("NODEVICE");
			
			Alarm alarm = new Alarm();
			
			alarm.setSpecificNumber("3");
			alarm.setDescription(value);
			log.debug("send message to OI device "+ value+" has not ipam linked in file "+propertiesContainer
						.getProperty("MULTI_DOMAIN_MAPPING_FILE"));
				
			listAlarm.add(alarm);
			
			}
		
		else if (eventName.equalsIgnoreCase("TOPOLOGY_NOFILES")){
			String value = mapMonitor.get("TOPOLOGY_NOFILES");
			String days=PropertiesContainer.getInstance().getProperty("DAYS_WITHOUT_FILES");
			Alarm alarm = new Alarm();
			alarm.setEms(PropertiesContainer.getInstance().getProperty("EMS_IP"));
			
			alarm.setDescription(days+"");
			if (value != null && value.equalsIgnoreCase("failed")) {
				log.debug("send message to OI no new topology files seen for "+days+" days [failed]");
				alarm.setSpecificNumber("12");
				
				
			}
			else if (value != null && value.equalsIgnoreCase("clear")) {
				log.debug("send message to OI no new topology files seen for "+days+" days [clear]");
				alarm.setSpecificNumber("13");
			}
			listAlarm.add(alarm);
		}
			else if (eventName.equalsIgnoreCase("TOPOLOGY_FILEMISSING")){
				String value = mapMonitor.get("TOPOLOGY_FILEMISSING");
				
				Alarm alarm = new Alarm();
				alarm.setEms(PropertiesContainer.getInstance().getProperty("EMS_IP"));
				
				
				if (value != null && value.equalsIgnoreCase("failed")) {
					log.debug("send message to OI list files incomplete [failed]");
					alarm.setSpecificNumber("10");
					
					
				}
				else if (value != null && value.equalsIgnoreCase("clear")) {
					log.debug("send message to OI list files incomplete [clear]");
					alarm.setSpecificNumber("11");
				}
				listAlarm.add(alarm);
				
			}
		
		return exitNumber;
	}
}