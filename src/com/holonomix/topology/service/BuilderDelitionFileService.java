package com.holonomix.topology.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.utility.EMSDeviceService;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class BuilderDelitionFileService {
	private static final Logger log = Logger.getLogger(BuilderDelitionFileService.class);
	private PropertiesContainer propertiesContainer;
	private Map<String, List<String>> mapDeviceIpam = new HashMap<String, List<String>>();
	private static BuilderDelitionFileService builderDelitionFileService;
	private Map<String,Device> mapDeviceEms = new HashMap<String, Device>();
	
	public static BuilderDelitionFileService getInstance(){
	 
		if (builderDelitionFileService==null){
			builderDelitionFileService=new BuilderDelitionFileService();
		}
		builderDelitionFileService.mapDeviceEms.clear();
		builderDelitionFileService.mapDeviceIpam.clear();
		return builderDelitionFileService;
	}
	
	private BuilderDelitionFileService() {

		propertiesContainer = PropertiesContainer.getInstance();

	}
	
	public void saveDevice(Device device){
		mapDeviceEms.put(device.getName(), device);
		
	}
	//create delete file
	
	public void buildFile() throws SmartsException {

		BrokerManager brokerManager = BrokerManager.getInstance();
		String ipamArray[] = propertiesContainer.getProperty("AM_DOMAINS")
				.split(",");
		for (String domain : ipamArray) {
			RemoteDomainManager remoteDomainManager = null;
			while (remoteDomainManager == null) {
				remoteDomainManager = brokerManager.getDomainManagerForAlarm(
						domain, true);
				try {
					if (remoteDomainManager == null) {

						MapMonitor.getInstance().put("IPAM", "down, " + domain);
						Thread.sleep(Integer.parseInt(propertiesContainer
								.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
					}
				} catch (InterruptedException e1) {
					
					log.error("interrupted thread alarm");
				}

			}

			SmartsReadService smartsReadService = new SmartsReadService(
					remoteDomainManager);
			

			Map<String, Device> map = 	smartsReadService.readingSmartsDevice();

			for (String deviceName: map.keySet()){
				Device device = map.get(deviceName);
				deviceName = device.getCreationClassName()+" "+deviceName;
				List<String> listIpams=new ArrayList<String>();
				if(mapDeviceIpam.containsKey(deviceName)){
					listIpams=mapDeviceIpam.get(deviceName);
				}
				listIpams.add(domain);
				mapDeviceIpam.put(deviceName,listIpams);
				
			}			

			
			
			
		}
		
		compareEmsAndIpam();

	}
	// compare ems and ipam devices and writes the list of devices to delete 
	private void compareEmsAndIpam(){
		
		List<String> list = new ArrayList<String>();
		for  (String deviceName: mapDeviceIpam.keySet()){
			
			if (mapDeviceEms.isEmpty() || !mapDeviceEms.containsKey(deviceName.split(" ")[1].trim())){
				List<String> ipams = mapDeviceIpam.get(deviceName);
				for (String ipam:ipams){
					String delete = deviceName+ " "+ ipam;
					list.add(delete);
				}
				//write this device on file
				
			}
			
		}
		
		EMSDeviceService.getInstance( propertiesContainer.getProperty("EMS_DEVICES_TO_DELETE")).writeFile(list);
		
		
	}
}
