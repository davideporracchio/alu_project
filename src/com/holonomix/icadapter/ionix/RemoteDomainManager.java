/*
 * Copyright 2006 holonomix
 *
 */

package com.holonomix.icadapter.ionix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.exception.SmartsException;
import com.smarts.remote.SmRemoteDomainManager;
import com.smarts.remote.SmRemoteException;
import com.smarts.repos.MR_AnyVal;
import com.smarts.repos.MR_AnyValBoolean;
import com.smarts.repos.MR_AnyValFloat;
import com.smarts.repos.MR_AnyValObjRef;
import com.smarts.repos.MR_AnyValObjRefSet;
import com.smarts.repos.MR_AnyValString;
import com.smarts.repos.MR_AnyValUnsignedInt;
import com.smarts.repos.MR_AnyValUnsignedLong;
import com.smarts.repos.MR_Choice;
import com.smarts.repos.MR_Ref;

/**
 * This is the main Smarts InCharge Domain Manager Connection class. IT
 * represents the connection to a selected Broker/Domain.
 * 
 */
public class RemoteDomainManager {

	public final static String ICTM_CLASSNAME = "ICF_TopologyManager";
	public final static String ICTM_INSTANCENAME = "ICF-TopologyManager";
	public final static String OPERATIONNAME_ADDPENDING = "addPending";
	public final static String OPERATIONNAME_FIND = "find";
	public final static String OPERATIONNAME_CONTAINS = "contains";
	public final static String OPERATIONNAME_CLEAR = "clear";
	public final static String OPERATIONNAME_INSERT = "insert";
	public final static String OPERATIONNAME_REMOVE = "remove";

	// ~ ICS Notification List (ICSNL) class paramaters
	public final static String ICSNL_CLASSNAME = "ICS_NotificationList";
	public final static String ICSNL_ALL_INSTANCENAME = "ICS_NL-ALL_NOTIFICATIONS";
	public final static String ICSNL_ALL_ALLNOTIFICATIONSPROPERTY = "AllNotifications";
	public final static String ICSNL_ALL_ALLNOTIFICATIONSDATAPROPERTY = "AllNotificationsData";

	public final static String ICSNF_NOTIFICATIONCLASSNAME = "ICS_Notification";

	// =========================================================================
	// ~ Smarts notification attributes - ones that we may want to influence
	// =========================================================================
	public final static String NOTIFICATION_ATTRIB_ACTIVE = "Active";
	public final static String NOTIFICATION_ATTRIB_ACKNOWLEDGED = "Acknowledged";
	public final static String NOTIFICATION_ATTRIB_ACKNOWLEDGMENTTIME = "AcknowledgmentTime";
	public final static String NOTIFICATION_ATTRIB_AUDITTRAIL = "AuditTrail";
	public final static String NOTIFICATION_ATTRIB_AUTOACKNOWLEDGMENTINTERVAL = "AutoAcknowledgmentInterval";
	public final static String NOTIFICATION_ATTRIB_CATEGORY = "Category";
	public final static String NOTIFICATION_ATTRIB_CERTAINTY = "Certainty";
	public final static String NOTIFICATION_ATTRIB_CLASSNAME = "ClassName";
	public final static String NOTIFICATION_ATTRIB_CLASSDISPLAYNAME = "ClassDisplayName";
	public final static String NOTIFICATION_ATTRIB_CLEARONACKNOWLEDGE = "CleanOnAcknowledged";
	public final static String NOTIFICATION_ATTRIB_CREATIONCLASSNAME = "CreationClassName";
	public final static String NOTIFICATION_ATTRIB_CUSTOMERS = "Customers";
	public final static String NOTIFICATION_ATTRIB_DISPLAYNAME = "DisplayName";
	public final static String NOTIFICATION_ATTRIB_DESCRIPTION = "Description";
	public final static String NOTIFICATION_ATTRIB_ELEMENTCLASSNAME = "ElementClassName";
	public final static String NOTIFICATION_ATTRIB_ELEMENTNAME = "ElementName";
	public final static String NOTIFICATION_ATTRIB_EVENTDISPLAYNAME = "EventDisplayName";
	public final static String NOTIFICATION_ATTRIB_EVENTNAME = "EventName";
	public final static String NOTIFICATION_ATTRIB_EVENTTEXT = "EventText";
	public final static String NOTIFICATION_ATTRIB_EVENTYPE = "EventType";
	public final static String NOTIFICATION_ATTRIB_EVENSTATE = "EventState";
	public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOCLEAR = "FirstScheduledAutoClear";
	public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOARCHIVE = "FirstScheduledAutoArchive";
	public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOACKNOWLEDGEMENT = "FirstScheduledAutoAcknowledge";
	public final static String NOTIFICATION_ATTRIB_FIRSTNOTIFIEDAT = "FirstNotifiedAt";
	public final static String NOTIFICATION_ATTRIB_IMPACT = "Impact";
	public final static String NOTIFICATION_ATTRIB_INMAINTENANCE = "InMaintenance";
	public final static String NOTIFICATION_ATTRIB_INSTANCENAME = "InstanceName";
	public final static String NOTIFICATION_ATTRIB_INSTANCEDISPLAYNAME = "InstanceDisplayName";
	public final static String NOTIFICATION_ATTRIB_INTERNALELEMENTCLASSNAME = "internalElementClassName";
	public final static String NOTIFICATION_ATTRIB_INTERNALELEMENTNAME = "internalElementName";
	public final static String NOTIFICATION_ATTRIB_INACTIVEAUTOARCHIVEINTERVAL = "InactiveAutoArchiveInterval";
	public final static String NOTIFICATION_ATTRIB_ISAGGREGATE = "IsAggregate";
	public final static String NOTIFICATION_ATTRIB_ISAGGREGATEDBY = "IsAggregatedBy";
	public final static String NOTIFICATION_ATTRIB_ISROOT = "IsRoot";
	public final static String NOTIFICATION_ATTRIB_LASTCHANGEDAT = "LastChangedAt";
	public final static String NOTIFICATION_ATTRIB_LASTCLEAREDAT = "LastClearedAt";
	public final static String NOTIFICATION_ATTRIB_LASTNOTIFIEDAT = "LastNotifiedAt";
	public final static String NOTIFICATION_ATTRIB_NEXTSERIALNUMBER = "NextSerialNumber";
	public final static String NOTIFICATION_ATTRIB_NOTIFICATIONTIME = "NotificationTime";
	public final static String NOTIFICATION_ATTRIB_NAME = "Name";
	public final static String NOTIFICATION_ATTRIB_OWNER = "Owner";
	public final static String NOTIFICATION_ATTRIB_OCCURRENCECOUNT = "OccurrenceCount";
	public final static String NOTIFICATION_ATTRIB_SEVERITY = "Severity";
	public final static String NOTIFICATION_ATTRIB_SERVICENAME = "ServiceName";
	public final static String NOTIFICATION_ATTRIB_SHOULDAUTOCLEAR = "ShouldAutoClear";
	public final static String NOTIFICATION_ATTRIB_SHOULDAUTOCLEARAT = "ShouldAutoClearAt";
	public final static String NOTIFICATION_ATTRIB_SHOULDAUTOACKNOWLEDGE = "ShouldAutoAcknowledge";
	public final static String NOTIFICATION_ATTRIB_SHOULDAUTOACKNOWLEDGEAT = "ShouldAutoAcknowledgeAt";
	public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEINACTIVEAUTOARCHIVE = "ShouldScheduleInactiveAutoArchive";
	public final static String NOTIFICATION_ATTRIB_SHOULDINACTIVEAUTOARCHIVE = "ShouldInactiveAutoArchive";
	public final static String NOTIFICATION_ATTRIB_SHOULDSHEDULEAUTOARCHIVE = "ShouldScheduleAutoArchive";
	public final static String NOTIFICATION_ATTRIB_SCHEDULEDFORNOTIFY = "scheduledForNotify";
	public final static String NOTIFICATION_ATTRIB_SHOULDINACTIVEAUTOARCHIVEAT = "ShouldInactiveAutoArchiveAt";
	public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEAUTOACKNOWLEDGE = "ShouldScheduleAutoAcknowledge";
	public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEAUTOCLEAR = "ShouldScheduleAutoClear";
	public final static String NOTIFICATION_ATTRIB_SOURCEDOMAINNAME = "SourceDomainName";
	public final static String NOTIFICATION_ATTRIB_TROUBLETICKETID = "TroubleTicketID";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED1 = "UserDefined1";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED2 = "UserDefined2";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED3 = "UserDefined3";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED4 = "UserDefined4";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED5 = "UserDefined5";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED6 = "UserDefined6";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED7 = "UserDefined7";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED8 = "UserDefined8";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED9 = "UserDefined9";
	public final static String NOTIFICATION_ATTRIB_USERDEFINED10 = "UserDefined10";
	public final static Integer MR_ValType_MR_DATE = 64;
	public final static String ICSPROPERTY_MEMBEROF = "MemberOf";

	public final static Logger log = Logger.getLogger(RemoteDomainManager.class);
	SmRemoteDomainManagerFacade smRemoteDomainManagerFacade;

	String domainName="";
	public RemoteDomainManager(SmRemoteDomainManager smRemoteDomainManager,String domainName) {

		this.smRemoteDomainManagerFacade = new SmRemoteDomainManagerFacade(smRemoteDomainManager);
		this.domainName=domainName;
	}
	
	public String getNameDomain(){
		return domainName;
		
	}

	public String getHostName() throws SmartsException {
		String host ="";
		
		MR_AnyVal anyVal = smRemoteDomainManagerFacade.get("SM_System", "SM-System","hostname");
		 host = anyVal.getValue().toString();
		
		return host;
	}
	/**
	 * Returns a list of instances of the class
	 * 
	 * @param scManager
	 *            the Smarts Connection Manager
	 * @param classname
	 * @return String[] the array of instance names
	 */
	public String[] getInstances(String className) throws SmartsException {

		String[] instanceNames = null;

		String eStr = null;
		

			instanceNames = smRemoteDomainManagerFacade.getInstances(className);
		

		return instanceNames;
	}

	public MR_AnyVal get(String className, String instanceName,
			String relationshipName) throws SmartsException {
		
			return smRemoteDomainManagerFacade.get(className, instanceName,
					relationshipName);
		
	}

	public void insert(String className, String instanceName,
			String relationshipName, MR_AnyVal ref) throws SmartsException {
		
			smRemoteDomainManagerFacade.insert(className, instanceName,
					relationshipName, ref);
		
	}
	
	public void remove(String className, String instanceName,
			String relationshipName, MR_AnyVal ref) throws SmartsException {
		
			smRemoteDomainManagerFacade.remove(className, instanceName,
					relationshipName, ref);
		
	}

	public MR_AnyVal getSingleRelation(String inchargeClassName,
			String instanceName, String relationType) throws SmartsException {

		MR_AnyVal anyVal;
		
			anyVal = smRemoteDomainManagerFacade.get(inchargeClassName, instanceName,
					relationType);
		
		return anyVal;
	}

	public void putRef(String className, String instanceName,
			String attributeName, MR_Ref ref) throws SmartsException {
		MR_AnyValObjRef objRef = new MR_AnyValObjRef(ref);
		
			smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					objRef);
		
	}

	public void putInteger(String className, String instanceName,
			String attributeName, Integer value) throws SmartsException {
		MR_AnyValUnsignedInt attributeValue = new MR_AnyValUnsignedInt(value);
		
			smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					attributeValue);
		
	}

	public void putFloat(String className, String instanceName,
			String attributeName, Float value) throws SmartsException {
		MR_AnyValFloat attributeValue = new MR_AnyValFloat(value);
		
			smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					attributeValue);
		
	}

	public void putString(String className, String instanceName,
			String attributeName, String value) throws SmartsException {
		MR_AnyValString attributeValue = new MR_AnyValString(value);
		
			smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					attributeValue);
		
	}

	public void putBoolean(String className, String instanceName,
			String attributeName, boolean value) throws SmartsException {
		MR_AnyValBoolean attributeValue = new MR_AnyValBoolean(value);
		
		smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					attributeValue);
		
	}

	public void putLong(String className, String instanceName,
			String attributeName, long value) throws SmartsException {
		MR_AnyValUnsignedLong attributeValue = new MR_AnyValUnsignedLong(value);
		
			smRemoteDomainManagerFacade.put(className, instanceName, attributeName,
					attributeValue);
			}

	public void removeOperation(String className, String instance,
			MR_AnyVal[] opsList) throws SmartsException {
		
			smRemoteDomainManagerFacade.invokeOperation(className, instance,
					"remove", opsList);
	
	}

	public MR_AnyVal invokeOperation(String className, String instance,
			String action, MR_AnyVal[] opsList) throws SmartsException {

		
			return smRemoteDomainManagerFacade.invokeOperation(className, instance,
					action, opsList);
		
	}

	public boolean isAttached() throws SmartsException {
		
		return 	smRemoteDomainManagerFacade.noop();
		
		

	}
	
public void quit() throws SmartsException {
		
		 	smRemoteDomainManagerFacade.quit();
		
		

	}

	/**
	 * Finds an instance object of a class
	 * 
	 * @param className
	 *            - the className
	 * @param instanceName
	 *            - the instanceName
	 * @return MR_AnyVal the classInstance object
	 */
	public MR_AnyVal findClassInstance(String className, String instanceName)
			throws SmartsException {

		MR_AnyVal instanceObj = null;
		instanceObj = findRepositoryInstance( className,
				instanceName);

		return instanceObj;
	}

	/**
	 * Finds an instance object of a class
	 * 
	 * @param classInstanceName
	 *            - the classInstance Name
	 * @return MR_AnyVal the classInstance object
	 */
	public MR_AnyVal findClassInstance(String classInstanceName)
			throws SmartsException {
		return findClassInstance(getClassName(classInstanceName),
				getInstanceName(classInstanceName));
	}

	/**
	 * Searches for an instance in the repository
	 * 
	 * @param scManager
	 *            the scManager
	 * @param instanceName
	 * @return MR_AnyVal the reference to the Host instance
	 */
	public  MR_AnyVal findRepositoryInstance(
			 String className,
			String instanceName) throws SmartsException {
		boolean exists = false;
		String eStr = null;

		
			exists = smRemoteDomainManagerFacade.instanceExists(className, instanceName);
			if (log.isDebugEnabled()) {
				log.debug("instance (" + className + "::" + instanceName
						+ ") exists in the repository? : " + exists);
			}
			if (exists) {
				MR_Ref[] instances = smRemoteDomainManagerFacade.findInstances(className,
						instanceName);
				if (instanceName != null) {
					if (log.isDebugEnabled()) {
						log.debug("findRepositoryInstance for " + className
								+ "::" + instanceName + " returning "
								+ instanceName.toString());
						log.debug("findRepositoryInstance for " + className
								+ "::" + instanceName + " returning ");
					}
					return new MR_AnyValObjRef(instances[0]);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("findRepositoryInstance returned null");
					}
				}
			}
		

		return null;
	}

	/**
	 * Returns whether an instance exists in the repository
	 * 
	 * @param className
	 *            the class name
	 * @param instanceName
	 *            the instance name
	 * @return boolean
	 */
	public boolean instanceExists(String className, String instanceName)
			throws SmartsException {
		boolean exists = false;

		if (smRemoteDomainManagerFacade == null) {
			throw new SmartsException(SmartsException.DATA_ERROR);
		}

		exists = smRemoteDomainManagerFacade.instanceExists(className,
					instanceName);
			if (log.isDebugEnabled()) {
				log.debug("instance (" + className + "::" + instanceName
						+ ") exists in the repository? : " + exists);
			}
		

		return exists;
	}

	/**
	 * Returns whether an instance exists in the repository
	 * 
	 * @param classInstanceName
	 * @return boolean whether the classInstance exists
	 */
	public boolean instanceExists(String classNameInstanceName)
			throws SmartsException {
		return instanceExists(getClassName(classNameInstanceName),
				getInstanceName(classNameInstanceName));
	}

	/**
	 * Creates an instance of a class
	 * 
	 * @param className
	 * @param instanceName
	 */
	public void createClassInstance(String className, String instanceName)
			throws SmartsException {

		
			if (!smRemoteDomainManagerFacade.instanceExists(className, instanceName))
				smRemoteDomainManagerFacade.createInstance(className, instanceName);
		
	}

	/**
	 * Creates an instance of a class
	 * 
	 * @param className
	 * @param instanceName
	 */
	public void createClassInstance(String classNameInstanceName)
			throws SmartsException {
		createClassInstance(getClassName(classNameInstanceName),
				getInstanceName(classNameInstanceName));
	}

	/**
	 * 
	 * @return
	 */
	protected String[] getNotificationAttributes() {

		String[] attribs = null;
		List<String> simpleAttribs = new ArrayList<String>();
		simpleAttribs.add(NOTIFICATION_ATTRIB_ACKNOWLEDGED);
		simpleAttribs.add(NOTIFICATION_ATTRIB_ACTIVE);
		simpleAttribs.add(NOTIFICATION_ATTRIB_CATEGORY);
		simpleAttribs.add(NOTIFICATION_ATTRIB_CERTAINTY);
		simpleAttribs.add(NOTIFICATION_ATTRIB_CLASSNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_DESCRIPTION);
		simpleAttribs.add(NOTIFICATION_ATTRIB_DISPLAYNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_ELEMENTCLASSNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_ELEMENTNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_EVENSTATE);
		simpleAttribs.add(NOTIFICATION_ATTRIB_EVENTDISPLAYNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_INSTANCEDISPLAYNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_EVENTNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_EVENTTEXT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_EVENTYPE);
		simpleAttribs.add(NOTIFICATION_ATTRIB_FIRSTNOTIFIEDAT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_IMPACT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_INMAINTENANCE);
		simpleAttribs.add(NOTIFICATION_ATTRIB_INSTANCENAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_LASTCHANGEDAT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_LASTCLEAREDAT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_LASTNOTIFIEDAT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_NAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_OCCURRENCECOUNT);
		simpleAttribs.add(NOTIFICATION_ATTRIB_OWNER);
		simpleAttribs.add(NOTIFICATION_ATTRIB_SEVERITY);
		simpleAttribs.add(NOTIFICATION_ATTRIB_SOURCEDOMAINNAME);
		simpleAttribs.add(NOTIFICATION_ATTRIB_TROUBLETICKETID);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED1);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED2);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED3);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED4);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED5);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED6);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED7);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED8);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED9);
		simpleAttribs.add(NOTIFICATION_ATTRIB_USERDEFINED10);

		attribs = new String[simpleAttribs.size()];
		simpleAttribs.toArray(attribs);
		return attribs;
	}

	/**
	 * Get an instance's complete attributes map
	 * 
	 * @param className
	 *            the name of the instance's class
	 * @param instanceName
	 *            the name of the instance
	 */
	public Map<String, String> getInstanceAttributesMap(String className,
			String instanceName) throws SmartsException {

		String[] attribs = null;
		Map vkMap = new HashMap();
		int i = 0;
		if (log.isDebugEnabled()) {
			log.debug("retrieving attributes for: " + className + "::"
					+ instanceName);
		}
		// try {
		attribs = getNotificationAttributes();

		/*
		 * } catch (IOException e) { if (log.isErrorEnabled()) {
		 * log.debug("Error reading the attribNamess array from the remoteDM");
		 * log.debug(e.getMessage()); return null; } } catch
		 * (SmRemoteException e) { if (log.isErrorEnabled()) {
		 * log.debug("Error reading the attribs array from the remoteDM");
		 * log.debug(re.getMessage()); return null; } }
		 */

		for (i = 0; i < attribs.length; i++) {
			
				MR_AnyVal anyVal = smRemoteDomainManagerFacade.get(className,
						instanceName, attribs[i]);
				int valType = anyVal.getType();
				vkMap.put(attribs[i], anyVal.toString());

				if (log.isDebugEnabled()) {
					log.debug("storing " + attribs[i] + "'s value of: "
							+ anyVal);
				}

			
		}
		return vkMap;
	}

	/**
	 * Get a selected list of an instance's attributes map
	 * 
	 * @param className
	 *            the name of the instance's class
	 * @param instanceName
	 *            the name of the instance
	 * @param attributesList
	 *            the list of attributes whose valies are to be returned
	 */
	public Map<String, String> getInstanceAttributesMap(String className,
			String instanceName, List<String> attributesList)
			throws SmartsException {

		String[] attribs = null;
		Map vkMap = new HashMap();
		int i = 0;
		if (log.isDebugEnabled()) {
			log.debug("retrieving attributes for: " + className + "::"
					+ instanceName + ". Using " + attributesList.size()
					+ " entries in the attributesList filter");
			
		}

		
			for( String nameAttr :attributesList) {
				
					MR_AnyVal anyVal = smRemoteDomainManagerFacade.get(className,
							instanceName, nameAttr);
					int valType = anyVal.getType();
					vkMap.put(nameAttr, anyVal.toString());

					if (log.isDebugEnabled()) {
						log.debug("storing " + nameAttr + "'s value of: "
								+ anyVal);
					}
				
			
		}
		return vkMap;
	}

	/**
	 * Returns the relation Set for a class' property
	 * 
	 * @param classInstanceName
	 * @param propertyName
	 *            the property Name that contains the relation set
	 * @return MR_Ref[] the relationset array
	 */
	public MR_Ref[] getRelationSetArray(String classInstanceName,
			String propertyName) throws SmartsException {
		String className = getClassName(classInstanceName);
		String instanceName = getInstanceName(classInstanceName);

		return getRelationSetArray(className, instanceName, propertyName);
	}

	/**
	 * Returns the relation Set for a class' property
	 * 
	 * @param className
	 * @param instanceName
	 * @param propertyName
	 *            the attribute that contains the relation set
	 * @return MR_Ref[] the relationset array
	 */
	public MR_Ref[] getRelationSetArray(String className, String instanceName,
			String propertyName) throws SmartsException {

		MR_Ref[] relationSetRef = null;
		MR_AnyValObjRefSet objRefSet = null;

		if (log.isDebugEnabled()) {
			log.debug("Getting the '" + propertyName + "' relationSet for: "
					+ className + "::" + instanceName);
		}

		DateTime d1 =new DateTime();
		objRefSet = (MR_AnyValObjRefSet) smRemoteDomainManagerFacade.get(
				className, instanceName, propertyName);
		
		DateTime d2 =new DateTime();
		log.debug("getting relationSet call completed in millis: "+(d2.getMillis()-d1.getMillis()));
		
		
		

		if (objRefSet != null) {
			relationSetRef = objRefSet.getObjRefSetValue();
		}

		if (log.isDebugEnabled()) {
			log.debug("returning relationSetRef: " + relationSetRef);
		}

		return relationSetRef;
	}

	/**
	 * Determines whether the row corresponding to the contents of the args
	 * exists in the given object table
	 * 
	 * @param classInstanceName
	 *            - the class instance name to clear
	 * @param MR_AnyVal
	 *            [] - a single value which represents the key into the table
	 * @return boolean - whether it exists or not
	 */
	public boolean invokeContainsOperation(String classInstanceName,
			MR_AnyVal[] args) throws SmartsException {
		String className = getClassName(classInstanceName);
		String instanceName = getInstanceName(classInstanceName);
		MR_AnyVal value = null;

		if (log.isDebugEnabled()) {
			log.debug("finding " + args[0] + " into: " + classInstanceName);
		}

		
			value = smRemoteDomainManagerFacade.invokeOperation(className,
					instanceName, OPERATIONNAME_CONTAINS, args);
		

		boolean exists = false;

		exists = StringUtils.isBlank(value.toString()) ? true : false;

		return exists;
	}

	/**
	 * Clears the contents of the instance via invokeOperation
	 * 
	 * @param classInstanceName
	 *            - the class instance name to clear
	 */
	public void invokeClearOperation(String classInstanceName, MR_AnyVal[] args)
			throws SmartsException {
		String className = getClassName(classInstanceName);
		String instanceName = getInstanceName(classInstanceName);

		if (log.isDebugEnabled()) {
			log.debug("Clearing: " + classInstanceName);
		}

		
			smRemoteDomainManagerFacade.invokeOperation(className, instanceName,
					OPERATIONNAME_CLEAR, args);
		
	}

	/**
	 * Inserts the contents of the args into the given object via
	 * invokeOperation
	 * 
	 * @param classInstanceName
	 *            - the class instance name to clear
	 */
	public void invokeInsertOperation(String classInstanceName, MR_AnyVal[] args)
			throws SmartsException {
		String className = getClassName(classInstanceName);
		String instanceName = getInstanceName(classInstanceName);

		if (log.isDebugEnabled()) {
			log.debug("Inserting " + args[0] + " into: " + classInstanceName);
		}

		
			smRemoteDomainManagerFacade.invokeOperation(className, instanceName,
					OPERATIONNAME_INSERT, args);
		
	}

	/**
	 * Removes the value specified value for the given object via
	 * invokeOperation
	 * 
	 * @param classInstanceName
	 *            - the class instance name to clear
	 */
	public void invokeRemoveOperation(String classInstanceName, MR_AnyVal[] args)
			throws SmartsException {
		String className = getClassName(classInstanceName);
		String instanceName = getInstanceName(classInstanceName);

		if (log.isDebugEnabled()) {
			log.debug("Removing " + args[0] + " from: " + classInstanceName);
		}

		
			smRemoteDomainManagerFacade.invokeOperation(className, instanceName,
					OPERATIONNAME_REMOVE, args);
		
	}

	/**
	 * Returns the short broker name for a given broker::domainManager
	 * 
	 * @param brokerDomain
	 *            the full broker::domain name
	 * @return String the short broker name
	 */
	public static String getBrokerName(String brokerDomain) {
		int brokerEnd = brokerDomain.toString().indexOf("::");

		return brokerDomain.substring(0, brokerEnd);
	}

	/**
	 * Returns the short domain name for a given broker::domainManager
	 * 
	 * @param brokerDomain
	 *            the full broker::domain name
	 * @return String the short domain name
	 */
	public static String getDomainName(String brokerDomain) {
		int domainStart = brokerDomain.toString().indexOf("::") + 2;

		return brokerDomain.substring(domainStart);
	}

	/**
	 * Returns the short class name for a given class::instance
	 * 
	 * @param classInstance
	 *            the full class::instance name
	 * @return String the short class name
	 */
	public static String getClassName(String classInstance) {
		if (StringUtils.isBlank(classInstance)) {
			return null;
		}

		int instanceEnd = classInstance.toString().indexOf("::");
		if (instanceEnd >= 0) {
			return classInstance.substring(0, instanceEnd);
		} else {
			return null;
		}
	}

	/**
	 * Returns the instance short name for a given class::instance
	 * 
	 * @param classInstance
	 *            the full class::instance name
	 * @return String the short instance name
	 */
	public static String getInstanceName(String classInstance) {
		if (StringUtils.isBlank(classInstance)) {
			return null;
		}

		int instanceStart = classInstance.toString().indexOf("::") + 2;

		if (instanceStart >= 0) {
			return classInstance.substring(instanceStart);
		} else {
			return null;
		}
	}

	/**
	 * Returns the list of date type attributes. For some unknown reason SMARTS
	 * is storing as unsigned ints, ready to be confused with standard int-based
	 * attribs, e.g. occurrenceConunt
	 * 
	 * @return List
	 */

	private static List getDateTypeAttributes() {
		List<String> dateTypeAttribs = new ArrayList();

		dateTypeAttribs.add(NOTIFICATION_ATTRIB_ACKNOWLEDGMENTTIME);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_FIRSTNOTIFIEDAT);
		dateTypeAttribs
				.add(NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOACKNOWLEDGEMENT);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOARCHIVE);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOCLEAR);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_LASTCHANGEDAT);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_LASTCLEAREDAT);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_LASTNOTIFIEDAT);
		dateTypeAttribs.add(NOTIFICATION_ATTRIB_SCHEDULEDFORNOTIFY);
		dateTypeAttribs.add(NOTIFICATIONAUDITLOG_ATTRIB_TIME);

		return dateTypeAttribs;
	}

	public final static String NOTIFICATIONAUDITLOG_ATTRIB_ID = "Id";
	public final static String NOTIFICATIONAUDITLOG_ATTRIB_TIME = "Time";
	public final static String NOTIFICATIONAUDITLOG_ATTRIB_USERID = "UserId";
	public final static String NOTIFICATIONAUDITLOG_ATTRIB_TYPE = "Type";
	public final static String NOTIFICATIONAUDITLOG_ATTRIB_DESCRIPTION = "Description";

}
