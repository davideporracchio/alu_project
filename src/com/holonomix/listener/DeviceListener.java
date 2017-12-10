package com.holonomix.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DeviceListener {

	private static final Logger log = Logger.getLogger(DeviceListener.class);

	private static List<String> listDevice = new ArrayList<String>();

	public static synchronized void setDevice(String name) {

		listDevice.add(name);
	}

	public static synchronized void clear() {
		listDevice.clear();
	}
	
	public static synchronized int size() {
		return listDevice.size();
	}
	
	public static synchronized boolean isDeviceNew(String name) {
		return listDevice.contains(name);
	}

}
