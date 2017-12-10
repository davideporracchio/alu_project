package com.holonomix.ionix.sam;

import org.apache.log4j.Logger;

import com.holonomix.exception.SmartsException;
import com.holonomix.icadapter.ionix.RemoteDomainManager;
import com.holonomix.icadapter.utils.IonixElement;
import com.smarts.repos.MR_AnyVal;

public class SmartsDeleteService {
	
	private static final Logger log = Logger.getLogger(SmartsDeleteService.class);

	RemoteDomainManager remoteDomainManager;

	public SmartsDeleteService(RemoteDomainManager remoteDomainManager) {
		this.remoteDomainManager=remoteDomainManager;
	}

	public void removeObject (
			IonixElement element) throws SmartsException{

		
			MR_AnyVal[] opsList = {};
			
			remoteDomainManager.removeOperation(element.getClassName(), element
					.getInstanceName(), opsList);

		

		return;

	}

}
