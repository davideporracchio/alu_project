package com.holonomix.alarm.service;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.StartUpInterface;
import com.holonomix.exception.SmartsException;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class StartUpService {
	private static final Logger log = Logger.getLogger(StartUpService.class);
	private PropertiesContainer propertiesContainer;

	public StartUpService() {

		propertiesContainer = PropertiesContainer.getInstance();

	}

	public int acquisition()  {
		// check oi smarts are up
		
		BrokerManager brokerManager = BrokerManager.getInstance();
		String oiArray[] = propertiesContainer.getProperty("OI_INSTANCES")
				.split(",");
		int sizeOI = oiArray.length;
		for (String oi : oiArray) {
				//log.info("opening connection with oi: "+oi);
				RemoteDomainManager remoteDomainManager;
				try {
					remoteDomainManager = brokerManager.getDomainManagerForAlarm(oi, false);
				} catch (SmartsException e) {
					
					log.error("failed to connect to the broker manager");
					return 20;
				}
				if (remoteDomainManager == null) {
					log.error("failed opened session with OI "+oi);
					MapMonitor.getInstance().put("OI", "down, " + oi);
					}
					else {
						log.info("successfully opened session with OI "+oi);
						MapMonitor.getInstance().put("OI","up, "+oi);
						sizeOI--;
					}
		}
		if(sizeOI!=0){
			return 20;
		}
		// check ipam smarts are up
		
		String ipamArray[] = propertiesContainer.getProperty("AM_DOMAINS")
		.split(",");
		int sizeIpam = ipamArray.length;
		for (String domain : ipamArray) {
				RemoteDomainManager remoteDomainManager;
				try {
					remoteDomainManager = brokerManager.getDomainManagerForAlarm(
							domain, true);
				} catch (SmartsException e) {
					
					log.error("failed to connect to the broker manager");
					return 20;
				}
				
					if (remoteDomainManager == null) {
						log.error("failed opened session with IPAM "+domain);
						MapMonitor.getInstance().put("IPAM", "down, " + domain);
					}
					else {
						log.info("successfully opened session with IPAM "+domain);
						MapMonitor.getInstance().put("IPAM","up, "+domain);
						sizeIpam--;
					}
		}
		if(sizeIpam!=0){
			return 20;
		}
		
		
		//acquisition alarm chack if ems is up
		StartUpInterface startUpInterface = ClassFactory.getStartUpInstance();
		if (!startUpInterface.acquisition())
			return 10;
		
		return 0;
	}
}
