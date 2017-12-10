package com.holonomix.icadapter.utils;

public class IonixElement {

	
	String instanceName;
	String className;
	
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String toString(){
		
		return className+":"+instanceName;
	}
	
	
}
