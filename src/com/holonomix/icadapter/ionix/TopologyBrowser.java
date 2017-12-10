/*
ok * Copyright 2006 Holonomix
 *
 */
package com.holonomix.icadapter.ionix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.holonomix.exception.SmartsException;
import com.holonomix.icadapter.utils.Constants;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.icadapter.utils.MR_AnyValUtils;
import com.holonomix.properties.PropertiesContainer;
import com.smarts.repos.MR_AnyVal;
import com.smarts.repos.MR_AnyValObjRef;
import com.smarts.repos.MR_Ref;

/**
 * This is the TopologyBrowser class
 */
public class TopologyBrowser {

    public final static Logger log = Logger.getLogger(TopologyBrowser.class);

   
    private RemoteDomainManager remoteDomainManager    = null;
    private PropertiesContainer propertiesContainer;

    private static final String DID_CLASS     = "DID_CLASS";
    private static final String DID_INSTANCE  = "DID_INSTANCE";
    
    public final static String PK_TOPOLOGY_CLASSLIST = "TOPOLOGY_CLASSLIST";
    
    public final static String PK_SMARTS_BROKERHOST = "SMARTS_BROKERHOST";
    public final static String PK_SMARTS_DOMAINMGR = "SMARTS_DOMAINMGR";
    public final static String PK_SMARTS_DEFAULTDOMAINMGR = "SMARTS_DEFAULTDOMAINMGR";
    public final static String PK_SMARTS_DOMAINUSERNAME = "SMARTS_DOMAINUSERNAME";
    public final static String PK_SMARTS_DOMAINUSERPASSWORD = "SMARTS_DOMAINUSERPASSWORD";

    
    

    /**
     * Default constructor 
     */
    public TopologyBrowser(RemoteDomainManager remoteDomainManager) {
    	propertiesContainer = PropertiesContainer.getInstance();
        this.remoteDomainManager=remoteDomainManager;
    }

    /**
	 * getListFromString
	 * 
	 * @param concatString
	 *            string to parse in List
	 * @return List
	 * 
	 *         return a List from a string
	 */
	public List<String> getListFromString(String concatString) {

		String[] arrayList = propertiesContainer.getProperty(concatString).split(",");
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < arrayList.length; i++) {
			list.add(arrayList[i].trim());
		}
		return list;

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
    	if (interfaceNameClass.length!=2)
    		return null;
    	interafceElement.setClassName(interfaceNameClass[0]);
    	interafceElement.setInstanceName(interfaceNameClass[1]);
    	return interafceElement;
    }
   
    
    public IonixElement getPortByDescription(IonixElement ionixElement,String description)throws SmartsException {
    	IonixElement portElement = new IonixElement();
    	MR_AnyVal portDescription = (MR_AnyVal) MR_AnyValUtils.mrAnyValString(description);
    	MR_AnyVal[] opsList = {portDescription};
    	MR_AnyVal result = remoteDomainManager.invokeOperation(
				ionixElement.getClassName(),
				ionixElement.getInstanceName(), "findNetworkAdapterByDescription", opsList);
    	
    	String[] portNameClass = result.getValue().toString().split("::");
    	if (portNameClass.length!=2)
    		return null;
    	portElement.setClassName(portNameClass[0]);
    	portElement.setInstanceName(portNameClass[1]);
    	return portElement;
    }


 
    /**
    * Get a specific instance attribute 
    * @param className
    * @param instanceName
    * @param attributeName
    */
    public String getInstanceAttribute(String className, String
            instanceName, String attributeName) throws SmartsException {

        

        return getInstanceAttribute(className, instanceName,
                attributeName);
    }

    /**
    * Get a specific instance attribute 
    * @param classInstanceName
    * @param attributeName as an MR_AnyVal
    */
    public String getInstanceAttribute(String classInstanceName, String
            attributeName) throws SmartsException {
        String className = getClassName(classInstanceName);
        String instanceName = getInstanceName(classInstanceName);

        return getInstanceAttribute(className, instanceName, attributeName);
    }

    /**
    * Get a specific instance attribute 
    * @param className
    * @param instanceName
    * @param attributeName
    */
    public Map<String, String> getInstanceAttributesMap(String className, String
            instanceName) throws SmartsException {

        

        return remoteDomainManager.getInstanceAttributesMap(className, instanceName);
    }

    /**
    * Get a Map of specific instance attribute s
    * @param className
    * @param instanceName
    * @param attributesList
    */
    public Map<String, String> getInstanceAttributesMap(String className, String
            instanceName, List attributesList) throws SmartsException {

        

        return remoteDomainManager.getInstanceAttributesMap(className, instanceName, attributesList);
    }

   
    
  
    /**
      * Runs a findComputerSystemName and returns whether the system was
      * found
      * @param systemName the name of the system
      * @return boolean
      */
    public boolean instanceExists(String className, String instanceName) throws SmartsException {
        

        boolean instanceExists = remoteDomainManager.instanceExists(className, instanceName);

        return instanceExists;
    }

   
   

    

   
    /**
     * Return the map of class name to instance-list
     * @param classList a list of classes to return instances for
     * @return Map - the value of which is a LIST of instances
     * object
     */
    public Map getTopologyClassInstanceMap(List classList) throws SmartsException {
        

        HashMap ciMap = new HashMap();

        for (int c = 0; c < classList.size(); c++) {
            String className = (String) classList.get(c);
            String classInstances[] = null;
            classInstances = remoteDomainManager.getInstances(className);
            List iList = new ArrayList();
            for (int i = 0; i < classInstances.length; i++) {
                String iName = classInstances[i];
                iList.add(iName);
            }
            ciMap.put(className, iList);
        }

        return ciMap;
    }

    /**
     * Return the List of instances for this class
     * @param classList a list of classes to return instances for
     * @return List - a LabelValue list of topology instances
     * object
     */
    public Map <String,String> getTopologyInstanceValueList(String className) throws SmartsException {
        

        String instances[] = remoteDomainManager.getInstances(className);
        if (log.isDebugEnabled()) {
            log.debug("remoteDomainManager returned " + instances.length + 
                    " instances of class " + className+ " in IPAM "+remoteDomainManager.getNameDomain());
        }

        List iNameList = new ArrayList();

        for (int i = 0; i < instances.length; i++) {
            String iName = instances[i];
            iNameList.add(iName);
        }

        Map <String,String> lvList = new HashMap <String,String>();
        
        for (int i = 0; i < iNameList.size(); i++) {
            String iName = (String) iNameList.get(i);
            lvList.put(iName, iName);
        }

        if (log.isDebugEnabled()) {
            log.debug("remoteDomainManager returning " + lvList.size() + " labelValue" +
                    " instances of class " + className + " in IPAM "+remoteDomainManager.getNameDomain());
        }
        return lvList;
    }

    /**
     * Return the List of instances for this class
     * @param classList a list of classes to return instances for
     * @return List - a LabelValue list of topology instances
     * object
     */
    public List <String> getClassInstanceNames(String className) throws SmartsException {
        

        String instanceNames[] = remoteDomainManager.getInstances(className);
        if (log.isDebugEnabled()) {
            log.debug("remoteDomainManager returned " + instanceNames.length + 
                    " instances of class " + className + " in IPAM "+remoteDomainManager.getNameDomain());
        }

        //List <String> iNameList = new Arrays.asList(instanceNames);
        List<String> iNameList = new ArrayList<String>(instanceNames.length);
        for(int i=0, n=instanceNames.length; i<n; i++){
                iNameList.add(instanceNames[i]);
        }

        return iNameList;
    }

    /**
     * Return the List of inchargeInstances that this inchargeInstance
     * is composed of
     * @param inchargeClassName the device class name
     * @param instanceName the device name
     * @param relationType the relationship type to search for
     * @return a list of strings which represent the values of the set
     */
    public List<String> getRelationList(String inchargeClassName, String instanceName,
                String relationType) throws SmartsException {
       

        if (log.isDebugEnabled()) {
            log.debug("searching relationType " + relationType + 
                    " for instance: " + instanceName+ " in IPAM "+remoteDomainManager.getNameDomain());
        }

        MR_Ref[] relationSetArray =
            remoteDomainManager.getRelationSetArray(inchargeClassName,
                    instanceName, relationType);

        if (log.isDebugEnabled()) {
            log.debug("getRelationSetArray returned: " + relationSetArray.length + " relations in IPAM "+remoteDomainManager.getNameDomain());
            log.debug(relationSetArray);
        }
        List relationValueList = new ArrayList();
        if (relationSetArray!=null){
        
        for (int i = 0; i < relationSetArray.length; i++) {
            MR_AnyVal relationValue = new MR_AnyValObjRef(relationSetArray[i]);
            String rvString = relationValue.toString();
            relationValueList.add(rvString);
        }
        }
        if (log.isDebugEnabled()) {
            log.debug("returning " + relationValueList.size() + " relations");
        }

        return relationValueList;
    }
    
    public String getSingleRelation(String inchargeClassName, String instanceName,
            String relationType) throws SmartsException {
    

    if (log.isDebugEnabled()) {
        log.debug("searching relationType " + relationType + 
                " for instance: " + instanceName);
    }

    MR_AnyValObjRef object = (MR_AnyValObjRef) remoteDomainManager.getSingleRelation(inchargeClassName,
            instanceName, relationType);
     String rvString = object.toString();
     
    
    
    if (log.isDebugEnabled()) {
        log.debug("returning " + rvString + " relation");
    }

    return rvString;
}
    
    public  String getClassName(String classInstance) {
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
	public  String getInstanceName(String classInstance) {
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
	
	public RemoteDomainManager getDomainManager(){
		return remoteDomainManager;
	}
   

}
