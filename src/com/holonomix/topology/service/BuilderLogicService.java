package com.holonomix.topology.service;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Card;
import com.holonomix.hsqldb.model.Chassis;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.GlobalTopologyCollection;
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
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.ionix.sam.SmartsAlarmService;
import com.holonomix.ionix.sam.SmartsBuilderService;
import com.holonomix.ionix.sam.SmartsDeleteService;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.properties.PropertiesContainer;
import com.smarts.repos.MR_AnyVal;

public class BuilderLogicService {
	private static final Logger log = Logger
			.getLogger(BuilderLogicService.class);

	SmartsBuilderService smartsBuilderService;
	SmartsDeleteService smartsDeleteService;
	private PropertiesContainer propertiesContainer;
	SmartsAlarmService smartsAlarmService;
	RemoteDomainManager remoteDomainManager;
	SmartsReadService smartsReadService;

	public BuilderLogicService(RemoteDomainManager remoteDomainManager) {
		this.remoteDomainManager = remoteDomainManager;
		smartsAlarmService = new SmartsAlarmService(remoteDomainManager);
		smartsBuilderService = new SmartsBuilderService(remoteDomainManager);
		smartsDeleteService = new SmartsDeleteService(remoteDomainManager);
		smartsReadService = new SmartsReadService(remoteDomainManager);
		propertiesContainer = PropertiesContainer.getInstance();

	}

	public IonixElement getInterfaceByKey(IonixElement ionixElement,
			String interfaceKey) {
		return smartsReadService.getInterfaceByKey(ionixElement, interfaceKey);
	}

	public IonixElement createIp(Ip ip, Item item) throws SmartsException {

		IonixElement element = createIpLogic(ip, item);

		return element;
	}

	public IonixElement createMac(Mac mac, Item item) throws SmartsException {

		IonixElement element = createMacLogic(mac, item);

		return element;
	}

	public IonixElement createSNMPAgent(SNMPAgent snmpAgent, Device device)
			throws SmartsException {

		IonixElement element = createSNMPAgentLogic(snmpAgent, device);

		return element;
	}

	public IonixElement createChassis(Chassis chassis, Device device)
			throws SmartsException {

		IonixElement element = createChassisLogic(chassis, device);

		return element;
	}

	public IonixElement createCard(Card card, Device device)
			throws SmartsException {

		IonixElement element = createCardLogic(card, device);

		return element;
	}

	public IonixElement createPort(Port port, Card card, Device device)
			throws SmartsException {

		IonixElement element = createPortLogic(port, card, device);

		return element;
	}

	public IonixElement createInterface(Interface interfac, Device device)
			throws SmartsException {

		IonixElement element = createInterfaceLogic(interfac, device);

		return element;
	}

	public IonixElement createNetworkConnection(
			NetworkConnection networkConnection) throws SmartsException {

		IonixElement element = createNetworkConnectionLogic(networkConnection);

		return element;
	}

	public boolean removeNetworkConnectionLink(Ncrg ncrg,
			NetworkConnection networkConnection) throws SmartsException {
		return removeNetworkConnectionLinkLogic(ncrg, networkConnection);

	}

	public boolean createNetworkConnectionLink(Ncrg ncrg,
			NetworkConnection networkConnection) throws SmartsException {
		return createNetworkConnectionLinkLogic(ncrg, networkConnection);

	}

	public IonixElement createNetworkConnectionLogic(
			NetworkConnection networkConnection) throws SmartsException {

		IonixElement ionixElement = smartsBuilderService
				.createNetworkConnection(networkConnection);
		if (ionixElement.getInstanceName() != null) {

			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "Description",
					networkConnection.getDescription());
		}
		return ionixElement;
	}

	public IonixElement createVLan(VLan vlan, Item item, Device device)
			throws SmartsException {

		IonixElement element = createVlanLogic(vlan, item, device);

		return element;
	}

	public IonixElement createNarg(Narg narg, Device device)
			throws SmartsException {

		IonixElement element = createNargLogic(narg, device);

		return element;
	}

	public IonixElement createNcrg(Ncrg ncrg, Device device)
			throws SmartsException {

		IonixElement element = createNcrgLogic(ncrg, device);

		return element;
	}

	public IonixElement removeVlanConnection(VLan vlan, Item item, Device device)
			throws SmartsException {
		IonixElement element = removeVlanConnectionLogic(vlan, item, device);

		return element;
	}

	public void finishImport(Ipam ipam) throws SmartsException {

		finishImportLogic(ipam);

	}

	public void removeObject(Item item) throws SmartsException {
		IonixElement element = new IonixElement();
		element.setClassName(item.getCreationClassName());
		element.setInstanceName(item.getDisplayName());
		if (item.getCreationClassName().equalsIgnoreCase("IP")) {
			element.setInstanceName("IP-"
					+ element.getInstanceName().substring(0,
							element.getInstanceName().indexOf(" ")));

		} else if (item.getCreationClassName().equalsIgnoreCase("SNMPAGENT")) {
			element.setInstanceName(item.getName());

		} else if (item.getCreationClassName().equalsIgnoreCase("CARD")) {
			if (element.getInstanceName().indexOf(" ") != -1) {
				element.setInstanceName(element.getInstanceName().substring(0,
						element.getInstanceName().indexOf(" ")));
			}
		} else if (item.getCreationClassName().equalsIgnoreCase("VLAN")) {
			element.setInstanceName(item.getName());

		} else if (item.getCreationClassName().equalsIgnoreCase("INTERFACE")) {

			element.setInstanceName(item.getName());

		} else if (item.getCreationClassName().equalsIgnoreCase(
				"NETWORKCONNECTION")) {

			element.setInstanceName(item.getName());

		} else if (item.getCreationClassName().equalsIgnoreCase("MAC")) {
			element.setInstanceName(((Mac) item).getName());

		}

		removeObjectLogic(element);

	}

	public void removeObject(IonixElement element) throws SmartsException {

		removeObjectLogic(element);

	}

	private IonixElement createIpLogic(Ip ip, Item item) throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(item.getCreationClassName());
		String deviceName = item.getDisplayName().substring(0,
				item.getDisplayName().indexOf("/") + 1);
		String name = "";
		if (item.getName().indexOf(deviceName) == -1) {
			name = item.getDisplayName().substring(0,
					item.getDisplayName().indexOf("/") + 1)
					+ item.getName();
		} else
			name = item.getName();
		element.setInstanceName(name);
		IonixElement ipElement = null;

		ipElement = smartsBuilderService.createIp(element, ip);
		ip.setName(ipElement.getInstanceName());
		if (ip.getNetmask() != null && !ip.getNetmask().equalsIgnoreCase("")) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					ipElement.getClassName(), ipElement.getInstanceName(),
					"Netmask", ip.getNetmask());
		}

		if (item instanceof Interface) {
			MR_AnyVal[] opsList = {};
			MR_AnyVal result = remoteDomainManager.invokeOperation(
					ipElement.getClassName(), ipElement.getInstanceName(),
					"unmanage", opsList);
		}
		return ipElement;
	}

	private IonixElement createMacLogic(Mac mac, Item item)
			throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(item.getCreationClassName());
		String deviceName = item.getDisplayName().substring(0,
				item.getDisplayName().indexOf("/") + 1);
		String name = "";
		if (item.getName().indexOf(deviceName) == -1) {
			name = item.getDisplayName().substring(0,
					item.getDisplayName().indexOf("/") + 1)
					+ item.getName();
		} else
			name = item.getName();
		element.setInstanceName(name);
		IonixElement macElement = null;

		macElement = smartsBuilderService.createMac(element, mac);
		mac.setName(macElement.getInstanceName());

		/*
		 * if (item instanceof Interface) { MR_AnyVal[] opsList = {}; MR_AnyVal
		 * result = remoteDomainManager.invokeOperation(ipElement
		 * .getClassName(), ipElement.getInstanceName(), "unmanage", opsList); }
		 */
		return macElement;
	}

	private IonixElement createSNMPAgentLogic(SNMPAgent snmpAgent, Device device)
			throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(device.getCreationClassName());
		element.setInstanceName(device.getName());
		IonixElement snmpAgentElement = null;

		snmpAgentElement = smartsBuilderService.createSNMPAgent(element);
		smartsBuilderService.insertRelationshipsBetweenClassInstances(
				element.getClassName(), element.getInstanceName(),
				snmpAgentElement.getClassName(),
				snmpAgentElement.getInstanceName(), "HostsServices");

		for (Ip ip : device.getIpList()) {
			IonixElement ipElement = new IonixElement();
			ipElement.setClassName(ip.getCreationClassName());
			ipElement.setInstanceName("IP-" + ip.getAddress());
			smartsBuilderService.insertRelationshipsBetweenClassInstances(
					snmpAgentElement.getClassName(),
					snmpAgentElement.getInstanceName(),
					ipElement.getClassName(), ipElement.getInstanceName(),
					"LayeredOver");
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					snmpAgentElement.getClassName(),
					snmpAgentElement.getInstanceName(), "AgentAddress",
					ip.getAddress());
			smartsBuilderService.updateAgentAddressList(snmpAgentElement);
		}
		return snmpAgentElement;
	}

	private IonixElement createNargLogic(Narg narg, Device device)
			throws SmartsException {
		IonixElement nargElement = null;
		if (narg.getInterfaceList().size() > 0) {

			nargElement = smartsBuilderService.createNARG(narg.getName());

			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					nargElement.getClassName(), nargElement.getInstanceName(),
					"DisplayName", narg.getName());

			IonixElement deviceElement = new IonixElement();
			deviceElement.setClassName(device.getCreationClassName());
			deviceElement.setInstanceName(device.getName());
			// delete the narg if there are not interface linked
			boolean deleteNarg = true;
			for (Interface interface1 : narg.getInterfaceList()) {

				IonixElement interfaceElement = smartsReadService
						.getInterfaceByKey(deviceElement,
								interface1.getDeviceId());
				if (interfaceElement != null) {
					deleteNarg = false;
					smartsBuilderService
							.insertRelationshipsBetweenClassInstances(
									nargElement.getClassName(),
									nargElement.getInstanceName(),
									interfaceElement.getClassName(),
									interfaceElement.getInstanceName(),
									"ComposedOf");
				} else {
					log.error("interface " + interface1.getDeviceId()
							+ " does not exist under device "
							+ device.getName());
				}
			}
			if (deleteNarg == true) {
				removeObject(narg);
			}
		}

		return nargElement;
	}

	private IonixElement createNcrgLogic(Ncrg ncrg, Device device)
			throws SmartsException {

		IonixElement ncrgElement = null;
		if (ncrg.getNetworkConnectionList().size() > 0) {

			if (ncrg.getInterfaceA() != null && ncrg.getInterfaceB() != null) {
				if (ClassFactory.getEnum() == EnumAdapterName.HWMETE) {
					/*
					 * NetworkConnection networkConnection1 = new
					 * NetworkConnection();
					 * networkConnection1.setInterfaceA(ncrg.getInterfaceA());
					 * networkConnection1.setInterfaceB(ncrg.getInterfaceB());
					 * IonixElement element =
					 * createNetworkConnectionLogic(networkConnection1);
					 * ncrg.setName(element.getInstanceName().replace("LINK",
					 * "NCRG"));
					 */
				}
				ncrgElement = smartsBuilderService.createNCRG(ncrg.getName());

				smartsBuilderService.setSmartsInstanceStringAttributeValue(
						ncrgElement.getClassName(),
						ncrgElement.getInstanceName(), "DisplayName",
						ncrg.getName());

				for (NetworkConnection networkConnection : ncrg
						.getNetworkConnectionList()) {

					createNetworkConnectionLinkLogic(ncrg, networkConnection);

				}
			}
		}
		return ncrgElement;
	}

	private boolean createNetworkConnectionLinkLogic(Ncrg ncrg,
			NetworkConnection networkConnection) throws SmartsException {
		IonixElement ncrgElement = smartsBuilderService.createNCRG(ncrg
				.getName());
		if (networkConnection != null) {
			smartsBuilderService.insertRelationshipsBetweenClassInstances(
					ncrgElement.getClassName(), ncrgElement.getInstanceName(),
					networkConnection.getCreationClassName(),
					networkConnection.getName(), "ComposedOf");
		} else {
			log.error("impossible to link networkConnection "
					+ networkConnection.getName() + " to ncrg "
					+ ncrgElement.getInstanceName() + " in ipam "
					+ remoteDomainManager.getNameDomain());
			return false;
		}
		return true;

	}

	private IonixElement createChassisLogic(Chassis chassis, Device device)
			throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(device.getCreationClassName());
		element.setInstanceName(device.getName());
		IonixElement chassisElement = null;

		chassisElement = smartsBuilderService.createChassis(chassis.getName());
		smartsBuilderService.insertRelationshipsBetweenClassInstances(
				chassisElement.getClassName(),
				chassisElement.getInstanceName(), element.getClassName(),
				element.getInstanceName(), "PackagesSystems");

		return chassisElement;
	}

	private IonixElement createCardLogic(Card card, Device device)
			throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(device.getCreationClassName());
		element.setInstanceName(device.getName());
		Chassis chassis = device.getChassisList().iterator().next();
		IonixElement chassisElement = new IonixElement();
		chassisElement.setClassName(chassis.getCreationClassName());
		String fullChassiName = chassis.getName();
		if (fullChassiName.indexOf("CHASSIS") == -1)
			fullChassiName = "CHASSIS-" + fullChassiName;
		chassisElement.setInstanceName(fullChassiName);
		IonixElement cardElement = null;
		String cardName;
		if (ClassFactory.getEnum() == EnumAdapterName.HWBRAS
				|| ClassFactory.getEnum() == EnumAdapterName.HWMETE|| ClassFactory.getEnum() == EnumAdapterName.HWZTE) {
			cardName = card.getNameForSmarts(true);
		} else {
			cardName = card.getNameForSmarts(false);
			
		}
		cardElement = smartsBuilderService.createCard(chassisElement, cardName);
		card.setName(cardElement.getInstanceName());
		smartsBuilderService.insertRelationshipsBetweenClassInstances(
				element.getClassName(), element.getInstanceName(),
				cardElement.getClassName(), cardElement.getInstanceName(),
				"ComposedOf");
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				cardElement.getClassName(), cardElement.getInstanceName(),
				"Description", card.getDescription());
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				cardElement.getClassName(), cardElement.getInstanceName(),
				"Location", card.getLocation());
		if (card.getSerialNumber() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					cardElement.getClassName(), cardElement.getInstanceName(),
					"SerialNumber", card.getSerialNumber());
		}
		if (ClassFactory.getEnum() == EnumAdapterName.HWBRAS
				|| ClassFactory.getEnum() == EnumAdapterName.HWMETE) {
			if (card.getDisplayName() != null
					&& !card.getDisplayName().equalsIgnoreCase("")) {
				smartsBuilderService.setSmartsInstanceStringAttributeValue(
						cardElement.getClassName(),
						cardElement.getInstanceName(),
						"DisplayName",
						cardElement.getInstanceName() + " ["
								+ card.getDisplayName() + "]");
			}
		}
		if (ClassFactory.getEnum() == EnumAdapterName.ALUFTTH) {
			String displayName = createAluDisplayName(cardElement.getInstanceName());
			if (displayName!=null){ 
				smartsBuilderService.setSmartsInstanceStringAttributeValue(
						cardElement.getClassName(),
						cardElement.getInstanceName(),
						"DisplayName",
						cardElement.getInstanceName() + " ["
								+ displayName + "]");
			}
		}
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				cardElement.getClassName(), cardElement.getInstanceName(),
				"Type", card.getType());
		if (card.getSerialNumber() == null)
			card.setSerialNumber("");
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				cardElement.getClassName(), cardElement.getInstanceName(),
				"SerialNumber", card.getSerialNumber());

		if (card.getName().split("/").length > 3 && ClassFactory.getEnum() != EnumAdapterName.ZTEMSAN) {
			cardName = card.getName().substring(0,
					card.getName().lastIndexOf("/"));
			try {
				smartsBuilderService.insertRelationshipsBetweenClassInstances(
						"Card", cardName, cardElement.getClassName(),
						cardElement.getInstanceName(), "ComposedOf");

			} catch (Exception e) {
				log.error(" card not found " + cardName);
			}
		}
		return cardElement;
	}
	
	private String createAluDisplayName(String name){
		try{
		String allNames = PropertiesContainer.getInstance().getProperty(
		"CARD_MAPPING");
		String number=name.substring(name.lastIndexOf("/")+1);
		for (String s :allNames.split(",")){
			if (s.startsWith(number+":")){
				return s.split(":")[1]; }
	}}catch (Exception e) {
		log.error("eror in creating alu diplayname " + e.getMessage());
	}
		return null;
	}
	private IonixElement createPortLogic(Port port, Card card, Device device)
			throws SmartsException {
		IonixElement portElement = null;

		IonixElement element = new IonixElement();

		element.setClassName(device.getCreationClassName());
		element.setInstanceName(device.getName());

		IonixElement cardElement = new IonixElement();
		cardElement.setClassName(card.getCreationClassName());

		String cardFullName = card.getName();
		if (cardFullName.indexOf("CARD-") == -1) {
			String chassisName = device.getChassisList().iterator().next()
					.getName();
			cardFullName = "CARD-" + chassisName + "/" + card.getName();

		}
		cardElement.setInstanceName(cardFullName);

		portElement = smartsBuilderService.createPort(element,
				port.getPortKey());

		smartsBuilderService.insertRelationshipsBetweenClassInstances(
				cardElement.getClassName(), cardElement.getInstanceName(),
				portElement.getClassName(), portElement.getInstanceName(),
				"Realizes");

		smartsBuilderService.setSmartsInstanceBooleanAttributeValue(
				portElement.getClassName(), portElement.getInstanceName(),
				"SuppressDisabledNotifications", false);

		if (port.getType() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					portElement.getClassName(), portElement.getInstanceName(),
					"Type", port.getType());
		}
		if (port.getRackNumber() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					portElement.getClassName(), portElement.getInstanceName(),
					"RackNumber", port.getRackNumber());
		}
		if (port.getShelfNumber() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					portElement.getClassName(), portElement.getInstanceName(),
					"ShelfNumber", port.getShelfNumber());
		}
		if (port.getSlotNumber() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					portElement.getClassName(), portElement.getInstanceName(),
					"SlotNumber", port.getSlotNumber());
		}
		if (port.getDescription() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					portElement.getClassName(), portElement.getInstanceName(),
					"Description", port.getDescription());
		}

		smartsBuilderService.setSmartsInstanceIntegerAttributeValue(
				portElement.getClassName(), portElement.getInstanceName(),
				"PortNumber", Integer.parseInt(port.getPortNumber()));

		if (port.getMaxSpeed() != null) {
			try {
				smartsBuilderService.setSmartsInstanceLongAttributeValue(
						portElement.getClassName(),
						portElement.getInstanceName(), "MaxSpeed",
						Long.parseLong(port.getMaxSpeed().trim()));
			} catch (Exception e) {
				log.error("maxSpeed is not a number " + port.getMaxSpeed());
			}
		}
		
		if (ClassFactory.getEnum() == EnumAdapterName.ALUFTTH) {
			String displayName = createAluDisplayName(cardElement.getInstanceName());
			if (displayName!=null){ 
				smartsBuilderService.setSmartsInstanceStringAttributeValue(
						portElement.getClassName(),
						portElement.getInstanceName(),
						"DisplayName",
						portElement.getInstanceName() + " ["
								+ displayName+"/"+port.getPortNumber() + "]");
			}
		}
		smartsBuilderService.manageOperation(portElement);

		return portElement;
	}

	private IonixElement createInterfaceLogic(Interface interface1,
			Device device) throws SmartsException {

		IonixElement element = new IonixElement();
		element.setClassName(device.getCreationClassName());
		element.setInstanceName(device.getName());
		IonixElement interfaceElement = null;

		interfaceElement = smartsBuilderService.createInterface(element,
				interface1);

		if (interface1.getDescription() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "Description",
					interface1.getDescription());
		}
		if (interface1.getDeviceId() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "DeviceID",
					interface1.getDeviceId());
		}
		if (interface1.getInterfaceNumber() != null) {
			smartsBuilderService.setSmartsInstanceIntegerAttributeValue(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "InterfaceNumber",
					Integer.parseInt(interface1.getInterfaceNumber()));
		}
		if (interface1.getMaxTransferUnit() != null) {
			smartsBuilderService.setSmartsInstanceIntegerAttributeValue(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "MaxTransferUnit",
					Integer.parseInt(interface1.getMaxTransferUnit()));
		}
		if (interface1.getMaxSpeed() != null) {
			try {
				smartsBuilderService.setSmartsInstanceLongAttributeValue(
						interfaceElement.getClassName(),
						interfaceElement.getInstanceName(), "MaxSpeed",
						Long.parseLong(interface1.getMaxSpeed().trim()));
			} catch (NumberFormatException e) {
				log.error(" max speed is not a number "
						+ interface1.getMaxSpeed());
			}
		}
		if (interface1.getType() != null) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "Type",
					interface1.getType());
		}

		smartsBuilderService.insertRelationshipsBetweenClassInstances(
				element.getClassName(), element.getInstanceName(),
				interfaceElement.getClassName(),
				interfaceElement.getInstanceName(), "ComposedOf");

		createConnectionInterfaceCard(interfaceElement);

		if (interface1.getParentInterface() != null) {
			IonixElement deviceElement = new IonixElement();
			deviceElement.setClassName(device.getCreationClassName());
			deviceElement.setInstanceName(device.getName());
			IonixElement parentElement = smartsReadService.getInterfaceByKey(
					deviceElement, interface1.getParentInterface());
			if (parentElement != null
					&& !parentElement.getInstanceName().equalsIgnoreCase("")) {

				smartsBuilderService.insertRelationshipsBetweenClassInstances(
						interfaceElement.getClassName(),
						interfaceElement.getInstanceName(),
						parentElement.getClassName(),
						parentElement.getInstanceName(), "LayeredOver");

			} else {
				log.error("no parent with this name "
						+ interface1.getParentInterface());
			}
		}

		return interfaceElement;
	}

	public void createConnectionInterfaceCard(IonixElement ionixElement,
			String interfacekey) {

		IonixElement interfaceElement = smartsReadService.getInterfaceByKey(
				ionixElement, interfacekey);
		if (interfaceElement != null)
			createConnectionInterfaceCard(interfaceElement);
	}

	private void createConnectionInterfaceCard(IonixElement interfaceElement) {
		String cardName = interfaceElement.getInstanceName().substring(0,
				interfaceElement.getInstanceName().lastIndexOf("/"));
		// for IPSC there is just one card so we do not do this check
		if (cardName.indexOf("/") != -1 && cardName.indexOf("Tunnel") == -1
				&& cardName.indexOf("Virtual") == -1
				&& cardName.indexOf("Atm-Trunk") == -1
				&& cardName.indexOf("/0/0") == -1
				&& ClassFactory.getEnum() != EnumAdapterName.HWIPSC) {
			cardName = cardName.replace("IF-", "CARD-");
			try {

				smartsBuilderService.insertRelationshipsBetweenClassInstances(
						"Card", cardName, interfaceElement.getClassName(),
						interfaceElement.getInstanceName(), "Realizes");
			} catch (Exception e) {
				log.error(" card not found " + cardName);
			}

		}
	}

	private void finishImportLogic(Ipam ipam) throws SmartsException {

		//

		List<VLan> listVlan = smartsReadService
				.startReadingVLANAfterImportSmarts();
		for (VLan vlan : listVlan) {
			if (vlan.getDeviceNameList() != null
					&& vlan.getDeviceNameList().size() > 0) {
				createConnectionVLanDevice(vlan, ipam);

			} else {
				IonixElement ionixElement = new IonixElement();
				ionixElement.setClassName("VLAN");
				ionixElement.setInstanceName(vlan.getName());
				smartsDeleteService.removeObject(ionixElement);
				log.info("deleted vlan " + vlan.getName() + " from "
						+ remoteDomainManager.getNameDomain());

			}

		}
		log.debug("run reconfigure on smarts "+ipam.getName());
		smartsBuilderService.reconfigure();

		// globalTopologyCollection creation we do not use for zte
		// if (ClassFactory.getEnum() != EnumAdapterName.HWZTE){

		GlobalTopologyCollection globalTopologyCollection = new GlobalTopologyCollection();
		String nameAdap = PropertiesContainer.getInstance().getProperty(
				"ADAPTER_NAME");
		globalTopologyCollection.setName("GTC-" + nameAdap);

		// clean previous state
		smartsReadService.findGlobalTypeCollection(globalTopologyCollection);

		// set admin status down
		for (Device device : ipam.getDeviceList()) {

			// clean object under this device that are in GlobalTypeCollection
			cleanItemInGlobalTypeCollection(globalTopologyCollection,
					device.getName());

			for (Card card : device.getCardList()) {
				// change status card using topology information
				if (card.getFlagStatus() == Status.UPDATED
						|| card.getFlagStatus() == Status.ADDED
						|| card.getFlagStatus() == Status.NOCHANGES
						|| card.getFlagStatus() == Status.COMPLETED) {
					IonixElement cardElement = new IonixElement();
					try {

						cardElement.setClassName("Card");
						cardElement.setInstanceName("CARD-" + device.getName()
								+ "/" + card.getNameForSmarts(false));
						if (ClassFactory.getEnum() == EnumAdapterName.HWBRAS
								|| ClassFactory.getEnum() == EnumAdapterName.HWMETE) {
							log.debug("changing status for card "+card.getName()+" under device "+device.getName());
							cardElement.setInstanceName("CARD-"
									+ device.getName() + "/"
									+ card.getNameForSmarts(true));
							if (!card.getStatus()
									.equalsIgnoreCase("IN_SERVICE")) {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "CRITICAL");
								globalTopologyCollection.getCardList()
										.add(card);
							} else {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "OK");
							}
						} else if (ClassFactory.getEnum() == EnumAdapterName.HWFFTH
								|| ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN
								|| ClassFactory.getEnum() == EnumAdapterName.HWMSAN) {
							if (card.getStatus().equalsIgnoreCase("offline")) {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "CRITICAL");
								card.setName(cardElement.getInstanceName());
								globalTopologyCollection.getCardList()
										.add(card);
							} else {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "OK");
								/*
								 * if (cardElement.getInstanceName()
								 * .equalsIgnoreCase("CARD-MA5600T/0/1")) {
								 * card.setName(cardElement.getInstanceName());
								 * globalTopologyCollection
								 * .getCardList().add(card); }
								 */
							}
						} else if (ClassFactory.getEnum() == EnumAdapterName.ALUFTTH) {
							if (card.getStatus().equalsIgnoreCase("DOWN")) {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "CRITICAL");
								card.setName(cardElement.getInstanceName());
								globalTopologyCollection.getCardList()
										.add(card);
							} else {
								smartsAlarmService.changeStatusObject(
										cardElement, "Status", "OK");
								/*
								 * if (cardElement.getInstanceName()
								 * .equalsIgnoreCase("CARD-MA5600T/0/1")) {
								 * card.setName(cardElement.getInstanceName());
								 * globalTopologyCollection
								 * .getCardList().add(card); }
								 */
							}
						}

					} catch (SmartsException e) {
						log.error("impossible to change status for card "
								+ cardElement.getInstanceName() + " in ipam "
								+ remoteDomainManager.getNameDomain());
					}
				}
				// change status port

				for (Port port : card.getPortList()) {
					if (ClassFactory.getEnum() == EnumAdapterName.ALUFTTH || ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN) {
						if (port.getFlagStatus() == Status.UPDATED
								|| port.getFlagStatus() == Status.ADDED
								|| port.getFlagStatus() == Status.NOCHANGES
								|| port.getFlagStatus() == Status.COMPLETED) {
							IonixElement portElement = new IonixElement();
							try {

								portElement.setClassName("Port");
								portElement.setInstanceName("PORT-"
										+ device.getName() + "/"
										+ port.getNameForSmarts());

								smartsAlarmService.changeStatusObject(
										portElement, "AdminStatus",
										port.getAdminStatus());
								smartsAlarmService.changeStatusObject(
										portElement, "OperStatus",
										port.getOperStatus());

								// save object in globalTopologyCollections when
								// admin
								// is down
								if (port.getAdminStatus().equalsIgnoreCase(
										"DOWN")) {
									globalTopologyCollection.getPortList().add(
											port);
								}
							} catch (SmartsException e) {
								log.error("impossible to change status for port "
										+ portElement.getInstanceName()
										+ " in ipam "
										+ remoteDomainManager.getNameDomain());
							}

						}

					}

					else if (port.getType().equalsIgnoreCase(Port.DEFAULTTYPE)) {
						if (port.getFlagStatus() == Status.UPDATED
								|| port.getFlagStatus() == Status.ADDED
								|| port.getFlagStatus() == Status.NOCHANGES
								|| port.getFlagStatus() == Status.COMPLETED) {
							IonixElement portElement = new IonixElement();
							try {

								portElement.setClassName("Port");
								portElement.setInstanceName("PORT-"
										+ device.getName() + "/"
										+ port.getNameForSmarts());
								if (port.getAdminStatus().equalsIgnoreCase(
										"Deactive") || port.getAdminStatus().equalsIgnoreCase(
										"DOWN")) {
									smartsAlarmService.changeStatusObject(
											portElement, "AdminStatus", "DOWN");
									smartsAlarmService.changeStatusObject(
											portElement, "OperStatus", "DOWN");
									// save object in globalTopologyCollection
									globalTopologyCollection.getPortList().add(
											port);

								} else {
									smartsAlarmService.changeStatusObject(
											portElement, "AdminStatus", "UP");
									smartsAlarmService.changeStatusObject(
											portElement, "OperStatus", "UP");
								}
							} catch (SmartsException e) {
								log.error("impossible to change status for port "
										+ portElement.getInstanceName()
										+ " in ipam "
										+ remoteDomainManager.getNameDomain());
							}
						}
					}

				}
			}

			// change status interface
			for (Interface interface1 : device.getInterfaceList()) {

				if (interface1.getFlagStatus() == Status.UPDATED
						|| interface1.getFlagStatus() == Status.ADDED
						|| interface1.getFlagStatus() == Status.NOCHANGES
						|| interface1.getFlagStatus() == Status.COMPLETED) {

					IonixElement deviceElement = new IonixElement();
					IonixElement element = new IonixElement();
					deviceElement.setClassName(device.getCreationClassName());
					deviceElement.setInstanceName(device.getName());
					try {
						element = smartsAlarmService.getInterfaceByKey(
								deviceElement, interface1.getDeviceId());
						if (element != null
								&& element.getInstanceName() != null) {
							if (interface1.getAdminStatus() != null
									&& interface1.getAdminStatus()
											.equalsIgnoreCase("down")) {

								smartsAlarmService.changeStatusObject(element,
										"AdminStatus", "DOWN");

								// save object in globalTopologyCollection
								globalTopologyCollection.getInterfaceList()
										.add(interface1);
							} else {
								smartsAlarmService.changeStatusObject(element,
										"AdminStatus", "UP");

							}
							if (interface1.getOperStatus() != null
									&& interface1.getOperStatus()
											.equalsIgnoreCase("down")) {
								smartsAlarmService.changeStatusObject(element,
										"OperStatus", "DOWN");

							} else {

								smartsAlarmService.changeStatusObject(element,
										"OperStatus", "UP");
							}
							if (ClassFactory.getEnum() == EnumAdapterName.HWZTE) {
								smartsAlarmService.changeStatusObject(element,
										"IsFlapping", false);

							}
						} else {
							log.warn("interface " + interface1.getDeviceId()
									+ " not found under device "
									+ deviceElement.getInstanceName());
						}
					} catch (SmartsException e) {
						log.error("impossible to change status for interface "
								+ element.getInstanceName() + " in ipam "
								+ remoteDomainManager.getNameDomain());
					}

				}
			}

		}
		// update or create GlobalTypeCollection

		createGlobalTypeCollection(globalTopologyCollection);
	}

	private void removeObjectLogic(IonixElement element) throws SmartsException {

		smartsDeleteService.removeObject(element);

	}

	private void commonCreationDevice(Device device, IonixElement element)
			throws SmartsException {
		if (device.getFlagStatus() == Status.ADDED) {
			Set<Ip> ipList = device.getIpList();
			for (Ip ip : ipList) {
				ip.setFlagStatus(Status.ADDED);
				// createIp(ip, device);
			}
			Set<Chassis> chassisList = device.getChassisList();
			for (Chassis chassis : chassisList) {
				chassis.setFlagStatus(Status.ADDED);
			}
			Set<SNMPAgent> snmpAgentList = device.getSnmpAgentList();
			for (SNMPAgent snmpAgent : snmpAgentList) {
				snmpAgent.setFlagStatus(Status.ADDED);
			}
			Set<Interface> interfaceList = device.getInterfaceList();
			for (Interface interface1 : interfaceList) {
				interface1.setFlagStatus(Status.ADDED);
				if (interface1.getMac() != null)
					interface1.getMac().setFlagStatus(Status.ADDED);
				for (VLan vlan : interface1.getVlanList()) {
					vlan.setFlagStatus(Status.ADDED);
				}
				for (Ip ip : interface1.getIpList()) {
					ip.setFlagStatus(Status.ADDED);
				}
			}
			Set<Card> cardList = device.getCardList();
			for (Card card : cardList) {
				card.setFlagStatus(Status.ADDED);
				for (Port port : card.getPortList()) {
					port.setFlagStatus(Status.ADDED);
					for (VLan vlan : port.getVlanList()) {
						vlan.setFlagStatus(Status.ADDED);
					}
				}

			}
			Set<Narg> nargList = device.getNargList();
			for (Narg narg : nargList) {
				narg.setFlagStatus(Status.ADDED);

			}
		}

		// change attributes
		if (device.getModel() != null
				&& !device.getModel().equalsIgnoreCase(""))
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(), "Model",
					device.getModel());
		if (device.getFlagStatus() == Status.ADDED) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(),
					"DiscoveredFirstAt",
					(new DateTime()).toString("dd-MMM-YYYY HH:mm:ss"));
		}
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				element.getClassName(), element.getInstanceName(),
				"DiscoveredLastAt",
				(new DateTime()).toString("dd-MMM-YYYY HH:mm:ss"));

		if (device.getDescription() != null
				&& !device.getDescription().equalsIgnoreCase("")) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(),
					"Description", device.getDescription());
		}

		if (device.getOsVersion() != null
				&& !device.getOsVersion().equalsIgnoreCase("")) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(),
					"OSVersion", device.getOsVersion());
		}
		if (device.getLocation() != null
				&& !device.getLocation().equalsIgnoreCase("")) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(),
					"Location", device.getLocation());
		}
		if (device.getVendor() != null
				&& !device.getVendor().equalsIgnoreCase("")) {
			smartsBuilderService.setSmartsInstanceStringAttributeValue(
					element.getClassName(), element.getInstanceName(),
					"Vendor", device.getVendor());
		}

	}

	public void updateDiscoveredLastAt(Device device) throws SmartsException {
		IonixElement element = new IonixElement();
		if (device.getType().equalsIgnoreCase(Device.CLASSNAMESWITCH)) {
			element.setClassName(Device.CLASSNAMESWITCH);
		} else if (device.getType().equalsIgnoreCase(Device.CLASSNAMEROUTER)) {
			element.setClassName(Device.CLASSNAMEROUTER);
		}
		element.setInstanceName(device.getName());
		smartsBuilderService.setSmartsInstanceStringAttributeValue(
				element.getClassName(), element.getInstanceName(),
				"DiscoveredLastAt",
				(new DateTime()).toString("dd-MMM-YYYY HH:mm:ss"));
	}

	public void createRouter(Device device) throws SmartsException {

		// makeSwitch
		IonixElement routerElement = smartsBuilderService.createRouter(
				device.getName(), device.getCreationClassName());

		commonCreationDevice(device, routerElement);

	}

	public void createSwitch(Device device) throws SmartsException {

		// makeSwitch
		IonixElement switchElement = smartsBuilderService.createSwitch(
				device.getName(), device.getCreationClassName());

		commonCreationDevice(device, switchElement);

	}

	private IonixElement removeVlanConnectionLogic(VLan vlan, Item item,
			Device device) throws SmartsException {

		IonixElement vlanElement = null;
		try {
			vlanElement = smartsBuilderService.createVLAN(vlan.getVLANkey());
		} catch (SmartsException e1) {
			log.error("error creating VLAN " + e1.getMessage());
			throw e1;
		}
		String fullName = "";
		if (item instanceof Port) {
			fullName = "PORT-" + device.getName() + "/"
					+ ((Port) item).getPortKey();
		}
		if (item instanceof Interface) {
			IonixElement deviceElement = new IonixElement();
			deviceElement.setClassName(device.getCreationClassName());
			deviceElement.setInstanceName(device.getName());
			IonixElement parentElement = smartsReadService.getInterfaceByKey(
					deviceElement, ((Interface) item).getDeviceId());
			fullName = parentElement.getInstanceName();
		}
		IonixElement ionixElement = new IonixElement();
		ionixElement.setClassName(item.getCreationClassName());
		ionixElement.setInstanceName(fullName);
		smartsBuilderService.removeRelationshipsBetweenClassInstances(
				vlanElement.getClassName(), vlanElement.getInstanceName(),
				ionixElement.getClassName(), ionixElement.getInstanceName(),
				"ComposedOf");
		return vlanElement;
	}

	private boolean removeNetworkConnectionLinkLogic(Ncrg ncrg,
			NetworkConnection networkConnection) throws SmartsException {

		smartsBuilderService.removeRelationshipsBetweenClassInstances(
				ncrg.getCreationClassName(), ncrg.getName(),
				networkConnection.getCreationClassName(),
				networkConnection.getName(), "ComposedOf");
		return true;
	}

	private IonixElement createVlanLogic(VLan vlan, Item item, Device device)
			throws SmartsException {

		IonixElement vlanElement = null;
		try {
			vlanElement = smartsBuilderService.createVLAN(vlan.getVLANkey());
		} catch (SmartsException e1) {
			log.error("error creating VLAN " + e1.getMessage());
			throw e1;
		}

		smartsBuilderService.setSmartsInstanceIntegerAttributeValue(
				vlanElement.getClassName(), vlanElement.getInstanceName(),
				"VLANNumber", Integer.parseInt(vlan.getVLANkey()));

		String fullName = "";
		if (item instanceof Port) {
			fullName = "PORT-" + device.getName() + "/"
					+ ((Port) item).getPortKey();
		}
		if (item instanceof Interface) {
			IonixElement deviceElement = new IonixElement();
			deviceElement.setClassName(device.getCreationClassName());
			deviceElement.setInstanceName(device.getName());
			IonixElement parentElement = smartsReadService.getInterfaceByKey(
					deviceElement, ((Interface) item).getDeviceId());
			fullName = parentElement.getInstanceName();
		}

		IonixElement ionixElement = new IonixElement();
		ionixElement.setClassName(item.getCreationClassName());
		ionixElement.setInstanceName(fullName);

		try {
			smartsBuilderService.insertRelationshipsBetweenClassInstances(
					vlanElement.getClassName(), vlanElement.getInstanceName(),
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "ComposedOf");
		} catch (SmartsException e) {
			log.error("error creating relationship ComposedOf between VLAN "
					+ vlanElement.getInstanceName() + " and "
					+ ionixElement.getClassName() + " "
					+ ionixElement.getInstanceName());
		}

		return vlanElement;
	}

	public void createConnectionVLanDevice(VLan vlan, Ipam ipam)
			throws SmartsException {
		IonixElement vlanElement = null;

		for (String deviceName : vlan.getDeviceNameList()) {
			// if device starts with DELETE- it means we need to delete all
			// connection between vlan and that device
			boolean deleteFlag = false;
			if (deviceName.startsWith("DELETE")) {
				deleteFlag = true;
				deviceName = deviceName.replace("DELETE-", "");
			}
			for (Device device : ipam.getDeviceList()) {
				if (device.getName().equalsIgnoreCase(deviceName)) {

					IonixElement deviceElement = new IonixElement();
					deviceElement.setClassName(device.getCreationClassName());
					deviceElement.setInstanceName(device.getName());
					try {
						vlanElement = smartsBuilderService.createVLAN(vlan
								.getVLANkey());
						if (deleteFlag == false) {
							smartsBuilderService
									.insertRelationshipsBetweenClassInstances(
											vlanElement.getClassName(),
											vlanElement.getInstanceName(),
											deviceElement.getClassName(),
											deviceElement.getInstanceName(),
											"LayeredOver");
						} else {
							smartsBuilderService
									.removeRelationshipsBetweenClassInstances(
											vlanElement.getClassName(),
											vlanElement.getInstanceName(),
											deviceElement.getClassName(),
											deviceElement.getInstanceName(),
											"LayeredOver");
						}
					} catch (SmartsException e) {

						log.error("error creating relationship LayeredOver between VLAN "
								+ vlanElement.getInstanceName()
								+ " and device "
								+ deviceElement.getInstanceName());
						throw e;
					}
					try {
						if (deleteFlag == false) {
							smartsBuilderService
									.insertRelationshipsBetweenClassInstances(
											vlanElement.getClassName(),
											vlanElement.getInstanceName(),
											deviceElement.getClassName(),
											deviceElement.getInstanceName(),
											"ConnectedSystems");
						} else {
							smartsBuilderService
									.removeRelationshipsBetweenClassInstances(
											vlanElement.getClassName(),
											vlanElement.getInstanceName(),
											deviceElement.getClassName(),
											deviceElement.getInstanceName(),
											"ConnectedSystems");

						}
					} catch (SmartsException e) {

						log.error("error creating relationship ConnectedSystems between VLAN "
								+ vlanElement.getInstanceName()
								+ " and device "
								+ deviceElement.getInstanceName());
						throw e;
					}
					break;
				}

			}

		}
	}

	private boolean cleanItemInGlobalTypeCollection(
			GlobalTopologyCollection globalTopologyCollection, String name)
			throws SmartsException {

		for (Interface interface1 : globalTopologyCollection.getInterfaceList()) {

			if (interface1.getName().indexOf("IF-" + name) != -1) {
				// cancel this interface from the group
				removeGlobalTypeCollectionLogic(globalTopologyCollection,
						interface1);
				interface1 = null;
			}
		}

		for (Card card : globalTopologyCollection.getCardList()) {

			if (card.getName().indexOf("CARD-" + name) != -1) {
				// cancel this card from the group
				removeGlobalTypeCollectionLogic(globalTopologyCollection, card);
				card = null;
			}
		}

		for (Port port : globalTopologyCollection.getPortList()) {

			if (port.getName().indexOf("PORT-" + name) != -1) {
				// cancel this interface from the group
				removeGlobalTypeCollectionLogic(globalTopologyCollection, port);
				port = null;
			}
		}

		return true;

	}

	private boolean removeGlobalTypeCollectionLogic(
			GlobalTopologyCollection globalTopologyCollection, Item item)
			throws SmartsException {

		smartsBuilderService.removeRelationshipsBetweenClassInstances(
				globalTopologyCollection.getCreationClassName(),
				globalTopologyCollection.getName(),
				item.getCreationClassName(), item.getName(), "ConsistsOf");
		return true;
	}

	private boolean createGlobalTypeCollection(
			GlobalTopologyCollection globalTopologyCollection)
			throws SmartsException {

		if (globalTopologyCollection != null) {

			IonixElement element = smartsBuilderService
					.createGlobalTypeCollection(globalTopologyCollection
							.getName());

			for (Card card : globalTopologyCollection.getCardList()) {
				if (card != null) {
					try {// I use MemberOf because I am going from child to parent
						smartsBuilderService
								.insertRelationshipsBetweenClassInstances(
										card.getCreationClassName(),
										card.getName(), element.getClassName(),
										element.getInstanceName(), "MemberOf");
					} catch (Exception e) {
						log.warn("error adding card "+card.getName());
					}
				}

			}
			for (Port port : globalTopologyCollection.getPortList()) {
				if (!port.getDisplayName().equalsIgnoreCase("")) {
					try {
						smartsBuilderService
								.insertRelationshipsBetweenClassInstances(
										port.getCreationClassName(),
										port.getDisplayName(),
										element.getClassName(),
										element.getInstanceName(), "MemberOf");
					} catch (Exception e) {
						log.warn("error adding port "+port.getDisplayName());
					}
				}
			}
			for (Interface interface1 : globalTopologyCollection
					.getInterfaceList()) {
				if (interface1 != null) {
					try {
					smartsBuilderService
							.insertRelationshipsBetweenClassInstances(
									interface1.getCreationClassName(),
									interface1.getName(),
									element.getClassName(),
									element.getInstanceName(), "MemberOf");
					} catch (Exception e) {
						log.warn("error adding interface "+interface1.getName());

					}
				}

			}

		} else {
			log.error("error creating object "
					+ globalTopologyCollection.getName());
			return false;
		}

		return true;

	}

}
