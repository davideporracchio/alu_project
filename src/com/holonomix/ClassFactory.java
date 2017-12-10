package com.holonomix;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.holonomix.commoninterface.ActiveAlarmInterface;
import com.holonomix.commoninterface.AlarmMappingFileInterface;
import com.holonomix.commoninterface.ListeningAlarmInterface;
import com.holonomix.commoninterface.StartUpInterface;
import com.holonomix.commoninterface.TopologyAdapterInterface;
import com.holonomix.enums.EnumAdapterName;
import com.holonomix.hsqldb.model.Alarm;
import com.holonomix.list.ActiveList;
import com.holonomix.properties.PropertiesContainer;

/**
 * this Class manages classes to load for each adapter
 * 
 * **/
public class ClassFactory {

	private static PropertiesContainer propertiesContainer = PropertiesContainer
			.getInstance();

	private static Logger log = Logger.getLogger(ClassFactory.class);

	static private AlarmMappingFileInterface alarmMappingFileInterface;

	static private ListeningAlarmInterface listeningAlarmInterface;

	static private ActiveAlarmInterface activeAlarmInterface;

	static private TopologyAdapterInterface topologyAdapterInterface;

	static private StartUpInterface startUpInterface;

	public static AlarmMappingFileInterface getAlarmMappingFileInstance() {

		if (alarmMappingFileInterface == null) {
			String mapAlarmsFileName = propertiesContainer
					.getProperty("RCA_SYMPTOMS_MAPPING_FILE");
			EnumAdapterName enumAdapterName = getEnum();
			switch (enumAdapterName) {
			case HWFFTH: {
				try {
					Class myClass = Class
							.forName("com.holonomix.corba.file.AlarmMappingFileService");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.file.AlarmMappingFileService");
				}
				break;
			}
			case HWMSAN: {
				try {
					Class myClass = Class
							.forName("com.holonomix.corba.file.AlarmMappingFileService");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.file.AlarmMappingFileService");
				}
				break;
			}
			case HWBRAS: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.file.AlarmMappingFileServiceXML");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.file.AlarmMappingFileServiceXML");
				}

				break;
			}
			case ALUFTTH: {
				try {
					Class myClass = Class
							.forName("com.holonomix.alu.file.AlarmMappingFileServiceALU");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.file.AlarmMappingFileServiceALU");
				}
				break;
			}
			case HWMETE: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.file.AlarmMappingFileServiceXML");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.file.AlarmMappingFileServiceXML");
				}

				break;
			}
			case HWZTE: {
				try {
					Class myClass = Class
							.forName("com.holonomix.zte.file.AlarmMappingFileServiceZTE");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.zte.file.AlarmMappingFileServiceZTE");
				}

				break;
			}
			case HWIPSC: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.file.AlarmMappingFileServiceXML");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.file.AlarmMappingFileServiceXML");
				}

				break;
			}
			case ZTEMSAN: {
				try {
					Class myClass = Class
							.forName("com.holonomix.alu.file.AlarmMappingFileService");
					Method m = myClass.getDeclaredMethod("getInstance",
							String.class);
					alarmMappingFileInterface = (AlarmMappingFileInterface) m
							.invoke(myClass, mapAlarmsFileName);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.file.AlarmMappingFileService");
				}
				break;
			}
			default:
				break;
			}

		}

		return alarmMappingFileInterface;

	}

	public static void deleteListeningAlarmInstance(){listeningAlarmInterface=null;}
	
	public static ListeningAlarmInterface getListeningAlarmInstance(
			ActiveList<Alarm> listeningList) {

		if (listeningAlarmInterface == null )

		{

			EnumAdapterName enumAdapterName = getEnum();
			switch (enumAdapterName) {
			case HWFFTH: {
				try {
					Class myClass = Class
							.forName("com.holonomix.corba.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.listening.ListeningAlarmClient");
				}
				break;
			}
			case HWMSAN: {
				try {
					Class myClass = Class
							.forName("com.holonomix.corba.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.listening.ListeningAlarmClient");
				}
				break;
			}
			case ALUFTTH: {
				try {
					Class myClass = Class
							.forName("com.holonomix.alu.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.listening.ListeningAlarmClient");
				}
				break;
			}
			case HWBRAS: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.listening.ListeningAlarmClient");
				}

				break;
			}
			case HWMETE: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					listeningAlarmInterface.setListeningList(listeningList);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.listening.ListeningAlarmClient");
				}

				break;
			}
			case HWZTE: {
				try {
					Class myClass = Class
							.forName("com.holonomix.zte.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					listeningAlarmInterface.setListeningList(listeningList);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.zte.listening.ListeningAlarmClient");
				}
				break;
			}
			case HWIPSC: {
				try {
					Class myClass = Class
							.forName("com.holonomix.xml.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.listening.ListeningAlarmClient");
				}

				break;
			}
			case ZTEMSAN: {
				try {
					Class myClass = Class
							.forName("com.holonomix.alu.listening.ListeningAlarmClient");
					Method m = myClass.getDeclaredMethod("getInstance");
					listeningAlarmInterface = (ListeningAlarmInterface) m
							.invoke(myClass);
					listeningAlarmInterface.setListeningList(listeningList);

				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.listening.ListeningAlarmClient");
				}
				break;
			}
			default:
				break;
			}

		}
		listeningAlarmInterface.setListeningList(listeningList);
		return listeningAlarmInterface;
	}
	
	public static void setListeningAlarmInterface(		ListeningAlarmInterface listeningAlarmInterfaceNew){
		listeningAlarmInterface = listeningAlarmInterfaceNew;
	}


	public static ActiveAlarmInterface getActiveAlarmInstance() {

		// if (activeAlarmInterface == null)

		{

			EnumAdapterName enumAdapterName = getEnum();
			switch (enumAdapterName) {
			case HWFFTH: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.corba.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.active.ActiveAlarmClient");
				}
				break;
			}
			case HWMSAN: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.corba.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.active.ActiveAlarmClient");
				}
				break;
			}
			case ALUFTTH: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.alu.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.active.ActiveAlarmClient");
				}
				break;
			}
			case HWBRAS: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.xml.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.active.ActiveAlarmClient");
				}
				break;
			}
			case HWMETE: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.xml.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.active.ActiveAlarmClient");
				}
				break;
			}

			case HWZTE: {

				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.zte.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.zte.active.ActiveAlarmClient");
				}
				break;

			}
			case HWIPSC: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.xml.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.active.ActiveAlarmClient");
				}
				break;
			}
			case ZTEMSAN: {
				try {
					activeAlarmInterface = (ActiveAlarmInterface) Class
							.forName(
									"com.holonomix.alu.active.ActiveAlarmClient")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.active.ActiveAlarmClient");
				}
				break;
			}
			
			default:
				break;
			}

		}
		return activeAlarmInterface;
	}

	public static TopologyAdapterInterface getTopologyAdapterInstance() {

		if (topologyAdapterInterface == null) {
			EnumAdapterName enumAdapterName = getEnum();
			switch (enumAdapterName) {
			case HWFFTH: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName("com.holonomix.tl1.TL1").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.tl1.TL1");
				}
				break;
			}
			case HWMSAN: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName("com.holonomix.tl1.TL1").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.tl1.TL1");
				}
				break;
			}
			case ALUFTTH: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName("com.holonomix.tl1.TL1").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.tl1.TL1");
				}
				break;
			}
			case HWBRAS: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName(
									"com.holonomix.xmlnbi.inventory.impl.InventoryManager")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xmlnbi.inventory.impl.InventoryManager");
				}
				break;
			}
			case HWMETE: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName(
									"com.holonomix.xmlnbi.inventory.impl.InventoryManager")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xmlnbi.inventory.impl.InventoryManager");
				}
				break;
			}
			case HWZTE: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName(
									"com.holonomix.topology.csv.task.TopologyAdapterCSVImpl")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.topology.csv.task.TopologyAdapterCSVImpl");
				}
				break;
			}
			case HWIPSC: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName(
									"com.holonomix.xmlnbi.inventory.impl.InventoryManager")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xmlnbi.inventory.impl.InventoryManager");
				}
				break;
			}
			case ZTEMSAN: {
				try {
					topologyAdapterInterface = (TopologyAdapterInterface) Class
							.forName("com.holonomix.tl1.TL1").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.tl1.TL1");
				}
				break;
			}
			
			
			default:
				break;
			}

		}
		return topologyAdapterInterface;
	}

	public static EnumAdapterName getEnum() {
		EnumAdapterName enumAdapterName = null;

		if (propertiesContainer.getProperty("ADAPTER_NAME").equalsIgnoreCase(
				"HW-FTTH")) {

			enumAdapterName = EnumAdapterName.HWFFTH;
		} else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("HW-MSAN")) {
			enumAdapterName = EnumAdapterName.HWMSAN;
		} else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("HW-METE")) {
			enumAdapterName = EnumAdapterName.HWMETE;
		}

		else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("HW-BRAS")) {
			enumAdapterName = EnumAdapterName.HWBRAS;
		} else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("ALU-FTTH")) {
			enumAdapterName = EnumAdapterName.ALUFTTH;
		} else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("ZTE-METE")) {
			enumAdapterName = EnumAdapterName.HWZTE;
		}else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("HW-IPSC")) {
			enumAdapterName = EnumAdapterName.HWIPSC;
		}else if (propertiesContainer.getProperty("ADAPTER_NAME")
				.equalsIgnoreCase("ZTE-MSAN")) {
			enumAdapterName = EnumAdapterName.ZTEMSAN;
		}
		return enumAdapterName;

	}

	public static StartUpInterface getStartUpInstance() {

		if (startUpInterface == null) {
			EnumAdapterName enumAdapterName = getEnum();
			switch (enumAdapterName) {
			case HWFFTH: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.corba.startup.StartUp")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.startup.StartUp");
				}
				break;
			}
			case HWMSAN: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.corba.startup.StartUp")
							.newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.corba.startup.StartUp");
				}
				break;
			}
			case ALUFTTH: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.alu.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.alu.startup.StartUp");
				}
				break;
			}
			case HWBRAS: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.xml.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.startup.StartUp");
				}
				break;
			}
			case HWMETE: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.xml.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.startup.StartUp");
				}
				break;
			}
			case HWZTE: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.zte.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.zte.startup.StartUp");
				}
				break;
			}
			case HWIPSC: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.xml.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.xml.startup.StartUp");
				}
				break;
			}
			case ZTEMSAN: {
				try {
					startUpInterface = (StartUpInterface) Class.forName(
							"com.holonomix.alu.startup.StartUp").newInstance();
				} catch (Exception e) {
					log
							.error("error creating new class check you library: com.holonomix.zte.startup.StartUp");
				}
				break;
			}
			
			default:
				break;
			}

		}
		return startUpInterface;
	}

	public static String getProtocol() {
		EnumAdapterName enumAdapterName = getEnum();
		String protocol = "";
		switch (enumAdapterName) {
		case HWFFTH: {

			protocol = "CORBA";
			break;
		}
		case HWMSAN: {
			protocol = "CORBA";
			break;
		}
		case HWBRAS: {
			protocol = "XMLNBI";
			break;
		}
		case HWMETE: {
			protocol = "XMLNBI";
			break;
		}
		case ALUFTTH: {
			protocol = "TL1ALU";
			break;
		}
		case HWZTE: {
			protocol = "XMLNBI";
			break;
		}
		case HWIPSC: {
			protocol = "XMLNBI";
			break;
		}
		case ZTEMSAN: {
			protocol = "ZTEMSAN";
			break;
		}
		default:
			break;
		}

		return protocol;

	}

	public static String getIdNotif() {
		EnumAdapterName enumAdapterName = getEnum();
		String idNotif = "";
		switch (enumAdapterName) {
		case HWFFTH: {

			idNotif = ".1.3.6.1.4.1.733.2014.2";
			break;
		}
		case HWMSAN: {
			idNotif = ".1.3.6.1.4.1.733.2014.3";
			break;
		}
		case ALUFTTH: {
			idNotif = ".1.3.6.1.4.1.733.2014.1";
			break;
		}
		case HWBRAS: {
			idNotif = ".1.3.6.1.4.1.733.2014.5";
			break;
		}
		case HWMETE: {
			idNotif = ".1.3.6.1.4.1.733.2014.6";
			break;
		}
		case HWZTE: {
			idNotif = ".1.3.6.1.4.1.733.2014.7";
			break;
		}
		case HWIPSC: {
			idNotif = ".1.3.6.1.4.1.733.2014.4";
			break;
		}
		case ZTEMSAN: {
			idNotif = ".1.3.6.1.4.1.733.2014.8";
			break;
		}
		default:
			break;
		}

		return idNotif;

	}

	public static String getVersion() {
		EnumAdapterName enumAdapterName = getEnum();
		String version = "";
		switch (enumAdapterName) {
		case HWFFTH: {

			version = "1.3.7"; //TL1 & CORBA
			break;
		}
		case HWMSAN: {
			version = "1.3.6"; //TL1 & CORBA
			break;
		}
		case ALUFTTH: {
			version = "1.3.9"; //TL1_ALU_TOPOLOGY & TL1_ALU_ALARM
			break;
		}
		case HWBRAS: {
			version = "1.3.12"; //XML_TOPOLOGY & XML_ALARM
			break;
		}
		case HWMETE: {
			version = "1.3.22"; //XML_TOPOLOGY & XML_ALARM
			break;
		}
		case HWZTE: {
			version = "1.3.9";//TL!ZTE & Tl1ZTE_ALARM
			break;
		}
		case HWIPSC: {
			version = "1.3.4"; //IPS_TOPOLOGY & IPS_ALARM
			break;
		}
		case ZTEMSAN: {
			version = "1.2.28"; //TL1_ZTE & TL1_ZTEALARM
			break;
		}
		default:
			break;
		}

		return version;

	}

}
