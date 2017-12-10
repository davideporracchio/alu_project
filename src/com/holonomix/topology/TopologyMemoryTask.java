package com.holonomix.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.properties.PropertiesContainer;

public class TopologyMemoryTask {

	private static final Logger log = Logger
			.getLogger(TopologyMemoryTask.class);
	private PropertiesContainer propertiesContainer;
	private static TopologyMemoryTask topologyMemoryTask =new TopologyMemoryTask();;
	private boolean isloading =false;
	private Map<String,List<String>> mapIpamDevies;
	private TopologyMemoryTask() {
		propertiesContainer = PropertiesContainer.getInstance();
		 
	}
	
	public static TopologyMemoryTask getInstance(){
		if(topologyMemoryTask==null)
			 new TopologyMemoryTask();
		
		return topologyMemoryTask;
	}

	public boolean isIsloading() {
		return isloading;
	}

	public void setIsloading(boolean isloading) {
		this.isloading = isloading;
	}

	public Map<String, List<String>> getMapIpamDevies() {
		return mapIpamDevies;
	}
	
	public List<String> getIpamsForDevice(String deviceName){
		
		List<String> listIpams = new ArrayList<String>();
		while(isloading==true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return listIpams;
			}
		}
		
		for(String ipamKey:mapIpamDevies.keySet()){
			List<String> listDEvice = mapIpamDevies.get(ipamKey);
			if(listDEvice.contains(deviceName)){
				listIpams.add(ipamKey);
				
			}
		}
		return listIpams;
	}

	public void setMapIpamDevies(Map<String, List<String>> mapIpamDevies) {
		this.mapIpamDevies = mapIpamDevies;
	}

	public void doImport() {
		Map<String, List<String>> mapIpamDeviesNew = new HashMap<String, List<String>>();
		
		// check if we have a list of device we want to migrate
		BrokerManager brokerManager= BrokerManager.getInstance();
		RemoteDomainManager remoteDomainManager;
		try {
			String ipams =propertiesContainer.getProperty("AM_DOMAINS");
			for(String ipam:ipams.split(",")){
			remoteDomainManager = brokerManager.getDomainManager(ipam);
			SmartsReadService ionix = new SmartsReadService(remoteDomainManager);
			Map<String,Device> mapDevices = ionix.readingSmartsDevice();
			List<String> listDevices=new ArrayList<String>();
			listDevices.addAll(mapDevices.keySet());
			mapIpamDeviesNew.put(ipam, listDevices);
			}
		} catch (SmartsException e) {
			log.error("Import topology memory failed");
		}
		isloading =true;
		mapIpamDevies=mapIpamDeviesNew;
		isloading = false;
	}

}
