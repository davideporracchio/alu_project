package com.holonomix;

import java.util.List;

import org.apache.log4j.Logger;

import com.holonomix.exception.AdapterException;
import com.holonomix.exception.SmartsException;
import com.holonomix.file.service.DeleteFileService;
import com.holonomix.hsqldb.model.Device;
import com.holonomix.hsqldb.model.Narg;
import com.holonomix.hsqldb.model.Ncrg;
import com.holonomix.hsqldb.model.NetworkConnection;
import com.holonomix.hsqldb.model.VLan;
import com.holonomix.icadapter.ionix.BrokerManager;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.utils.IonixElement;
import com.holonomix.ionix.sam.SmartsReadService;
import com.holonomix.properties.PropertiesContainer;
import com.holonomix.topology.service.BuilderLogicService;



public class DeleteAdapter {
		

	private PropertiesContainer propertiesContainer;
	private static final Logger log = Logger.getLogger(DeleteAdapter.class);

	
	public DeleteAdapter() {
		 propertiesContainer = PropertiesContainer.getInstance();
		
	}
	

	public void start() throws AdapterException {

		
		

		DeleteFileService deleteFileService = DeleteFileService.getInstance(propertiesContainer.getProperty("DELETE_FILE"));
		List< String[]> listElement=deleteFileService.getDeleteFileList();
		//read list from file
		 
		
		
		
		BrokerManager brokerManager= BrokerManager.getInstance();
		
		 
		int i=0;
		
		for (String[] element :listElement){
			
			
			RemoteDomainManager remoteDomainManager;
			try {
				remoteDomainManager = brokerManager.getDomainManager(element[2]);
			
			BuilderLogicService builderLogicService=new BuilderLogicService(remoteDomainManager);
			IonixElement io=new IonixElement();
			log.info("deleting device "+element[1]+ " from ipam "+element[2]);
			io.setClassName(element[0]);
			io.setInstanceName(element[1]); 
			builderLogicService.removeObject(io);
			i++;
			
			SmartsReadService smartsReadService= new SmartsReadService(remoteDomainManager);
			Device dev= new Device();
			dev.setName(element[1]);
			smartsReadService.findNarg(element[1], dev);
			for (Narg narg : dev.getNargList()){
				IonixElement nObjcect=new IonixElement();
				log.info("deleting narg "+narg.getName()+ " from ipam "+element[2]);
				nObjcect.setClassName(narg.getCreationClassName());
				nObjcect.setInstanceName(narg.getName()); 
				builderLogicService.removeObject(nObjcect);
				
				
			}
			smartsReadService.findExistingNcrg(dev);
			for (Ncrg ncrg : dev.getNcrgList()){
				IonixElement nObjcect=new IonixElement();
				log.info("deleting ncrg "+ncrg.getName()+ " from ipam "+element[2]);
				nObjcect.setClassName(ncrg.getCreationClassName());
				nObjcect.setInstanceName(ncrg.getName()); 
				builderLogicService.removeObject(nObjcect);
				
				
			}
			
			List<NetworkConnection> listNetworkConnection = smartsReadService.getNetworkConnection();
			
			for (NetworkConnection netcon : listNetworkConnection){
				if (netcon.getName().indexOf(dev.getName())!=-1){
				IonixElement nObjcect=new IonixElement();
				log.info("deleting Net workConnection "+netcon.getName()+ " from ipam "+element[2]);
				nObjcect.setClassName(netcon.getCreationClassName());
				nObjcect.setInstanceName(netcon.getName()); 
				builderLogicService.removeObject(nObjcect);
				}
				
			}
			List<VLan> listVlan = smartsReadService
			.startReadingVLANAfterImportSmarts();
			for (VLan vlan : listVlan) {
		if (vlan.getDeviceNameList() != null
				&& vlan.getDeviceNameList().size() > 0) {
			

		} else {
			IonixElement ionixElement = new IonixElement();
			ionixElement.setClassName("VLAN");
			ionixElement.setInstanceName(vlan.getName());
			builderLogicService.removeObject(ionixElement);
			log.info("deleted vlan " + vlan.getName() + " from "
					+ remoteDomainManager.getNameDomain());

		}

	}
			
			} catch (SmartsException e) {
				// TODO Auto-generated catch block
				log.error("error removing object " +element[1]+ " in Ipam "+element[2]);
			}
		}
		log.info("removed "+i+" devices from smarts");
		System.exit(0);
	}
	
	

	
	
	

}
