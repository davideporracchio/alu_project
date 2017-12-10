package com.holonomix.ionix.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.IgnoreDeviceFileService;
import com.holonomix.file.service.SeedFileService;
import com.holonomix.hsqldb.model.Card;
import com.holonomix.hsqldb.model.Chassis;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.GlobalTopologyCollection;
import com.holonomix.hsqldb.model.Interface;
import com.holonomix.hsqldb.model.Ip;
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
import com.holonomix.icadapter.ionix.TopologyBrowser;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.service.ModelUtility;

public class SmartsReadService {

	private static final Logger log = Logger.getLogger(SmartsReadService.class);

	private PropertiesContainer propertiesContainer;
	TopologyBrowser topologyBrowser;
	private IgnoreDeviceFileService ignoreDeviceFileService;
	public SmartsReadService(RemoteDomainManager remoteDomainManager) {

		topologyBrowser = new TopologyBrowser(remoteDomainManager);
		propertiesContainer = PropertiesContainer.getInstance();
		ignoreDeviceFileService = IgnoreDeviceFileService.getInstance(propertiesContainer.getProperty("SMARTS_DEVICES_TO_IGNORE"));
		ignoreDeviceFileService.updateList();
		
	}

	public IonixElement getInterfaceByKey(IonixElement ionixElement,
			String interfaceKey) {

		try {
			return topologyBrowser
					.getInterfaceByKey(ionixElement, interfaceKey);

		} catch (SmartsException e) {
			// TODO Auto-generated catch block
			log.error(" no interface with this key: " + interfaceKey);
			return null;
		}
	}

	public IonixElement getPortByDescription(IonixElement ionixElement,
			String description) throws SmartsException{

		try {
			return topologyBrowser.getPortByDescription(ionixElement,
					description);

		} catch (SmartsException e) {
			if (e.getTypeError() == 3) {
				throw e;
			}
			log.warn(" no port with this description: " + description);
			return null;
		}
	}
	
	private boolean matchModel (String modelFromFile,String model){
		
		Pattern pattern = Pattern.compile(modelFromFile);
		Matcher matcher = pattern.matcher(model);
		if(matcher.find()){
			if(matcher.group().equalsIgnoreCase(model))
			return true;
		}
		return false;
	}

	public Map<String, Device> readingSmartsDevice() throws SmartsException {

		List<String> classInstanceList = topologyBrowser
				.getListFromString("EMS_DEVICEMAP");

		List<String> deviceProperties = topologyBrowser
				.getListFromString("LIST_DEVICEPROPERTIES");
		Map<String, Device> mapDevices = new HashMap<String, Device>();

		for (String instance : classInstanceList) {

			String classInstance = instance.split("::")[1];
			// setup smart connection

			// first call to have the list of devices
			List<String> scInstanceLValList = topologyBrowser
					.getClassInstanceNames(classInstance);

			//

			int index = 0;
			for (String deviceName : scInstanceLValList) {
				if(!isValidDevice(deviceName)) continue;
				log.debug(" device " + index + " : " + deviceName);
				// create device and put in the map
				Device device = new Device();
				device.setName(deviceName);
				device.setCreationClassName(classInstance);
				index++;
				// find property for this device
				setProperties(classInstance, deviceName, deviceProperties,
						device);

				if (matchModel(instance.split("::")[0],device.getModel())) {

					mapDevices.put(deviceName, device);

				}

			}

		}

		return mapDevices;

	}

	
	public Map<String, Device> startReadingSmarts(String name)
			throws SmartsException {

		List<String> classInstanceList = topologyBrowser
				.getListFromString("EMS_DEVICEMAP");

		List<String> deviceProperties = topologyBrowser
				.getListFromString("LIST_DEVICEPROPERTIES");
		Map<String, Device> mapDevices = new HashMap<String, Device>();

		for (String instance : classInstanceList) {

			String classInstance = instance.split("::")[1];
			// setup smart connection

			// first call to have the list of devices
			List<String> scInstanceLValList = topologyBrowser
					.getClassInstanceNames(classInstance);

			//

			int index = 0;
			for (String deviceName : scInstanceLValList) {
				if(!isValidDevice(deviceName)) continue;
				populateDevice(name, deviceProperties, mapDevices, instance,
						classInstance, index, deviceName);
			}

		}

		return mapDevices;

	}

	public void populateDevice(String name, List<String> deviceProperties,
			Map<String, Device> mapDevices, String instance,
			String classInstance, int index, String deviceName)
			throws SmartsException {
		if (name == null || deviceName.equalsIgnoreCase(name)) {
			log.debug(" device " + index + " : " + deviceName);
			index++;
			// create device and put in the map
			Device device = new Device();
			device.setName(deviceName);
			device.setCreationClassName(classInstance);

			// find property for this device
			setProperties(classInstance, deviceName, deviceProperties,
					device);

			if (matchModel(instance.split("::")[0],device.getModel())) {

				mapDevices.put(deviceName, device);

				// chassis
				findChassis(classInstance, deviceName, device);
				// card interface
				findComposedOf(classInstance, deviceName, device);
				// snmpagent
				findHostServices(classInstance, deviceName, device);
				// ip
				// findHostAccessPoints(classInstance, deviceName,
				// device);
				if (ClassFactory.getEnum() == EnumAdapterName.HWMETE || ClassFactory.getEnum() == EnumAdapterName.HWZTE)
					findNarg(deviceName, device);

				
			}

		}
	}

	public List getNetworkConnection() throws SmartsException {

		List<NetworkConnection> list = new ArrayList<NetworkConnection>();
		String classInstance = "NetworkConnection";
		// setup smart connection

		List<String> networkConnectionProperties = topologyBrowser
				.getListFromString("LIST_NETWORKCONNECTIONPROPERTIES");
		// first call to have the list of devices
		List<String> nameInstanceList = topologyBrowser
				.getClassInstanceNames(classInstance);
		for (String nameInstance : nameInstanceList) {
			NetworkConnection networkConnection = new NetworkConnection();
			networkConnection.setName(nameInstance);

			// interface
			try{
			findConnectedTo(classInstance, nameInstance, networkConnection);
			// connected system
			setProperties(classInstance, nameInstance,
					networkConnectionProperties, networkConnection);
			// findConnectedSystem(classInstance, nameInstance,
			// networkConnection);
			
			list.add(networkConnection );
			}catch(SmartsException e){}

		}
		return list;

	}

	public void findNarg(String deviceName, Device device)
			throws SmartsException {

		String classInstance = "NetworkAdapterRedundancyGroup";
		// setup smart connection

		List<String> networkConnectionProperties = topologyBrowser
				.getListFromString("LIST_NETWORKCONNECTIONPROPERTIES");
		// first call to have the list of devices
		List<String> nameInstanceList = topologyBrowser
				.getClassInstanceNames(classInstance);
		for (String nameInstance : nameInstanceList) {
			if (nameInstance.indexOf(deviceName) != -1) {
				Narg narg = new Narg();
				narg.setName(nameInstance);

				// interface
				findComposedOf(classInstance, nameInstance, narg);
				// connected system
				setProperties(classInstance, nameInstance,
						networkConnectionProperties, narg);
				// findConnectedSystem(classInstance, nameInstance,
				// networkConnection);
				device.getNargList().add(narg);
			}

		}

	}

	public void findGlobalTypeCollection(GlobalTopologyCollection globalTopologyCollection)
			throws SmartsException {

		String classInstance = "Global_TopologyCollection";
		// setup smart connection

		// first call to have the list of gtc
		List<String> nameInstanceList = topologyBrowser
				.getClassInstanceNames(classInstance);
		for (String nameInstance : nameInstanceList) {
			if (nameInstance.indexOf(globalTopologyCollection.getName()) != -1) {
				//populate cards and interfaces
				findConsistsOf(classInstance, nameInstance, globalTopologyCollection);
				
			}

		}

	}

	public void findExistingNcrg(Device device) throws SmartsException {

		String classInstance = "NetworkConnectionRedundancyGroup";
		// setup smart connection

		List<String> networkConnectionProperties = topologyBrowser
				.getListFromString("LIST_NETWORKCONNECTIONPROPERTIES");
		// first call to have the list of devices
		List<String> nameInstanceList = topologyBrowser
				.getClassInstanceNames(classInstance);
		for (String nameInstance : nameInstanceList) {
			if (nameInstance.indexOf(device.getName()) != -1) {
				Ncrg ncrg = new Ncrg();
				ncrg.setName(nameInstance);

				// interface
				findComposedOf(classInstance, nameInstance, ncrg);
				// connected system
				setProperties(classInstance, nameInstance,
						networkConnectionProperties, ncrg);
				// findConnectedSystem(classInstance, nameInstance,
				// networkConnection);
				boolean isadded = device.getNcrgList().add(ncrg);

			}

		}

	}

	public void findNcrg(Device device) throws SmartsException {

		findNarg(device.getName(), device);
		List<String> interfaceProperties = topologyBrowser
				.getListFromString("LIST_INTERFACEPROPERTIES");
		interfaceProperties.add("Name");
		log.debug("LOG--:device "+device.getName()+ " has "+device.getNargList().size()+" nargs");
		
		for (Narg narg : device.getNargList()) {
			List<String> networkConnectionList = new ArrayList<String>();
			
			String nargZ = "";
			boolean nargsAreDifferent = false;
			log.debug("LOG--:narg "+narg.getName()+ " has "+narg.getInterfaceList().size()+" interfaces");
			
			for (Interface interface1 : narg.getInterfaceList()) {
//nargZ = ""; // CHRIS LOWTH - Experimental / untested
				try {
					
					IonixElement element = new IonixElement();
					element.setClassName(device.getCreationClassName());
					element.setInstanceName(device.getName());
					IonixElement interfaceElement = topologyBrowser
							.getInterfaceByKey(element,
									interface1.getDeviceId());
					if (interfaceElement != null) {
						setProperties(interfaceElement.getClassName(),
								interfaceElement.getInstanceName(),
								interfaceProperties, interface1);
						List<String> connectedViaList = topologyBrowser
								.getRelationList(
										interfaceElement.getClassName(),
										interfaceElement.getInstanceName(),
										"ConnectedVia");
log.debug("LOWTH: interface "+interfaceElement.getInstanceName()+" connected via:"+connectedViaList.size());
						if (connectedViaList.size() == 1) {
							for (String networkConnectionName : connectedViaList) {
								if(networkConnectionName.indexOf("::")==-1){
									log.error("LOG--:networkconnection name is not following convention "+networkConnectionName);
								}	
								List<String> interfaceList = topologyBrowser
										.getRelationList("NetworkConnection",
												networkConnectionName
														.split("::")[1],
												"ConnectedTo");
log.debug("LOWTH: connection "+networkConnectionName+" connected to: "+interfaceList.size());
								for (String interfaceNameZ : interfaceList) {
									if(interfaceNameZ.indexOf("::")==-1){
										log.error("LOG--:interfaceNameZ name is not following convention "+interfaceNameZ);
										return;
									}
log.debug("LOWTH: interface1 name="+interface1.getName() + " interfaceNameZ ="+ interfaceNameZ.split("::")[1]);
									if (!interfaceNameZ.split("::")[1]
											.equalsIgnoreCase(interface1
													.getName())) {
										List<String> nargList = topologyBrowser
												.getRelationList(
														"Interface",
														interfaceNameZ
																.split("::")[1],
														"PartOf");
log.debug("LOWTH: interfaceZ "+interfaceNameZ+" part of:"+nargList.size());
										for (String nargTemp : nargList) {
											log.debug("LOG--:nargTemp name "+nargTemp +  interfaceNameZ +" networkConnectionName "+networkConnectionName);
											if(nargTemp.indexOf("::")==-1){
												log.error("LOG--:nargTemp name is not following convention "+nargTemp);
												return;
											}
											
											if (nargTemp.split("::")[0]
													.equalsIgnoreCase("NetworkAdapterRedundancyGroup")) {
												log.debug("LOG--:found NetworkAdapterRedundancyGroup "+nargTemp);
												if (nargZ.equalsIgnoreCase("")) {
													nargZ = nargTemp;
log.debug("LOWTH: adding connection "+networkConnectionName+" to list because nargZ was blank");
													networkConnectionList
															.add(networkConnectionName);
												} else if (!nargZ
														.equalsIgnoreCase(nargTemp)) {
													// NARG ARE DIFFERENT ERROR
													nargsAreDifferent = true;
log.debug("LOWTH: not adding connection "+networkConnectionName+" to list because nargs are different" + "   nargz="+nargZ +"  nargTemp="+nargTemp );													
												} else {
													networkConnectionList
															.add(networkConnectionName);
log.debug("LOWTH: adding connection "+networkConnectionName+" to list");
												}

											}
										}
									}

								}
							}
						}
					}
				} catch (Exception e) {
					log.error("error creating ncrg  for narg " + narg.getName());
				}
			}
log.debug("LOWTH: nargsAreDefferent = "+nargsAreDifferent);
log.debug("LOWTH: network connection list size = "+networkConnectionList.size());
			if (!nargsAreDifferent && networkConnectionList.size() > 0) {
				Ncrg ncrg = new Ncrg();
				Interface interface1 = new Interface();
				
				interface1.setName(narg.getName().replace("NARG-", ""));
				Interface interface2 = new Interface();
				if(nargZ.indexOf("::")==-1){
					log.error("LOG--:nargTemp name is not following convention "+nargZ);
					return;
				}
				interface2.setName(nargZ.split("::")[1].replace("NARG-", ""));
				ncrg.setName("NCRG-" + interface1.getName() + "<->"
						+ interface2.getName());
				takeParseInterfaceName(interface1);
				takeParseInterfaceName(interface2);
				ncrg.setInterfaceA(interface1);
				ncrg.setInterfaceB(interface2);

				List<String> netoworkConnectionProperties = topologyBrowser
						.getListFromString("LIST_NETWORKCONNECTIONPROPERTIES");
				// ncrg.setName(narg.getName().replace("NARG", "NCRG"));
				// ncrg.setDisplayName(narg.getName().replace("NARG", ""));
				for (String networkConnectionName : networkConnectionList) {
					NetworkConnection n1 = new NetworkConnection();
					if(networkConnectionName.indexOf("::")==-1){
						log.error("LOG--:networkConnectionName name is not following convention "+networkConnectionName);
						return;
					}
					n1.setName(networkConnectionName.split("::")[1]);

					setProperties(n1.getCreationClassName(), n1.getName(),
							netoworkConnectionProperties, n1);

					ncrg.getNetworkConnectionList().add(n1);
				}
				networkConnectionList.clear();
				device.getNcrgList().add(ncrg);
				// create NRCG

			}

		}

	}

	private void takeParseInterfaceName(Interface interface1) {

		String name = interface1.getName();
		String deviceName = name.replace("IF-", "");
		deviceName = deviceName.substring(0, deviceName.indexOf("/"));
		if(name.indexOf("[")!=-1){
		String deviceId = name.substring(name.indexOf("[") + 1,
				name.indexOf("]"));
		interface1.setDeviceId(deviceId);
		interface1.setParentDevice(deviceName);
		}else if( name.indexOf("Eth-")!=-1){
			
			String deviceId = name.substring(name.indexOf("/") + 1);
			interface1.setDeviceId(deviceId);
			interface1.setParentDevice(deviceName);
		} else 
			interface1=null;

	}
	
	

	public void getInfoDevice(Device device) throws SmartsException {
		log.debug("starting infoDevice method for device: "+device.getName());
		List<String> classInstanceList = topologyBrowser
				.getListFromString("INSTANCE_CLASS");
		for (String classInstance : classInstanceList) {

			// setup smart connection

			// first call to have the list of devices
			List<String> scInstanceLValList = topologyBrowser
					.getClassInstanceNames(classInstance);
			for (String deviceName : scInstanceLValList) {
				if (deviceName.equalsIgnoreCase(device.getName())) {
					// snmpagent
					device.setCreationClassName(classInstance);
					log.debug("found device populate snmpagent");
					findHostServices(classInstance, deviceName, device);
					log.debug("completed infoDevice method");
					// ip
					// findHostAccessPoints(classInstance, deviceName, device);
					return;
				}
			}
		}
		log.debug("completed infoDevice method without any result");

	}

	public void getAllIpDevice(Device device) throws SmartsException {

		List<String> classInstanceList = topologyBrowser
				.getListFromString("INSTANCE_CLASS");
		for (String classInstance : classInstanceList) {

			List<String> scInstanceLValList = topologyBrowser
					.getClassInstanceNames(classInstance);
			for (String deviceName : scInstanceLValList) {
				if (deviceName.equalsIgnoreCase(device.getName())) {

					findHostServices(classInstance, deviceName, device);
					for (Ip ip : device.getIpList()) {
						ip.setFlagStatus(Status.DELETED);
					}
					device.setCreationClassName(classInstance);
					// ip
					findHostAccessPoints(classInstance, deviceName, device);

					return;
				}
			}
		}

	}

	

	public List<VLan> startReadingVLANAfterImportSmarts()
			throws SmartsException {

		String classInstance = "VLAN";
		// setup smart connection

		// first call to have the list of vlan
		List<String> scInstanceLValList = topologyBrowser
				.getClassInstanceNames(classInstance);

		int index = 0;
		List<VLan> listVlan = new ArrayList<VLan>();
		for (String vlanName : scInstanceLValList) {
			log.debug(" VLAN " + vlanName);
			// create device and put in the map
			VLan vlan = new VLan();
			vlan.setName(vlanName);

			vlan.setVLANkey(vlanName.replace("VLAN-", ""));

			listVlan.add(vlan);
			// find property for this device
			// card interface
			findComposedOf(classInstance, vlanName, vlan);
			findLayeredOver(classInstance, vlanName, vlan);
			// layeredOver
			index++;

		}
		return listVlan;

	}

	private void findComposedOf(String classInstance, String deviceName,
			Item parent) throws SmartsException {

		try {
			List<String> composedOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "ComposedOf");
			for (String name : composedOfList) {
				Item item = null;
				log.debug("name component: " + name);
				String[] componentClass = name.split("::");

				if (componentClass[0].equalsIgnoreCase("CARD")) {
					List<String> cardProperties = topologyBrowser
							.getListFromString("LIST_CARDPROPERTIES");
					List<String> portProperties = topologyBrowser
							.getListFromString("LIST_PORTPROPERTIES");
					item = new Card();
					if(ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN){
					((Card) item).splitNameWithRack(componentClass[1]
							.substring(componentClass[1].indexOf("/") + 1));
					}else{
						((Card) item).splitName(componentClass[1]
								.substring(componentClass[1].indexOf("/") + 1));
					}
					((Card) item).setName(componentClass[1]
							.substring(componentClass[1].indexOf("/") + 1));
					Set<Card> cardList = null;
					if (parent instanceof Device)
						cardList = ((Device) parent).getCardList();

					// go deep look for port linked to card
					List<String> realizesOfList = topologyBrowser
							.getRelationList(componentClass[0],
									componentClass[1], "Realizes");
					//I exclude adpater without cards
					if (ClassFactory.getEnum() != EnumAdapterName.HWMETE
							&& ClassFactory.getEnum() != EnumAdapterName.HWBRAS
							&& ClassFactory.getEnum() != EnumAdapterName.HWZTE
							&& ClassFactory.getEnum() != EnumAdapterName.HWIPSC) {
						for (String namePort : realizesOfList) {
							Port port = new Port();

							log.debug("name component: " + namePort);
							String[] portClass = namePort.split("::");
							if(ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN){
							port.splitNameWithRack(portClass[1].substring(portClass[1]
									.indexOf("/") + 1));
							}else{
								port.splitName(portClass[1].substring(portClass[1]
										.indexOf("/") + 1));
							}
							port.setName(portClass[1].substring(portClass[1]
									.indexOf("/") + 1));
							setProperties(portClass[0], portClass[1],
									portProperties, port);
							Set<Port> portList = ((Card) item).getPortList();
							portList.add(port);
							findPartOf(portClass[0], portClass[1], port);

						}
					}
					setProperties(componentClass[0], componentClass[1],
							cardProperties, item);
					//item.setName(componentClass[1]);
					cardList.add((Card) item);
				}

				else if (componentClass[0].equalsIgnoreCase("INTERFACE")) {
					List<String> interfaceProperties = topologyBrowser
							.getListFromString("LIST_INTERFACEPROPERTIES");
					item = new Interface();
					Set<Interface> interfaceList = null;
					((Interface) item).setName(componentClass[1]);
					try{
					// device list should be populated
					if (parent instanceof VLan) {
						String nameDevice="";
						//davide
						if(componentClass[1].indexOf("/")!=-1){
						 nameDevice = componentClass[1].substring(3,
								componentClass[1].indexOf("/"));
						}
						else{
							nameDevice=componentClass[1];
						}
						((VLan) parent).getDeviceNameList().add(nameDevice);

					} else if (parent instanceof Narg) {

						setProperties(componentClass[0], componentClass[1],
								interfaceProperties, item);
						((Narg) parent).getInterfaceList()
								.add((Interface) item);

					} else if (parent instanceof Device) {

						interfaceList = ((Device) parent).getInterfaceList();
						setProperties(componentClass[0], componentClass[1],
								interfaceProperties, item);
						// ((Interface) item).splitName(((Interface)
						// item).getDeviceId());

						interfaceList.add(((Interface) item));
						findPartOf(componentClass[0], componentClass[1], item);
						findUnderlying(componentClass[0], componentClass[1],
								item);
					}
					}catch(Exception e){
						
					log.error("interface with wrong name format "+componentClass[1]);
					}

				}

				else if (componentClass[0].equalsIgnoreCase("PORT")
						&& parent instanceof VLan) {

					String nameDevice = componentClass[1].substring(5,
							componentClass[1].indexOf("/"));
					((VLan) parent).getDeviceNameList().add(nameDevice);

				} else if (componentClass[0]
						.equalsIgnoreCase("NETWORKCONNECTION")
						&& parent instanceof Ncrg) {
					List<String> netoworkConnectionProperties = topologyBrowser
							.getListFromString("LIST_NETWORKCONNECTIONPROPERTIES");
					NetworkConnection networkConnection = new NetworkConnection();
					networkConnection.setName(componentClass[1]);
					setProperties(componentClass[0], componentClass[1],
							netoworkConnectionProperties, networkConnection);
					((Ncrg) parent).getNetworkConnectionList().add(
							networkConnection);

				}

			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of ComposedOf. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findPartOf(String classInstance, String deviceName, Item parent)
			throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "PartOf");
			// List<String> deviceProperties =
			// topologyBrowser.getListFromString("LIST_VLANPROPERTIES");
			for (String name : memberOfList) {
				String[] componentClass = name.split("::");
				if (componentClass.length == 2
						&& componentClass[0].equalsIgnoreCase("VLAN")) {
					log.debug("name partof services: " + name);

					VLan vlan = new VLan();
					vlan.setCreationClassName(componentClass[0]);
					vlan.setName(componentClass[1]);
					vlan.setVLANkey(componentClass[1]
							.substring(componentClass[1].indexOf("-") + 1));

					if (parent instanceof Port)
						((Port) parent).getVlanList().add(vlan);
					if (parent instanceof Interface)
						((Interface) parent).getVlanList().add(vlan);

				}

			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of PartOf. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findLayeredOver(String classInstance, String deviceName,
			VLan vlan) throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "LayeredOver");
			List<String> deviceProperties = topologyBrowser
					.getListFromString("LIST_DEVICEPROPERTIES");
			for (String name : memberOfList) {

				log.debug("name layered services: " + name);
				String[] componentClass = name.split("::");
				Device device = new Device();
				device.setCreationClassName(componentClass[0]);
				device.setName(componentClass[1]);
				Set<String> deviceList = null;
				deviceList = vlan.getDeviceNameList();
				if (!deviceList.contains(device.getName())){
					deviceList.add("DELETE-"+device.getName());
				}
				
			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of LayeredOver. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findConsistsOf(String classInstance, String deviceName,
			Item parent) throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "ConsistsOf");
			List<String> interfaceProperties = topologyBrowser
					.getListFromString("LIST_INTERFACEPROPERTIES");
			for (String name : memberOfList) {

				log.debug("name consistOf services: " + name);
				String[] componentClass = name.split("::");
				if (componentClass[0].equalsIgnoreCase("CARD")) {
					Card card = new Card();
					card.setName(componentClass[1]);
					List<String> cardProperties = topologyBrowser
					.getListFromString("LIST_CARDPROPERTIES");
					setProperties(componentClass[0], componentClass[1],
							cardProperties, card);
					((GlobalTopologyCollection) parent).getCardList().add(card);
					
					
				} else if (componentClass[0].equalsIgnoreCase("INTERFACE")) {
					Interface interface1 = new Interface();
					interface1.setName(componentClass[1]);
					setProperties(componentClass[0], componentClass[1],
							interfaceProperties, interface1);
					((GlobalTopologyCollection) parent).getInterfaceList().add(
							interface1);

				} else if (componentClass[0].equalsIgnoreCase("PORT")) {
					Port port = new Port();
					port.setName(componentClass[1]);
					port.setPortKey(componentClass[1]);
					if(ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN){
						port.splitNameWithRack(port.getName().substring(port.getName().indexOf("/")+1));
					}else {
						port.splitName(port.getName().substring(port.getName().indexOf("/")+1));
							
					}
					((GlobalTopologyCollection) parent).getPortList().add(port);

				}

			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of ConsistsOf "
					+ e.getMessage());
			throw e;
		}
	}

	private void findConnectedSystem(String classInstance, String instanceName,
			Item parent) throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, instanceName, "ConnectedSystems");
			List<String> deviceProperties = topologyBrowser
					.getListFromString("LIST_DEVICEPROPERTIES");
			for (String name : memberOfList) {

				log.debug("name connected system: " + name);
				String[] componentClass = name.split("::");

				Device device = new Device();
				device.setCreationClassName(componentClass[0]);
				device.setName(componentClass[1]);
				Set<Device> deviceList = null;
				/*
				 * if (parent instanceof VLan){ deviceList = ((VLan)
				 * parent).getDeviceList(); deviceList.add(device); }
				 */
				setProperties(componentClass[0], componentClass[1],
						deviceProperties, device);
			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of ConnectedSystem. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findConnectedTo(String classInstance, String instanceName,
			Item parent) throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, instanceName, "ConnectedTo");
			List<String> interfaceProperties = topologyBrowser
					.getListFromString("LIST_INTERFACEPROPERTIES");
			if (memberOfList.size() == 2) {
				try{
				String[] componentClass1 = memberOfList.get(0).split("::");
				String[] componentClass2 = memberOfList.get(1).split("::");
				
				Interface interface1 = new Interface();
				interface1.setCreationClassName(componentClass1[0]);
				interface1.setName(componentClass1[1]);
				String device1 =componentClass1[1].substring(0,componentClass1[1].indexOf('/'));
				interface1.setParentDevice(device1.replace("IF-", ""));
				Interface interface2 = new Interface();
				interface2.setCreationClassName(componentClass2[0]);
				interface2.setName(componentClass2[1]);
				String device2 =componentClass2[1].substring(0,componentClass1[1].indexOf('/'));
				interface2.setParentDevice(device2.replace("IF-", ""));

				if (parent instanceof NetworkConnection) {
					((NetworkConnection) parent).setInterfaceA(interface1);
					((NetworkConnection) parent).setInterfaceB(interface2);
				}
				

				setProperties(componentClass1[0], componentClass1[1],
						interfaceProperties, interface1);
				setProperties(componentClass2[0], componentClass2[1],
						interfaceProperties, interface2);
				//davide
				}catch(Exception e){
					throw new SmartsException("This element "+instanceName+" is not following standard convention name:"+e.getMessage());
				}
			}else{
				if(classInstance.equalsIgnoreCase("NetworkConnection"))
				throw new SmartsException("This NetworkConnection has just one relations");
				
			}

		} catch (SmartsException e) {
			log.error(""
					+ e.getMessage());
			throw e;
		}
	}

	private void findHostServices(String classInstance, String deviceName,
			Item parent) throws SmartsException {

		try {
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "HostsServices");
			List<String> snmpagentProperties = topologyBrowser
					.getListFromString("LIST_SNMPAGENTPROPERTIES");
			List<String> ipProperties = topologyBrowser
					.getListFromString("LIST_IPPROPERTIES");
			for (String name : memberOfList) {

				log.debug("name host services: " + name);
				String[] componentClass = name.split("::");
				SNMPAgent snmpAgent = new SNMPAgent();
				snmpAgent.setName(componentClass[1]);
				Set<SNMPAgent> snmpAgentList = null;
				if (parent instanceof Device) {
					snmpAgentList = ((Device) parent).getSnmpAgentList();

					snmpAgentList.add(snmpAgent);
					setProperties(componentClass[0], componentClass[1],
							snmpagentProperties, snmpAgent);

					List<String> layerdOverList = topologyBrowser
							.getRelationList(componentClass[0],
									componentClass[1], "LayeredOver");
					for (String nameIp : layerdOverList) {

						log.debug("name access points: " + nameIp);
						String[] componentClassIp = nameIp.split("::");
						Ip ip = new Ip();
						ip.setName(componentClassIp[1]);
						ip.setAddress(componentClassIp[1].split("-")[1]);
						if (parent instanceof Device) {
							((Device) parent).getIpList().add(ip);

							setProperties(componentClassIp[0],
									componentClassIp[1], ipProperties, ip);
						}

					}
				}
			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of HostsServices. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findHostAccessPoints(String classInstance, String deviceName,
			Item item) throws SmartsException {

		try {
			List<String> ipProperties = topologyBrowser
					.getListFromString("LIST_IPPROPERTIES");
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "HostsAccessPoints");
			for (String name : memberOfList) {

				log.debug("name access points: " + name);
				String[] componentClass = name.split("::");
				Ip ip = new Ip();
				ip.setName(componentClass[1]);
				ip.setAddress(componentClass[1].split("-")[1]);
				if (item instanceof Device) {
					((Device) item).getIpList().add(ip);
				} else if (item instanceof Interface) {
					((Interface) item).getIpList().add(ip);
				}

				setProperties(componentClass[0], componentClass[1],
						ipProperties, ip);
			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of HostsAccessPoints. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findUnderlying(String classInstance, String deviceName,
			Item item) throws SmartsException {

		try {
			List<String> ipProperties = topologyBrowser
					.getListFromString("LIST_IPPROPERTIES");
			List<String> macProperties = topologyBrowser
			.getListFromString("LIST_MACPROPERTIES");
			List<String> memberOfList = topologyBrowser.getRelationList(
					classInstance, deviceName, "Underlying");
			for (String name : memberOfList) {

				String[] componentClass = name.split("::");
				if (componentClass[0].equalsIgnoreCase("IP")) {
					log.debug("name access points: " + name +" under "+item.getName());
					Ip ip = new Ip();
					ip.setName(componentClass[1]);
					ip.setAddress(componentClass[1].split("-")[1]);
					if (item instanceof Device) {
						((Device) item).getIpList().add(ip);
					} else if (item instanceof Interface) {
						((Interface) item).getIpList().add(ip);
					}

					setProperties(componentClass[0], componentClass[1],
							ipProperties, ip);
				}
				
				

				
					if (componentClass[0].equalsIgnoreCase("MAC")) {
						log.debug("name access points: " + name);
						Mac mac = new Mac();
						mac.setName(componentClass[1]);
						mac.setAddress(componentClass[1].substring(componentClass[1].indexOf("-")+1));
						if (item instanceof Device) {
							
						} else if (item instanceof Interface) {
							((Interface) item).setMac(mac);
						}

						setProperties(componentClass[0], componentClass[1],
								macProperties, mac);
					}
					
				
				
			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of Underlying. "
					+ e.getMessage());
			throw e;
		}
	}

	private void findChassis(String classInstance, String deviceName,
			Device device) throws SmartsException {

		try {
			List<String> chassisProperties = topologyBrowser
					.getListFromString("LIST_CHASSISPROPERTIES");

			String name = topologyBrowser.getSingleRelation(classInstance,
					deviceName, "SystemPackagedIn");

			if (name != null && !name.equalsIgnoreCase("")
					&& !name.equalsIgnoreCase("::")) {

				log.debug("name chassis: " + name);
				String[] componentClass = name.split("::");

				// create chassis and link it to the device
				Chassis chassis = new Chassis();
				chassis.setName(componentClass[1].substring(componentClass[1]
						.indexOf("-") + 1));
				Set<Chassis> chassisList = device.getChassisList();
				chassisList.add(chassis);

				// find attributes for this chassis
				setProperties(componentClass[0], componentClass[1],
						chassisProperties, chassis);

			}
		} catch (SmartsException e) {
			log.error("unable to retrieve the list of SystemPackagedIn. "
					+ e.getMessage());
			throw e;
		}
	}

	private void setProperties(String className, String objectName,
			List<String> instanceProperties, Item item) throws SmartsException {
		Map<String, String> instanceAttributesMap = null;
		try {
			instanceAttributesMap = topologyBrowser.getInstanceAttributesMap(
					className, objectName, instanceProperties);
			
			for (String key : instanceAttributesMap.keySet()) {
				ModelUtility.setAttribute(key, instanceAttributesMap.get(key),
						item);
				log.debug("key " + key + " = " + instanceAttributesMap.get(key));

			}
		} catch (SmartsException e) {
			// TODO Auto-generated catch block
			log.error("unable to read and save properties from smarts, object name: "
					+ objectName + ". " + e.getMessage() +" ipam= "+topologyBrowser.getDomainManager().getNameDomain());
			throw new SmartsException(e.getMessage()+" ipam= "+topologyBrowser.getDomainManager().getNameDomain());
			
			
		}

	}
	
	private boolean isValidDevice(String name){
		
		log.debug("check if device is valid with name "+name);
		return ignoreDeviceFileService.isValidDevice(name);
	}

}
