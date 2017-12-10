package com.holonomix.alarm.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.AlarmMappingFileInterface;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.AlarmException;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.hsqldb.model.Card;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.Interface;
import com.holonomix.hsqldb.model.Ip;
import com.holonomix.hsqldb.model.Item;
import com.holonomix.hsqldb.model.Port;
import com.holonomix.hsqldb.model.SNMPAgent;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.ionix.sam.SmartsAlarmService;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.TopologyMemoryTask;

public class AlarmLogicService {
	private static final Logger log = Logger.getLogger(AlarmLogicService.class);

	private AlarmMappingFileInterface alarmMappingFileInterface;

	private PropertiesContainer propertiesContainer;
	private int defaultStatus = 0;
	SmartsAlarmService smartsAlarmService;
	RemoteDomainManager remoteDomainManager;
	SmartsReadService smartsReadService;
	private int checkObjectRetry=3;
	public AlarmLogicService(RemoteDomainManager remoteDomainManager) {
		this.remoteDomainManager = remoteDomainManager;
		smartsAlarmService = new SmartsAlarmService(remoteDomainManager);
		smartsReadService = new SmartsReadService(remoteDomainManager);
		propertiesContainer = PropertiesContainer.getInstance();
		alarmMappingFileInterface = ClassFactory.getAlarmMappingFileInstance();
		propertiesContainer = PropertiesContainer.getInstance();
		defaultStatus = Integer.parseInt(propertiesContainer
				.getProperty("IPAM_CHECKOBJECT_DEFAULTSTATUS"));
		checkObjectRetry=Integer.parseInt(propertiesContainer.getProperty("IPAM_CHECKOBJECT_RETRY"));
	}

	
	
	public void findInfoDevice(Device device)  throws SmartsException{
		int numberConnection =0;
		while(numberConnection< checkObjectRetry)
		try{
			numberConnection++;
			infoDeviceCallable(device);
			break;
		}catch (SmartsException e ){
			if (e.getTypeError()==3){
			if (numberConnection<checkObjectRetry) {
				
				log.error("Problem reading Ipam object for alarm, try again. Error: "+e.getMessage());
			}
			else{
				log.error("Reached max number of retry - reading Ipam object for alarm. Error: "+e.getMessage());
				throw e;
			}
			}
			else throw e;
		}
	}
	
	
	public IonixElement getPortByDescription(IonixElement deviceElement,String component)  throws SmartsException{
		int numberConnection =0;
		IonixElement  ionixElement=null;
		while(numberConnection< checkObjectRetry)
		try{
			numberConnection++;
			ionixElement=getPortByDescriptionCallable(deviceElement,
					component);
			break;
		}catch (SmartsException e ){
			if (e.getTypeError()==3){
			if (numberConnection<checkObjectRetry) {
				
				log.error("Problem reading Ipam object for alarm, try again. Error: "+e.getMessage());
			}
			else{
				log.error("Reached max number of retry - reading Ipam object for alarm. Error: "+e.getMessage());
				
				throw e;
			}
			}
			else throw e;
		}
		return ionixElement;
	}
	
	
	
	public int sendAlarmTimeout(final Alarm alarm, final String[] instruction) throws SmartsException {
		int numberConnection =0;
		int status =0;
		while(numberConnection< checkObjectRetry)
		try{
			numberConnection++;
			status = sendAlarmCallable(alarm, instruction);
			break;
		}catch (SmartsException e ){
			if (e.getTypeError()==3){
			if (numberConnection<checkObjectRetry) {
				
				log.error("Problem reading Ipam object for alarm, try again. Error: "+e.getMessage());
			}
			else{
				
				status = Integer.parseInt(propertiesContainer.getProperty("IPAM_CHECKOBJECT_DEFAULTSTATUS"));
				log.error("Reached max number of retry - reading Ipam object for alarm. Default status sent is :"+ status+". Error: "+e.getMessage());
			}
			}
			else throw e;
		}
		return status;
	}
	
	
	private IonixElement getPortByDescriptionCallable(final IonixElement deviceElement,final String component) throws SmartsException {

		IonixElement result = null;
		Callable<IonixElement> callReadBuffer = new Callable<IonixElement>() {
			public IonixElement call() throws SmartsException {
				return smartsReadService.getPortByDescription( deviceElement, component);
				
			}
		};
		ExecutorService exectutor = Executors.newCachedThreadPool();
		try {
			DateTime d1 =new DateTime();
			
			final Future<IonixElement> handler = exectutor.submit(callReadBuffer);

			exectutor.shutdown();
			result = handler
					.get(Long.parseLong(propertiesContainer
							.getProperty("IPAM_CHECKOBJECT_TIMEOUT")),
							TimeUnit.SECONDS);
			DateTime d2 =new DateTime();
			log.debug("info port completed in millis: "+(d2.getMillis()-d1.getMillis()));
			
		}catch (Exception ex) {
				if(ex instanceof TimeoutException){
						log.error("Timeout reading Smarts");
						throw new SmartsException(3);
					}
				throw new SmartsException(ex.getMessage());

			}		return result;

	}

	private int infoDeviceCallable(final Device device) throws SmartsException {

		Integer result = null;
		Callable<Integer> callReadBuffer = new Callable<Integer>() {
			public Integer call() throws SmartsException {
				smartsReadService.getInfoDevice(device);
					return 0;
			}
		};
		ExecutorService exectutor = Executors.newCachedThreadPool();
		try {
			DateTime d1 =new DateTime();
			
			final Future<Integer> handler = exectutor.submit(callReadBuffer);

			exectutor.shutdown();
			result = handler
					.get(Long.parseLong(propertiesContainer
							.getProperty("IPAM_CHECKOBJECT_TIMEOUT")),
							TimeUnit.SECONDS);
			DateTime d2 =new DateTime();
			log.debug("info device completed in millis: "+(d2.getMillis()-d1.getMillis()));
			
		} catch (Exception ex) {
			if(ex instanceof TimeoutException){
					log.error("Timeout reading Smarts");
					throw new SmartsException(3);
				}
			throw new SmartsException(ex.getMessage());

		}

		return result;

	}
	
	private int sendAlarmCallable(final Alarm alarm, final String[] instruction) throws SmartsException {

		Integer result = null;
		Callable<Integer> callReadBuffer = new Callable<Integer>() {
			public Integer call() throws SmartsException {
				
					return sendAlarm(alarm,instruction);
					

				
			}
		};
		ExecutorService exectutor = Executors.newCachedThreadPool();
		try {
			DateTime d1 =new DateTime();
			
			final Future<Integer> handler = exectutor.submit(callReadBuffer);

			exectutor.shutdown();
			result = handler
					.get(Long.parseLong(propertiesContainer
							.getProperty("IPAM_CHECKOBJECT_TIMEOUT")),
							TimeUnit.SECONDS);
			DateTime d2 =new DateTime();
			log.debug("send alarm call completed in millis: "+(d2.getMillis()-d1.getMillis()));
			
		} catch (Exception ex) {
			if(ex instanceof TimeoutException){
				log.error("Timeout reading Smarts");
				throw new SmartsException(3);
			}
		throw new SmartsException(ex.getMessage());

	}

		return result;

	}

	
	
	// 0 object not found in ipam
	// 1 object founds
	// 2 device does not exist
	private int sendAlarm(Alarm alarm, String[] instruction) {

		boolean foundInIpam = false;

		if (instruction != null)

		{

			// check if object exists in each ipam
			Item item = null;
			try {
				
				item = createItemUsingAlarmInformation(instruction, alarm);
				if(item==null){
					//device is not present
					return 2;
				}
			} catch (AlarmException e1) {

				log.debug(e1.getMessage());
				return 0;

			} catch (SmartsException e1) {

				if (e1.getTypeError() == 3) {
					return defaultStatus;
				}
			}

			Device deviceSmarts;
			try {
				deviceSmarts = checkDeviceExist(item);
			} catch (SmartsException e1) {
				return defaultStatus;
			}
			if (deviceSmarts != null
					&& !deviceSmarts.getCreationClassName()
							.equalsIgnoreCase("")) {
				alarm.setDeviceType(deviceSmarts.getCreationClassName());
			}
			if (deviceSmarts == null) {
				log.debug("device is not present in this  ipam");
				return 2;
			}
			if (instruction[0].equalsIgnoreCase("DEVICE")) {
				if (instruction[1].equalsIgnoreCase("DOWN")) {

					try {
						findInfoDevice((Device) item);

						if (((Device) item).getSnmpAgentList().size() > 0) {
							setDeviceDisable((Device) item);
							foundInIpam = true;
							log.info(" set device  " + item.getName()
									+ " DOWN in ipam "
									+ remoteDomainManager.getNameDomain());
						}
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.debug(" device " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					} catch (SmartsException e) {
						if (e.getTypeError() == 3) {
							return defaultStatus;
						}
						log.debug(" device " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}
				} else if (instruction[1].equalsIgnoreCase("UP")) {

					try {
						findInfoDevice((Device) item);
						if (((Device) item).getSnmpAgentList().size() > 0) {
							setDeviceEnable((Device) item);
							foundInIpam = true;
							log.info(" set device " + item.getName()
									+ " UP in ipam "
									+ remoteDomainManager.getNameDomain());
						}
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.debug(" device " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					} catch (SmartsException e) {
						if (e.getTypeError() == 3) {
							return defaultStatus;
						}
						log.debug(" device " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}
				}
			}
			if (instruction[0].equalsIgnoreCase("INTERFACE")) {
				if (instruction[1].equalsIgnoreCase("DOWN")) {

					try {

						IonixElement interfaceElemt = setInterfaceDown(
								deviceSmarts, (Interface) item);
						foundInIpam = true;
						log.info(" set Interface "
								+ interfaceElemt.getInstanceName()
								+ " on device " + deviceSmarts.getName()
								+ " DOWN in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" Interface with key "
								+ ((Interface) item).getDeviceId()
								+ " on device "
								+ ((Interface) item).getParentDevice()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}
				} else if (instruction[1].equalsIgnoreCase("UP")) {

					try {

						// IonixElement interfaceElemt setInterfaceDown(device,
						// (Interface) item);
						// foundInIpam = true;
						IonixElement interfaceElemt = setInterfaceEnable(
								deviceSmarts, (Interface) item);
						foundInIpam = true;
						log.info(" set Interface "
								+ interfaceElemt.getInstanceName()
								+ " on device " + deviceSmarts.getName()
								+ " UP in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" Interface with key "
								+ ((Interface) item).getDeviceId()
								+ " on device " + deviceSmarts.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}
				}
			} else if (instruction[0].equalsIgnoreCase("CARD")) {
				if (instruction[1].equalsIgnoreCase("DOWN")) {

					try {
						setCardDown((Card) item);
						foundInIpam = true;
						log.info(" set card " + item.getName()
								+ " DOWN in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" card " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);

					}
				} else if (instruction[1].equalsIgnoreCase("UP")) {

					try {
						setCardUp((Card) item);
						foundInIpam = true;
						log.info(" set card " + item.getName() + " UP in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" card " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);

					}
				}
			} else if (instruction[0].equalsIgnoreCase("PORT")) {
				if (instruction[1].equalsIgnoreCase("DOWN")) {

					try {
						setPortDown((Port) item);
						foundInIpam = true;
						log.info(" set port " + item.getName()
								+ " DOWN in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" port " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}

				} else if (instruction[1].equalsIgnoreCase("UP")) {

					try {
						setPortEnable((Port) item);
						foundInIpam = true;
						log.info(" set port " + item.getName() + " UP in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" port " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}

				} else if (instruction[1].equalsIgnoreCase("DISABLE")) {

					try {
						setPortDisable((Port) item);
						foundInIpam = true;
						log.info(" set port " + item.getName()
								+ " DOWN in ipam "
								+ remoteDomainManager.getNameDomain());
					} catch (AlarmException e) {
						// card not present or some error run pushevent
						log.warn(" port " + item.getName()
								+ " not present in ipam "
								+ remoteDomainManager.getNameDomain());

					}

					if (foundInIpam == false) {
						String descriptionPushEvent = alarm.getDescription()
								+ " Object not found in IPAM for RCA Condition: "
								+ item.getName();
						alarm.setDescription(descriptionPushEvent);
						// pushEvent(alarm);
					}
				}
			}

		}
		if (foundInIpam == true)
			return 1;
		else
			return 0;

	}

	private Device checkDeviceExist(Item item) throws SmartsException {
		String name = "";
		if (item.getCreationClassName().equalsIgnoreCase("card")) {
			name = item.getName().replace("CARD-", "");
			name = name.substring(0, name.indexOf("/"));
		} else if (item.getCreationClassName().equalsIgnoreCase("interface")) {

			name = ((Interface) item).getParentDevice();

		} else if (item.getCreationClassName().equalsIgnoreCase("port")) {
			name = item.getName().replace("PORT-", "");
			name = name.substring(0, name.indexOf("/"));

		} else {
			name = item.getName();
		}
		Device device = new Device();
		device.setName(name);
		findInfoDevice(device);
		if (device.getCreationClassName().equalsIgnoreCase("Unknown"))
			return null;
		else
			return device;

	}

	public boolean pushEventFailOver(Alarm alarm) {
		try {

			smartsAlarmService.pushEventFailOver(alarm);
			return true;
		} catch (SmartsException e1) {

			log.error("error in pushing event..." + e1.getMessage());
			return false;
		}
	}

	public boolean pushEvent(Alarm alarm) {
		try {

			smartsAlarmService.pushEvent(alarm);
			return true;
		} catch (SmartsException e1) {

			log.error("error in pushing event..." + e1.getMessage());
			return false;
		}
	}

	private Card createCard(String component) {
		Card card = new Card();

		if (ClassFactory.getEnum() == EnumAdapterName.HWZTE ) {

			String[] singleValueList = component.split("_");

			card.setShelfNumber(singleValueList[2]);
			card.setSlotNumber(singleValueList[3]);
			if(singleValueList.length==5){
				card.setSubSlotNumber(singleValueList[4]);
			}

		} else {
			String[] singleValueList = component.split("/");
			for (String singleValue : singleValueList) {
				if (singleValue.startsWith("rack=")) {
					String rack = singleValue.substring(
							singleValue.indexOf("=") + 1).trim();
					card.setRackNumber(rack);
				}
				if (singleValue.startsWith("shelf=")) {
					String shelf = singleValue.substring(
							singleValue.indexOf("=") + 1).trim();
					card.setShelfNumber(shelf);
				}
				if (singleValue.startsWith("slot=")) {
					String slot = singleValue.substring(
							singleValue.indexOf("=") + 1).trim();
					card.setSlotNumber(slot);
				}
				if (singleValue.startsWith("sub_slot=")) {
					String subSlot = singleValue.substring(
							singleValue.indexOf("=") + 1).trim();
					card.setSubSlotNumber(subSlot);
				}
			}
		}

		return card;

	}

	private Device createDevice(String deviceName) throws SmartsException {
		Device device = new Device();
		device.setName(deviceName);
		findInfoDevice(device);
		return device;

	}

	private Interface createInterface(String component) {
		Interface interfac = new Interface();

		if (ClassFactory.getEnum() == EnumAdapterName.HWZTE) {
			try {
				String temp = component
						.substring(component.lastIndexOf("_") + 1);
				if (component.indexOf("xgei") != -1) {
					temp = "xgei_" + temp;
				} else if (component.indexOf("gei") != -1) {
					temp = "gei_" + temp;
				}
				interfac.setDeviceId(temp);
				if (temp.length() < 2)
					interfac.setDeviceId(component);
			} catch (Exception e) {
				interfac.setDeviceId(component);
			}

		} else if (ClassFactory.getEnum() == EnumAdapterName.HWIPSC) {
			interfac.setDeviceId(component);

		} else {
			if (component.indexOf("::") == -1) {

				if (component.indexOf("cli_name=") != -1) {
					String deviceId = component.substring(
							component.indexOf("cli_name=") + 9).trim();
					interfac.setDeviceId(deviceId);
				}

			} else {
				String subPort = component.split("::")[1];
				component = component.split("::")[0];

				if (component.indexOf("cli_name=") != -1) {
					String deviceId = component.substring(
							component.indexOf("cli_name=") + 9).trim();
					subPort = subPort.substring(
							subPort.indexOf("sub_port=") + 9).trim();
					interfac.setDeviceId(deviceId + "." + subPort);
				}

			}
		}
		return interfac;

	}

	private Port createPort(String component) {
		Port port = new Port();
		String[] singleValueList = component.split("/");
		for (String singleValue : singleValueList) {
			if (singleValue.startsWith("rack=")) {
				String rack = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setRackNumber(rack);
			}
			if (singleValue.startsWith("shelf=")) {
				String shelf = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setShelfNumber(shelf);
			}
			if (singleValue.startsWith("slot=")) {
				String slot = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setSlotNumber(slot);
			}

			if (singleValue.startsWith("sub_slot=")) {
				String subSlot = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setSubSlotNumber(subSlot);
			}

			if (singleValue.startsWith("port=")) {
				String portNumber = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setPortNumber(portNumber);
			}

			if (singleValue.startsWith("sub_port=")) {
				String subPortNumber = singleValue.substring(
						singleValue.indexOf("=") + 1).trim();
				port.setSubPortNumber(subPortNumber);
			}

		}

		return port;

	}

	private Item createItemUsingAlarmInformation(String[] instruction,
			Alarm alarm) throws AlarmException, SmartsException {
		Item item = null;
		if (instruction[0].equalsIgnoreCase("CARD")) {
			Card card = null;
			try {
				card = createCard(alarm.getComponent());
			} catch (Exception e) {
				throw new AlarmException(
						"  card  "+alarm.getDeviceName()+" "
								+ alarm.getComponent()+" not present in ipam "+remoteDomainManager.getNameDomain());
			}
			String key;
			if (ClassFactory.getEnum() == EnumAdapterName.HWBRAS
					|| ClassFactory.getEnum() == EnumAdapterName.HWMETE || ClassFactory.getEnum() == EnumAdapterName.HWZTE) {
				key = "CARD-" + alarm.getDeviceName() + "/" 
						+ card.getNameForSmarts(true);
			} else {
				key = "CARD-" + alarm.getDeviceName() + "/"
						+ card.getNameForSmarts(false);
			}
			card.setName(key);
			item = card;
		} else if (instruction[0].equalsIgnoreCase("PORT")) {
			Port port = null;

			if (ClassFactory.getEnum() == EnumAdapterName.ALUFTTH) {
				if (alarm.getComponent().startsWith("LANX")) {
					Device device = new Device();
					device.setName(alarm.getDeviceName());
					try {
						findInfoDevice(device);
						if (device.getCreationClassName().equalsIgnoreCase("UNKNOWN")) {
							log.warn("no device with this  name " + device.getName()
									+ " present in ipam "
									+ remoteDomainManager.getNameDomain());
						}
						else{
						IonixElement deviceElement = new IonixElement();
						deviceElement.setClassName(device
								.getCreationClassName());
						deviceElement.setInstanceName(device.getName());
						IonixElement portElement =getPortByDescription(deviceElement,
										alarm.getComponent());
						if (portElement != null) {
							port = new Port();
							port.setName(portElement.getInstanceName());
						}
						}
					} catch (SmartsException e) {
						if (e.getTypeError() == 3) {
							throw e;
						}
						log.warn("no device with this  name "
								+ device.getName() + " present in ipam "
								+ remoteDomainManager.getNameDomain());
					}
				} else {
					try {
						port = createPort(alarm.getComponent());
					} catch (Exception e) {
						throw new AlarmException(
								"error creating the  port using this information > "
										+ alarm.getComponent());
					}

					String key = "PORT-" + alarm.getDeviceName() + "/"
							+ port.getNameForSmarts();
					port.setName(key);
				}
				item = port;
			} else {
				Device device = new Device();
				device.setName(alarm.getDeviceName());
				findInfoDevice(device);
				if (device.getCreationClassName().equalsIgnoreCase("UNKNOWN")) {
					log.warn("no device with this  name " + device.getName()
							+ " present in ipam "
							+ remoteDomainManager.getNameDomain());
				}
				if (device.getCreationClassName().equalsIgnoreCase("ROUTER")) {
					Interface interfac = createInterface(alarm.getComponent());
					interfac.setParentDevice(alarm.getDeviceName());
					item = interfac;
				} else {
					port = createPort(alarm.getComponent());
					String key = "PORT-" + alarm.getDeviceName() + "/"
							+ port.getNameForSmarts();
					port.setName(key);
					item = port;
				}
			}

		} else if (instruction[0].equalsIgnoreCase("DEVICE")) {
			try{
			Device device = createDevice(alarm.getDeviceName());
			item = device;
			}catch(Exception e){
				log.error("Error creating device for alarm "+alarm.toString()+". error:"+e.getMessage());
				return null;
				
			}

		} else if (instruction[0].equalsIgnoreCase("INTERFACE")) {
			Device device = new Device();
			device.setName(alarm.getDeviceName());
			try{
			//	alarm.setComponent("");
			findInfoDevice(device);

			Interface interfac = createInterface(alarm.getComponent());
			interfac.setParentDevice(alarm.getDeviceName());
			item = interfac;
			}catch(Exception e){
				log.error("Error creating interface for alarm "+alarm.toString()+". error:"+e.getMessage());
				throw new AlarmException();
				
			}
			

		} else {
			log.error("Item has not been created instruction is not correct "
					+ instruction[0] + " " + instruction[1]);

		}
		return item;

	}

	public IonixElement setPortEnable(Port port) throws AlarmException {

		IonixElement element = setPortEnableLogic(port);

		return element;
	}

	public IonixElement setPortDisable(Port port) throws AlarmException {

		IonixElement element = setPortDisableLogic(port);

		return element;
	}

	public IonixElement setPortDown(Port port) throws AlarmException {

		IonixElement element = setPortDownLogic(port);

		return element;
	}

	public IonixElement setInterfaceEnable(Device device, Interface interface1)
			throws AlarmException {

		IonixElement element = setInterfaceEnableLogic(device, interface1);

		return element;
	}

	public IonixElement setInterfaceDown(Device device, Interface interface1)
			throws AlarmException {

		IonixElement element = setInterfaceDownLogic(device, interface1);

		return element;
	}

	public IonixElement setDeviceDisable(Device device) throws AlarmException {

		IonixElement element = setDeviceDisableLogic(device);

		return element;
	}

	public IonixElement setDeviceEnable(Device device) throws AlarmException {

		IonixElement element = setDeviceEnableLogic(device);

		return element;
	}

	public IonixElement cleanCard(Card card) throws AlarmException {

		IonixElement element = setStatusCardLogic(card, "OK", true);

		return element;
	}

	public IonixElement setCardUp(Card card) throws AlarmException {

		IonixElement element = setStatusCardLogic(card, "OK", false);

		return element;
	}

	public IonixElement setCardDown(Card card) throws AlarmException {

		IonixElement element = setStatusCardLogic(card, "CRITICAL", false);

		return element;
	}

	private IonixElement setPortDisableLogic(Port portElement)
			throws AlarmException {

		IonixElement element = new IonixElement();
		element.setClassName(portElement.getCreationClassName());
		element.setInstanceName(portElement.getName());
		try {
			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "OperStatus",
						"DOWN");
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"DOWN");
				smartsAlarmService.changeStatusObject(element, "IsFlapping",
						false);
			} else {
				throw new AlarmException("port not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}
		return element;
	}

	private IonixElement setPortEnableLogic(Port portElement)
			throws AlarmException {

		IonixElement element = new IonixElement();
		element.setClassName(portElement.getCreationClassName());
		element.setInstanceName(portElement.getName());
		try {
			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "OperStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "IsFlapping",
						false);
			} else {
				throw new AlarmException("port not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}
		return element;
	}

	private IonixElement setPortDownLogic(Port portElement)
			throws AlarmException {

		IonixElement element = new IonixElement();
		element.setClassName(portElement.getCreationClassName());
		element.setInstanceName(portElement.getName());
		try {
			smartsAlarmService
					.changeStatusObject(element, "OperStatus", "DOWN");
			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "IsFlapping",
						false);
			} else {
				throw new AlarmException("port not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;

			throw new AlarmException(message);
		}
		return element;

	}

	public IonixElement setAdminAndOperStatusDown(Item portElement)
			throws AlarmException {

		IonixElement element = new IonixElement();
		element.setClassName(portElement.getCreationClassName());
		element.setInstanceName(portElement.getName());
		try {

			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"DOWN");
				smartsAlarmService.changeStatusObject(element, "OperStatus",
						"DOWN");

			} else {
				throw new AlarmException("port or interface not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;

			throw new AlarmException(message);
		}
		return element;

	}

	private IonixElement setInterfaceEnableLogic(Device device,
			Interface interfaceElement) throws AlarmException {

		IonixElement deviceElement = new IonixElement();
		IonixElement element = new IonixElement();
		deviceElement.setClassName(device.getCreationClassName());
		deviceElement.setInstanceName(device.getName());
		try {
			element = smartsAlarmService.getInterfaceByKey(deviceElement,
					interfaceElement.getDeviceId());

			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "OperStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "IsFlapping",
						false);
			} else {
				throw new AlarmException("interface with deviceId "
						+ deviceElement + " not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}
		return element;
	}

	private IonixElement setInterfaceDownLogic(Device device,
			Interface interfaceElement) throws AlarmException {

		IonixElement deviceElement = new IonixElement();
		IonixElement element = new IonixElement();
		deviceElement.setClassName(device.getCreationClassName());
		deviceElement.setInstanceName(device.getName());
		try {
			element = smartsAlarmService.getInterfaceByKey(deviceElement,
					interfaceElement.getDeviceId());
			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "OperStatus",
						"DOWN");
				smartsAlarmService.changeStatusObject(element, "AdminStatus",
						"UP");
				smartsAlarmService.changeStatusObject(element, "IsFlapping",
						false);
			} else {
				throw new AlarmException("interface with deviceId "
						+ deviceElement + " not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;

			throw new AlarmException(message);
		}
		return element;

	}

	private IonixElement setStatusCardLogic(Card cardElement, String value,
			boolean isCleaning) throws AlarmException {

		IonixElement element = new IonixElement();
		element.setClassName(cardElement.getCreationClassName());
		element.setInstanceName(cardElement.getName());
		try {
			if (element.getClassName() != null
					|| element.getInstanceName() != null) {
				smartsAlarmService.changeStatusObject(element, "Status", value);
				if (!isCleaning) {
					if (ClassFactory.getEnum() != EnumAdapterName.HWZTE) {
						if (value.equalsIgnoreCase("OK")) {
							// remove card from GlobalTypeCollection
							smartsAlarmService
									.cleanCardInGlobalTypeCollection(cardElement
											.getName());
						} else {
							// add card from GlobalTypeCollection
							smartsAlarmService
									.addCardInGlobalTypeCollection(cardElement
											.getName());
						}
					}
				}
			} else {
				throw new AlarmException("card not found");
			}
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}
		return element;
	}

	private IonixElement setDeviceDisableLogic(Device device)
			throws AlarmException {
		IonixElement element = new IonixElement();
		try {

			for (Ip ip : device.getIpList()) {
				IonixElement elementIP = new IonixElement();
				elementIP.setClassName(ip.getCreationClassName());
				elementIP.setInstanceName(ip.getName());
				if (elementIP.getClassName() != null
						|| elementIP.getInstanceName() != null) {
					smartsAlarmService.changeStatusObject(elementIP,
							"IPStatus", "TIMEDOUT");
				} else {
					throw new AlarmException("ip not found");
				}

			}
			for (SNMPAgent snmpAgent : device.getSnmpAgentList()) {
				IonixElement elementSNMP = new IonixElement();
				elementSNMP.setClassName(snmpAgent.getCreationClassName());
				elementSNMP.setInstanceName(snmpAgent.getName());
				if (elementSNMP.getClassName() != null
						|| elementSNMP.getInstanceName() != null) {
					smartsAlarmService.changeStatusObject(elementSNMP,
							"SNMPStatus", "TIMEDOUT");
				} else {
					throw new AlarmException("snmpAgent not found");
				}
			}

			element.setClassName(device.getCreationClassName());
			element.setInstanceName(device.getName());
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}

		return element;
	}

	private IonixElement setDeviceEnableLogic(Device device)
			throws AlarmException {
		IonixElement element = new IonixElement();
		try {

			for (Ip ip : device.getIpList()) {
				IonixElement elementIP = new IonixElement();
				elementIP.setClassName(ip.getCreationClassName());
				elementIP.setInstanceName(ip.getName());
				if (elementIP.getClassName() != null
						|| elementIP.getInstanceName() != null) {
					smartsAlarmService.changeStatusObject(elementIP,
							"IPStatus", "OK");
				} else {
					throw new AlarmException("ip not found");
				}
			}
			for (SNMPAgent snmpAgent : device.getSnmpAgentList()) {
				IonixElement elementSNMP = new IonixElement();
				elementSNMP.setClassName(snmpAgent.getCreationClassName());
				elementSNMP.setInstanceName(snmpAgent.getName());
				if (elementSNMP.getClassName() != null
						|| elementSNMP.getInstanceName() != null) {
					smartsAlarmService.changeStatusObject(elementSNMP,
							"SNMPStatus", "OK");
				} else {
					throw new AlarmException("snmpAgent not found");
				}
			}

			element.setClassName(device.getCreationClassName());
			element.setInstanceName(device.getName());
		} catch (SmartsException e) {
			String message = e.getMessage() + " " + element;
			throw new AlarmException(message);
		}

		return element;
	}

}
