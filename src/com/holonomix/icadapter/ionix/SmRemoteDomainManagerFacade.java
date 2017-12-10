package com.holonomix.icadapter.ionix;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.holonomix.exception.SmartsException;
import com.holonomix.exception.TopologyException;
import com.holonomix.properties.PropertiesContainer;
import com.smarts.remote.SmRemoteDomainManager;
import com.smarts.remote.SmRemoteException;
import com.smarts.repos.MR_AnyVal;
import com.smarts.repos.MR_AnyValBoolean;
import com.smarts.repos.MR_AnyValFloat;
import com.smarts.repos.MR_AnyValObjRef;
import com.smarts.repos.MR_AnyValString;
import com.smarts.repos.MR_AnyValUnsignedInt;
import com.smarts.repos.MR_AnyValUnsignedLong;
import com.smarts.repos.MR_Choice;
import com.smarts.repos.MR_Ref;

public class SmRemoteDomainManagerFacade {

	SmRemoteDomainManager smRemoteDomainManager;
	PropertiesContainer propertiesContainer = null;
	public final static Logger log = Logger
			.getLogger(SmRemoteDomainManagerFacade.class);

	public SmRemoteDomainManagerFacade(
			SmRemoteDomainManager smRemoteDomainManager) {
		propertiesContainer = PropertiesContainer.getInstance();
		this.smRemoteDomainManager = smRemoteDomainManager;
	}

	public MR_AnyVal get(final String param1, final String param2,
			final String param3) throws SmartsException {

		try {
			MR_AnyVal anyVal = smRemoteDomainManager
					.get(param1, param2, param3);

			return anyVal;

		} catch (IOException e) {
			String message = "error invoking get using relation name: "
					+ param3 + " on " + param1 + " " + param2 + "  error: "
					+ e.getMessage();
			if (log.isDebugEnabled())
				log.debug(message);
			throw new SmartsException(message);
		} catch (SmRemoteException e) {

			String message = "error invoking get using relation name: "
					+ param3 + " on " + param1 + " " + param2 + "  error: "
					+ e.getMessage();
			if (log.isDebugEnabled())
				log.debug(message);
			throw new SmartsException(message);
		} catch (Exception e) {

			String message = "error invoking get using relation name: "
					+ param3 + " on " + param1 + " " + param2 + "  error: "
					+ e.getMessage();
			if (log.isDebugEnabled())
				log.debug(message);
			throw new SmartsException(message);
		}

	}

	public String[] getInstances(final String param1) throws SmartsException {

		try {

			String[] instanceNames = smRemoteDomainManager.getInstances(param1);
			return instanceNames;

		} catch (SmRemoteException e) {
			String eStr = "Unable to getInstances for: " + param1 + "\n"
					+ e.getMessage();

			log.debug(eStr);

			throw new SmartsException(eStr);
		} catch (IOException e) {
			String eStr = "Unable to getInstances for: " + param1 + "\n"
					+ e.getMessage();

			log.debug(eStr);

			throw new SmartsException(eStr);
		}
	}

	public MR_AnyVal insert(final String param1, final String param2,
			final String param3, final MR_AnyVal ref) throws SmartsException {

		try {
			smRemoteDomainManager.insert(param1, param2, param3, ref);
			return null;

		} catch (IOException e) {
			log.debug("error invoking insert  using relation name: " + param3
					+ " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking insert  using relation name: " + param3
					+ " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal remove(final String param1, final String param2,
			final String param3, final MR_AnyVal ref) throws SmartsException {

		try {
			smRemoteDomainManager.remove(param1, param2, param3, ref);
			return null;

		} catch (IOException e) {
			log.debug("error invoking remove using relation name: " + param3
					+ " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking remove using relation name: " + param3
					+ " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal put(final String param1, final String param2,
			final String param3, final MR_AnyValUnsignedInt objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);
			return null;

		} catch (IOException e) {
			log.debug("error invoking put Integer operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put Integer operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal put(final String param1, final String param2,
			final String param3, final MR_AnyValObjRef objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);
			return null;

		} catch (IOException e) {
			log.debug("error invoking put Ref operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put Ref operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal put(final String param1, final String param2,
			final String param3, final MR_AnyValFloat objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);
			return null;

		} catch (IOException e) {
			log.debug("error invoking put Float operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put Float operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal put(final String param1, final String param2,
			final String param3, final MR_AnyValString objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);
			return null;

		} catch (IOException e) {
			log.debug("error invoking put String operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put String operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal put(final String param1, final String param2,
			final String param3, final MR_AnyValBoolean objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);
			return null;

		} catch (IOException e) {
			log.debug("error invoking put Boolean operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put Boolean operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public void put(final String param1, final String param2,
			final String param3, final MR_AnyValUnsignedLong objRef)
			throws SmartsException {

		try {
			smRemoteDomainManager.put(param1, param2, param3, objRef);

		} catch (IOException e) {
			log.debug("error invoking put Long operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking put Long operation using attribute: "
					+ param3 + " error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_AnyVal invokeOperation(final String param1, final String param2,
			final String param3, final MR_AnyVal[] objRef)
			throws SmartsException {

		try {
			return smRemoteDomainManager.invokeOperation(param1, param2,
					param3, objRef);

		} catch (IOException e) {
			log.debug("error invoking operation " + param3 + "  error: "
					+ e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking operation " + param3 + "  error: "
					+ e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (Exception e) {
			log.warn(" invoking operation " + param3 + "  message: "
					+ e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public boolean noop() throws SmartsException {

		try {
			// log.debug("processed id: "+smRemoteDomainManager.serverInfo().getProcessId());
			smRemoteDomainManager.noop();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("no connection to  " + e.getMessage());
			// log.error("smRemoteDomainManager" + smRemoteDomainManager.);

			return false;
		}
	}

	public boolean instanceExists(final String param1, final String param2)
			throws SmartsException {

		try {
			return smRemoteDomainManager.instanceExists(param1, param2);

		} catch (IOException e) {
			log.debug("error invoking instanceExists " + param2 + " " + param1
					+ "  error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking instanceExists " + param2 + " " + param1
					+ "  error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public MR_Ref[] findInstances(final String param1, final String param2)
			throws SmartsException {

		try {
			return smRemoteDomainManager.findInstances(param1, param2,
					MR_Choice.NONE);

		} catch (IOException e) {
			log.debug("error invoking findInstance " + param2 + " " + param1
					+ "  error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("error invoking findInstance " + param2 + " " + param1
					+ "  error: " + e.getMessage());
			throw new SmartsException(e.getMessage());
		}
	}

	public void createInstance(final String param1, final String param2)
			throws SmartsException {

		try {
			smRemoteDomainManager.createInstance(param1, param2);

		} catch (IOException e) {
			log.debug("Unable to create class::instance for: " + param1 + "::"
					+ param2);
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("Unable to create class::instance for: " + param1 + "::"
					+ param2);
			throw new SmartsException(e.getMessage());
		}
	}

	public void quit() throws SmartsException {

		try {
			smRemoteDomainManager.quit();

			log.debug("quit connection");

		} catch (IOException e) {
			log.debug("Unable to quit IoException "+e.getMessage());
			//smRemoteDomainManager.detach();
			throw new SmartsException(e.getMessage());
		} catch (SmRemoteException e) {

			log.debug("Unable to quit "+e.getMessage());
			//smRemoteDomainManager.detach();
			throw new SmartsException(e.getMessage());
		}

	}
}