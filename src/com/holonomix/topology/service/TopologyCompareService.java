package com.holonomix.topology.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.IgnoreDeviceFileService;
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
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.properties.PropertiesContainer;

public class TopologyCompareService {

	private static final Logger log = Logger
			.getLogger(TopologyCompareService.class);
	private PropertiesContainer propertiesContainer;
	private RemoteDomainManager remoteDomainManager;
	private SmartsReadService smartsReadService;
	private IgnoreDeviceFileService ignoreDeviceFileService;
	Ipam ipamnew = null;

	public TopologyCompareService(RemoteDomainManager remoteDomainManager) {
		this.remoteDomainManager = remoteDomainManager;
		propertiesContainer = PropertiesContainer.getInstance();
		smartsReadService = new SmartsReadService(remoteDomainManager);
		ignoreDeviceFileService = IgnoreDeviceFileService.getInstance(propertiesContainer.getProperty("SMARTS_DEVICES_TO_IGNORE"));
		
	}

	public void compareIpam(Ipam ipamnew) throws SmartsException {

		this.ipamnew = ipamnew;

		compareDevice();
		if (ClassFactory.getEnum() == EnumAdapterName.HWMETE
				|| ClassFactory.getEnum() == EnumAdapterName.HWZTE) {

			compareNetworkConnection();
		}
		// compareVLAN();

	}

	private void compareDevice() throws SmartsException {

		Set<Device> deviceList = ipamnew.getDeviceList();

		Map<String, Device> mapDevices = new HashMap<String, Device>();

		for (Device device : deviceList) {
			// check if the device is incomplete (status == error)
			// clean serialnumber for devices
			device.setSerialNumber("");
			if (device.getFlagStatus() != null
					&& device.getFlagStatus() == Status.ERROR) {
				mapDevices.put(device.getName(), device);
			} else {
				// check if this device is in smarts
				mapDevices.putAll(smartsReadService.startReadingSmarts(device
						.getName()));
				Device deviceSmarts = mapDevices.get(device.getName());
				if (deviceSmarts == null && isValidDevice(device.getName())) {
					log.info("device with name  " + device.getName()
							+ " does not exist in smart. Create it in "
							+ ipamnew.getName());
					device.setFlagStatus(Status.ADDED);
					mapDevices.put(device.getName(), device);
				} else if(isValidDevice(device.getName())){
					// device exists we need to check if it the same or not
					// check attributes inside device
					String allAtributes = ModelUtility
							.concatAllAttributes(device);
					String allAtributesPrevious = ModelUtility
							.concatAllAttributes(deviceSmarts);
					if (allAtributes.equalsIgnoreCase(allAtributesPrevious)) {
						deviceSmarts.setFlagStatus(Status.NOCHANGES);
						log.debug("compareDevice device, " + device.getName()
								+ ", is equal to the old one in "
								+ ipamnew.getName());
						// check cards
						compareCard(device, deviceSmarts);
						// check check interface
						compareInterface(device, deviceSmarts);
						// check ip
						compareIp(device, deviceSmarts);
						// check snmpagent
						compareSNMPAgent(device, deviceSmarts);
						// check chassis
						compareChassis(device, deviceSmarts);
						// check narg
						compareNarg(device, deviceSmarts);
						mapDevices.put(device.getName(), deviceSmarts);

					} else {
						deviceSmarts.setFlagStatus(Status.UPDATED);
						// save in the hashmap the device updated
						mapDevices.put(device.getName(), device);
						log.debug("compareDevice device, " + device.getName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
						// check cards
						deviceSmarts.setDescription(device.getDescription());
						deviceSmarts.setDisplayName(device.getDisplayName());
						deviceSmarts.setLocation(device.getLocation());
						deviceSmarts.setModel(device.getModel());
						deviceSmarts.setSerialNumber(device.getSerialNumber());
						deviceSmarts.setOsVersion(device.getOsVersion());
						deviceSmarts.setVendor(device.getVendor());
						compareCard(device, deviceSmarts);
						// check check interface
						compareInterface(device, deviceSmarts);
						// check ip
						compareIp(device, deviceSmarts);
						// check snmpagent
						compareSNMPAgent(device, deviceSmarts);
						// check chassis
						compareChassis(device, deviceSmarts);
						// check narg
						compareNarg(device, deviceSmarts);
						mapDevices.put(device.getName(), deviceSmarts);
					}
				}else{
					log.debug("device is in ignore list "+device.getName());
				}
			}
		}
		deviceList = new HashSet<Device>();
		for (Device device : mapDevices.values()) {
			if (device.getFlagStatus() != null)
				deviceList.add(device);
		}
		ipamnew.setDeviceList(new HashSet<Device>(deviceList));

	}

	private void compareChassis(Device device, Device deviceSmarts) {
		Set<Chassis> chassisList = device.getChassisList();
		Set<Chassis> chassisSmartsList = deviceSmarts.getChassisList();
		Set<Chassis> chassisFinalList = new HashSet<Chassis>();

		for (Chassis chassis : chassisList) {
			// check if this ipam is in the previous db
			boolean isChassisNew = true;
			for (Chassis chassisSmarts : chassisSmartsList) {

				if (chassis.equals(chassisSmarts)) {
					// found same ipam
					isChassisNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(chassis);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(chassisSmarts);
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						chassis.setFlagStatus(Status.NOCHANGES);
						log.debug("chassis " + chassis.getName()
								+ ", is equal to the old one in "
								+ ipamnew.getName());

					} else {
						chassis.setFlagStatus(Status.UPDATED);
						log.debug("chassis " + chassis.getName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
					}
					chassisFinalList.add(chassis);
				}
			}
			// add new ones
			if (isChassisNew) {
				// ipam is new
				log.debug("chassis " + chassis.getName() + ", is new in "
						+ ipamnew.getName());
				chassis.setFlagStatus(Status.ADDED);
				chassisFinalList.add(chassis);
			}

		}
		// add chassis to delete
		for (Chassis chassis : chassisSmartsList) {

			boolean isadded = chassisFinalList.add(chassis);
			if (isadded) {
				chassis.setFlagStatus(Status.DELETED);
				log.debug("chassis " + chassis.getName() + ", is to delete in "
						+ ipamnew.getName());
			}
		}

		// now deviceSmart contains for each chassis the operation to do
		deviceSmarts.setChassisList(chassisFinalList);
	}

	private void compareSNMPAgent(Device device, Device deviceSmarts) {
		Set<SNMPAgent> snmpAgentList = device.getSnmpAgentList();
		Set<SNMPAgent> snmpAgentSmartsList = deviceSmarts.getSnmpAgentList();
		Set<SNMPAgent> snmpAgentFinalList = new HashSet<SNMPAgent>();

		for (SNMPAgent snmpAgent : snmpAgentList) {
			// check if this ipam is in the previous db
			boolean isSNMPAgentNew = true;
			for (SNMPAgent snmpAgentSmarts : snmpAgentSmartsList) {

				if (snmpAgent.equals(snmpAgentSmarts)) {
					// found same ipam
					isSNMPAgentNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(snmpAgent);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(snmpAgentSmarts);
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						snmpAgent.setFlagStatus(Status.NOCHANGES);
						log.debug(" snmpAgent " + snmpAgent.getName()
								+ ", is equal to the old one in "
								+ ipamnew.getName());

					} else {
						snmpAgent.setFlagStatus(Status.UPDATED);
						log.debug(" snmpAgent " + snmpAgent.getName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
					}
					snmpAgentFinalList.add(snmpAgent);
				}
			}
			// add new ones
			if (isSNMPAgentNew) {
				// ipam is new
				log.debug("snmpAgent " + snmpAgent.getName() + ", is new in "
						+ ipamnew.getName());
				snmpAgent.setFlagStatus(Status.ADDED);
				snmpAgentFinalList.add(snmpAgent);
			}

		}
		// add snmpAgent to delete
		for (SNMPAgent snmpAgent : snmpAgentSmartsList) {

			boolean isadded = snmpAgentFinalList.add(snmpAgent);
			if (isadded) {
				snmpAgent.setFlagStatus(Status.DELETED);
				log.debug("snmpAgent " + snmpAgent.getName()
						+ ", is to delete in " + ipamnew.getName());
			}
		}

		// now deviceSmart contains for each snmpAgent the operation to do
		deviceSmarts.setSnmpAgentList(snmpAgentFinalList);
	}

	private void compareIp(Item item, Item itemSmarts) {
		Set<Ip> ipList = null;
		Set<Ip> ipSmartsList = null;
		if (item instanceof Device) {
			ipList = ((Device) item).getIpList();
			ipSmartsList = ((Device) itemSmarts).getIpList();
		}
		if (item instanceof Interface) {
			ipList = ((Interface) item).getIpList();
			ipSmartsList = ((Interface) itemSmarts).getIpList();
		}
		Set<Ip> ipFinalList = new HashSet<Ip>();

		for (Ip ip : ipList) {
			// check if this ipam is in the previous db
			boolean isIpNew = true;
			for (Ip ipSmarts : ipSmartsList) {

				if (ip.equals(ipSmarts)) {
					// found same ipam
					isIpNew = false;

					if (ClassFactory.getEnum() == EnumAdapterName.HWZTE
							&& item instanceof Interface) {
						ip.setNetmask(ipSmarts.getNetmask());
					}
					// check if the attributes are the same
					String allAtributes = ModelUtility.concatAllAttributes(ip);

					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(ipSmarts);

					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						ip.setFlagStatus(Status.NOCHANGES);
						log.debug("ip " + ip.getAddress()
								+ ", is equal to the old one in "
								+ ipamnew.getName());

					} else {
						ip.setFlagStatus(Status.UPDATED);
						log.debug("ip " + ip.getAddress()
								+ ", has different attributes values in "
								+ ipamnew.getName());
						if (item instanceof Interface) {
							((Interface) item).setFlagStatus(Status.UPDATED);
						} else if (item instanceof Device) {
							((Device) item).setFlagStatus(Status.UPDATED);
						}

					}
					ipFinalList.add(ip);
				}
			}
			// add new ones
			if (isIpNew) {
				// ipam is new
				ip.setFlagStatus(Status.ADDED);
				if (item instanceof Interface) {
					((Interface) item).setFlagStatus(Status.UPDATED);
				} else if (item instanceof Device) {
					((Device) item).setFlagStatus(Status.UPDATED);
				}

				log.debug("ip " + ip.getAddress() + "(" + item.getName()
						+ "), is new in " + ipamnew.getName());
				ipFinalList.add(ip);
			}

		}
		// add ip to delete
		for (Ip ip : ipSmartsList) {

			boolean isadded = ipFinalList.add(ip);
			if (isadded) {
				ip.setFlagStatus(Status.DELETED);
				log.debug("ip " + ip.getName() + ", is to delete ");
				if (item instanceof Interface) {
					((Interface) item).setFlagStatus(Status.UPDATED);
				} else if (item instanceof Device) {
					((Device) item).setFlagStatus(Status.UPDATED);
				}

			}
		}

		// now deviceSmart contains for each ip the operation to do
		if (item instanceof Device) {

			((Device) item).setIpList(ipFinalList);
		} else if (item instanceof Interface) {
			((Interface) item).setIpList(ipFinalList);
		}

	}

	private void compareMac(Item item, Item itemSmarts) {
		Mac mac = null;
		Mac macSmarts = null;
		boolean isMacNew = true;
		if (item instanceof Interface) {
			mac = ((Interface) item).getMac();
			macSmarts = ((Interface) itemSmarts).getMac();
		}

		if (mac != null && mac.equals(macSmarts)) {
			// found same ipam
			isMacNew = false;

		}
		// add new ones
		if (isMacNew && mac != null) {
			// ipam is new
			mac.setFlagStatus(Status.ADDED);
			if (item instanceof Interface) {
				((Interface) item).setFlagStatus(Status.UPDATED);
			}

			log.debug("mac " + mac.getAddress() + ", is new in "
					+ ipamnew.getName());
			((Interface) item).setMac(mac);
		}

		if (macSmarts != null && mac == null) {
			// mac is to delete

			macSmarts.setFlagStatus(Status.DELETED);
			((Interface) item).setMac(macSmarts);
			log.debug("mac " + macSmarts.getName() + ", is to delete ");
			if (item instanceof Interface) {
				((Interface) item).setFlagStatus(Status.UPDATED);

			}

		}

	}

	private void compareInterface(Device device, Device deviceSmarts) {
		Set<Interface> interfaceList = device.getInterfaceList();
		Set<Interface> interfaceSmartsList = deviceSmarts.getInterfaceList();
		Set<Interface> interfaceFinalList = new HashSet<Interface>();

		for (Interface interface1 : interfaceList) {
			// check if this interface is in the previous db
			if (interface1.getFlagStatus() == Status.DELETED)
				continue;
			boolean isInterfaceNew = true;
			for (Interface interfaceSmarts : interfaceSmartsList) {

				if (interface1.equals(interfaceSmarts)) {
					// found same interface
					isInterfaceNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(interface1);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(interfaceSmarts);
					interface1.setDisplayName(interfaceSmarts.getDisplayName());
					interface1.setInterfaceKey(interfaceSmarts
							.getInterfaceKey());
					interface1.setInterfaceNumber(interfaceSmarts
							.getInterfaceNumber());
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						interface1.setFlagStatus(Status.NOCHANGES);
						interface1.setName(interfaceSmarts.getName());
						log.debug("interface " + interface1.getDisplayName()
								+ ", is equal to the old one in "
								+ ipamnew.getName());

					} else {
						interface1.setFlagStatus(Status.UPDATED);
						log.debug("interface " + interface1.getDisplayName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
					}
					compareVLAN(interface1, interfaceSmarts);
					compareIp(interface1, interfaceSmarts);
					compareMac(interface1, interfaceSmarts);
					interfaceFinalList.add(interface1);
					break;
				}
			}
			// add new ones
			if (isInterfaceNew) {
				// interface is new
				interface1.setFlagStatus(Status.ADDED);
				log.debug("interface with deviceID " + interface1.getDeviceId()
						+ ", is new in " + ipamnew.getName());
				interfaceFinalList.add(interface1);
				for (VLan vlan : interface1.getVlanList()) {
					vlan.setFlagStatus(Status.ADDED);
				}
				for (Ip ip : interface1.getIpList()) {
					ip.setFlagStatus(Status.ADDED);
				}
				if (interface1.getMac() != null) {
					interface1.getMac().setFlagStatus(Status.ADDED);
				}
			}

		}
		// add ip to delete
		for (Interface interface1 : interfaceSmartsList) {

			boolean isadded = interfaceFinalList.add(interface1);
			if (isadded) {
				interface1.setFlagStatus(Status.DELETED);
				log.debug("interface " + interface1.getDisplayName()
						+ ", is to delete ");
			}
		}

		// now deviceSmart contains for each ip the operation to do
		deviceSmarts.setInterfaceList(interfaceFinalList);
	}

	private void compareNarg(Device device, Device deviceSmarts) {
		Set<Narg> nargList = device.getNargList();
		Set<Narg> nargSmartsList = deviceSmarts.getNargList();
		Set<Narg> nargFinalList = new HashSet<Narg>();

		for (Narg narg1 : nargList) {
			// check if this nargam is in the previous db
			boolean isNargNew = true;
			for (Narg nargSmarts : nargSmartsList) {

				if (narg1.equals(nargSmarts)) {
					// found same narg
					isNargNew = false;
					// check if the attributes are the same

					narg1.setDisplayName(nargSmarts.getDisplayName());
					// narg1.setNargKey(nargSmarts.getNargKey());

					narg1.setFlagStatus(Status.UPDATED);
					log.debug("narg " + narg1.getDisplayName()
							+ ", has different attributes values in "
							+ ipamnew.getName());
					nargFinalList.add(narg1);
					for (Interface interface1 : narg1.getInterfaceList()) {
						interface1.setFlagStatus(Status.ADDED);

					}

				}
			}
			// add new ones
			if (isNargNew) {
				// nargam is new
				narg1.setFlagStatus(Status.ADDED);
				log.debug("narg with name  " + narg1.getName() + ", is new in "
						+ ipamnew.getName());
				nargFinalList.add(narg1);
				for (Interface interface1 : narg1.getInterfaceList()) {
					interface1.setFlagStatus(Status.ADDED);
				}
			}

		}
		// add narg to delete
		for (Narg narg1 : nargSmartsList) {
			narg1.setFlagStatus(Status.DELETED);
			boolean isadded = nargFinalList.add(narg1);
			if (isadded)
				log.debug("narg " + narg1.getDisplayName() + ", is to delete ");
		}

		// now deviceSmart contains for each narg the operation to do
		deviceSmarts.setNargList(nargFinalList);
	}

	private void compareNetworkConnection() throws SmartsException {

		List<NetworkConnection> listNetworkConnectionSmarts = null;

		listNetworkConnectionSmarts = smartsReadService.getNetworkConnection();

		Set<NetworkConnection> listNetworkConnection = ipamnew
				.getNetworkConnectionList();
		String deviceList = "";
		for (Device device : ipamnew.getDeviceList()) {
			if (device.getFlagStatus() == null
					|| device.getFlagStatus() != Status.ERROR) {
				deviceList += device.getName() + ",";
			}
		}
		Set<NetworkConnection> networkConnectionFinalList = new HashSet<NetworkConnection>();
		for (NetworkConnection networkConnection : listNetworkConnection) {
			String name1 = networkConnection.getInterfaceA().getParentDevice();
			String name2 = networkConnection.getInterfaceB().getParentDevice();
			if (deviceList.indexOf(name1) != -1
					&& deviceList.indexOf(name2) != -1
					&& !name1.equalsIgnoreCase("")
					&& !name2.equalsIgnoreCase("")) {
				// we put networkConnection in ipam
				networkConnectionFinalList.add(networkConnection);
			}
		}
		listNetworkConnection.clear();
		listNetworkConnection.addAll(networkConnectionFinalList);
		networkConnectionFinalList = new HashSet<NetworkConnection>();

		for (NetworkConnection networkConnection : listNetworkConnection) {
			// check if this interface is in the previous db
			boolean isNetworkConnectionNew = true;
			for (NetworkConnection networkConnectionSmarts : listNetworkConnectionSmarts) {

				if (networkConnection.equals(networkConnectionSmarts)) {
					// found same interface
					isNetworkConnectionNew = false;

					networkConnection.setFlagStatus(Status.NOCHANGES);
					log.debug("networkConnection "
							+ networkConnection.getName()
							+ ", is equal to the old one in "
							+ ipamnew.getName());
					networkConnection
							.setName(networkConnectionSmarts.getName());

					networkConnectionFinalList.add(networkConnection);
				}
			}
			// add new ones
			if (isNetworkConnectionNew) {
				// interfaceam is new
				networkConnection.setFlagStatus(Status.ADDED);
				log.debug("networkConnection with name "
						+ networkConnection.getName() + ", is new in "
						+ ipamnew.getName());
				networkConnectionFinalList.add(networkConnection);
			}

		}
		for (NetworkConnection networkConnection : listNetworkConnectionSmarts) {
			networkConnection.setFlagStatus(Status.DELETED);
			String name1 = networkConnection.getInterfaceA().getParentDevice();
			String name2 = networkConnection.getInterfaceB().getParentDevice();
			if (deviceList.indexOf(name1) != -1
					&& deviceList.indexOf(name2) != -1
					&& !name1.equalsIgnoreCase("")
					&& !name2.equalsIgnoreCase("")) {
				boolean isadded = networkConnectionFinalList
						.add(networkConnection);
				if (isadded)
					log.debug("networkConnection with name "
							+ networkConnection.getName() + ", is to delete ");
			}
		}

		ipamnew.getNetworkConnectionList().clear();
		ipamnew.getNetworkConnectionList().addAll(networkConnectionFinalList);

	}

	private void compareCard(Device device, Device deviceSmarts) {
		Set<Card> cardList = device.getCardList();
		Set<Card> cardSmartsList = deviceSmarts.getCardList();
		Set<Card> cardFinalList = new HashSet<Card>();

		for (Card card : cardList) {
			// check if this card is in the previous db
			boolean isCardNew = true;
			for (Card cardSmarts : cardSmartsList) {

				if (card.equals(cardSmarts)) {
					// found same card
					isCardNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(card);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(cardSmarts);
					
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						card.setName(cardSmarts.getName());
						card.setFlagStatus(Status.NOCHANGES);
						log.debug("card CARD-" + device.getName() + "/"
								+ card.getName()
								+ ", is equal to the old one in "
								+ ipamnew.getName());
						// check if port are the same
						comparePort(card, cardSmarts);
					} else {
						card.setFlagStatus(Status.UPDATED);
						log.debug("card CARD-" + device.getName() + "/"
								+ card.getName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
						comparePort(card, cardSmarts);
					}
					cardFinalList.add(card);
				}
			}
			// add new ones
			if (isCardNew) {
				// cardam is new
				card.setFlagStatus(Status.ADDED);
				log.debug("card CARD-" + device.getName() + "/"
						+ card.getName() + ", is new in " + ipamnew.getName());
				cardFinalList.add(card);
				for (Port port : card.getPortList()) {
					port.setFlagStatus(Status.ADDED);
					for (VLan vlan : port.getVlanList()) {
						vlan.setFlagStatus(Status.ADDED);
					}
				}
			}

		}
		// add ip to delete
		for (Card card : cardSmartsList) {

			boolean isadded = cardFinalList.add(card);
			if (isadded) {
				card.setFlagStatus(Status.DELETED);
				log.debug("card CARD-" + device.getName() + "/"
						+ card.getName() + ", is to delete in "
						+ ipamnew.getName());
				for (Port port : card.getPortList()) {
					port.setFlagStatus(Status.DELETED);

				}
				// delete interface
				String keyName = card.getName();
				for (Interface interface1 : device.getInterfaceList()) {
					if (interface1.getName().startsWith(keyName))
						interface1.setFlagStatus(Status.DELETED);
				}
			}
		}

		// now deviceSmart contains for each card the operation to do
		deviceSmarts.setCardList(cardFinalList);
	}

	private void comparePort(Card card, Card cardSmarts) {
		Set<Port> portList = card.getPortList();
		Set<Port> portSmartsList = cardSmarts.getPortList();
		Set<Port> portFinalList = new HashSet<Port>();

		for (Port port : portList) {
			// check if this portam is in the previous db
			boolean isPortNew = true;
			for (Port portSmarts : portSmartsList) {

				if (port.equals(portSmarts)) {
					// found same port
					isPortNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(port);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(portSmarts);
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						port.setFlagStatus(Status.NOCHANGES);
						log.debug("port " + port.getPortKey()
								+ ", is equal to the old one  in "
								+ ipamnew.getName());

					} else {
						port.setFlagStatus(Status.UPDATED);
						log.debug("port " + port.getPortKey()
								+ ", has different attributes values in "
								+ ipamnew.getName());
					}
					compareVLAN(port, portSmarts);
					portFinalList.add(port);
					break;
				}
			}
			// add new ones
			if (isPortNew) {
				// portam is new
				port.setFlagStatus(Status.ADDED);
				log.debug("port " + port.getPortKey() + ", is new in "
						+ ipamnew.getName());
				portFinalList.add(port);
				for (VLan vlan : port.getVlanList()) {
					vlan.setFlagStatus(Status.ADDED);
				}
			}

		}
		// add ip to delete
		for (Port port : portSmartsList) {

			boolean isadded = portFinalList.add(port);
			if (isadded) {
				port.setFlagStatus(Status.DELETED);
				log.debug("port " + port.getPortKey() + ", is to delete in "
						+ ipamnew.getName());

			}
		}

		// now deviceSmart contains for each ip the operation to do
		card.setPortList(portFinalList);
	}

	private void compareVLAN(Item item, Item itemSmarts) {
		Set<VLan> vlanList = null;
		Set<VLan> vlanSmartsList = null;
		if (item instanceof Port) {

			vlanList = ((Port) item).getVlanList();
			vlanSmartsList = ((Port) itemSmarts).getVlanList();
		}
		if (item instanceof Interface) {
			vlanList = ((Interface) item).getVlanList();
			vlanSmartsList = ((Interface) itemSmarts).getVlanList();
		}
		Set<VLan> vlanFinalList = new HashSet<VLan>();
		// set all vlan in smarts with flag DELETE

		for (VLan vlan : vlanList) {
			// check if this portam is in the previous db
			boolean isVlanNew = true;
			for (VLan vlanSmarts : vlanSmartsList) {

				if (vlan.equals(vlanSmarts)) {
					// found same port
					isVlanNew = false;
					// check if the attributes are the same
					String allAtributes = ModelUtility
							.concatAllAttributes(vlan);
					String allAtributesSmarts = ModelUtility
							.concatAllAttributes(vlanSmarts);
					if (allAtributes.equalsIgnoreCase(allAtributesSmarts)) {
						vlan.setFlagStatus(Status.NOCHANGES);
						vlan.setName(vlanSmarts.getName());
						log.debug("vlan " + vlan.getName()
								+ ", is equal to the old one  in "
								+ ipamnew.getName());

					} else {
						vlan.setFlagStatus(Status.UPDATED);
						log.debug("vlan " + vlan.getName()
								+ ", has different attributes values in "
								+ ipamnew.getName());
					}
					vlanFinalList.add(vlan);
					break;
				}
			}
			// add new ones
			if (isVlanNew) {
				// vlanam is new
				vlan.setFlagStatus(Status.ADDED);
				log.debug("vlan " + vlan.getName() + ", is new in "
						+ ipamnew.getName());
				vlanFinalList.add(vlan);
			}

		}
		// add vlan to delete
		for (VLan vlan : vlanSmartsList) {

			boolean isadded = vlanFinalList.add(vlan);
			if (isadded) {
				vlan.setFlagStatus(Status.DELETED);
				log.debug("vlan " + vlan.getName() + ", is to delete in "
						+ remoteDomainManager.getNameDomain());
			}
		}

		// now deviceSmart contains for each ip the operation to do
		if (item instanceof Port) {
			((Port) item).getVlanList().addAll(vlanFinalList);
		}
		if (item instanceof Interface) {
			((Interface) item).getVlanList().addAll(vlanFinalList);
		}

	}

	public void compareNcrg(Device device, Device deviceSmarts)
			throws SmartsException {

		device.getNcrgList().clear();
		smartsReadService.findNcrg(device);

		smartsReadService.findExistingNcrg(deviceSmarts);
		Set<Ncrg> ncrgList = device.getNcrgList();
		Set<Ncrg> ncrgSmartsList = deviceSmarts.getNcrgList();
		Set<Ncrg> ncrgFinalList = new HashSet<Ncrg>();

		for (Ncrg ncrg1 : ncrgList) {
			// check if this ncrgam is in the previous db
			boolean isNcrgNew = true;
			for (Ncrg ncrgSmarts : ncrgSmartsList) {

				if (ncrg1.equals(ncrgSmarts)) {
					// found same ncrg
					isNcrgNew = false;
					// check if the attributes are the same

					ncrg1.setDisplayName(ncrgSmarts.getDisplayName());
					ncrg1.setName(ncrgSmarts.getName());
					// ncrg1.setNcrgKey(ncrgSmarts.getNcrgKey());

					ncrg1.setFlagStatus(Status.NOCHANGES);
					log.debug("ncrg " + ncrg1.getDisplayName()
							+ ", is equal to the old one in "
							+ remoteDomainManager.getNameDomain());

					compareNetworkConnectionInsideNCRG(ncrg1, ncrgSmarts);
					ncrgFinalList.add(ncrg1);
				}
			}
			// add new ones
			if (isNcrgNew) {
				// ncrgam is new
				ncrg1.setFlagStatus(Status.ADDED);
				log.debug("ncrg with  name  " + ncrg1.getName()
						+ ", is new in " + remoteDomainManager.getNameDomain());
				ncrgFinalList.add(ncrg1);
				for (NetworkConnection networkConnection : ncrg1
						.getNetworkConnectionList()) {
					networkConnection.setFlagStatus(Status.ADDED);
				}
			}

		}
		// add ip to delete
		for (Ncrg ncrg1 : ncrgSmartsList) {

			boolean isadded = ncrgFinalList.add(ncrg1);
			if (isadded) {
				ncrg1.setFlagStatus(Status.DELETED);
				log.debug("ncrg " + ncrg1.getDisplayName() + ", is to delete ");
			}
		}

		// now deviceSmart contains for each ncrg the operation to do
		deviceSmarts.setNcrgList(ncrgFinalList);
	}

	private void compareNetworkConnectionInsideNCRG(Ncrg ncrg, Ncrg ncrgSmarts)
			throws SmartsException {

		Set<NetworkConnection> listNetworkConnectionSmarts = ncrgSmarts
				.getNetworkConnectionList();

		Set<NetworkConnection> listNetworkConnection = ncrg
				.getNetworkConnectionList();

		Set<NetworkConnection> networkConnectionFinalList = new HashSet<NetworkConnection>();

		for (NetworkConnection networkConnection : listNetworkConnection) {
			// check if this networkConnection is in the previous ipam
			boolean isNetworkConnectionNew = true;
			for (NetworkConnection networkConnectionSmarts : listNetworkConnectionSmarts) {

				if (networkConnection.equals(networkConnectionSmarts)) {
					// found same networkConnection
					isNetworkConnectionNew = false;

					networkConnection.setFlagStatus(Status.NOCHANGES);
					log.debug("networkConnection "
							+ networkConnection.getName()
							+ ", is equal to the old one in "
							+ remoteDomainManager.getNameDomain());
					networkConnection
							.setName(networkConnectionSmarts.getName());

					networkConnectionFinalList.add(networkConnection);
				}
			}
			// add new ones
			if (isNetworkConnectionNew) {
				// interfaceam is new
				networkConnection.setFlagStatus(Status.ADDED);
				log.debug("networkConnection with name "
						+ networkConnection.getName() + ", is new in "
						+ ipamnew.getName());
				networkConnectionFinalList.add(networkConnection);
			}

		}

		for (NetworkConnection networkConnection : listNetworkConnectionSmarts) {

			boolean isadded = networkConnectionFinalList.add(networkConnection);
			if (isadded) {
				networkConnection.setFlagStatus(Status.DELETED);
				log.debug("ncrg " + networkConnection.getName()
						+ ", is to delete ");
			}
		}

		// now deviceSmart contains for each ip the operation to do
		ncrg.getNetworkConnectionList().clear();
		ncrg.getNetworkConnectionList().addAll(networkConnectionFinalList);
	}
	private boolean isValidDevice(String name){
		if(ignoreDeviceFileService == null) {
			log.error("ignoreDeviceFileService is null");
		}
		log.debug("check if device is valid with name "+name);
		return ignoreDeviceFileService.isValidDevice(name);
	}

}
