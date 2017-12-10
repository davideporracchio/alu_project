package com.holonomix.alarm.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.TopologyAdapterInterface;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.AlarmException;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.IgnoreDeviceFileService;
import com.holonomix.hsqldb.model.Card;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.GlobalTopologyCollection;
import com.holonomix.hsqldb.model.Interface;
import com.holonomix.hsqldb.model.Port;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.ionix.TopologyBrowser;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;

public class CleanAlarmService {
	private static final Logger log = Logger.getLogger(CleanAlarmService.class);
	private PropertiesContainer propertiesContainer;
	private IgnoreDeviceFileService ignoreDeviceFileService;
	public CleanAlarmService() {
		propertiesContainer = PropertiesContainer.getInstance();
		ignoreDeviceFileService = IgnoreDeviceFileService.getInstance(propertiesContainer.getProperty("SMARTS_DEVICES_TO_IGNORE"));
		ignoreDeviceFileService.updateList();
		
		}

	public void cleanObjects() throws SmartsException {
		BrokerManager brokerManager = BrokerManager.getInstance();
		String ipamArray[] = propertiesContainer.getProperty("AM_DOMAINS").split(",");
		for (String domain : ipamArray) {
			RemoteDomainManager remoteDomainManager = null;
			log.debug("Starting cleaning for Domain " + domain);
			while (remoteDomainManager == null) {
				remoteDomainManager = brokerManager.getDomainManagerForAlarm(domain, true);
				try {

					if (remoteDomainManager == null) {
						log.debug("Domain " + domain + " is down, wait and retry");
						MapMonitor.getInstance().put("IPAM", "down, " + domain);
						Thread.sleep(Integer.parseInt(propertiesContainer.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
					}
				} catch (InterruptedException e1) {

					log.error("interrupted thread alarm");
				}

			}

			SmartsReadService smartsReadService = new SmartsReadService(remoteDomainManager);

			// look for card and port
			AlarmLogicService alarmLogicService = new AlarmLogicService(remoteDomainManager);
			if (ClassFactory.getEnum() == EnumAdapterName.HWMETE) {
				startReadingSmartsForCleaning(domain, alarmLogicService, remoteDomainManager, smartsReadService);
			} else {

				Map<String, Device> mapDevice = smartsReadService.startReadingSmarts(null);
				// for each device in smarts
				for (Device device : mapDevice.values()) {
					cleanObject(domain, alarmLogicService, device);
				}
			}
			log.debug("Completed cleaning for Domain " + domain);
		}

	}

	private void cleanObject(String domain, AlarmLogicService alarmLogicService, Device device) {

		try {
			alarmLogicService.setDeviceEnable(device);
			log.debug("cleaning status device " + device.getName() + " in Ipam " + domain);
		} catch (AlarmException e) {

			log.error(" error cleaning status device " + device.getName() + " in Ipam " + domain);
		}
		// clean all cards
		for (Card card : device.getCardList()) {
			// clear status card
			try {
				card.setName("CARD-" + device.getName() + "/" + card.getName());
				alarmLogicService.cleanCard(card);
				log.debug("cleaning status card " + card.getName() + " in Ipam " + domain);
			} catch (AlarmException e) {

				log.error(" error cleaning status card " + card.getName() + " in Ipam " + domain);
			}
			// clean port
			for (Port port : card.getPortList()) {

				try {
					port.setName("PORT-" + device.getName() + "/" + port.getName());
					alarmLogicService.setPortEnable(port);
					log.debug("cleaning status port " + port.getName() + " in Ipam " + domain);
				} catch (AlarmException e) {
					log.error(" error cleaning status port " + port.getName() + " in Ipam " + domain);
				}
			}
		}
		for (Interface interface1 : device.getInterfaceList()) {
			// clear status interface
			try {

				alarmLogicService.setInterfaceEnable(device, interface1);
				log.debug("cleaning status interface " + interface1.getName() + " in Ipam " + domain);
			} catch (AlarmException e) {

				log.error(" error cleaning status interface " + interface1.getName() + " in Ipam " + domain);
			}

		}

	}

	public Map<String, Device> startReadingSmartsForCleaning(String domain, AlarmLogicService alarmLogicService,
			RemoteDomainManager remoteDomainManager, SmartsReadService smartsReadService) throws SmartsException {
		TopologyBrowser topologyBrowser = new TopologyBrowser(remoteDomainManager);
		List<String> classInstanceList = topologyBrowser.getListFromString("EMS_DEVICEMAP");

		List<String> deviceProperties = topologyBrowser.getListFromString("LIST_DEVICEPROPERTIES");
		Map<String, Device> mapDevices = new HashMap<String, Device>();

		for (String instance : classInstanceList) {

			String classInstance = instance.split("::")[1];

			// first call to have the list of devices
			List<String> scInstanceLValList = topologyBrowser.getClassInstanceNames(classInstance);
			int index = 0;
			for (String deviceName : scInstanceLValList) {
				if(!isValidDevice(deviceName)) continue;
				smartsReadService.populateDevice(deviceName, deviceProperties, mapDevices, instance, classInstance, index, deviceName);
				if(mapDevices.containsKey(deviceName)){
					cleanObject(domain, alarmLogicService, mapDevices.get(deviceName));
				}
				mapDevices.clear();
			}

		}

		return mapDevices;

	}

	// use GTC entry to set down interface port and card
	public void cleanUsingGTCEntry() throws SmartsException {
		BrokerManager brokerManager = BrokerManager.getInstance();
		String ipamArray[] = propertiesContainer.getProperty("AM_DOMAINS").split(",");
		for (String domain : ipamArray) {
			RemoteDomainManager remoteDomainManager = null;
			while (remoteDomainManager == null) {
				remoteDomainManager = brokerManager.getDomainManagerForAlarm(domain, true);
				try {
					if (remoteDomainManager == null) {

						MapMonitor.getInstance().put("IPAM", "down, " + domain);
						Thread.sleep(Integer.parseInt(propertiesContainer.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
					}
				} catch (InterruptedException e1) {

					log.error("interrupted thread alarm");
				}

			}

			SmartsReadService smartsReadService = new SmartsReadService(remoteDomainManager);

			AlarmLogicService alarmLogicService = new AlarmLogicService(remoteDomainManager);

			GlobalTopologyCollection globalTopologyCollection = new GlobalTopologyCollection();
			String nameAdap = PropertiesContainer.getInstance().getProperty("ADAPTER_NAME");
			globalTopologyCollection.setName("GTC-" + nameAdap);

			// read objects in GTC
			smartsReadService.findGlobalTypeCollection(globalTopologyCollection);
			// check status card using topology call
			if (globalTopologyCollection.getCardList().size() > 0) {
				Set<Card> setCardEms = new HashSet<Card>(globalTopologyCollection.getCardList());
				TopologyAdapterInterface topologyAdapterInterface = ClassFactory.getTopologyAdapterInstance();
				try {
					log.debug("connect to Ems to check cards");
					topologyAdapterInterface.checkCardStatus(setCardEms);
					for (Card cardInGTC : globalTopologyCollection.getCardList()) {
						if (ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN) {
							cardInGTC.splitNameWithRack(cardInGTC.getName().substring(cardInGTC.getName().indexOf("/") + 1));

						} else {
							cardInGTC.splitName(cardInGTC.getName().substring(cardInGTC.getName().indexOf("/") + 1));

						}
						if (!setCardEms.contains(cardInGTC))
							log.debug("clear card in GlobalTopologyCollection");
						alarmLogicService.setCardUp(cardInGTC);
					}

				} catch (AdapterException e1) {
					// TODO Auto-generated catch block

					log.debug("error changing status of card");
				} catch (AlarmException e) {
					// TODO Auto-generated catch block
					log.error("error reading smarts...");
				}

				// clean previous state
				// put card down
				for (Card card : setCardEms) {
					if (card != null) {

						log.debug("set status card " + card.getName() + " in Ipam " + domain + " to DOWN using information last topology ");
						try {
							alarmLogicService.setCardDown(card);
						} catch (AlarmException e) {

							log.debug(e.getMessage());
						}
					}

				}
			}
			// put port down
			for (Port port : globalTopologyCollection.getPortList()) {
				if (!port.getName().equalsIgnoreCase("")) {
					log.debug("set adminstatus port " + port.getName() + " in Ipam " + domain + " to DOWN using information last topology ");
					try {
						alarmLogicService.setAdminAndOperStatusDown(port);
					} catch (AlarmException e) {

						log.debug(e.getMessage());
					}
				}
			}
			// put interface down
			for (Interface interface1 : globalTopologyCollection.getInterfaceList()) {
				if (interface1 != null) {
					log.debug("set adminstatus interface " + interface1.getName() + " in Ipam " + domain
							+ " to DOWN using information last topology ");
					try {
						alarmLogicService.setAdminAndOperStatusDown(interface1);
					} catch (AlarmException e) {

						log.debug(e.getMessage());
					}
				}

			}

		}

	}
	private boolean isValidDevice(String name){
		
		log.debug("check if device is valid with name "+name);
		return ignoreDeviceFileService.isValidDevice(name);
	}
}
