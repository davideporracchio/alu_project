package com.holonomix;

import org.apache.log4j.Logger;

import com.holonomix.alarm.ImportAlarmThread;
import com.holonomix.alarm.service.CleanAlarmService;
import com.holonomix.alarm.service.StartUpService;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.listener.MonitorListener;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.ImportPartialTopologyThread;
import com.holonomix.topology.ImportTopologyThread;
import com.holonomix.topology.TopologyMemoryThread;

public class ProcessAdapter {

	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger.getLogger(ProcessAdapter.class);

	public ProcessAdapter(String flag) {
		// load properties file values using "secondary" server
		propertiesContainer = PropertiesContainer.getInstance();
		if (flag.equalsIgnoreCase("-s")) {
			if (propertiesContainer.getProperty("EMS_SECONDARY") != null)
				propertiesContainer.setProperty("EMS_IP",
						propertiesContainer.getProperty("EMS_SECONDARY"));
			propertiesContainer.setProperty("EMS_DESC", "Secondary");

			if (propertiesContainer.getProperty("CORBA_PASSWORD_SECONDARY") != null)
				propertiesContainer.setProperty("CORBA_PASSWORD",
						propertiesContainer
								.getProperty("CORBA_PASSWORD_SECONDARY"));

			if (propertiesContainer.getProperty("CORBA_USERNAME_SECONDARY") != null)
				propertiesContainer.setProperty("CORBA_USERNAME",
						propertiesContainer
								.getProperty("CORBA_USERNAME_SECONDARY"));

			if (propertiesContainer.getProperty("TL1_PASSWORD_SECONDARY") != null)
				propertiesContainer.setProperty("TL1_PASSWORD",
						propertiesContainer
								.getProperty("TL1_PASSWORD_SECONDARY"));

			if (propertiesContainer.getProperty("TL1_USERNAME_SECONDARY") != null)
				propertiesContainer.setProperty("TL1_USERNAME",
						propertiesContainer
								.getProperty("TL1_USERNAME_SECONDARY"));

			if (propertiesContainer.getProperty("TOPOLOGY_PASSWORD_SECONDARY") != null)
				propertiesContainer.setProperty("TOPOLOGY_PASSWORD",
						propertiesContainer
								.getProperty("TOPOLOGY_PASSWORD_SECONDARY"));

			if (propertiesContainer.getProperty("TOPOLOGY_USERNAME_SECONDARY") != null)
				propertiesContainer.setProperty("TOPOLOGY_USERNAME",
						propertiesContainer
								.getProperty("TOPOLOGY_USERNAME_SECONDARY"));

			if (propertiesContainer.getProperty("ALARM_USERNAME_SECONDARY") != null)
				propertiesContainer.setProperty("ALARM_USERNAME",
						propertiesContainer
								.getProperty("ALARM_USERNAME_SECONDARY"));

			if (propertiesContainer.getProperty("ALARM_PASSWORD_SECONDARY") != null)
				propertiesContainer.setProperty("ALARM_PASSWORD",
						propertiesContainer
								.getProperty("ALARM_PASSWORD_SECONDARY"));

			if (propertiesContainer
					.getProperty("ALARM_ACTIVE_USERNAME_SECONDARY") != null)
				propertiesContainer
						.setProperty(
								"ALARM_ACTIVE_USERNAME",
								propertiesContainer
										.getProperty("ALARM_ACTIVE_USERNAME_SECONDARY"));

			if (propertiesContainer
					.getProperty("ALARM_ACTIVE_PASSWORD_SECONDARY") != null)
				propertiesContainer
						.setProperty(
								"ALARM_ACTIVE_PASSWORD",
								propertiesContainer
										.getProperty("ALARM_ACTIVE_PASSWORD_SECONDARY"));

		} else {
			// load properties file values using "primary" server
			if (propertiesContainer.getProperty("EMS_PRIMARY") != null)
				propertiesContainer.setProperty("EMS_IP",
						propertiesContainer.getProperty("EMS_PRIMARY"));

			propertiesContainer.setProperty("EMS_DESC", "Primary");

			if (propertiesContainer.getProperty("CORBA_PASSWORD_PRIMARY") != null)
				propertiesContainer.setProperty("CORBA_PASSWORD",
						propertiesContainer
								.getProperty("CORBA_PASSWORD_PRIMARY"));

			if (propertiesContainer.getProperty("CORBA_USERNAME_PRIMARY") != null)
				propertiesContainer.setProperty("CORBA_USERNAME",
						propertiesContainer
								.getProperty("CORBA_USERNAME_PRIMARY"));

			if (propertiesContainer.getProperty("TL1_PASSWORD_PRIMARY") != null)
				propertiesContainer
						.setProperty("TL1_PASSWORD", propertiesContainer
								.getProperty("TL1_PASSWORD_PRIMARY"));

			if (propertiesContainer.getProperty("TL1_USERNAME_PRIMARY") != null)
				propertiesContainer
						.setProperty("TL1_USERNAME", propertiesContainer
								.getProperty("TL1_USERNAME_PRIMARY"));

			if (propertiesContainer.getProperty("TOPOLOGY_PASSWORD_PRIMARY") != null)
				propertiesContainer.setProperty("TOPOLOGY_PASSWORD",
						propertiesContainer
								.getProperty("TOPOLOGY_PASSWORD_PRIMARY"));

			if (propertiesContainer.getProperty("TOPOLOGY_USERNAME_PRIMARY") != null)
				propertiesContainer.setProperty("TOPOLOGY_USERNAME",
						propertiesContainer
								.getProperty("TOPOLOGY_USERNAME_PRIMARY"));

			if (propertiesContainer.getProperty("ALARM_USERNAME_PRIMARY") != null)
				propertiesContainer.setProperty("ALARM_USERNAME",
						propertiesContainer
								.getProperty("ALARM_USERNAME_PRIMARY"));

			if (propertiesContainer.getProperty("ALARM_PASSWORD_PRIMARY") != null)
				propertiesContainer.setProperty("ALARM_PASSWORD",
						propertiesContainer
								.getProperty("ALARM_PASSWORD_PRIMARY"));

			if (propertiesContainer
					.getProperty("ALARM_ACTIVE_USERNAME_PRIMARY") != null)
				propertiesContainer.setProperty("ALARM_ACTIVE_USERNAME",
						propertiesContainer
								.getProperty("ALARM_ACTIVE_USERNAME_PRIMARY"));

			if (propertiesContainer
					.getProperty("ALARM_ACTIVE_PASSWORD_PRIMARY") != null)
				propertiesContainer.setProperty("ALARM_ACTIVE_PASSWORD",
						propertiesContainer
								.getProperty("ALARM_ACTIVE_PASSWORD_PRIMARY"));

		}

	}

	public void start() {
		// start the adapter
		Boolean topologyUpdateNow = new Boolean(propertiesContainer
				.getProperty("TOPOLOGY_IMPORT_NOW").trim());
		Boolean alarmUpdateNow = new Boolean(propertiesContainer.getProperty(
				"ALARM_IMPORT_NOW").trim());
		MapMonitor mapMonitor = MapMonitor.getInstance();
		MonitorListener monitorListener = new MonitorListener(mapMonitor);
		mapMonitor.addActiveMapListener(monitorListener);
		ImportTopologyThread importTopology = new ImportTopologyThread();
		ImportAlarmThread importAlarm = new ImportAlarmThread();

		ImportPartialTopologyThread importPartialTopology = new ImportPartialTopologyThread();

		TopologyMemoryThread topologyMemory = new TopologyMemoryThread();
		// startup process
		// check if ipam/oi smarts is up and ems alarm is up
		StartUpService startUpService = new StartUpService();

		// davide2
		int exitNumber = startUpService.acquisition();
		if (exitNumber > 0) {
			// stop the adapter
			// 20 ipam
			// 10 ems
			log.error("Problem to open one of these connections: "
					+ "Ionix Ipams, Ionix OI, EMS Active Alarm, EMS new alarm");
			log.info("shut down application. Exit code: " + exitNumber);
			System.exit(exitNumber);

		}
		log.info("Startup acquisition completed with success");

		// clean status using globalTopologyCollection information

		log.debug(" clean status cards and ports/interface before importing alarms");

		CleanAlarmService cleanAlarmService = new CleanAlarmService();
		// davide
		try {
			log.info("cleaning status objects (all object are UP)...");
			//if(1==2)
			cleanAlarmService.cleanObjects();
			log.info("updating status objects using GlobalTopologyCollection.. ");

			// davide
			//if(1==2)
			cleanAlarmService.cleanUsingGTCEntry();
		} catch (SmartsException e) {

			log.error(" error connection Smarts " + e.getMessage());
			String message = e.getMessage();
			if(message.contains("ipam=")){
				message =message.substring(message.indexOf("ipam=")+6);
			}
			MapMonitor.getInstance().put("IPAM",
					"down, " + message);

		}
		Thread tm = new Thread(topologyMemory, "Topology_memory");
		tm.start();
		// run the topologyImport
		Thread t = new Thread(importTopology, "Import_topology");
		t.start();
		if (com.holonomix.ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN) {
			Thread tp = new Thread(importPartialTopology,
					"Import_partial_topology");
			tp.start();
		}
		// wait until the topology ends or alarmupdatenow is true
		while (alarmUpdateNow == false
				&& (importTopology.isCompleted() == false)) {

			try {
				Thread.currentThread().sleep(10000);

			} catch (InterruptedException e) {
				log.error("Thread interrupted");
			}
		}
		// star thread alarm
		log.debug("start import alarms ");
		Thread t2 = new Thread(importAlarm, "Import_alarm");
		t2.setDaemon(true);
		t2.start();

	}

}
