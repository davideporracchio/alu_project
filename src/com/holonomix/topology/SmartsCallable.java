package com.holonomix.topology;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Card;
import com.holonomix.hsqldb.model.Chassis;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.Interface;
import com.holonomix.hsqldb.model.Ip;
import com.holonomix.hsqldb.model.Ipam;
import com.holonomix.hsqldb.model.Item;
import com.holonomix.hsqldb.model.Item.Status;
import com.holonomix.hsqldb.model.Mac;
import com.holonomix.hsqldb.model.Narg;
import com.holonomix.hsqldb.model.Ncrg;
import com.holonomix.hsqldb.model.NetworkConnection;
import com.holonomix.hsqldb.model.Port;
import com.holonomix.hsqldb.model.SNMPAgent;
import com.holonomix.hsqldb.model.VLan;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.listener.DeviceListener;
import com.holonomix.log.topology.TopologyLog;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.service.BuilderLogicService;
import com.holonomix.topology.service.TopologyCompareService;

public class SmartsCallable implements Callable<Boolean> {
	private static final Logger log = Logger.getLogger(SmartsCallable.class);
	private PropertiesContainer propertiesContainer;
	private Ipam ipam;

	TopologyCompareService topologyCompareService;
	BuilderLogicService builderLogicService;

	BrokerManager brokerManager;

	public SmartsCallable(Ipam ipam) {

		this.ipam = ipam;
		propertiesContainer = PropertiesContainer.getInstance();
		brokerManager = BrokerManager.getInstance();

	}

	public Boolean call() throws SmartsException {
		try{
		Thread.currentThread().setName("SmartsCallable");
		RemoteDomainManager remoteDomainManager = brokerManager
				.getDomainManager(ipam.getName());
		builderLogicService = new BuilderLogicService(remoteDomainManager);
		topologyCompareService = new TopologyCompareService(remoteDomainManager);

		// compare Ipam with topology imported
		// here we read smarts object and compare them with our new objects
		log.debug("start to compare devices in ipam "+ipam.getName());
		topologyCompareService.compareIpam(ipam);
		log.debug("completed to compare devices in ipam "+ipam.getName());
		
		for (Device device : ipam.getDeviceList()) {
			try{
			if (device.getFlagStatus() == Status.DELETED) {
				log.debug("delete device " + device.getName() + ", from Ipam "
						+ ipam.getName());

				try {
					removeObject(device);

				} catch (SmartsException e) {
					log.error("error removing object " + device.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (device.getFlagStatus() == Status.UPDATED
					|| device.getFlagStatus() == Status.ADDED) {
				log.debug("creating device " + device.getName()
						+ ", add it in  Ipam " + ipam.getName());
				// creating device
				saveDeviceForZte(device);
				try {
					createGenericDevice(device);
					//create subcomponent
					checkSubComponent(device);
					if (device.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology().setNumberDeviceChanged(
								ipam.getSummaryTopology()
										.getNumberDeviceChanged() + 1);
					if (device.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology().setNumberDeviceCreated(
								ipam.getSummaryTopology()
										.getNumberDeviceCreated() + 1);
					device.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating device " + device.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (device.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this device " + device.getName()
						+ " in Ipam " + ipam.getName());

				try {
					updateDiscoveredLastAt(device);
					checkSubComponent(device);
				} catch (SmartsException e) {
					log.error("error creating subcomponent " + device.getName()
							+ " in Ipam  " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (device.getFlagStatus() == Status.ERROR) {
				log.debug("error in nbi for this device " + device.getName()
						+ " in Ipam " + ipam.getName());
				ipam.getSummaryTopology().setNumberDeviceError(
						ipam.getSummaryTopology()
								.getNumberDeviceError() + 1);
				
			}
		}catch(RuntimeException e){
			log.error("LOG-- runtimeexception checking device "+device.getName());
			
		}}
			
			
		log.debug("LOG-- check network connection for ipam "+ipam.getName());
		

		// check network connection
		if (ClassFactory.getEnum() == EnumAdapterName.HWMETE || ClassFactory.getEnum() == EnumAdapterName.HWZTE) {
			for (NetworkConnection networkConnection : ipam
					.getNetworkConnectionList()) {
				updateNetworkConnection(networkConnection);
			}

			// ncrg group

			for (Device device : ipam.getDeviceList()) {
				{
					Device deviceSmarts = new Device();
					deviceSmarts.setName(device.getName());
					deviceSmarts.setCreationClassName(device
							.getCreationClassName());
					//campare
					try{
					topologyCompareService.compareNcrg(device, deviceSmarts);
					}catch(RuntimeException e){
						log.error("LOG-- runtimeexception comparing ncrg "+device.getName());
						
					}
					// logic to create or delete them
					updateNcrg(deviceSmarts);

				}
			}
		}
		// refresh ipam
		builderLogicService.finishImport(ipam);
		// log device we did not instantiate.
		TopologyLog.printIpamStatus(ipam);
		log.info(ipam.getSummaryTopology().toString());
		}catch(RuntimeException e1){
			log.error("LOG-- runtimeexception "+e1.getMessage());
			log.error("LOG-- runtimeexception "+e1.getStackTrace().toString());
			throw e1;
		}catch(Exception e1){
			log.error("LOG-- exception "+e1.getMessage());
			log.error("LOG-- exception "+e1.getStackTrace().toString());
			if (e1 instanceof SmartsException) throw (SmartsException)e1;	
		}
		return true;
	}

	private void saveDeviceForZte(Device device) {
		if(propertiesContainer.getProperty("THREAD_FLAG_PARTIAL_DISCOVERY")!=null && propertiesContainer.getProperty("THREAD_FLAG_PARTIAL_DISCOVERY").equalsIgnoreCase("true")){
			DeviceListener.setDevice(device.getName());
		}
	}

	private void createGenericVlan(VLan vlan, Item item, Device device)
			throws SmartsException {

		builderLogicService.createVLan(vlan, item, device);

	}

	private void removeVlanConnection(VLan vlan, Item item, Device device)
			throws SmartsException {

		builderLogicService.removeVlanConnection(vlan, item, device);

	}

	private void removeObject(Item item) throws SmartsException {

		builderLogicService.removeObject(item);

	}

	private void checkSubComponent(Device device) throws SmartsException {
		try{
		// check ip
		updateIp(device);
		// check snmpagent
		updateSnmpAgent(device);
		// check chassis
		updateChassis(device);
		// check card
		updateCard(device);
		// check interface
		updateInterface(device);
		// check narg
		updateNarg(device);
		}catch(RuntimeException e){
			log.error("LOG-- runtimeexception checking subComponent "+device.getName());
			
		}
	}

	private void createGenericDevice(Device device) throws SmartsException {

		if (device.getType().equalsIgnoreCase(Device.CLASSNAMESWITCH)) {
			builderLogicService.createSwitch(device);
		} else if (device.getType().equalsIgnoreCase(Device.CLASSNAMEROUTER)) {
			builderLogicService.createRouter(device);
		}

	}
	public void updateDiscoveredLastAt(Device device) throws SmartsException {
		builderLogicService.updateDiscoveredLastAt(device);
	}
	
	private void updateNetworkConnection(NetworkConnection networkConnection)
			throws SmartsException {
		try{
		if (networkConnection.getFlagStatus() == Status.DELETED
				|| networkConnection.getFlagStatus() == Status.UPDATED) {
			log.info("delete networkConnection " + networkConnection.getName()
					+ ", from Ipam " + ipam.getName());
			try {
				removeObject(networkConnection);

				// ipam.getSummaryTopology().setNumberCardDeleted(ipam.getSummaryTopology().getNumberCardDeleted()+1);
			} catch (SmartsException e) {
				log.error("error removing networkConnection  "
						+ networkConnection.getName() + ",  in Ipam "
						+ ipam.getName());
				if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
					throw e;
			}

		}
		if (networkConnection.getFlagStatus() == Status.UPDATED
				|| networkConnection.getFlagStatus() == Status.ADDED) {
			log.info("creating networkConnection "
					+ networkConnection.getName() + ", add it in Ipam "
					+ ipam.getName());
			// creating it
			try {
				builderLogicService.createNetworkConnection(networkConnection);

				/*
				 * if (card.getFlagStatus()== Status.UPDATED)
				 * ipam.getSummaryTopology
				 * ().setNumberCardChanged(ipam.getSummaryTopology
				 * ().getNumberCardChanged()+1); if (card.getFlagStatus()==
				 * Status.ADDED)
				 * ipam.getSummaryTopology().setNumberCardCreated(ipam
				 * .getSummaryTopology().getNumberCardCreated()+1);
				 */
				networkConnection.setFlagStatus(Status.COMPLETED);
			} catch (Exception e) {
				if (e instanceof SmartsException) {
					if (((SmartsException) e).getTypeError() == SmartsException.CONNECTION_ERROR) {
						throw (SmartsException) e;
					}

				}
				log.error(" error creating networkConnection "
						+ networkConnection.getName() + " message: "
						+ e.getMessage());

			}

		}
		if (networkConnection.getFlagStatus() == Status.NOCHANGES) {
			log.debug("no changes for this networkConnection "
					+ networkConnection.getName() + " in Ipam "
					+ ipam.getName());

		}
		}catch(RuntimeException e){
			log.error("LOG-- runtimeexception checking networkconnection "+networkConnection.getName());
			
		}

	}

	private void updateVlanInPort(Port port, Device device)
			throws SmartsException {
		for (VLan vlan : port.getVlanList()) {
			String portName = "PORT-" + device.getName() + "/"
					+ port.getPortKey();
			if (vlan.getFlagStatus() == Status.UPDATED
					|| vlan.getFlagStatus() == Status.DELETED) {
				if (vlan.getFlagStatus() == Status.DELETED)
					log.debug("delete connection between vlan "
							+ vlan.getName() + " and port " + portName
							+ ", from Ipam " + ipam.getName());
				try {
					removeVlanConnection(vlan, port, device);
					// if (vlan.getFlagStatus()== Status.DELETED)
					// ipam.getSummaryTopology().setNumberVlanDeleted(ipam.getSummaryTopology().getNumberVlanDeleted()+1);
				} catch (SmartsException e) {
					log.error("error removing connection between vlan "
							+ vlan.getName() + " and port " + portName
							+ ", from Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (vlan.getFlagStatus() == Status.UPDATED
					|| vlan.getFlagStatus() == Status.ADDED) {
				log.info("creating  connection between vlan VLAN-"
						+ vlan.getVLANkey() + " and port " + portName
						+ ", from Ipam " + ipam.getName());
				// creating it
				try {
					createGenericVlan(vlan, port, device);
					/*
					 * if (vlan.getFlagStatus()== Status.UPDATED)
					 * ipam.getSummaryTopology
					 * ().setNumberVlanChanged(ipam.getSummaryTopology
					 * ().getNumberVlanChanged()+1); if (vlan.getFlagStatus()==
					 * Status.ADDED)
					 * ipam.getSummaryTopology().setNumberVlanCreated
					 * (ipam.getSummaryTopology().getNumberVlanCreated()+1);
					 */
					vlan.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating connection between vlan VLAN-"
							+ vlan.getVLANkey() + " and port " + portName
							+ ", from Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (vlan.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes between vlan " + vlan.getName()
						+ " and port " + portName + ", from Ipam "
						+ ipam.getName());

			}
		}
	}

	private void updateVlanInInterface(Interface interface1, Device device)
			throws SmartsException {
		for (VLan vlan : interface1.getVlanList()) {
			String interfaceName = "";
			if (!interface1.getDisplayName().equalsIgnoreCase(""))
				interfaceName = interface1.getDisplayName();
			else
				interfaceName = "IF-" + device.getName() + "/"
						+ interface1.getInterfaceKey();
			if (vlan.getFlagStatus() == Status.UPDATED
					|| vlan.getFlagStatus() == Status.DELETED) {
				if (vlan.getFlagStatus() == Status.DELETED)
					log.debug("delete connection between vlan "
							+ vlan.getName() + " and interface "
							+ interfaceName + ", from Ipam " + ipam.getName());
				try {
					removeVlanConnection(vlan, interface1, device);
					// if (vlan.getFlagStatus()== Status.DELETED)
					// ipam.getSummaryTopology().setNumberVlanDeleted(ipam.getSummaryTopology().getNumberVlanDeleted()+1);
				} catch (SmartsException e) {
					log.error("error removing connection between vlan "
							+ vlan.getName() + " and interface "
							+ interfaceName + ", from Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (vlan.getFlagStatus() == Status.UPDATED
					|| vlan.getFlagStatus() == Status.ADDED) {
				log.info("creating  connection between vlan " + vlan.getName()
						+ " and interface " + interfaceName + ", from Ipam "
						+ ipam.getName());
				// creating it
				saveDeviceForZte(device);
				try {
					createGenericVlan(vlan, interface1, device);
					/*
					 * if (vlan.getFlagStatus()== Status.UPDATED)
					 * ipam.getSummaryTopology
					 * ().setNumberVlanChanged(ipam.getSummaryTopology
					 * ().getNumberVlanChanged()+1); if (vlan.getFlagStatus()==
					 * Status.ADDED)
					 * ipam.getSummaryTopology().setNumberVlanCreated
					 * (ipam.getSummaryTopology().getNumberVlanCreated()+1);
					 */
					vlan.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating connection between vlan "
							+ vlan.getName() + " and interface "
							+ interfaceName + ", from Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (vlan.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes between vlan " + vlan.getName()
						+ " and interface " + interfaceName + ", from Ipam "
						+ ipam.getName());

			}
		}
	}

	private void updateCard(Device device) throws SmartsException {

		
		// check cards
		Set<Card> cardList = device.getCardList();
		TreeSet<Card> treeSet = new TreeSet<Card>(cardList);
		for (Card card : treeSet) {

			if(card.getName().indexOf("CARD-")!=-1){
				card.setName("CARD-"+device.getName()+ "/" + card.getName());
			}
			if (card.getFlagStatus() == Status.DELETED) {
				log.info("delete card "+ card.getName()+ ", from Ipam " + ipam.getName());
				try {
					removeObject(card);
					updatePort(card, device);
					ipam.getSummaryTopology()
							.setNumberCardDeleted(
									ipam.getSummaryTopology()
											.getNumberCardDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing card "+ card.getName()+ ", in Ipam "
							+ ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (card.getFlagStatus() == Status.UPDATED
					|| card.getFlagStatus() == Status.ADDED) {
				log.info("creating card  "+ card.getName()+ ", add it in  Ipam "
						+ ipam.getName());
				// creating it
				saveDeviceForZte(device);
				try {
					builderLogicService.createCard(card, device);
					updatePort(card, device);
					if (card.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology().setNumberCardChanged(
								ipam.getSummaryTopology()
										.getNumberCardChanged() + 1);
					if (card.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology().setNumberCardCreated(
								ipam.getSummaryTopology()
										.getNumberCardCreated() + 1);
					card.setFlagStatus(Status.COMPLETED);
				} catch (Exception e) {
					if (e instanceof SmartsException) {
						if (((SmartsException) e).getTypeError() == SmartsException.CONNECTION_ERROR) {
							throw (SmartsException) e;
						}

					}
					log.error(" error creating card: "+ card.getName()+ ", message: "
							+ e.getMessage());

				}

			}
			if (card.getFlagStatus() == Status.NOCHANGES) {
				card.setName("CARD-"+device.getName()+ "/" + card.getName());
				log.debug("no changes for this card "+ card.getName()+ ", in Ipam " + ipam.getName());
				
				// check port inside
				updatePort(card, device);
			}

		}

	}
	
	private void updateMac(Item item) throws SmartsException {
		Mac mac = null;
		
		if (item instanceof Interface) {
			mac = ((Interface) item).getMac();
		}
		
		if (mac!=null){
			if (mac.getFlagStatus() == Status.DELETED) {
				log.info("delete mac " + mac.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					removeObject(mac);
					//ipam.getSummaryTopology().setNumberIpDeleted(
					///		ipam.getSummaryTopology().getNumberIpDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing mac " + mac.getName() + " in Ipam "
							+ ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (mac.getFlagStatus() == Status.UPDATED
					|| mac.getFlagStatus() == Status.ADDED) {
				log.info("creating mac " + mac.getAddress()
						+ ", add it in  Ipam " + ipam.getName());
				// creating it
				try {
					builderLogicService.createMac(mac, item);
					/*if (mac.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology()
								.setNumberIpChanged(
										ipam.getSummaryTopology()
												.getNumberIpChanged() + 1);
					if (mac.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology()
								.setNumberIpCreated(
										ipam.getSummaryTopology()
												.getNumberIpCreated() + 1);*/
					mac.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating mac " + mac.getAddress()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (mac.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this mac " + mac.getAddress()
						+ " in Ipam " + ipam.getName());
			}
		}
		
	}

	private void updateIp(Item item) throws SmartsException {
		Set<Ip> ipList = null;
		if (item instanceof Device) {
			ipList = ((Device) item).getIpList();
		} else if (item instanceof Interface) {
			ipList = ((Interface) item).getIpList();
		}
		for (Ip ip : ipList) {

			if (ip.getFlagStatus() == Status.DELETED) {
				log.info("delete ip " + ip.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					removeObject(ip);
					ipam.getSummaryTopology().setNumberIpDeleted(
							ipam.getSummaryTopology().getNumberIpDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing ip " + ip.getName() + " in Ipam "
							+ ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (ip.getFlagStatus() == Status.UPDATED
					|| ip.getFlagStatus() == Status.ADDED) {
				log.info("creating ip " + ip.getAddress()
						+ ", add it in  Ipam " + ipam.getName());
				// creating it
				
				try {
					builderLogicService.createIp(ip, item);
					if (ip.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology()
								.setNumberIpChanged(
										ipam.getSummaryTopology()
												.getNumberIpChanged() + 1);
					if (ip.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology()
								.setNumberIpCreated(
										ipam.getSummaryTopology()
												.getNumberIpCreated() + 1);
					ip.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					if(ip.getAddress().split(":").length!=8){
					log.error("error creating ip " + ip.getAddress()
							+ " in Ipam " + ipam.getName());
					}
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR){
						throw e;
					}
				}catch (Exception e){
					log.error("error creating ip " + ip.getAddress()
							+ " in Ipam " + ipam.getName());
				}

			}
			if (ip.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this ip " + ip.getAddress()
						+ " in Ipam " + ipam.getName());
			}

		}
	}
	
	

	private void updateSnmpAgent(Device device) throws SmartsException {
		Set<SNMPAgent> snmpAgentList = device.getSnmpAgentList();
		for (SNMPAgent snmpAgent : snmpAgentList) {

			if (snmpAgent.getFlagStatus() == Status.DELETED) {
				log.info("delete snmpAgent " + snmpAgent.getName()
						+ ", from Ipam " + ipam.getName());
				try {
					removeObject(snmpAgent);
					ipam.getSummaryTopology()
							.setNumberCardDeleted(
									ipam.getSummaryTopology()
											.getNumberCardDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing snmpAgent " + snmpAgent.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (snmpAgent.getFlagStatus() == Status.UPDATED
					|| snmpAgent.getFlagStatus() == Status.ADDED) {
				log.info("creating snmpAgent " + snmpAgent.getName()
						+ ", add it in  Ipam " + ipam.getName());
				// creating it
				saveDeviceForZte(device);
				try {
					builderLogicService.createSNMPAgent(snmpAgent, device);
					if (snmpAgent.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology().setNumberSnmpAgentChanged(
								ipam.getSummaryTopology()
										.getNumberSnmpAgentChanged() + 1);
					if (snmpAgent.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology().setNumberSnmpAgentCreated(
								ipam.getSummaryTopology()
										.getNumberSnmpAgentCreated() + 1);
					snmpAgent.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating snmpagent " + snmpAgent.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (snmpAgent.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this snmpAgent "
						+ snmpAgent.getName() + " in Ipam " + ipam.getName());
			}

		}

	}

	private void updateChassis(Device device) throws SmartsException {
		Set<Chassis> chassisList = device.getChassisList();
		for (Chassis chassis : chassisList) {

			if (chassis.getFlagStatus() == Status.DELETED) {
				log.info("delete chassis " + chassis.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					removeObject(chassis);
					ipam.getSummaryTopology()
							.setNumberChassisDeleted(
									ipam.getSummaryTopology()
											.getNumberChassisDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing chassis " + chassis.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (chassis.getFlagStatus() == Status.UPDATED
					|| chassis.getFlagStatus() == Status.ADDED) {
				log.info("creating chassis " + chassis.getName()
						+ ", add it in  Ipam " + ipam.getName());
				// creating it
				saveDeviceForZte(device);
				try {
					builderLogicService.createChassis(chassis, device);
					if (chassis.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology().setNumberChassisChanged(
								ipam.getSummaryTopology()
										.getNumberChassisChanged() + 1);
					if (chassis.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology().setNumberChassisCreated(
								ipam.getSummaryTopology()
										.getNumberChassisCreated() + 1);
					chassis.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating chassis " + chassis.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (chassis.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this chassis " + chassis.getName()
						+ " in Ipam " + ipam.getName());
			}

		}
	}

	private void updateInterface(Device device) throws SmartsException {
		Set<Interface> interfaceList = device.getInterfaceList();

		int idNumber = 0;
		int key = 0;
		for (Interface interfac : interfaceList) {
			try {
				key = Integer.parseInt(interfac.getInterfaceNumber());
			} catch (Exception e) {
				key = 0;
			}
			if (idNumber < key)
				idNumber = key;

		}

		// before loop parent interface
		for (Interface interfac : interfaceList) {
			if (interfac.getParentInterface() == null
					|| interfac.getParentInterface().equalsIgnoreCase("")) {
				updateInterfaceLogic(interfac, device, idNumber);
				idNumber++;
			}
		}
		// after loop children interface
		for (Interface interfac : interfaceList) {
			if (interfac.getParentInterface() != null
					&& !interfac.getParentInterface().equalsIgnoreCase("")) {
				updateInterfaceLogic(interfac, device, idNumber);
				idNumber++;
			}
		}
	}

	private void updateInterfaceLogic(Interface interfac, Device device,
			int idNumber) throws SmartsException {

		if (interfac.getFlagStatus() == Status.DELETED) {
			log.info("delete interface " + interfac.getDisplayName()
					+ ", from Ipam " + ipam.getName());
			try {
				IonixElement ionixElement = new  IonixElement();
				ionixElement.setClassName(device.getCreationClassName());
				ionixElement.setInstanceName(device.getName());
				if (builderLogicService.getInterfaceByKey(ionixElement, interfac.getDeviceId())!=null)
					removeObject(interfac);
				ipam.getSummaryTopology()
						.setNumberInterfaceDeleted(
								ipam.getSummaryTopology()
										.getNumberInterfaceDeleted() + 1);
			} catch (SmartsException e) {
				log.error("error removing interface "
						+ interfac.getDisplayName() + " in Ipam "
						+ ipam.getName());
				if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
					throw e;
			}
		}
		if (interfac.getFlagStatus() == Status.UPDATED
				|| interfac.getFlagStatus() == Status.ADDED) {
			saveDeviceForZte(device);
			String interfaceDisplayName = interfac.getName();
			if (interfaceDisplayName.indexOf("[") != -1)
				interfaceDisplayName = interfaceDisplayName.substring(0,
						interfaceDisplayName.indexOf("[") - 1);
			interfaceDisplayName = "IF-" + device.getName() + "/"
					+ interfaceDisplayName + " [" + interfac.getDeviceId()
					+ "]";
			if (interfac.getFlagStatus() == Status.UPDATED){
				interfaceDisplayName = interfac.getDisplayName();
			}
			// creating it
			try {
				if (interfac.getFlagStatus() == Status.ADDED)
				log.info("creating interface " + interfaceDisplayName
						+ ", add it in Ipam " + ipam.getName());
				try {
					idNumber = Integer.parseInt(interfac.getInterfaceNumber());
				} catch (Exception e) {
					
				}
				interfac.setInterfaceNumber(idNumber + "");
				
				IonixElement element = builderLogicService.createInterface(
						interfac, device);
				interfac.setDisplayName(element.getInstanceName());

				updateVlanInInterface(interfac, device);
				interfac.setName(element.getInstanceName());
				updateIp(interfac);
				updateMac(interfac);
				if (interfac.getFlagStatus() == Status.UPDATED)
					ipam.getSummaryTopology().setNumberInterfaceChanged(
							ipam.getSummaryTopology()
									.getNumberInterfaceChanged() + 1);
				if (interfac.getFlagStatus() == Status.ADDED)
					ipam.getSummaryTopology().setNumberInterfaceCreated(
							ipam.getSummaryTopology()
									.getNumberInterfaceCreated() + 1);
				interfac.setFlagStatus(Status.COMPLETED);
			} catch (SmartsException e) {
				log.error("error creating interface " + interfaceDisplayName
						+ " in Ipam " + ipam.getName());
				if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
					throw e;
			}

		}
		if (interfac.getFlagStatus() == Status.NOCHANGES) {
			log.debug("no changes for this interface "
					+ interfac.getDisplayName() + " in Ipam " + ipam.getName());
			
			IonixElement element = new IonixElement();
			element.setClassName(device.getCreationClassName());
			element.setInstanceName(device.getName());
			builderLogicService.createConnectionInterfaceCard(element,interfac.getDeviceId());
			updateVlanInInterface(interfac, device);
			updateIp(interfac);
			updateMac(interfac);
		}

	}

	private void updatePort(Card card, Device device) throws SmartsException {
		Set<Port> portList = card.getPortList();
		for (Port port : portList) {

			String fullPortName = port.getPortKey();
			if (fullPortName.indexOf("PORT-") == -1) {
				fullPortName = "PORT-" + device.getName() + "/" + fullPortName;
				port.setDisplayName(fullPortName);
			}
			if (port.getFlagStatus() == Status.DELETED) {
				log.info("delete port " + fullPortName + ", from Ipam "
						+ ipam.getName());
				// port name can be incomplete

				try {
					removeObject(port);
					ipam.getSummaryTopology()
							.setNumberPortDeleted(
									ipam.getSummaryTopology()
											.getNumberPortDeleted() + 1);
				} catch (SmartsException e) {
					log.error("error removing port " + fullPortName
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (port.getFlagStatus() == Status.UPDATED
					|| port.getFlagStatus() == Status.ADDED) {
				log.info("creating port " + fullPortName + ", add it in Ipam "
						+ ipam.getName());
				// creating it
				try {
					try{
						int number=Integer.parseInt(port.getPortNumber());}
						catch(Exception e){
							log.error("port with not a numeric port number " + port.getPortNumber() + ".This port is not added to ipam"+ipam.getName());
							continue;
					}
					builderLogicService.createPort(port, card, device);
					updateVlanInPort(port, device);
					if (port.getFlagStatus() == Status.UPDATED)
						ipam.getSummaryTopology().setNumberPortChanged(
								ipam.getSummaryTopology()
										.getNumberPortChanged() + 1);
					if (port.getFlagStatus() == Status.ADDED)
						ipam.getSummaryTopology().setNumberPortCreated(
								ipam.getSummaryTopology()
										.getNumberPortCreated() + 1);
					port.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating port " + fullPortName
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (port.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this port " + fullPortName
						+ " in Ipam " + ipam.getName());
				updateVlanInPort(port, device);
			}

		}
	}

	private void updateNarg(Device device) throws SmartsException {

		Set<Narg> nargList = device.getNargList();
		for (Narg narg : nargList) {
			if (narg.getFlagStatus() == Status.DELETED) {
				log.info("delete narg " + narg.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					removeObject(narg);
					// ipam.getSummaryTopology().setNumberInterfaceDeleted(ipam.getSummaryTopology().getNumberInterfaceDeleted()+1);
				} catch (SmartsException e) {
					log.error("error removing narg " + narg.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (narg.getFlagStatus() == Status.UPDATED
					|| narg.getFlagStatus() == Status.ADDED) {

				String nargName = narg.getName();
				if (narg.getFlagStatus()==Status.UPDATED)
					removeObject(narg);
				// creating it
				saveDeviceForZte(device);
				try {
					
					if(narg.getInterfaceList().size()>0){
					log.info("creating narg " + nargName + ", add it in Ipam "
							+ ipam.getName());

					IonixElement element = builderLogicService.createNarg(narg,
							device);

					narg.setFlagStatus(Status.COMPLETED);
					}
				} catch (SmartsException e) {
					log.error("error creating narg " + narg.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (narg.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this narg " + narg.getName()
						+ " in Ipam " + ipam.getName());

			}

		}
	}

	private void updateNcrg(Device device) throws SmartsException {

		Set<Ncrg> ncrgList = device.getNcrgList();
		for (Ncrg ncrg : ncrgList) {
			try{
			if (ncrg.getFlagStatus() == Status.DELETED) {
				log.info("delete ncrg " + ncrg.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					NetworkConnection networkConnection= new NetworkConnection();
					String name =ncrg.getName().replace("NCRG-","");
					if(!name.startsWith("LINK-"))
						name="LINK-"+name;
					networkConnection.setName(name);
					
					//removeObject(networkConnection);
					removeObject(ncrg);
					// ipam.getSummaryTopology().setNumberInterfaceDeleted(ipam.getSummaryTopology().getNumberInterfaceDeleted()+1);
				} catch (SmartsException e) {
					log.error("error removing ncrg " + ncrg.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}
			}
			if (ncrg.getFlagStatus() == Status.UPDATED
					|| ncrg.getFlagStatus() == Status.ADDED) {

				String ncrgName = ncrg.getName();

				// creating it
				try {

					log.info("creating ncrg " + ncrgName + ", add  it in Ipam "
							+ ipam.getName());

					IonixElement element = builderLogicService.createNcrg(ncrg,
							device);

					ncrg.setFlagStatus(Status.COMPLETED);
				} catch (SmartsException e) {
					log.error("error creating ncrg " + ncrg.getName()
							+ " in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (ncrg.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this ncrg " + ncrg.getName()
						+ " in Ipam " + ipam.getName());
				// check if there are new or old network connection
				//create again the link between ETrunk in network connection 
				/*NetworkConnection networkConnection1 = new NetworkConnection();
				networkConnection1.setInterfaceA(ncrg.getInterfaceA());
				networkConnection1.setInterfaceB(ncrg.getInterfaceB());
				IonixElement element = builderLogicService.createNetworkConnectionLogic(networkConnection1);*/
				updateNetworkConnectionInNcrg(ncrg);

			}

				}catch(RuntimeException e){
			log.error("LOG-- runtimeexception updating ncrg "+ncrg.getName());
				}	
		}
	}

	private void updateNetworkConnectionInNcrg(Ncrg ncrg)
			throws SmartsException {

		for (NetworkConnection networkConnection : ncrg
				.getNetworkConnectionList()) {
			if (networkConnection.getFlagStatus() == Status.DELETED) {
				log.info("delete networkConnection "
						+ networkConnection.getName() + ", from Ipam "
						+ ipam.getName());
				try {
					builderLogicService.removeNetworkConnectionLink(ncrg,
							networkConnection);

				} catch (SmartsException e) {
					log.error("error removing link networkConnection  "
							+ networkConnection.getName() + "from "
							+ ncrg.getName() + ",  in Ipam " + ipam.getName());
					if (e.getTypeError() == SmartsException.CONNECTION_ERROR)
						throw e;
				}

			}
			if (networkConnection.getFlagStatus() == Status.UPDATED
					|| networkConnection.getFlagStatus() == Status.ADDED) {
				log.info("connect networkConnection "
						+ networkConnection.getName() + "to " + ncrg.getName()
						+ ",  in Ipam " + ipam.getName());
				// creating it
				try {
					builderLogicService.createNetworkConnectionLink(ncrg,
							networkConnection);

					networkConnection.setFlagStatus(Status.COMPLETED);
				} catch (Exception e) {
					if (e instanceof SmartsException) {
						if (((SmartsException) e).getTypeError() == SmartsException.CONNECTION_ERROR) {
							throw (SmartsException) e;
						}

					}
					log.error(" error link networkConnection "
							+ networkConnection.getName() + "from "
							+ ncrg.getName() + ",  message: " + e.getMessage());

				}

			}
			if (networkConnection.getFlagStatus() == Status.NOCHANGES) {
				log.debug("no changes for this networkConnection "
						+ networkConnection.getName() + " in Ipam "
						+ ipam.getName());

			}
		}

	}

}
