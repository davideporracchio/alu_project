package com.holonomix.topology.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.holonomix.hsqldb.model.Item;

public class ModelUtility {

	private static final Logger log = Logger.getLogger(ModelUtility.class);

	
public static String concatAllAttributes(Item item){
	Object paramsObj[] = {};
	StringBuffer concatValues=new StringBuffer();
	    // Loop through the methods 
		 try {
		 Method[] methods = item.getClass().getDeclaredMethods();
		 for (Method method : methods) {
	    	//concat all attributes there are in object "item"
			 if (method.getName().startsWith("get") 
					 && method.getName().indexOf("List")==-1 
					 && !method.getName().equalsIgnoreCase("getId")
					 && !method.getName().equalsIgnoreCase("getAdminStatus")
					 && !method.getName().equalsIgnoreCase("getOperStatus")
					 && !method.getName().equalsIgnoreCase("getMac")
					 && !method.getName().equalsIgnoreCase("getType")
					 && !method.getName().equalsIgnoreCase("getName")
					 && !method.getName().equalsIgnoreCase("getNumberPort")
					 && !method.getName().equalsIgnoreCase("getPortType")
					 && !method.getName().equalsIgnoreCase("getStatus")
					 && !method.getName().equalsIgnoreCase("getParentInterface")
					 && !method.getName().equalsIgnoreCase("getInterfaceNumber")
					 && !method.getName().equalsIgnoreCase("getInterfaceKey")
					 && !method.getName().equalsIgnoreCase("getVlanId")
					 && !method.getName().equalsIgnoreCase("getAdminStatus")
					 && !method.getName().equalsIgnoreCase("getNameForSmarts")
					 && !method.getName().equalsIgnoreCase("getBoardType")
					 //&& !method.getName().equalsIgnoreCase("getSerialNumber")
				 	&& !method.getName().equalsIgnoreCase("getPackageSystem")){
				 
	    	Object obj= method.invoke(item, paramsObj);
	    	
	    	 if (obj!=null){
	    		 concatValues=concatValues.append(obj.toString());
	    	 }
	    	 	
	    	}
		 }
		 // check attributes in superclass
		 //Class parent =  ;
		 methods = item.getClass().getSuperclass().getDeclaredMethods();
		 for (Method method : methods) {
	    	//concat all attributes there are in object "item"
			 if (method.getName().startsWith("get") && method.getName().indexOf("List")==-1 
					 && !method.getName().equalsIgnoreCase("getId") 
					 && !method.getName().equalsIgnoreCase("getDateCreated") 
					 && !method.getName().equalsIgnoreCase("getLastUpdated")
					 && !method.getName().equalsIgnoreCase("getVersion")
					 && !method.getName().equalsIgnoreCase("getDisplayName")
					 && !method.getName().equalsIgnoreCase("getName")
					 && !method.getName().equalsIgnoreCase("getTag")	
					 && !method.getName().equalsIgnoreCase("getInterfaceNumber")
					 && !method.getName().equalsIgnoreCase("getInterfaceKey")
					 //&& !method.getName().equalsIgnoreCase("getSerialNumber")
				 	 && !method.getName().equalsIgnoreCase("getPackageSystem")
					 && !method.getName().equalsIgnoreCase("getFlagStatus")){
	    	Object obj= method.invoke(item, paramsObj);
	    	 if (obj!=null){
	    		 concatValues=concatValues.append(obj.toString());
	    	 }
	    	 	 
	    	}
		 }
		 
		 
		} catch (IllegalArgumentException e) {
			
			log.error(" ModelUtility "+e.getMessage());
		} catch (IllegalAccessException e) {
			
			log.error(" ModelUtility "+e.getMessage());
		} catch (InvocationTargetException e) {
			
			log.error(" ModelUtility "+e.getMessage());
		}
		log.debug("ModelUtility attributes in the object :"+concatValues);
	  return concatValues.toString();
	}

public static void setAttribute(String nameMethod,String value,Item item){
	Object paramsObj[] = {value};
	if(item!=null){
	boolean isMethodPresent = false;
	
	    // Loop through the methods 
		 try {
		 Method[] methods = item.getClass().getDeclaredMethods();
		 for (Method method : methods) {
	    	//concat all attributes there are in object "item"
			 if (method.getName().startsWith("set") && method.getName().toLowerCase().indexOf(nameMethod.toLowerCase())!=-1 ){
	    	Object obj= method.invoke(item, paramsObj);
	    	//log.debug("set "+nameMethod +" value "+value);
	    	isMethodPresent=true;
	    	break;
	    	}
			
		 }
		 // check attributes in superclass
		 
		 if (isMethodPresent==false){
		 methods = item.getClass().getSuperclass().getDeclaredMethods();
		 for (Method method : methods) {
	    	//concat all attributes there are in object "item"
			 if (method.getName().startsWith("set") && method.getName().toLowerCase().indexOf(nameMethod.toLowerCase())!=-1 ){
	    	Object obj= method.invoke(item, paramsObj);
	    	//log.debug("set "+nameMethod +" value "+value);
	    	isMethodPresent=true;
	    	break;
	    	}
			 
		 }
		 if (isMethodPresent==false)
			 log.error("method "+nameMethod +" not found. value to set "+value);
		 }
		 
		} catch (IllegalArgumentException e) {
			
			log.error("  "+e.getMessage());
		} catch (IllegalAccessException e) {
			
			log.error("  "+e.getMessage());
		} catch (InvocationTargetException e) {
			
			log.error("  "+e.getMessage());
		}
	}
	else 
		log.error("it is not possible to set attributes if  item is null  ");
	 
	}

}
