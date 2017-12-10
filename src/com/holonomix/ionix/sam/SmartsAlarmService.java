package com.holonomix.ionix.sam;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.hsqldb.model.GlobalTopologyCollection;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.icadapter.utils.MR_AnyValUtils;
import com.holonomix.properties.PropertiesContainer;
import com.smarts.repos.MR_AnyVal;
import com.smarts.repos.MR_AnyValObjRef;
import com.smarts.repos.MR_AnyValObjRefSet;
import com.smarts.repos.MR_Ref;

public class SmartsAlarmService {
	private static final Logger log = Logger.getLogger(SmartsAlarmService.class);

	RemoteDomainManager remoteDomainManager;
	String notifId;
	
	public SmartsAlarmService(RemoteDomainManager remoteDomainManager) {

		this.remoteDomainManager = remoteDomainManager;
		notifId = ClassFactory.getIdNotif();
	}

	//this method is used to create a notif from an alarm
	private String parseAlarmToString(Alarm alarm) {
		String host ="";
		try{
		 host = remoteDomainManager.getHostName();
		}catch(SmartsException e){
			
		}
		StringBuffer alarmString = new StringBuffer("");

		alarmString.append("0").append("|").append(cleanString(host)).append("|");
		alarmString.append(cleanString(notifId)).append("|");
		alarmString.append(Alarm.KEYGENERICNUMBER).append("|")
				.append(cleanString(alarm.getSpecificNumber())).append("|");

		alarmString.append(Alarm.KEYDEVICE).append("|")
				.append(cleanString(alarm.getDeviceName())).append("|");
		alarmString.append(Alarm.KEYCOMPONENT).append("|")
				.append(cleanString(alarm.getComponent())).append("|");
		alarmString.append(Alarm.KEYCOMPONENTTYPE).append("|")
				.append(cleanString(alarm.getComponentType())).append("|");
		alarmString.append(Alarm.KEYSEVERITY).append("|")
				.append(cleanString(alarm.getSeverityText())).append("|");
		alarmString.append(Alarm.KEYCONDITION).append("|")
				.append(cleanString(alarm.getCondition())).append("|");
		alarmString.append(Alarm.KEYSERVICEEFFECT).append("|")
				.append(cleanString(alarm.getServiceEffectText())).append("|");
		alarmString.append(Alarm.KEYLOCATION).append("|")
				.append(cleanString(alarm.getLocation())).append("|");
		alarmString.append(Alarm.KEYDIRECTION).append("|")
				.append(cleanString(alarm.getDirection())).append("|");
		alarmString.append(Alarm.KEYDESCRIPTION).append("|")
				.append(cleanString(getDescriptionAndX733(alarm))).append("|");
		alarmString.append(Alarm.KEYDEVICETYPE).append("|")
				.append(cleanString(alarm.getDeviceType())).append("|");
		return alarmString.toString();
	}
	private String getDescriptionAndX733(Alarm alarm ){
		String text=alarm.getDescription()+" ";
		
		return text;
		
	}

	private String cleanString(String s){
		//remove "|" form the string
		s= s.replace("|","_");
		s= s.replace("-->","");
		return s;
		
	}
	
	/*
	 * this function creates particular notif for fail over
	 * specific number 3 = no device
	 * specific number 4 = ems down 
	 * specific number 5 = ems down [clear]
	 * specific number 6 = ipam down 
	 * specific number 7 = ipam down [clear]
	 * specific number 8 = no alarm for x minutes 
	 * specific number 9 = no alarm for x minutes [clear]
	 * specific number 10 = list files incomplete zte 
	 * specific number 11 = list files incomplete zte [clear]
	 * specific number 12 = no topology files zte for x days
	 * specific number 13 = no topology files zte for x days [clear]
	 * 
	 */
	private String parseAlarmFailOver(Alarm alarm) {

		StringBuffer alarmString = new StringBuffer("");
		String name = "";
		try {
			name= InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error("no InetAddress ");
		}
		if (alarm.getSpecificNumber().equalsIgnoreCase("3")) {
			alarmString.append("0").append("|"+name+"|").append(notifId).append("|");
			alarmString.append(Alarm.KEYGENERICNUMBER).append("|")
					.append(alarm.getSpecificNumber()).append("|");

			alarmString.append(Alarm.KEYDEVICE).append("|").append(cleanString(alarm.getDescription()))
					.append("|");
			
			
		}
		
		else if (alarm.getSpecificNumber().equalsIgnoreCase("4") ||
				alarm.getSpecificNumber().equalsIgnoreCase("5")){

		alarmString.append("0").append("|"+cleanString(name)+"|").append(notifId).append("|");
		alarmString.append(Alarm.KEYGENERICNUMBER).append("|")
				.append(alarm.getSpecificNumber()).append("|");

		alarmString.append(Alarm.KEYDEVICE).append("|").append(cleanString(alarm.getEms()))
				.append("|");
		alarmString.append(Alarm.KEYCOMPONENT).append("|")
				.append(cleanString(alarm.getDescription())).append("|");
		alarmString.append(Alarm.KEYCOMPONENTTYPE).append("|")
		.append(cleanString(alarm.getProtocol())).append("|");
		}else 
			if (alarm.getSpecificNumber().equalsIgnoreCase("6") ||
					alarm.getSpecificNumber().equalsIgnoreCase("7") ||
					alarm.getSpecificNumber().equalsIgnoreCase("8") ||
					alarm.getSpecificNumber().equalsIgnoreCase("9") ||
					alarm.getSpecificNumber().equalsIgnoreCase("10") ||
					alarm.getSpecificNumber().equalsIgnoreCase("11") ||
					alarm.getSpecificNumber().equalsIgnoreCase("12") ||
					alarm.getSpecificNumber().equalsIgnoreCase("13")){

			alarmString.append("0").append("|"+cleanString(name)+"|").append(notifId).append("|");
			alarmString.append(Alarm.KEYGENERICNUMBER).append("|")
					.append(alarm.getSpecificNumber()).append("|");

			alarmString.append(Alarm.KEYDEVICE).append("|").append(cleanString(alarm.getEms()))
					.append("|");
			alarmString.append(Alarm.KEYCOMPONENT).append("|")
					.append(cleanString(alarm.getDescription())).append("|");
			alarmString.append(Alarm.KEYCOMPONENTTYPE).append("|")
			.append(cleanString(alarm.getName())).append("|");
			
			}
		return alarmString.toString();
	
	}

	//send event to smarts
	public boolean pushEvent(Alarm alarm) throws SmartsException {
		
		if(alarm.getDeviceType().equalsIgnoreCase("Unknown"))
			alarm.setDeviceType("Router");
		
		String stringAlarm = parseAlarmToString(alarm);

		MR_AnyVal inputVal = (MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(stringAlarm);
		MR_AnyVal inputVal2 = (MR_AnyVal) MR_AnyValUtils.mrAnyValString("");
		MR_AnyVal[] opsList = { inputVal, inputVal2 };

		MR_AnyVal result = remoteDomainManager.invokeOperation(
				"notifInterface", "notifEventQueue", "pushEvent", opsList);

		String result1 = result.getValue().toString();
		if (result1.equalsIgnoreCase("true"))
			return true;

		return false;

	}

	//send event to smarts
	public boolean pushEventFailOver(Alarm alarm) throws SmartsException {

		String stringAlarm = parseAlarmFailOver(alarm);
		
		MR_AnyVal inputVal = (MR_AnyVal) MR_AnyValUtils
				.mrAnyValString(stringAlarm);
		MR_AnyVal inputVal2 = (MR_AnyVal) MR_AnyValUtils.mrAnyValString("");
		MR_AnyVal[] opsList = { inputVal, inputVal2 };

		MR_AnyVal result = remoteDomainManager.invokeOperation(
				"notifInterface", "notifEventQueue", "pushEvent", opsList);
		log.debug("String sent to OI "+stringAlarm);
		String result1 = result.getValue().toString();
		if (result1.equalsIgnoreCase("true"))
			return true;

		return false;

	}
	//change status of device, interface, port, card
	public void changeStatusObject(IonixElement element, String attribute,
			String value) throws SmartsException {

		MR_AnyValObjRefSet objRefSet=null;
			try{
				objRefSet = (MR_AnyValObjRefSet) remoteDomainManager
					.get(element.getClassName(), element.getInstanceName(),
							"InstrumentedBy");
			}catch (Exception e) {
				throw new SmartsException("no element with name "+element.getInstanceName()+" in ipam "+remoteDomainManager.getNameDomain());
			}
			try {
				if (objRefSet!=null && objRefSet.getObjRefSetValue().length!=0){
			String[] ionixObject = new String[2];
			ionixObject[0] = (String) objRefSet.getObjRefSetValue()[0]
					.getClassName();
			ionixObject[1] = (String) objRefSet.getObjRefSetValue()[0]
					.getInstanceName();
			setSmartsInstanceStringAttributeValue(ionixObject[0],
					ionixObject[1], attribute, value);
			log.info("changed "+ element.getClassName()+" " +element.getInstanceName()+ " attribute: "+attribute+ " value: "+value+ " in IPAM "+remoteDomainManager.getNameDomain());
				}
				else {
					log.error("not found instrumentby for element "+element.getClassName()+" "+ element.getInstanceName());		}
		} catch (Exception e) {
			throw new SmartsException(SmartsException.DATA_ERROR);
		}
		return;

	}

	public void setSmartsInstanceStringAttributeValue(String className,
			String instanceName, String attributeName, String value)
			throws SmartsException {

		remoteDomainManager.putString(className, instanceName, attributeName,
				value);

	}

	public void setSmartsInstanceBooleanAttributeValue(String className,
			String instanceName, String attributeName, boolean value)
			throws SmartsException {

		remoteDomainManager.putBoolean(className, instanceName, attributeName,
				value);

	}

	//change status of interfaces and port
	public void changeStatusObject(IonixElement element, String attribute,
			boolean value) throws SmartsException {

		MR_AnyValObjRefSet objRefSet = (MR_AnyValObjRefSet) remoteDomainManager
				.get(element.getClassName(), element.getInstanceName(),
						"InstrumentedBy");
		String[] cardfaultnameclass = new String[2];
		if (objRefSet!=null && objRefSet.getObjRefSetValue().length!=0){
		cardfaultnameclass[0] = (String) objRefSet.getObjRefSetValue()[0]
				.getClassName();
		cardfaultnameclass[1] = (String) objRefSet.getObjRefSetValue()[0]
				.getInstanceName();
		setSmartsInstanceBooleanAttributeValue(cardfaultnameclass[0],
				cardfaultnameclass[1], attribute, value);
		log.info("changed "+ element.getClassName()+" " +element.getInstanceName()+ " attribute: "+attribute+ " value: "+value+ " in IPAM "+remoteDomainManager.getNameDomain());
		}
		else {
			log.error("not found instrumentby for element "+element.getClassName()+" "+ element.getInstanceName());		}
		return;

	}
	
	 public IonixElement getInterfaceByKey(IonixElement ionixElement,String interfaceKey)throws SmartsException {
	    	IonixElement interafceElement = new IonixElement();
	    	MR_AnyVal interfaceName = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(interfaceKey);
	    	MR_AnyVal[] opsList = {interfaceName};
	    	MR_AnyVal result = remoteDomainManager.invokeOperation(
					ionixElement.getClassName(),
					ionixElement.getInstanceName(), "findNetworkAdapterByDeviceID", opsList);
	    	
	    	
	    	String[] interfaceNameClass = result.getValue().toString()
			.split("::");
	    	if (interfaceNameClass.length==2){
	    	interafceElement.setClassName(interfaceNameClass[0]);
	    	interafceElement.setInstanceName(interfaceNameClass[1]);
	    	}else{
	    		log.warn("interface with key "+interfaceKey+ " and device " +ionixElement.getInstanceName()+ " not found in ipam "+remoteDomainManager.getNameDomain());
	    	}
	    	return interafceElement;
	 }
	 
	 public boolean cleanCardInGlobalTypeCollection(
				 String cardName)
		throws SmartsException {
		 
		 GlobalTopologyCollection globalTopologyCollection = new GlobalTopologyCollection();
			String nameAdap = PropertiesContainer.getInstance().getProperty(
					"ADAPTER_NAME");
			globalTopologyCollection.setName("GTC-" + nameAdap);
			
					// cancel this card from the GlobalTopologyCollection					
					MR_Ref ref = new MR_Ref("Card", cardName);
					try {
						MR_AnyValObjRef objRef = new MR_AnyValObjRef(ref);

						remoteDomainManager.remove(globalTopologyCollection.getCreationClassName(), globalTopologyCollection.getName(),
								"ConsistsOf", objRef);
						log.debug("The Relationship " + "ConsistsOf"
								+ " is successfully removed from " + globalTopologyCollection.getName()
								+  " and "+cardName);
					} catch (SmartsException e) {
						log
								.error("SmRemoteException when trying to remove connection between : "
										+ globalTopologyCollection.getName()
										+ " and: "
										+ cardName);
						throw e;
					}
					return true;
				
		}

	 
	 public boolean addCardInGlobalTypeCollection(
			 String cardName)
	throws SmartsException {
	 
		 GlobalTopologyCollection globalTopologyCollection = new GlobalTopologyCollection();
		String nameAdap = PropertiesContainer.getInstance().getProperty(
				"ADAPTER_NAME");
		globalTopologyCollection.setName("GTC-" + nameAdap);
		
				//add this card from the GlobalTopologyCollection 
			
				MR_Ref ref = new MR_Ref("Card", cardName);
				try {
					MR_AnyValObjRef objRef = new MR_AnyValObjRef(ref);

					remoteDomainManager.insert(globalTopologyCollection.getCreationClassName(), globalTopologyCollection.getName(),
							"ConsistsOf", objRef);
					log.debug("The Relationship " + "ConsistsOf"
							+ " is successfully added to "  + globalTopologyCollection.getName()
							+  " and "+cardName);
				} catch (SmartsException e) {
					log
							.error("SmRemoteException when trying to add connection between : "
									+ globalTopologyCollection.getName()
									+ " and: "
									+ cardName);
					throw e;
				}
				return true;
			
	}
	 
	 
}