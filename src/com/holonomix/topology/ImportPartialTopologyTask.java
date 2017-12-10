package com.holonomix.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.ClassFactory;
import com.holonomix.commoninterface.TopologyAdapterInterface;
import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.MappingFileService;
import com.holonomix.file.service.SeedFileService;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.Ionix;
import com.holonomix.hsqldb.model.Ipam;
import com.holonomix.hsqldb.model.NetworkConnection;
import com.holonomix.hsqldb.model.VLan;
import com.holonomix.log.topology.TopologyLog;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.service.BuilderDelitionFileService;


public class ImportPartialTopologyTask {

	private static final Logger log = Logger
			.getLogger(ImportPartialTopologyTask.class);
	private PropertiesContainer propertiesContainer;

	public ImportPartialTopologyTask() {
		propertiesContainer = PropertiesContainer.getInstance();

	}

	public void doImport() {

		DateTime timeImport = new DateTime();
		// load the right class from classFactory
		TopologyAdapterInterface topologyAdapterInterface = ClassFactory
				.getTopologyAdapterInstance();
		Ionix ionix = new Ionix();

		ionix.setName(timeImport.toString("dd/MM/YYYY HH:mm"));
		// ask for list devices

		// check if we have a list of device we want to migrate
		if (log.isDebugEnabled())
			log.debug("check seed file "
					+ propertiesContainer.getProperty("SEED_FILE"));
		SeedFileService seedFile = SeedFileService
				.getInstance(propertiesContainer.getProperty("SEED_FILE"));
		Set<String> listSeedFile = seedFile.getSeedFileList();
		
		// device to import
		
		if (listSeedFile.size()>0){
			log.info("Starting partial import");
		}else {
			log.info("file is empty");
			return;
		}
		Map<String, Device> mapDevice = new HashMap<String, Device>();
		Map<String, VLan> mapVLans = new HashMap<String, VLan>();
		List<NetworkConnection> listNetworkConnection = new ArrayList<NetworkConnection>();
		try {
			//import objects from ems
			topologyAdapterInterface.getDevicesAndVlans(mapDevice, mapVLans,
					listSeedFile);
			
		} catch (AdapterException e) {
			if (e.getCategoryError() == 1) {
				// MapMonitor.getInstance().put("TOPOLOGY", e.getMessage());
				return;

			}

			log.error(" error AdapterException " + e.getMessage());

		}

		if (listSeedFile == null || listSeedFile.size() == 0) {
			// migrate everything
			ionix.setDescription(Ionix.COMPLETEIMPORT);

		} else {
			// migrate just what is in the set list
			ionix.setDescription(Ionix.PARTIALIMPORT);

		}

		// migrate the right devices or all

		// search the right ipam for each device
		if (log.isDebugEnabled())
			log.debug("check map file "
					+ propertiesContainer
							.getProperty("MULTI_DOMAIN_MAPPING_FILE"));
		MappingFileService mappingFile = MappingFileService.getInstance(
				propertiesContainer.getProperty("MULTI_DOMAIN_MAPPING_FILE"),
				propertiesContainer.getProperty("AM_DOMAINS"));

		// update list of ipams each time there is a new import
		mappingFile.update();
		// if device is not in ipamFileMap I do not import it because I do not
		// know the ipam

		Set<Ipam> setIpam = ionix.getIpamList();
		BuilderDelitionFileService builderDelitionFileService = BuilderDelitionFileService
				.getInstance();
		// for each device to import check its ipam
		for (String deviceName : mapDevice.keySet()) {
			// put each device we found in ems in builderDelitionFileService
			Device device = mapDevice.get(deviceName);
			if (device == null) {
				device = new Device();
				device.setName(deviceName);
				builderDelitionFileService.saveDevice(device);
			} else {
				builderDelitionFileService.saveDevice(device);
				
				Set<String> ipamArray = mappingFile.findIpamsForDevice(device
						.getName());

				if (ipamArray != null && ipamArray.size() > 0) {

					if (device.getFlagStatus() != null) {
						TopologyLog.printDeviceStatus(device);
					}
					// fill ionix object with all ipams
					for (String ipamName : ipamArray) {
						Ipam ipam = new Ipam();
						ipam.setName(ipamName);
						ipam.getSummaryTopology().setName(ipamName);
						log.debug("Device " + device.getName()
								+ " is linked to ipam " + ipamName);
						setIpam.add(ipam);

						for (Ipam ipamTemp : setIpam) {
							if (ipamTemp.equals(ipam)) {
								Set<Device> setDevice = ipamTemp
										.getDeviceList();
								// add device
								Device d = device.clone();
								d.setFlagStatus(device.getFlagStatus());
								setDevice.add(d);
							}
						}
					}
				} else {

					TopologyLog.printDeviceStatus(device);

					log.warn("Device(s) not instantiated into IPAM due to no IPAM Mapping ");
					MapMonitor
							.getInstance()
							.put("NODEVICE",
									"Device(s) not instantiated into IPAM due to no IPAM Mapping");

				}
			}
		}

		// Load networkConnection
		if (listNetworkConnection.size() != 0) {

			for (Ipam ipamTemp : setIpam) {
				String deviceList = "";
				for (Device device : ipamTemp.getDeviceList()) {
					deviceList += device.getName() + ",";
				}
				for (NetworkConnection networkConnection : listNetworkConnection) {
					String name1="";
					String name2="";
					try{
					 name1 = networkConnection.getInterfaceA()
							.getParentDevice();
					 name2 = networkConnection.getInterfaceB()
							.getParentDevice();
					}catch(Exception e){
						log.warn("NetworkConnection "+networkConnection.getName()+ " does not match interfaces");
						continue;
					}
					if (deviceList.indexOf(name1) != -1
							&& deviceList.indexOf(name2) != -1) {
						// we put networkConnection in ipam
						ipamTemp.getNetworkConnectionList().add(
								networkConnection.clone());
					} else {
						log.warn("There are not devices: " + name1 + " and "
								+ name2 + " in " + ipamTemp.getName()
								+ " to link this NetworkConnection "
								+ networkConnection.getName() + " ");
					}

				}
			}

		}

		if (ionix.getIpamList().size() == 0) {
			log.debug("no devices to create in the new ionix db");

		} else {
			// start to compare ipams

			ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 10, 50000L,
					TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			int maxConnection = Integer.parseInt(propertiesContainer
					.getProperty("DOMAIN_RETRY_NUMBER"));
			FutureTask<Boolean>[] tasks = new FutureTask[setIpam.size()];

			Map<Integer, Ipam> taskIpam = new HashMap<Integer, Ipam>();
			List<Ipam> ipamsToExecute = new ArrayList<Ipam>(setIpam);
			int numberConnection = 0;
			try {
				while (ipamsToExecute.size() > 0
						&& numberConnection < maxConnection) {
					runFutureTask(ipamsToExecute, tpe, tasks, taskIpam);

					waitFutureTask(tasks);

					resultTask(ipamsToExecute, tasks, taskIpam);

					if (ipamsToExecute.size() > 0
							&& numberConnection < maxConnection) {
						
						Thread.sleep(Integer.parseInt(propertiesContainer
								.getProperty("DOMAIN_RETRY_HOLDTIME")) * 1000);
					}

					numberConnection++;
				}
				if (numberConnection >= maxConnection) {
					log.error(" error importing data in IPAM ");
					for (Ipam ipam : ipamsToExecute) {
						if (ipam != null) {
							MapMonitor.getInstance().put("IPAM",
									"down, " + ipam.getName());
							// log situation ipam
							TopologyLog.printIpamStatus(ipam);
						}
					}
				}

				tpe.shutdown();
				tpe.awaitTermination(1000, TimeUnit.MILLISECONDS);
				// log situation ipam
				for (Ipam ipamTemp : taskIpam.values()) {
					if (ipamTemp != null) {
						TopologyLog.printIpamStatus(ipamTemp);
					}
				}

			} catch (InterruptedException ignored) {
				log.error("interrupted thread ");
			}

		}
		try {
			builderDelitionFileService.buildFile();
		} catch (SmartsException e) {
			log.error("smarts exception ");
		}

		// itemDao.save(ionix);

	}

	private void runFutureTask(List<Ipam> ipamsToExecute,
			ThreadPoolExecutor tpe, FutureTask<Boolean>[] tasks,
			Map<Integer, Ipam> taskIpam) {
		taskIpam.clear();
		int i = 0;
		for (Ipam ipamNew : ipamsToExecute) {
			tasks[i] = new FutureTask<Boolean>(new SmartsCallable(ipamNew));
			tpe.execute(tasks[i]);
			taskIpam.put(i, ipamNew);
			i++;
		}
	}

	private void waitFutureTask(FutureTask<Boolean>[] tasks) {
		for (int i = 0; i < tasks.length; i++) {

			if (tasks[i] != null) {
				while (!tasks[i].isDone()) {
					// log.debug("Task not yet completed.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						log.error("Interrupted");
					}
				}
			}
		}
	}

	private List<Ipam> resultTask(List<Ipam> ipamsToExecute,
			FutureTask<Boolean>[] tasks, Map<Integer, Ipam> taskIpam) {
		ipamsToExecute.clear();
		for (int i = 0; i < tasks.length; i++) {

			if (tasks[i] != null) {
				try {
					Boolean result = tasks[i].get();
					if (result.booleanValue() == false)
						log.error("Error in import topology ");
				} catch (Exception e) {

					if (e instanceof ExecutionException
							&& e.getCause() instanceof SmartsException) {
						if (((SmartsException) e.getCause()).getTypeError() == SmartsException.CONNECTION_ERROR)
							ipamsToExecute.add(taskIpam.get(i));
					} else
						ipamsToExecute.add(taskIpam.get(i));
					if (taskIpam.get(i) != null){
						log.error("error in ipam: " + taskIpam.get(i).getName()+ " error:"+e.getMessage());
					
					}
				}
			}
		}
		return ipamsToExecute;
	}

}
