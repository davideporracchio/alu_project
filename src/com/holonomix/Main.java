package com.holonomix;

import org.apache.log4j.Logger;

import com.holonomix.hsqldb.model.utility.EncryptDecrypt;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class);

	public static void main(String args[]) {
		try {
			if (args.length == 2 && args[0].equalsIgnoreCase("-encrypt")) {

				System.out.println("encrypt password " + args[1]);

				EncryptDecrypt.encryptPassword(args[1].trim());

			} else if (args.length == 1) {

				if (args[0].equalsIgnoreCase("-delete")) {
					log.info("delete device in progress...");
					DeleteAdapter deleteAdapter = new DeleteAdapter();
					deleteAdapter.start();
				}

				else if (args[0].equalsIgnoreCase("-s")) {
					// use primary
					log.info("start adapter using secondary EMS...");
					log.info("version:" + ClassFactory.getVersion());
					ProcessAdapter processAdapter = new ProcessAdapter("-s");
					processAdapter.start();
				}

				else if (args[0].equalsIgnoreCase("-p")) {
					log.info("start adapter using primary EMS...");
					log.info("version:" + ClassFactory.getVersion());
					ProcessAdapter processAdapter = new ProcessAdapter("-p");
					processAdapter.start();

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exit from Adapter " + e.getMessage());
		}

	}

}
