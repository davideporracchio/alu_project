package com.holonomix.icadapter.ionix;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.holonomix.ClassFactory;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.exception.SmartsException;
import com.holonomix.hsqldb.model.utility.EncryptDecrypt;
import com.holonomix.monitor.MapMonitor;
import com.holonomix.properties.PropertiesContainer;
import com.smarts.remote.SmRemoteBroker;
import com.smarts.remote.SmRemoteDomainManager;
import com.smarts.remote.SmRemoteException;

public class BrokerManager {

	private static BrokerManager brokerManager;
	private SmRemoteBroker broker;
	

	private Map<String, RemoteDomainManager> mapDomainManager;
	private Map<String, RemoteDomainManager> mapDomainManagerAlarm;
	public final static Logger log = Logger.getLogger(BrokerManager.class);
	private PropertiesContainer propertiesContainer;
	
	public static BrokerManager getInstance( ) {
		if (brokerManager == null)
			brokerManager = new BrokerManager();
		return brokerManager;
	}

	private BrokerManager( ) {
		 propertiesContainer = PropertiesContainer.getInstance();

		mapDomainManager = new HashMap<String, RemoteDomainManager>();
		mapDomainManagerAlarm = new HashMap<String, RemoteDomainManager>();
		attach();
	}

	private boolean attach()  {
		try {
			broker = new SmRemoteBroker(propertiesContainer.getProperty("BROKER_HOST"),
					Integer.parseInt(propertiesContainer.getProperty("BROKER_PORT")));
		
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		

	}

	public boolean isAttached() {
		if (broker != null) {
			return broker.attached();
		}
		return false;

	}

	public synchronized BrokerManager resetConnectionManager(
			Properties properties) throws SmartsException {

		brokerManager = new BrokerManager();
		return brokerManager;
	}

	public synchronized RemoteDomainManager getDomainManager(String domain)
			throws SmartsException {
		
		if (mapDomainManager.containsKey(domain)) {
			RemoteDomainManager remoteDomainManager = mapDomainManager
					.get(domain);
			try{
			if (remoteDomainManager.isAttached()) {
				return remoteDomainManager;
			}else{
				remoteDomainManager.quit();
			}}
			catch(Exception e ){
				log.debug("try to attach domain "+domain+" after connection issue");
			
			}

		}
		log.debug("Domain "+domain +" for topology is not in the pool or is not attached");	
		try {
			attach();
			SmRemoteDomainManager remoteServer = new SmRemoteDomainManager();
			String password_domain = "";
			//password is encrypted for HWMETE HWZTE HWBRAS ALUFTTH
			if (ClassFactory.getEnum() == EnumAdapterName.HWMETE || ClassFactory.getEnum() == EnumAdapterName.HWZTE ||ClassFactory.getEnum() == EnumAdapterName.HWBRAS ||
					ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN ||		ClassFactory.getEnum() == EnumAdapterName.ALUFTTH || ClassFactory.getEnum() == EnumAdapterName.HWIPSC){
			 password_domain = EncryptDecrypt.decryptPassword(propertiesContainer
					.getProperty("DOMAIN_PASSWORD").trim()); }
			else{
			 password_domain = (propertiesContainer
					.getProperty("DOMAIN_PASSWORD").trim());
			}
			log.debug("trying to connect to domain manager: "+domain);
			
			remoteServer.attach(broker, domain, propertiesContainer
					.getProperty("DOMAIN_USERNAME"), password_domain);
			log.debug("completed connection to domain manager: "+domain);
			remoteServer.noop();
			log.debug("noop successfully completed for domain: "+domain);
			broker.detach();
			RemoteDomainManager remoteDomainManager = new RemoteDomainManager(
					remoteServer,domain);
			//check host 
			//String host = remoteDomainManager.getHostName();
			//checkHost(host,remoteDomainManager);
			
			mapDomainManager.put(domain, remoteDomainManager);
			return remoteDomainManager;
		} catch (UnknownHostException e) {

			log.error(e.getMessage());
			throw new SmartsException(SmartsException.CONNECTION_ERROR);
		} catch (IOException e) {

			log.error(e.getMessage());
			throw new SmartsException();
		} catch (SmRemoteException e) {

			log.error(e.getMessage());
			throw new SmartsException(SmartsException.CONNECTION_ERROR);
		}

	}

	public synchronized RemoteDomainManager getDomainManagerForAlarm(
			String domain,boolean checkServer) throws SmartsException {
		
		if (mapDomainManagerAlarm.containsKey(domain)) {
			try{
			RemoteDomainManager remoteDomainManager = mapDomainManagerAlarm
					.get(domain);
			if (remoteDomainManager.isAttached()) {
				return remoteDomainManager;
			}else{
				log.debug("trying to quit domain "+domain);
				remoteDomainManager.quit();
				
				log.debug("ipam "+domain+" is not attached");
			}
			}
			catch(Exception e)
			{
				log.debug("Error with domain "+domain + " message "+e.getMessage());
				throw new SmartsException("ipam "+domain+" is not attached");}
		}
		RemoteDomainManager remoteDomainManager=null;
		
		log.debug("Domain "+domain +" is not in the pool or is not attached");	
					
		try {
			attach();
			SmRemoteDomainManager remoteServer  = new SmRemoteDomainManager();
			String password_domain = "";
			//password is encrypted for HWMETE HWZTE HWBRAS ALUFTTH
			if (ClassFactory.getEnum() == EnumAdapterName.HWMETE || ClassFactory.getEnum() == EnumAdapterName.HWZTE || ClassFactory.getEnum() == EnumAdapterName.HWBRAS ||
					ClassFactory.getEnum() == EnumAdapterName.ZTEMSAN ||	ClassFactory.getEnum() == EnumAdapterName.ALUFTTH || ClassFactory.getEnum() == EnumAdapterName.HWIPSC){
			 password_domain = EncryptDecrypt.decryptPassword(propertiesContainer
					.getProperty("DOMAIN_PASSWORD").trim()); }
			else{
			 password_domain = (propertiesContainer
					.getProperty("DOMAIN_PASSWORD").trim());
			}
			log.debug("trying to connect to domain manager: "+domain);
			
			long start = System.nanoTime();
			remoteServer.attach(broker, domain, propertiesContainer
					.getProperty("DOMAIN_USERNAME"), password_domain);
			long end = System.nanoTime();
			long sec = TimeUnit.SECONDS.convert(end-start, TimeUnit.NANOSECONDS);
			
			log.debug("completed connection to domain manager: "+domain +" in seconds " +sec);
			remoteServer.noop();
			log.debug("noop successfully completed for domain: "+domain);
			
			broker.detach();
			remoteDomainManager = new RemoteDomainManager(
					remoteServer,domain);
			//check host 
			String host = remoteDomainManager.getHostName();
			mapDomainManagerAlarm.put(domain, remoteDomainManager);
			//if (checkServer)
			//	checkHost(host,remoteDomainManager);
			
			
			
		} catch (UnknownHostException e) {

			log.error(e.getMessage());
			//throw new SmartsException(SmartsException.CONNECTION_ERROR);
		} catch (IOException e) {

			log.error(e.getMessage());
			//throw new SmartsException(SmartsException.DATA_ERROR);
		} catch (SmRemoteException e) {

			log.error(e.getMessage());
			//throw new SmartsException(SmartsException.CONNECTION_ERROR);
		}
		return remoteDomainManager;

	}
	
	//removed to fix bug 312
	
	/*private void checkHost(String name,RemoteDomainManager remoteDomainManager){
		
		String primaryhost = propertiesContainer.getProperty("PRIMARY_IPAM_HOST");
		String secondaryhost = propertiesContainer.getProperty("SECONDARY_IPAM_HOST");
		if (primaryhost==null || secondaryhost==null){
			log.error("PRIMARY_IPAM_HOST and SECONDARY_IPAM_HOST can not be null in properties file");
		}
		if (primaryhost.contains(name)){
			//found in primary
			MapMonitor.getInstance().put("IPAM_PRIMARY","up,"+name+":"+remoteDomainManager.domainName);
		}
		else if (secondaryhost.contains(name)){
			//found in primary
			MapMonitor.getInstance().put("IPAM_PRIMARY","down,"+name+":"+remoteDomainManager.domainName);
			MapMonitor.getInstance().put("IPAM_SECONDARY","up,"+name+":"+remoteDomainManager.domainName);
		}
		else {
			log.debug(" not found host "+name + " in any IPAM configured in properties file");
		}
		
	}*/

}
