package com.holonomix.ionix.sam;

import java.util.List;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.Interface;
import com.holonomix.hsqldb.model.Ip;
import com.holonomix.hsqldb.model.Item.Status;
import com.holonomix.hsqldb.model.Mac;
import com.holonomix.hsqldb.model.NetworkConnection;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.ionix.TopologyBrowser;
import com.holonomix.icadapter.utils.Constants;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.icadapter.utils.MR_AnyValUtils;
import com.smarts.repos.MR_AnyVal;
import com.smarts.repos.MR_AnyValBoolean;
import com.smarts.repos.MR_AnyValObjRef;
import com.smarts.repos.MR_Ref;

public class SmartsBuilderService {
	private static final Logger log = Logger
			.getLogger(SmartsBuilderService.class);

	RemoteDomainManager remoteDomainManager;
	TopologyBrowser topologyBrowser;

	public SmartsBuilderService(RemoteDomainManager remoteDomainManager) {

		this.remoteDomainManager = remoteDomainManager;
		topologyBrowser = new TopologyBrowser(remoteDomainManager);
	}

	public enum SmartsTargetedRouterTopologyImportStatus {
		fullImported, partialImported, updated, notFound, found
	};

	public void setSmartsInstanceStringAttributeValue(String className,
			String instanceName, String attributeName, String value)
			throws SmartsException {

		try {
			remoteDomainManager.putString(className, instanceName,
					attributeName, value);

		} catch (SmartsException e) {
			log.error("IOException when trying to set value: " + value
					+ ", to attribute: " + attributeName + " of instance "
					+ instanceName);

			throw e;
			// TODO Auto-generated catch block

		}
	}

	public void setSmartsInstanceBooleanAttributeValue(String className,
			String instanceName, String attributeName, boolean value)
			throws SmartsException {

		try {

			remoteDomainManager.putBoolean(className, instanceName,
					attributeName, value);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to set value: " + value
					+ " to attribute: " + attributeName + " of instance "
					+ instanceName);
			throw e;
		}
	}

	public void setSmartsInstanceLongAttributeValue(String className,
			String instanceName, String attributeName, long value)
			throws SmartsException {

		try {
			remoteDomainManager.putLong(className, instanceName, attributeName,
					value);
		} catch (SmartsException e) {
			log.error("IOException when trying to set value: " + value
					+ " to attribute: " + attributeName + " of instance "
					+ instanceName);
			throw e;
		}
	}

	public void setSmartsInstanceIntegerAttributeValue(String className,
			String instanceName, String attributeName, int value)
			throws SmartsException {

		try {
			remoteDomainManager.putInteger(className, instanceName,
					attributeName, value);
		} catch (SmartsException e) {
			log.error("IOException when trying to set value: " + value
					+ " to attribute: " + attributeName + " of instance "
					+ instanceName);
			throw e;
		}
	}

	public void setSmartsInstanceFloatAttributeValue(String className,
			String instanceName, String attributeName, float value)
			throws SmartsException {

		try {
			remoteDomainManager.putFloat(className, instanceName,
					attributeName, value);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to set value: " + value
					+ " to attribute: " + attributeName + " of instance "
					+ instanceName);
			throw e;
		}
	}

	public void putRelationshipsBetweenClassInstances(String hostClassName,
			String hostClassInstanceName, String targetClassName,
			String targetClassInstanceName, String relationshipName)
			throws SmartsException {
		MR_Ref ref = new MR_Ref(targetClassName, targetClassInstanceName);
		try {

			remoteDomainManager.putRef(hostClassName, hostClassInstanceName,
					relationshipName, ref);
			log.debug("The Relationship " + relationshipName
					+ " is successfully added to " + hostClassInstanceName
					+ " and " + targetClassInstanceName);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create connection between : "
					+ hostClassInstanceName
					+ " and: "
					+ targetClassInstanceName);
			throw e;
		}

	}

	public void insertRelationshipsBetweenClassInstances(String hostClassName,
			String hostClassInstanceName, String targetClassName,
			String targetClassInstanceName, String relationshipName)
			throws SmartsException {

		// String instanceName = existingTargetClassInstanceName.get(iii);
		MR_Ref ref = new MR_Ref(targetClassName, targetClassInstanceName);
		try {
			MR_AnyValObjRef objRef = new MR_AnyValObjRef(ref);

			remoteDomainManager.insert(hostClassName, hostClassInstanceName,
					relationshipName, objRef);
			log.debug("The Relationship " + relationshipName
					+ " is successfully added to " + hostClassInstanceName
					+ " and " + targetClassInstanceName);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create connection between : "
					+ hostClassInstanceName
					+ " and: "
					+ targetClassInstanceName);
			throw e;
		}

	}

	public void removeRelationshipsBetweenClassInstances(String hostClassName,
			String hostClassInstanceName, String targetClassName,
			String targetClassInstanceName, String relationshipName)
			throws SmartsException {

		// String instanceName = existingTargetClassInstanceName.get(iii);
		MR_Ref ref = new MR_Ref(targetClassName, targetClassInstanceName);
		try {
			MR_AnyValObjRef objRef = new MR_AnyValObjRef(ref);

			remoteDomainManager.remove(hostClassName, hostClassInstanceName,
					relationshipName, objRef);
			log.debug("The Relationship " + relationshipName
					+ " is successfully removed to " + hostClassInstanceName
					+ " and " + targetClassInstanceName);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to remove connection between : "
					+ hostClassInstanceName
					+ " and: "
					+ targetClassInstanceName);
			throw e;
		}

	}

	public IonixElement createVLAN(String name) throws SmartsException {

		MR_AnyVal vlanName = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(name);
		MR_AnyVal[] opsList = { vlanName };
		IonixElement ionixElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					Constants.ICOF_CLASSNAME, Constants.ICOF_INSTANCENAME,
					"makeVLAN", opsList);

			String[] vlanNameClass = result.getValue().toString().split("::");
			ionixElement.setClassName(vlanNameClass[0]);
			ionixElement.setInstanceName(vlanNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Switch : "
					+ name);

			throw e;
		}

		return ionixElement;

	}

	public IonixElement createSwitch(String name, String classNameString)
			throws SmartsException {

		MR_AnyVal switchName = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(name);

		MR_AnyVal className = (MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(classNameString);
		MR_AnyVal[] opsList = { switchName, switchName, className };
		IonixElement ionixElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					Constants.ICOF_CLASSNAME, Constants.ICOF_INSTANCENAME,
					"makeSwitch", opsList);

			String[] switchNameClass = result.getValue().toString().split("::");
			ionixElement.setClassName(switchNameClass[0]);
			ionixElement.setInstanceName(switchNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Switch : "
					+ name);

			throw e;
		}

		return ionixElement;

	}

	public IonixElement createRouter(String name, String classNameString)
			throws SmartsException {

		MR_AnyVal routerName = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(name);
		MR_AnyVal className = (MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(classNameString);
		MR_AnyVal[] opsList = { routerName, routerName, className };
		IonixElement ionixElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					Constants.ICOF_CLASSNAME, Constants.ICOF_INSTANCENAME,
					"makeRouter", opsList);

			String[] switchNameClass = result.getValue().toString().split("::");
			ionixElement.setClassName(switchNameClass[0]);
			ionixElement.setInstanceName(switchNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Router : "
					+ name + " ");

			throw e;
		}

		return ionixElement;

	}

	public IonixElement createNetworkConnection(
			NetworkConnection networkConnection) throws SmartsException {

		IonixElement ionixElement = new IonixElement();
		IonixElement deviceElement1 = new IonixElement();
		IonixElement deviceElement2 = new IonixElement();

		Interface interface1 = networkConnection.getInterfaceA();
		Interface interface2 = networkConnection.getInterfaceB();
		if (interface1.getParentDevice() != null
				&& !interface1.getParentDevice().equalsIgnoreCase("")
				&& interface2.getDeviceId() != null
				&& !interface2.getParentDevice().equalsIgnoreCase("")) {
			deviceElement1.setInstanceName(interface1.getParentDevice());
			deviceElement2.setInstanceName(interface2.getParentDevice());

			List<String> classInstanceList = topologyBrowser
					.getListFromString("INSTANCE_CLASS");
			for (String classInstance : classInstanceList) {

				// first call to have the list of devices
				List<String> scInstanceLValList = topologyBrowser
						.getClassInstanceNames(classInstance);
				for (String deviceName : scInstanceLValList) {
					if (deviceName.equalsIgnoreCase(deviceElement1
							.getInstanceName())) {
						deviceElement1.setClassName(classInstance);
					}
					if (deviceName.equalsIgnoreCase(deviceElement2
							.getInstanceName())) {
						deviceElement2.setClassName(classInstance);
					}
				}
			}
			if (deviceElement1.getClassName() != null
					&& !deviceElement1.getClassName().equalsIgnoreCase("")) {
				if (interface1.getDeviceId() != null
						&& interface2.getDeviceId() != null) {
					IonixElement interfaceElement1 = topologyBrowser
							.getInterfaceByKey(deviceElement1,
									interface1.getDeviceId());
					IonixElement interfaceElement2 = topologyBrowser
							.getInterfaceByKey(deviceElement2,
									interface2.getDeviceId());

					MR_Ref ref1 = new MR_Ref(interfaceElement1.getClassName(),
							interfaceElement1.getInstanceName());
					MR_Ref ref2 = new MR_Ref(interfaceElement2.getClassName(),
							interfaceElement2.getInstanceName());
					try {
						MR_AnyValObjRef objRef1 = new MR_AnyValObjRef(ref1);
						MR_AnyValObjRef objRef2 = new MR_AnyValObjRef(ref2);

						MR_AnyVal[] opsList = { objRef1, objRef2 };

						MR_AnyVal result = remoteDomainManager.invokeOperation(
								Constants.ICOF_CLASSNAME,
								Constants.ICOF_INSTANCENAME,
								"makeNetworkDeviceConnection", opsList);

						String[] switchNameClass = result.getValue().toString()
								.split("::");
						ionixElement.setClassName(switchNameClass[0]);
						ionixElement.setInstanceName(switchNameClass[1]);

					} catch (SmartsException e) {
						log.error("SmRemoteException when trying to create network connection between: "
								+ interface1.getDeviceId()
								+ " and "
								+ interface2.getDeviceId());

						throw e;
					}
				} else {
					log.warn("remoteConnection " + networkConnection.getName()
							+ " is not correct, missed name of the interface");
				}
			} else {
				log.warn("remoteConnection " + networkConnection.getName()
						+ " is not correct, device is not in ipam"
						+ remoteDomainManager.getNameDomain());
			}
		} else {
			log.warn("remoteConnection " + networkConnection.getName()
					+ " is not correct,  device is not in ipam "
					+ remoteDomainManager.getNameDomain());
		}
		return ionixElement;

	}

	public IonixElement createChassis(String name) throws SmartsException {

		MR_AnyVal chassisName = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(name);
		MR_AnyVal[] opsList = { chassisName };
		IonixElement ionixElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					Constants.ICOF_CLASSNAME, Constants.ICOF_INSTANCENAME,
					"makeChassis", opsList);
			String[] chasissNameClass = result.getValue().toString()
					.split("::");
			ionixElement.setClassName(chasissNameClass[0]);
			ionixElement.setInstanceName(chasissNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Chassis : "
					+ name + " ");

			throw e;
		}

		return ionixElement;

	}

	public IonixElement createIp(IonixElement ionixElement, Ip ip)
			throws SmartsException {

		MR_AnyVal ipAddressVal = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(ip
				.getAddress());
		MR_AnyVal[] opsList = { ipAddressVal };
		IonixElement ipElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "makeIP", opsList);

			String[] ipNameClass = result.getValue().toString().split("::");
			ipElement.setClassName(ipNameClass[0]);
			ipElement.setInstanceName(ipNameClass[1]);

		} catch (SmartsException e) {
			if(ip.getAddress().split(":").length!=8){
			log.error("SmRemoteException when trying to create Ip : "
					+ ip.getAddress() + " ");
			
			throw e;
			}
		}

		return ipElement;

	}

	public IonixElement createMac(IonixElement ionixElement, Mac mac)
			throws SmartsException {

		MR_AnyVal macAddressVal = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(mac
				.getAddress());
		MR_AnyVal[] opsList = { macAddressVal };
		IonixElement macElement = new IonixElement();
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "makeMAC", opsList);

			String[] macNameClass = result.getValue().toString().split("::");
			macElement.setClassName(macNameClass[0]);
			macElement.setInstanceName(macNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create mac : "
					+ mac.getAddress() + " ");

			throw e;
		}

		return macElement;

	}

	public IonixElement createInterface(IonixElement ionixElement,
			Interface interfac) throws SmartsException {
		
		IonixElement interfaceElement = topologyBrowser
		.getInterfaceByKey(ionixElement,
				interfac.getDeviceId());
		if (interfaceElement==null){
			
			interfaceElement = new IonixElement();
		String interfaceDisplayName= interfac.getDisplayName();
		MR_AnyVal interfaceVal = null;
		if(interfac.getFlagStatus()!=Status.UPDATED){
		interfaceVal = (MR_AnyVal) MR_AnyValUtils
			.mrAnyValString(interfac.getName().trim());
		interfaceDisplayName = getDisplayName(ionixElement, interfac);
		}else {
			String temp = interfac.getDisplayName();
			if (temp.indexOf("[")!=-1){
			interfaceVal =(MR_AnyVal) MR_AnyValUtils
			.mrAnyValString(temp.substring(temp.indexOf("/")+1,temp.indexOf("[")).trim());
			}else{
				interfaceVal =(MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(temp.substring(temp.indexOf("/")+1).trim());
			}
		}
		MR_AnyVal interfaceDisplayNameString = (MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(interfaceDisplayName);
		MR_AnyVal[] opsList = { interfaceVal, interfaceDisplayNameString };
		
		try {

			MR_AnyVal result = remoteDomainManager.invokeOperation(
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "makeInterface", opsList);

			String[] ipNameClass = result.getValue().toString().split("::");
			interfaceElement.setClassName(ipNameClass[0]);
			interfaceElement.setInstanceName(ipNameClass[1]);
			
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Interface : "
					+ ionixElement.getInstanceName() + " ");

			throw e;
		}
		}if(interfac.getFlagStatus()==Status.UPDATED){
			remoteDomainManager.putString(
					interfaceElement.getClassName(),
					interfaceElement.getInstanceName(), "DisplayName",
					getDisplayName(ionixElement, interfac));
		}
		return interfaceElement;
		

	}

	private String getDisplayName(IonixElement ionixElement, Interface interfac) {
		String interfaceDisplayName;
		if(ClassFactory.getEnum()==EnumAdapterName.HWIPSC){
			if (interfac.getType().equalsIgnoreCase("GIGABITETHERNET")){
			interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
			+ interfac.getName() ;
			}
			else{
				interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
				+ interfac.getName() + " [" + interfac.getDescription()	+ "]";
			}
		}
		else{
		interfaceDisplayName = interfac.getName();
		
		if (interfaceDisplayName.indexOf("[") != -1)
			interfaceDisplayName = interfaceDisplayName.substring(0,
					interfaceDisplayName.indexOf("[") - 1);
		// full display name
		if (!interfaceDisplayName.equalsIgnoreCase(interfac.getDeviceId())) {
			// MB 040813
			//interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
			//+ interfaceDisplayName + " [" + interfac.getDeviceId()
			//+ "]";
			//if (interfac.getDescription() != ""){ 
			if (interfac.getDescription() != null && !interfac.getDescription().isEmpty() ){
				interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
					+ interfaceDisplayName + " [" + interfac.getDeviceId()
					+ "] ["  + interfac.getDescription() + "]";
			} else {
				interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
				+ interfaceDisplayName + " [" + interfac.getDeviceId()
				+ "]";
			}
		} else {
			interfaceDisplayName = "IF-" + ionixElement.getInstanceName() + "/"
					+ interfaceDisplayName + " ";
		}}
		if (interfac.getIpList() != null && interfac.getIpList().size() > 0) {
			Ip ip = (Ip) interfac.getIpList().toArray()[0];
			if (ip != null) {
				
				String address = ip.getAddress();
				if (address != null && address.split("\\.").length == 4) {
					interfaceDisplayName += "[" + address + "]";
				}
			}
		}
		return interfaceDisplayName;
	}

	public IonixElement createSNMPAgent(

	IonixElement ionixElement) throws SmartsException {

		String[] agentnameclass = { "SNMPAgent",
				"SNMPAgent-" + ionixElement.getInstanceName() };
		IonixElement snmpAgentElement = new IonixElement();
		try {
			remoteDomainManager.createClassInstance(agentnameclass[0],
					agentnameclass[1]);
			snmpAgentElement.setClassName(agentnameclass[0]);
			snmpAgentElement.setInstanceName(agentnameclass[1]);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create SNMPAgent : "
					+ ionixElement.getInstanceName());

			throw e;
		}

		return snmpAgentElement;

	}

	public IonixElement createNCRG(

	String name) throws SmartsException {

		String[] ncrg = { "NetworkConnectionRedundancyGroup", name };
		IonixElement ncrgElement = new IonixElement();
		try {
			remoteDomainManager.createClassInstance(ncrg[0], ncrg[1]);
			ncrgElement.setClassName(ncrg[0]);
			ncrgElement.setInstanceName(ncrg[1]);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create NetworkConnectionRedundancyGroup : "
					+ name);

			throw e;
		}

		return ncrgElement;

	}

	public IonixElement createNARG(

	String name) throws SmartsException {

		String[] narg = { "NetworkAdapterRedundancyGroup", name };
		IonixElement nargElement = new IonixElement();
		try {
			remoteDomainManager.createClassInstance(narg[0], narg[1]);
			nargElement.setClassName(narg[0]);
			nargElement.setInstanceName(narg[1]);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create NetworkAdapterRedundancyGroup : "
					+ name);

			throw e;
		}

		return nargElement;

	}

	public IonixElement createGlobalTypeCollection(

	String name) throws SmartsException {

		String[] hg = { "Global_TopologyCollection", name };
		IonixElement hgElement = new IonixElement();
		try {
			remoteDomainManager.createClassInstance(hg[0], hg[1]);
			hgElement.setClassName(hg[0]);
			hgElement.setInstanceName(hg[1]);
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create GlobalTopologyCollection : "
					+ name);

			throw e;
		}

		return hgElement;

	}

	public IonixElement createCard(IonixElement chassisElement, String value)
			throws SmartsException {

		MR_AnyVal cardId = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(value);
		MR_AnyVal[] opsList = { cardId };
		IonixElement ionixElement = new IonixElement();
		try {
			MR_AnyVal result = remoteDomainManager.invokeOperation(
					chassisElement.getClassName(),
					chassisElement.getInstanceName(), "makeCard", opsList);

			String[] cardNameClass = result.getValue().toString().split("::");
			ionixElement.setClassName(cardNameClass[0]);
			ionixElement.setInstanceName(cardNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Card : " + value);

			throw e;
		}

		return ionixElement;

	}

	public IonixElement createPort(IonixElement switchElement, String value)
			throws SmartsException {

		MR_AnyVal portId = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(value);
		MR_AnyVal[] opsList = { portId };
		IonixElement ionixElement = new IonixElement();
		try {
			MR_AnyVal result = remoteDomainManager.invokeOperation(
					switchElement.getClassName(),
					switchElement.getInstanceName(), "makePort", opsList);

			String[] cardNameClass = result.getValue().toString().split("::");
			ionixElement.setClassName(cardNameClass[0]);
			ionixElement.setInstanceName(cardNameClass[1]);

		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to create Port : " + value);

			throw e;
		}

		return ionixElement;

	}

	public void reconfigure() throws SmartsException {

		try {
			MR_AnyValBoolean booleanPr = new MR_AnyValBoolean(true);
			remoteDomainManager.invokeOperation(Constants.ICPM_CLASSNAME,
					Constants.ICPM_INSTANCENAME, "reconfigureAndWait",
					new MR_AnyVal[] { booleanPr });
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to reconfigure  "
					+ e.getMessage());

			throw e;
		}

	}

	public void manageOperation(IonixElement portElement)
			throws SmartsException {

		try {
			remoteDomainManager
					.invokeOperation(portElement.getClassName(),
							portElement.getInstanceName(), "manage",
							new MR_AnyVal[] {});
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to manageOperation  "
					+ e.getMessage());

			throw e;
		}

	}

	public void updateAgentAddressList(IonixElement snmpAgent)
			throws SmartsException {

		try {
			remoteDomainManager.invokeOperation(snmpAgent.getClassName(),
					snmpAgent.getInstanceName(), "updateAgentAddressList",
					new MR_AnyVal[] {});
		} catch (SmartsException e) {
			log.error("SmRemoteException when trying to manageOperation  "
					+ e.getMessage());

			throw e;
		}

	}

}