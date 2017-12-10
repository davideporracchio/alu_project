package com.holonomix.icadapter.utils;
/**
 * Constant values used throughout the application.
 *
 * <p>
 * <a href="Constants.java.html"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:godfrey.carnegie@holonomix.com">Godfrey Carnegie</a>
 */
public class Constants {
    // =========================================================================
    //~ Static fields/initialisers 
    // =========================================================================

    public final static String APPLICATION_NAME = "EMC SMARTS Maintenance Console";

    // =========================================================================
    //~ Property Keys  - as used in the properties file
    // =========================================================================

    public final static String PROPERTIES_FILE                              =
        "spv.properties";
    public final static String PK_SMARTS_BROKERHOST                         =
        "SMARTS_BROKERHOST";
    public final static String PK_SMARTS_DOMAINMGR                          =
        "SMARTS_DOMAINMGR";
    public final static String PK_SMARTS_DEFAULTDOMAINMGR                   =
        "SMARTS_DEFAULTDOMAINMGR";
    public final static String PK_SMARTS_DOMAINUSERNAME                     =
        "SMARTS_DOMAINUSERNAME";
    public final static String PK_SMARTS_DOMAINUSERPASSWORD                 =
        "SMARTS_DOMAINUSERPASSWORD";
    public final static String PK_TOPOLOGY_CLASSLIST                        =
        "TOPOLOGY_CLASSLIST";
    public final static String PK_REDISCOVER_EVENTSLIST                     =
        "REDISCOVER_EVENTSLIST";
    public final static String PK_NOTIFICATIONFIELD_USERDEFINED_DEVICENAME  =
        "NOTIFICATIONFIELD_USERDEFINED_DEVICENAME";
    public final static String PK_AMSERVERCOLLECTION_CLASSNAME              =
        "AMSERVERCOLLECTION_CLASSNAME";
    public final static String PK_DEFAULT_DISCOVERY_AMSERVER                =
        "DEFAULT_DISCOVERY_AMSERVER";
    public final static String PK_MODELEXPORT_POLLINGINTERVAL               =
        "MODELEXPORT_POLLINGINTERVAL";
    public final static String PK_MODELEXPORT_STARTTIME_MAXINTERVAL_SECS    =
        "MODELEXPORT_STARTTIME_MAXINTERVAL_SECS";
    // How old a maintperiod record can be and still qualify as "Live" - in
    // order to get conclude now records exported
    public final static String LIVEMAINTENANCEPERIOD_ENDTIME_GRACEPERIOD_SECS  =
        "1200";
    public final static String PK_TOPOLOGYIMPORT_SO_BROKERDOMAINLIST        =
        "TOPOLOGYIMPORT_SO_BROKERDOMAINLIST";
    public final static String PK_SAFBROKERDOMAINLIST                       =
        "SAFBROKERDOMAINLIST";
    public final static String PK_BUSINESSOBJECTS_CSVINPUTFILE              = 
        "BUSINESSOBJECTS_CSVINPUTFILE";
    public final static String PK_BUSINESSOBJECTS_CSVFIELDSEPARATOR         = 
        "BUSINESSOBJECTS_CSVFIELDSEPARATOR";
    public final static String PK_BUSINESSOBJECTS_CSVQUOTECHAR              = 
        "BUSINESSOBJECTS_CSVQUOTECHAR";
    public final static String PK_BUSINESSOBJECTS_CSVDATEFORMAT             = 
        "BUSINESSOBJECTS_CSVDATEFORMAT";
    public final static String PK_BUSINESSOBJECTS_MPUPDATEID                = 
        "BUSINESSOBJECTS_MPUPDATEID";
    public final static String PK_BUSINESSOBJECTS_MPREASONID                = 
        "BUSINESSOBJECTS_MPREASONID";
    public final static String PK_BUSINESSOBJECTS_MPSTATUSID                = 
        "BUSINESSOBJECTS_MPSTATUSID";
    public final static String PK_BUSINESSOBJECTS_CSVINDEX_DEVICE           = 
        "BUSINESSOBJECTS_CSVINDEX_DEVICE";
    public final static String PK_BUSINESSOBJECTS_CSVINDEX_STARTTIME        = 
        "BUSINESSOBJECTS_CSVINDEX_STARTTIME";
    public final static String PK_BUSINESSOBJECTS_CSVINDEX_ENDTIME          = 
        "BUSINESSOBJECTS_CSVINDEX_ENDTIME";
    public final static String PK_NCRG_DEVICENAMEPREPEND                    = 
        "NCRG_DEVICENAMEPREPEND";
    public final static String PK_SAFDEVICESCLASSLIST                       = 
        "SAFDEVICESCLASSLIST";
    public final static String PK_SOFDEVICESCLASSLIST                       = 
        "SOFDEVICESCLASSLIST";
    public final static String PK_DOIMPORTTOPOLOGY                          = 
        "DOIMPORTTOPOLOGY";
    public final static String PK_DOUPDATENETWORKCONNECTIONS                = 
        "DOUPDATENETWORKCONNECTIONS";
    public final static String PK_DOUPDATENETWORKCONNECTIONS2               = 
        "DOUPDATENETWORKCONNECTIONS2";
    public final static String PK_DOUPDATECOMPOSEDOF                        = 
        "DOUPDATECOMPOSEDOF";
    public final static String PK_DOUPDATECONSISTSOF                        = 
        "DOUPDATECONSISTSOF";
    public final static String PK_DOUPDATENCRGROUPS                         = 
        "DOUPDATENCRGROUPS";
    public final static String PK_DOUPDATESAFS                              = 
        "DOUPDATESAFS";
    public final static String PK_DOUPDATESAFSPLITSERVICEIMPACTED       = 
        "DOUPDATESAFSPLITSERVICEIMPACTED";
    public final static String PK_DOUPDATESAFSPLITRESILIENCEIMPACTED       = 
        "DOUPDATESAFSPLITRESILIENCEIMPACTED";
    public final static String PK_DOSETUPDEVICESERVICEOFFERINGS             = 
        "DOSETUPDEVICESERVICEOFFERINGS";
    public final static String PK_SOFMEMBERCLASSLIST                        = 
        "SOFMEMBERCLASSLIST";
    public final static String PK_SAFTOPLEVELBROKERDOMAIN                   = 
        "SAFTOPLEVELBROKERDOMAIN";
    public final static String PK_SOFTOPLEVELBROKERDOMAIN                   = 
        "SOFTOPLEVELBROKERDOMAIN";
    public final static String PK_CONSISTSOFBROKERDOMAIN                    = 
        "CONSISTSOFBROKERDOMAIN";
    public final static String PK_CONSISTSOFCLASSLIST                       = 
        "CONSISTSOFCLASSLIST";
    public final static String PK_COMPOSEDOFBROKERDOMAIN                    = 
        "COMPOSEDOFBROKERDOMAIN";
    public final static String PK_COMPOSEDOFCLASSLIST                       = 
        "COMPOSEDOFCLASSLIST";
    public final static String PK_PROFILETIMES                              = 
        "PROFILETIMES";
    public final static String PK_CATCHALL_CLASSINSTANCE                    = 
        "CATCHALL_CLASSINSTANCE";
    public final static String PK_DOUPDATETOPOLOGYWITHAMDEVICEINFO          = 
        "DOUPDATETOPOLOGYWITHAMDEVICEINFO";
    public final static String PK_NOTIFICATIONIMPORT_BROKERDOMAINLIST       = 
        "NOTIFICATIONIMPORT_BROKERDOMAINLIST";
    public final static String PK_NOTIFICATIONIMPORT_AMDOMAINLIST           = 
        "NOTIFICATIONIMPORT_AMDOMAINLIST";
    public final static String PK_NOTIFICATIONIMPORT_AMCLASSLIST            = 
        "NOTIFICATIONIMPORT_AMCLASSLIST";
    public final static String PK_NETWORKCONNNECTION_DEVICEREGEX            = 
        "NETWORKCONNNECTION_DEVICEREGEX";
    public final static String PK_CACHENETWORKCONNECIONENDPOINTS            = 
        "CACHENETWORKCONNECIONENDPOINTS";

    public final static String PK_DASHBOARDSEARCHFILTERFIELDS               =
        "DASHBOARDSEARCHFILTERFIELDS";

    public static final String PK_IMPORTNOTIFICATIONEVENTAUDITLOGS          =
        "NOTIFICATIONIMPORT_IMPORTAUDITLOGS";

    public final static String AUDITLOGTYPE_INCHARGEINSTANCE                = 
        "inchargeinstance";
    public final static String AUDITLOGTYPE_MAINTENACEPERIOD                = 
        "maintenanceperiod";
    public final static String AUDITLOGTYPE_COMMISSIONINGREQUEST            = 
        "commissioningrequest";
    public final static String AUDITLOGTYPE_DECOMMISSIONINGREQUEST          = 
        "decommissioningrequest";

    public final static String STATIC_APPCONTEXT                            =
        "spvdashboard";

    public final static String WEBAPP_VIEWDOMAINMANAGER_URL                 =
        "/" + STATIC_APPCONTEXT + "/" + "searchMaintenancePeriodsAtDL.html";
    public final static String WEBAPP_VIEWINCHARGECLASS_URL                 =
        "/" + STATIC_APPCONTEXT + "/" + "searchMaintenancePeriodsAtCL.html";
    public final static String WEBAPP_VIEWINCHARGEINSTANCE_URL              =
        "/" + STATIC_APPCONTEXT + "/" + "viewInchargeInstance.html";

    public final static String WEBAPP_NAVIGATIONSEARCHTABDOMAIN_URL         =
        "/" + STATIC_APPCONTEXT + "/" + "navigationSearchTabAtDL.html";
    public final static String WEBAPP_NAVIGATIONSEARCHTABCLASS_URL          =
        "/" + STATIC_APPCONTEXT + "/" + "navigationSearchTabAtCL.html";
    public final static String WEBAPP_VIEWDASHBOARD_URL                     =
        "viewServiceDashboard.html";

    public final static String TOPOLOGY_MAXCLASSNODES                       = "500";
    public final static String TOPOLOGY_POLLINGINTERVAL                     = "21600"; // 6 hours
    public final static String MODELEXPORT_POLLINGINTERVAL                  = "300"; // 5 mins
    public final static String MODELSTATEUPDATE_POLLINGINTERVAL             = "120"; // 2 mins
    public final static String MPMPROV_POLLINGINTERVAL                      = "300"; // 5 mins
    // sec min hour day-of-month month day-of-week year?
    // ? is used by day-of* and means no specific value, used when you want
    // to specify ONLY one of the two day-of fields
    // '/' is the increment e.g. 0/15 in the secs fields
    public final static String TOPOLOGYIMPORT_CRONEXPRESSION                = "0 0 6,18 * * ?";
    public final static String MODELSTATEUPDATE_CRONEXPRESSION              = "0 0/20 * * * ?";
    public final static String PROVISIONINGEXPORT_CRONEXPRESSION            = "0 10/20 * * * ?";
    public final static String PROVISIONINGEXPORT_DIR                       = "/tmp/s3cexport";
    public final static String PROVISIONINGEXPORT_FILENAME                  = "maintenanceperiods.csv";

    public static final String SEARCH_FILTER_INCHARGEINSTANCE_MAINTENANCE = "Devices that can be placed into Maintenance";
    public static final String SEARCH_FILTER_INCHARGEINSTANCE_COMMISSIONING = "Devices that can be Commissioned";
    public static final String SEARCH_FILTER_MAINTENANCEPERIOD_DETAILS = "Maintenance Period entries whose details can be changed";
    public static final String SEARCH_FILTER_MAINTENANCEPERIOD_APPROVE = "Maintenance Period entries that can be Approved";
    public static final String SEARCH_FILTER_MAINTENANCEPERIOD_REJECT = "Maintenance Period entries that can be Rejected";
    public static final String SEARCH_FILTER_MAINTENANCEPERIOD_CONCLUDE = "Maintenance Period entries that can be Concluded";
    public static final String SEARCH_FILTER_COMMISSIONINGREQUEST_DETAILS = "Commissioning Request entries whose details can be changed";
    public static final String SEARCH_FILTER_COMMISSIONINGREQUEST_APPROVE = "Commissioning Request entries that can be Approved";
    public static final String SEARCH_FILTER_COMMISSIONINGREQUEST_REJECT = "Commissioning Request entries that can be Rejected";
    public static final String SEARCH_FILTER_COMMISSIONINGREQUEST_CONCLUDE = "Commissioning Request entries that can be Concluded";

    // =========================================================================
    //~ The list of AUTODISCOVERY_TYPE
    // =========================================================================
    public final static String AUTODISCOVERYTYPE_PINGSWEEP = "icmpSweep";
    public final static String AUTODISCOVERYTYPE_SNMPSWEEP = "snmpSweep";
    public final static String AUTODISCOVERYTYPE_MULITHOPDISCOVER = "standard";
    public final static String SEEDAM_SNMPVERSION = "V1";

    // =========================================================================
    //~ The list of class types that we shall be instantiating
    // =========================================================================
    public final static String CLASSNAME_HOST                   = "Host";
    public final static String CLASSNAME_ROUTER                 = "Router";
    public final static String CLASSNAME_SWITCH                 = "Switch";
    public final static String CLASSNAME_APPLICATION            = "Application";
    public final static String CLASSNAME_NETWORKCONNECTION     = "NetworkConnection";
    public final static String CLASSNAME_NETWORKCONNECTIONREDUNDANCYGROUP
        = "NetworkConnectionRedundancyGroup";
    public final static String CLASSNAME_PARTITION              = "Partition";
    public final static String CLASSNAME_SAF                    = "SAF";
    public final static String CLASSNAME_SAFSPLITSERVICEIMPACTED
        = "SAFSplitServiceImpacted";
    public final static String CLASSNAME_SAFSPLITRESILIENCEIMPACTED
        = "SAFSplitResilienceImpacted";
    public final static String CLASSNAME_SERVICEOFFERING        = "ServiceOffering";
    public final static String CLASSNAME_SYSTEMREDUNDANCYGROUP  = "SystemRedundancyGroup";

    // =========================================================================
    //~ The list of interesting property types
    // =========================================================================
    public final static String ICSPROPERTY_SITEID               = "SiteID";
    public final static String ICSPROPERTY_OTHERLEGSITEID       = "OtherLegSiteID";
    public final static String ICSPROPERTY_MEMBEROF             = "MemberOf";

    // =========================================================================
    //~ The list of inter-class relation types
    // =========================================================================
    public final static String TOPOLOGYRELATION_COMPOSEDOF      = "ComposedOf";
    public final static String TOPOLOGYRELATION_CONNECTEDSYSTEMS
        = "ConnectedSystems";
    public final static String TOPOLOGYRELATION_CONSISTSOF      = "ConsistsOf";
    public final static String TOPOLOGYRELATION_MEMBEROF        = "MemberOf";

    // =========================================================================
    //~ Smarts Operation Name parameters per ICIM class
    // =========================================================================
    public final static String OPERATIONNAME_CHANGED            = "changed";
    public final static String OPERATIONNAME_CLEAR              = "clear";
    public final static String OPERATIONNAME_CONTAINS           = "contains";
    public final static String OPERATIONNAME_CREATE             = "create";
    public final static String OPERATIONNAME_FINDCOMPUTERSYSTEM = "findComputerSystem";
    public final static String OPERATIONNAME_MAKEAGGREGATE      = "makeAggregate";
    public final static String OPERATIONNAME_MAKENOTIFICATION   = "makeNotification";
    public final static String OPERATIONNAME_MAKEOBJECT         = "makeObject";
    public final static String OPERATIONNAME_NOTIFY             = "notify";
    public final static String OPERATIONNAME_INSERT             = "insert";
    public final static String OPERATIONNAME_FIND               = "find";
    public final static String OPERATIONNAME_GETENTRY           = "getEntry";
    public final static String OPERATIONNAME_REMOVE             = "remove";
    public final static String OPERATIONNAME_ADDPENDING         = "addPending";
    public final static String OPERATIONNAME_CHECKDISCOVERYSTATE = "checkDiscoveryState";
    public final static String OPERATIONNAME_ADDAUDITENTRY = "addAuditEntry";
    public final static String OPERATIONNAME_ACKNOWLEDGE = "acknowledge";
    public final static String OPERATIONNAME_UNACKNOWLEDGE = "unacknowledge";

    // =========================================================================
    //~ Smarts Operation Invoke operation makeNotification parameters
    // =========================================================================
    
    //~ ICS Notification Factory (ICN) class paramaters
    public final static String ICN_CLASSNAME               = "ICIM_Notification";
    
    //~ ICS Notification Factory (ICSNF) class paramaters
    public final static String ICSNF_CLASSNAME             = "ICS_NotificationFactory";
    public final static String ICSNF_INSTANCENAME          = "ICS-NotificationFactory";
    public final static String ICSNF_NOTIFICATIONCLASSNAME = "ICS_Notification";
    public final static String ICSNF_INSTANCENAME_PREPEND  = "NOTIFICATION-";

    //~ ICS Notification List (ICSNL) class paramaters
    public final static String ICSNL_CLASSNAME             = "ICS_NotificationList";
    public final static String ICSNL_ALL_INSTANCENAME      = "ICS_NL-ALL_NOTIFICATIONS";
    public final static String ICSNL_ALL_ALLNOTIFICATIONSPROPERTY = "AllNotifications";
    public final static String ICSNL_ALL_ALLNOTIFICATIONSDATAPROPERTY = "AllNotificationsData";

    //~ ICIM_ObjectFactory Factory (ICOF) class paramaters
    public final static String ICOF_CLASSNAME             = "ICIM_ObjectFactory";
    public final static String ICOF_INSTANCENAME          = "ICIM-ObjectFactory";

    //~ ICF_TopologyManager Factory (ICTM) class paramaters
    public final static String ICTM_CLASSNAME             = "ICF_TopologyManager";
    public final static String ICTM_INSTANCENAME          = "ICF-TopologyManager";

    //~ ICF_TopologyManager Factory (ICTM) class paramaters
    public final static String ICPM_CLASSNAME             = "ICF_PolicyManager";
    public final static String ICPM_INSTANCENAME          = "ICF-PolicyManager";
    
    //~ Maintenance Period class paramaters
    public final static String MAINTENANCEPERIOD_CLASSNAME    = "GA_StringDictionary";
    public final static String MAINTENANCEPERIOD_INSTANCENAME = "S3C_MaintenancePeriod";


    // =========================================================================
    //~ Smarts general Operation parameters
    // =========================================================================
    public final static String OP_PARAM_GENERAL_ATTRIBUTES       = "Attributes";
    public final static String OP_PARAM_GENERAL_CLASSNAME        = "ClassName";
    public final static String OP_PARAM_GENERAL_CLEARTIME        = "ClearTime";
    public final static String OP_PARAM_GENERAL_COUNT            = "Count";
    public final static String OP_PARAM_GENERAL_EVENTNAME        = "EventName";
    public final static String OP_PARAM_GENERAL_EXPIRATION       = "Expiration";
    public final static String OP_PARAM_GENERAL_INSTANCENAME     = "InstanceName";
    public final static String OP_PARAM_GENERAL_NLNAME           = "NlName";
    public final static String OP_PARAM_GENERAL_NOTIFICATIONTIME = "NotificationTime";
    public final static String OP_PARAM_GENERAL_SOURCE           = "Source";
    public final static String OP_PARAM_GENERAL_UNKNOWNAGENT     = "UnknownAgent";

    // =========================================================================
    //~ Smarts general Operation parameter values
    // =========================================================================
    public final static String OP_VALUE_GENERAL_USER = "admin";

    // =========================================================================
    //~ Smarts notification import related stuff
    // =========================================================================    
    public final static String PK_ARCHIVED_NOTIFICATIONS_RETRIEVEALMODE     =
        "ARCHIVED_NOTIFICATIONS_RETRIEVEALMODE";
    public final static String ARCHIVED_NOTIFICATIONS_RETRIEVEALMODE_HEARTBEAT =
        "HEARTBEAT";
    public final static String PK_ARCHIVED_NOTIFICATION_RETRIEVAL_DATE      =
        "ARCHIVED_NOTIFICATION_RETRIEVAL_DATE";
    public final static String PK_ARCHIVED_NOTIFICATION_RETRIEVAL_DATEFORMAT =
    	"ARCHIVED_NOTIFICATION_RETRIEVAL_DATEFORMAT";
    public final static String PK_REALTIMEEVENTSSUBSCRIPTION_SMARTSNOTIFICATIONLIST = 
        "REALTIMEEVENTSSUBSCRIPTION_SMARTSNOTIFICATIONLIST";
    public final static String PK_DASHBOARDCLIENTPOLLINTERVALSECS = "DASHBOARDCLIENTPOLLINTERVALSECS";

    
    // =========================================================================
    //~ Smarts notification attributes - ones that we may want to influence
    // =========================================================================
    public final static String NOTIFICATION_ATTRIB_ACTIVE             = "Active";
    public final static String NOTIFICATION_ATTRIB_ACKNOWLEDGED       = "Acknowledged";
    public final static String NOTIFICATION_ATTRIB_ACKNOWLEDGMENTTIME = "AcknowledgmentTime";
    public final static String NOTIFICATION_ATTRIB_AUDITTRAIL         = "AuditTrail";
    public final static String NOTIFICATION_ATTRIB_AUTOACKNOWLEDGMENTINTERVAL         = "AutoAcknowledgmentInterval";
    public final static String NOTIFICATION_ATTRIB_CATEGORY           = "Category";
    public final static String NOTIFICATION_ATTRIB_CERTAINTY          = "Certainty";
    public final static String NOTIFICATION_ATTRIB_CLASSNAME          = "ClassName";
    public final static String NOTIFICATION_ATTRIB_CLASSDISPLAYNAME   = "ClassDisplayName";
    public final static String NOTIFICATION_ATTRIB_CLEARONACKNOWLEDGE = "CleanOnAcknowledged";
    public final static String NOTIFICATION_ATTRIB_CREATIONCLASSNAME  = "CreationClassName";
    public final static String NOTIFICATION_ATTRIB_CUSTOMERS          = "Customers";
    public final static String NOTIFICATION_ATTRIB_DISPLAYNAME        = "DisplayName";
    public final static String NOTIFICATION_ATTRIB_DESCRIPTION        = "Description";
    public final static String NOTIFICATION_ATTRIB_ELEMENTCLASSNAME   = "ElementClassName";
    public final static String NOTIFICATION_ATTRIB_ELEMENTNAME        = "ElementName";
    public final static String NOTIFICATION_ATTRIB_EVENTDISPLAYNAME   = "EventDisplayName";
    public final static String NOTIFICATION_ATTRIB_EVENTNAME          = "EventName";
    public final static String NOTIFICATION_ATTRIB_EVENTTEXT          = "EventText";
    public final static String NOTIFICATION_ATTRIB_EVENTYPE           = "EventType";
    public final static String NOTIFICATION_ATTRIB_EVENSTATE          = "EventState";
    public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOCLEAR          = "FirstScheduledAutoClear";
    public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOARCHIVE          = "FirstScheduledAutoArchive";
    public final static String NOTIFICATION_ATTRIB_FIRSTSCHEDULEDAUTOACKNOWLEDGEMENT          = "FirstScheduledAutoAcknowledge";
    public final static String NOTIFICATION_ATTRIB_FIRSTNOTIFIEDAT    = "FirstNotifiedAt";
    public final static String NOTIFICATION_ATTRIB_IMPACT             = "Impact";
    public final static String NOTIFICATION_ATTRIB_INMAINTENANCE      = "InMaintenance";
    public final static String NOTIFICATION_ATTRIB_INSTANCENAME       = "InstanceName";
    public final static String NOTIFICATION_ATTRIB_INSTANCEDISPLAYNAME = "InstanceDisplayName";
    public final static String NOTIFICATION_ATTRIB_INTERNALELEMENTCLASSNAME = "internalElementClassName";
    public final static String NOTIFICATION_ATTRIB_INTERNALELEMENTNAME = "internalElementName";
    public final static String NOTIFICATION_ATTRIB_INACTIVEAUTOARCHIVEINTERVAL = "InactiveAutoArchiveInterval";
    public final static String NOTIFICATION_ATTRIB_ISAGGREGATE         = "IsAggregate";
    public final static String NOTIFICATION_ATTRIB_ISAGGREGATEDBY         = "IsAggregatedBy";
    public final static String NOTIFICATION_ATTRIB_ISROOT             = "IsRoot";
    public final static String NOTIFICATION_ATTRIB_LASTCHANGEDAT      = "LastChangedAt";
    public final static String NOTIFICATION_ATTRIB_LASTCLEAREDAT      = "LastClearedAt";
    public final static String NOTIFICATION_ATTRIB_LASTNOTIFIEDAT     = "LastNotifiedAt";
    public final static String NOTIFICATION_ATTRIB_NEXTSERIALNUMBER   = "NextSerialNumber";
    public final static String NOTIFICATION_ATTRIB_NOTIFICATIONTIME   = "NotificationTime";
    public final static String NOTIFICATION_ATTRIB_NAME               = "Name";
    public final static String NOTIFICATION_ATTRIB_OWNER              = "Owner";
    public final static String NOTIFICATION_ATTRIB_OCCURRENCECOUNT    = "OccurrenceCount";
    public final static String NOTIFICATION_ATTRIB_SEVERITY           = "Severity";
    public final static String NOTIFICATION_ATTRIB_SERVICENAME           = "ServiceName";
    public final static String NOTIFICATION_ATTRIB_SHOULDAUTOCLEAR    = "ShouldAutoClear";
    public final static String NOTIFICATION_ATTRIB_SHOULDAUTOCLEARAT  = "ShouldAutoClearAt";
    public final static String NOTIFICATION_ATTRIB_SHOULDAUTOACKNOWLEDGE  = "ShouldAutoAcknowledge";
    public final static String NOTIFICATION_ATTRIB_SHOULDAUTOACKNOWLEDGEAT  = "ShouldAutoAcknowledgeAt";
    public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEINACTIVEAUTOARCHIVE  = "ShouldScheduleInactiveAutoArchive";
    public final static String NOTIFICATION_ATTRIB_SHOULDINACTIVEAUTOARCHIVE  = "ShouldInactiveAutoArchive";
    public final static String NOTIFICATION_ATTRIB_SHOULDSHEDULEAUTOARCHIVE  = "ShouldScheduleAutoArchive";
    public final static String NOTIFICATION_ATTRIB_SCHEDULEDFORNOTIFY    = "scheduledForNotify";
    public final static String NOTIFICATION_ATTRIB_SHOULDINACTIVEAUTOARCHIVEAT    = "ShouldInactiveAutoArchiveAt";
    public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEAUTOACKNOWLEDGE           = "ShouldScheduleAutoAcknowledge";
    public final static String NOTIFICATION_ATTRIB_SHOULDSCHEDULEAUTOCLEAR           = "ShouldScheduleAutoClear";
    public final static String NOTIFICATION_ATTRIB_SOURCEDOMAINNAME   = "SourceDomainName";
    public final static String NOTIFICATION_ATTRIB_TROUBLETICKETID   = "TroubleTicketID";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED1       = "UserDefined1";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED2       = "UserDefined2";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED3       = "UserDefined3";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED4       = "UserDefined4";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED5       = "UserDefined5";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED6       = "UserDefined6";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED7       = "UserDefined7";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED8       = "UserDefined8";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED9       = "UserDefined9";
    public final static String NOTIFICATION_ATTRIB_USERDEFINED10      = "UserDefined10";
    public final static String S3CNOTIFICATION_ATTRIB_CUSTOMERS       = "Customers";

    // =========================================================================
    //~ AudtLog attributes
    // =========================================================================
    public final static String NOTIFICATIONAUDITLOG_ATTRIB_ID           = "Id";
    public final static String NOTIFICATIONAUDITLOG_ATTRIB_TIME         = "Time";
    public final static String NOTIFICATIONAUDITLOG_ATTRIB_USERID       = "UserId";
    public final static String NOTIFICATIONAUDITLOG_ATTRIB_TYPE         = "Type";
    public final static String NOTIFICATIONAUDITLOG_ATTRIB_DESCRIPTION  = "Description";

    // =========================================================================
    //~ inchargeNotification severity labels
    // =========================================================================
    public final static String EVENTSEVERITYLABEL_CRITICAL="Critical";
    public final static String EVENTSEVERITYLABEL_MAJOR="Major";
    public final static String EVENTSEVERITYLABEL_MINOR="Minor";
    public final static String EVENTSEVERITYLABEL_UNKNOWN="Unknown";
    public final static String EVENTSEVERITYLABEL_NORMAL="Normal";
    public final static String EVENTSEVERITYLABEL_UNSPECIFIED="Unspecified";

    public final static String EVENTSEVERITYVALUE_CRITICAL="1";
    public final static String EVENTSEVERITYVALUE_MAJOR="2";
    public final static String EVENTSEVERITYVALUE_MINOR="3";
    public final static String EVENTSEVERITYVALUE_UNKNOWN="4";
    public final static String EVENTSEVERITYVALUE_NORMAL="5";
    public final static String EVENTSEVERITYVALUE_UNSPECIFIED="6";

    public final static String EVENTSEVERITYICON_CRITICAL="Critical.png";
    public final static String EVENTSEVERITYICON_MAJOR="Major.png";
    public final static String EVENTSEVERITYICON_MINOR="Minor.png";
    public final static String EVENTSEVERITYICON_UNKNOWN="Unknown.png";
    public final static String EVENTSEVERITYICON_NORMAL="Normal.png";
    public final static String EVENTSEVERITYICON_UNSPECIFIED="Unspecified.png";

    // =========================================================================
    //~ Interested UnitaryComputerSystem attributes
    // =========================================================================
    public final static String UCS_ATTRIB_CERTIFICATION   = "Certification";
    public final static String UCS_ATTRIB_SYSTEMNAME      = "SystemName";

    // =========================================================================
    //~ NOTIFY Operation parameters
    // =========================================================================
    public final static String OP_PARAM_NOTIFY_AUDITTRAILTEXT   = "AuditTrail";
    public final static String OP_PARAM_NOTIFY_COUNT            = "Count";
    public final static String OP_PARAM_NOTIFY_EXPIRATION_TIME  = "Expiration Time";
    public final static String OP_PARAM_NOTIFY_NOTIFICATIONTIME = "Notification Time";
    public final static String OP_PARAM_NOTIFY_NLNAME           = "NlName";
    public final static String OP_PARAM_NOTIFY_SOURCE           = "Source";
    public final static String OP_PARAM_NOTIFY_USER             = "User";

    // =========================================================================
    //~ InchargeInstance Status ident values;
    // =========================================================================
    public final static String INCHARGEINSTANCESTATUS_NEW                    = "1";
    public final static String INCHARGEINSTANCESTATUS_PENDINGCOMMISSIONING   = "2";
    public final static String INCHARGEINSTANCESTATUS_INSERVICE              = "3";
    public final static String INCHARGEINSTANCESTATUS_INMAINTENANCE          = "4";
    public final static String INCHARGEINSTANCESTATUS_DEVICENOTFOUND         = "5";
    public final static String INCHARGEINSTANCESTATUS_PENDINGDECOMMISSIONING = "6";
    public final static String INCHARGEINSTANCESTATUS_DECOMMISSIONED         = "7";

    // =========================================================================
    //~ MaintenancePeriod Status ident values;
    // =========================================================================
    public final static String MAINTENANCEPERIODSTATUS_NEW              = "1";
    public final static String MAINTENANCEPERIODSTATUS_PENDINGAPPROVAL  = "2";
    public final static String MAINTENANCEPERIODSTATUS_APPROVED         = "3";
    public final static String MAINTENANCEPERIODSTATUS_CANCELLED        = "4";
    public final static String MAINTENANCEPERIODSTATUS_COMPLETED        = "5";

    // =========================================================================
    //~ CommissioningRequestStatus Status ident values;
    // =========================================================================
    public final static String COMMISSIONINGREQUESTSTATUS_NEW             = "1";
    public final static String COMMISSIONINGREQUESTSTATUS_PENDINGAPPROVAL = "2";
    public final static String COMMISSIONINGREQUESTSTATUS_APPROVED        = "3";
    public final static String COMMISSIONINGREQUESTSTATUS_PHASE1          = "4";
    public final static String COMMISSIONINGREQUESTSTATUS_PHASE2          = "5";
    public final static String COMMISSIONINGREQUESTSTATUS_CANCELLED       = "6";
    public final static String COMMISSIONINGREQUESTSTATUS_COMPLETED       = "7";

    // =========================================================================
    //~ The AMDEVICE attributes
    // =========================================================================
    public final static String AMDEVICE_ATTRIB_ACCESSMODE                   = "AccessMode";
    public final static String AMDEVICE_ATTRIB_MODEL                        = "Model";
    public final static String AMDEVICE_ATTRIB_SNMPADDRESS                  = "SNMPAddress";
    public final static String AMDEVICE_ATTRIB_READCOMMUNITYSTRING          = "ReadCommunity";
    public final static String AMDEVICE_ATTRIB_SYSTEMNAME                   = "SystemName";
    public final static String AMDEVICE_ATTRIB_DEVICETYPE                   = "Type";
    public final static String AMDEVICE_ATTRIB_DEVICEVENDOR                 = "Vendor";
    public final static String AMDEVICE_ATTRIB_DEVICEDESCRIPTION            = "Description";

    public final static String INCHARGEINSTANCE_AMDEVICEPREPEND             = "amDevice";

    // =========================================================================
    //~ The config file default location
    // =========================================================================

    public final static String CONFIGFILE_DEFAULTNAME       = "spvdashboard.properties";

    // =========================================================================
    //~ The special case MR_DATE value type
    // =========================================================================

    public final static Integer MR_ValType_MR_DATE = 64;

    // =========================================================================
    //~ The properties that are required for the displaytag columns
    // =========================================================================
    public final static String TABLEPROP_PROPERTY   = "property";
    public final static String TABLEPROP_TITLE      = "title";
    public final static String TABLEPROP_SORTABLE   = "sortable";

    public final static String SERVICESUBSCRIPTION_ENDTTIME_INTERVAL = "31536000";
    
    /** The name of the ResourceBundle used in this application */
    public static final String BUNDLE_KEY = "ApplicationResources";

    /** The encryption algorithm key to be used for passwords */
    public static final String ENC_ALGORITHM = "algorithm";

    /** A flag to indicate if passwords should be encrypted */
    public static final String ENCRYPT_PASSWORD = "encryptPassword";

    /** File separator from System properties */
    public static final String FILE_SEP = System.getProperty("file.separator");

    /** User home from System properties */
    public static final String USER_HOME = System.getProperty("user.home") + FILE_SEP;

    /** The name of the configuration hashmap stored in application scope. */
    public static final String CONFIG = "appConfig";

    /** 
     * Session scope attribute that holds the locale set by the user. By setting this key
     * to the same one that Struts uses, we get synchronization in Struts w/o having
     * to do extra work or have two session-level variables.
     */ 
    public static final String PREFERRED_LOCALE_KEY = "org.apache.struts.action.LOCALE";
    
    /**
     * The request scope attribute under which an editable user form is stored
     */
    public static final String USER_KEY = "userForm";

    /**
     * The request scope attribute that holds the user list
     */
    public static final String USER_LIST = "userList";

    /**
     * The request scope attribute for indicating a newly-registered user
     */
    public static final String REGISTERED = "registered";

    /** The yesNo selection list */
    public static final String YESNO_LIST = "Yes No";

    /** The maximum query results for a selection list */
    public static final String MAX_SELECT_RESULTS = "1000";

    /** The username that this app represents */
    public static final String SYSTEM_USERNAME = "S3C";

    /**
     * The name of the Administrator role, as specified in the db
     */
    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    
    /**
     * Admin username as specified in db
     */
    public static final String ADMIN_USERNAME = "admin";

    /**
     * Admin organisationame as specified in db
     */
    public static final String ADMIN_ORGANISATIONNAME = "Holonomix";


    /**
     * Unauthenticated WS API username as specified
     */
    public static final String UNAUTHENTICATED_WSAPIUSERNAME = "WS API";

    /**
     * The name of the Operator role, as specified in the db
     */
    public static final String OPERATOR_ROLE = "operator";

    /**
     * The name of the Approval role, as specified in the db
     */
    public static final String APPROVER_ROLE = "approver";

    /**
     * The name of the Scheduler role, as specified in web.xml
     */
    public static final String SCHEDULER_ROLE = "scheduler";

    /**
     * The name of the User role, as specified in web.xml
     */
    public static final String USER_ROLE = "ROLE_USER";

    /**
     * The name of the user's role list, a request-scoped attribute
     * when adding/editing a user.
     */
    public static final String USER_ROLES = "userRoles";

     /**
     * The name of the Subscriber role, as specified in web.xml
     */
    public static final String SUBSCRIBER_ROLE = "ROLE_SUBSCRIBER";

    /**
     * The name of the Organisation role, as specified in web.xml
     */
    public static final String PROVIDER_ROLE = "ROLE_PROVIDER";

    /**
     * The name of the default NotificationProfile
     */
    public static final String DEFAULT_NOTIFICATIONPROFILE = "default_user";


    /**
     * The name of the available roles list, a request-scoped attribute
     * when adding/editing a user.
     */
    public static final String AVAILABLE_ROLES = "availableRoles";

    /**
     * The name of the CSS Theme setting.
     */
    public static final String CSS_THEME = "csstheme";

    /**
     * The request scope attribute that holds the maintenancePeriodStatus form.
     */
    public static final String MAINTENANCEPERIODSTATUS_KEY = "maintenancePeriodStatusForm";

    /**
     * The request scope attribute that holds the maintenancePeriodStatus list
     */
    public static final String MAINTENANCEPERIODSTATUS_LIST = "maintenancePeriodStatusList";

    /**
     * The request scope attribute that holds the inchargeBroker form.
     */
    public static final String INCHARGEBROKER_KEY = "inchargeBrokerForm";

    /**
     * The request scope attribute that holds the inchargeBroker list
     */
    public static final String INCHARGEBROKER_LIST = "inchargeBrokerList";

    /**
     * The request scope attribute that holds the active inchargeBroker list
     */
    public static final String ACTIVEINCHARGEBROKER_LIST = "activeInchargeBrokerList";

    /**
     * The request scope attribute that holds the domainManager form.
     */
    public static final String DOMAINMANAGER_KEY = "domainManagerForm";

    /**
     * The request scope attribute that holds the domainManager list
     */
    public static final String DOMAINMANAGER_LIST = "domainManagerList";

    /**
     * The request scope attribute that holds the active domainManager list
     */
    public static final String ACTIVEDOMAINMANAGER_LIST = "activeDomainManagerList";

    /**
     * The request scope attribute that holds the broker/domainManager list
     */
    public static final String BROKERDOMAINMANAGER_LIST = "brokerDomainManagerList";

    /**
     * The request scope attribute that holds the active broker/domainManager list
     */
    public static final String ACTIVEBROKERDOMAINMANAGER_LIST = "activeBrokerDomainManagerList";

    /**
     * The request scope attribute that holds the inchargeClass form.
     */
    public static final String INCHARGECLASS_KEY = "inchargeClassForm";

    /**
     * The request scope attribute that holds the inchargeClass list
     */
    public static final String INCHARGECLASS_LIST = "inchargeClassList";

    /**
     * The request scope attribute that holds the
     * domainManager/inchargeclass list
     */
    public static final String DOMAINMANAGERINCHARGECLASS_LIST = "domainManagerInchargeClassList";

    /**
     * The request scope attribute that holds the inchargeInstance form.
     */
    public static final String INCHARGEINSTANCE_KEY = "inchargeInstanceForm";

    /**
     * The request scope attribute that holds the inchargeInstance list
     */
    public static final String INCHARGEINSTANCE_LIST = "inchargeInstanceList";

    /**
     * The request scope attribute that holds the maintenancePeriod form.
     */
    public static final String MAINTENANCEPERIOD_KEY = "maintenancePeriodForm";

    /**
     * The request scope attribute that holds the maintenancePeriod list
     */
    public static final String MAINTENANCEPERIOD_LIST = "maintenancePeriodList";

    /**
     * The request scope attribute that holds the maintenancePeriodLog form.
     */
    public static final String MAINTENANCEPERIODLOG_KEY = "maintenancePeriodLogForm";

    /**
     * The request scope attribute that holds the maintenancePeriodLog list
     */
    public static final String MAINTENANCEPERIODLOG_LIST = "maintenancePeriodLogList";

    /**
     * The request scope attribute that holds the maintenancePeriodReason form.
     */
    public static final String MAINTENANCEPERIODREASON_KEY = "maintenancePeriodReasonForm";

    /**
     * The request scope attribute that holds the maintenancePeriodReason list
     */
    public static final String MAINTENANCEPERIODREASON_LIST = "maintenancePeriodReasonList";

    /**
     * The request scope attribute that holds the commissioningRequestStatus form.
     */
    public static final String COMMISSIONINGREQUESTSTATUS_KEY = "commissioningRequestStatusForm";

    /**
     * The request scope attribute that holds the commissioningRequestStatus list
     */
    public static final String COMMISSIONINGREQUESTSTATUS_LIST = "commissioningRequestStatusList";

    /**
     * The request scope attribute that holds the commissioningRequestReason form.
     */
    public static final String COMMISSIONINGREQUESTREASON_KEY = "commissioningRequestReasonForm";

    /**
     * The request scope attribute that holds the commissioningRequestReason list
     */
    public static final String COMMISSIONINGREQUESTREASON_LIST = "commissioningRequestReasonList";

    /**
     * The request scope attribute that holds the commissioningRequestLog form.
     */
    public static final String COMMISSIONINGREQUESTLOG_KEY = "commissioningRequestLogForm";

    /**
     * The request scope attribute that holds the commissioningRequestLog list
     */
    public static final String COMMISSIONINGREQUESTLOG_LIST = "commissioningRequestLogList";

    /**
     * The request scope attribute that holds the commissioningRequest form.
     */
    public static final String COMMISSIONINGREQUEST_KEY = "commissioningRequestForm";

    /**
     * The request scope attribute that holds the commissioningRequest list
     */
    public static final String COMMISSIONINGREQUEST_LIST = "commissioningRequestList";

    /**
     * The request scope attribute that holds the inchargeInstanceStatus form.
     */
    public static final String INCHARGEINSTANCESTATUS_KEY = "inchargeInstanceStatusForm";

    /**
     * The request scope attribute that holds the inchargeInstanceStatus list
     */
    public static final String INCHARGEINSTANCESTATUS_LIST = "inchargeInstanceStatusList";

    /**
     * The request scope attribute that holds the inchargeClassName form.
     */
    public static final String INCHARGECLASSNAME_KEY = "inchargeClassNameForm";

    /**
     * The request scope attribute that holds the inchargeClassName list
     */
    public static final String INCHARGECLASSNAME_LIST = "inchargeClassNameList";

    /**
     * The request scope attribute that holds the eventSeverity form.
     */
    public static final String EVENTSEVERITY_KEY = "eventSeverityForm";

    /**
     * The request scope attribute that holds the eventSeverity list
     */
    public static final String EVENTSEVERITY_LIST = "eventSeverityList";

    /**
     * The request scope attribute that holds the eventImpact form.
     */
    public static final String EVENTIMPACT_KEY = "eventImpactForm";

    /**
     * The request scope attribute that holds the eventImpact list
     */
    public static final String EVENTIMPACT_LIST = "eventImpactList";

    /**
     * The request scope attribute that holds the eventDuration form.
     */
    public static final String EVENTDURATION_KEY = "eventDurationForm";

    /**
     * The request scope attribute that holds the eventDuration list
     */
    public static final String EVENTDURATION_LIST = "eventDurationList";

    /**
     * The request scope attribute that holds the eventClass form.
     */
    public static final String EVENTCLASS_KEY = "eventClassForm";

    /**
     * The request scope attribute that holds the eventClass list
     */
    public static final String EVENTCLASS_LIST = "eventClassList";

    /**
     * The request scope attribute that holds the auditLog form.
     */
    public static final String AUDITLOG_KEY = "auditLogForm";

    /**
     * The request scope attribute that holds the auditLog list
     */
    public static final String AUDITLOG_LIST = "auditLogList";

    /**
     * The request scope attribute that holds the inchargeInstanceLog form.
     */
    public static final String INCHARGEINSTANCELOG_KEY = "inchargeInstanceLogForm";

    /**
     * The request scope attribute that holds the inchargeInstanceLog list
     */
    public static final String INCHARGEINSTANCELOG_LIST = "inchargeInstanceLogList";

    /**
     * The request scope attribute that holds the networkEvent form.
     */
    public static final String NETWORKEVENT_KEY = "networkEventForm";

    /**
     * The request scope attribute that holds the networkEvent list
     */
    public static final String NETWORKEVENT_LIST = "networkEventList";

    /**
     * The request scope attribute that holds the networkEventAuditLog form.
     */
    public static final String NETWORKEVENTAUDITLOG_KEY = "networkEventAuditLogForm";

    /**
     * The request scope attribute that holds the networkEventAuditLog list
     */
    public static final String NETWORKEVENTAUDITLOG_LIST = "networkEventAuditLogList";

    /**
     * The request scope attribute that holds the notificationEvent form.
     */
    public static final String NOTIFICATIONEVENT_KEY = "notificationEventForm";

    /**
     * The request scope attribute that holds the notificationEvent list
     */
    public static final String NOTIFICATIONEVENT_LIST = "notificationEventList";

    /**
     * The request scope attribute that holds the inchargeNotification form.
     */
    public static final String INCHARGENOTIFICATION_KEY = "inchargeNotificationForm";

    /**
     * The request scope attribute that holds the inchargeNotification list
     */
    public static final String INCHARGENOTIFICATION_LIST = "inchargeNotificationList";

    /**
     * The request scope attribute that holds the eventCategory form.
     */
    public static final String EVENTCATEGORY_KEY = "eventCategoryForm";

    /**
     * The request scope attribute that holds the eventCategory list
     */
    public static final String EVENTCATEGORY_LIST = "eventCategoryList";

    /**
     * The request scope attribute that holds the inchargeNotificationAuditLog form.
     */
    public static final String INCHARGENOTIFICATIONAUDITLOG_KEY = "inchargeNotificationAuditLogForm";

    /**
     * The request scope attribute that holds the inchargeNotificationAuditLog list
     */
    public static final String INCHARGENOTIFICATIONAUDITLOG_LIST = "inchargeNotificationAuditLogList";

    /**
     * The request scope attribute that holds the eventName form.
     */
    public static final String EVENTNAME_KEY = "eventNameForm";

    /**
     * The request scope attribute that holds the eventName list
     */
    public static final String EVENTNAME_LIST = "eventNameList";

    /**
     * The request scope attribute that holds the deviceModel form.
     */
    public static final String DEVICEMODEL_KEY = "deviceModelForm";

    /**
     * The request scope attribute that holds the deviceModel list
     */
    public static final String DEVICEMODEL_LIST = "deviceModelList";

    /**
     * The request scope attribute that holds the deviceVendor form.
     */
    public static final String DEVICEVENDOR_KEY = "deviceVendorForm";

    /**
     * The request scope attribute that holds the deviceVendor list
     */
    public static final String DEVICEVENDOR_LIST = "deviceVendorList";

    /**
     * The request scope attribute that holds the service form.
     */
    public static final String SERVICE_KEY = "serviceForm";

    /**
     * The request scope attribute that holds the service list
     */
    public static final String SERVICE_LIST = "serviceList";

    /**
     * The request scope attribute that holds the active customer list
     */
    public static final String ACTIVESERVICE_LIST = "activeServiceList";

    /**
     * The request scope attribute that holds the notificationProfile form.
     */
    public static final String NOTIFICATIONPROFILE_KEY = "notificationProfileForm";

    /**
     * The request scope attribute that holds the notificationProfile list
     */
    public static final String NOTIFICATIONPROFILE_LIST = "notificationProfileList";

    /**
     * The request scope attribute that holds the active notificationProfile list
     */
    public static final String ACTIVENOTIFICATIONPROFILE_LIST = "activeNotificationProfileList";

    /**
     * The request scope attribute that holds the portalView form.
     */
    public static final String PORTALVIEW_KEY = "portalViewForm";

    /**
     * The request scope attribute that holds the portalView list
     */
    public static final String PORTALVIEW_LIST = "portalViewList";

    /**
     * The request scope attribute that holds the organisation form.
     */
    public static final String ORGANISATION_KEY = "organisationForm";

    /**
     * The request scope attribute that holds the organisation list
     */
    public static final String ORGANISATION_LIST = "organisationList";

    /**
     * The request scope attribute that holds the active organisation list
     */
    public static final String ACTIVEORGANISATION_LIST = "activeOrganisationList";

    /**
     * The request scope attribute that holds the serviceSubscription form.
     */
    public static final String SERVICESUBSCRIPTION_KEY = "serviceSubscriptionForm";

    /**
     * The request scope attribute that holds the serviceSubscription list
     */
    public static final String SERVICESUBSCRIPTION_LIST = "serviceSubscriptionList";

    /**
     * The request scope attribute that holds the serviceAccessPoint form.
     */
    public static final String SERVICEACCESSPOINT_KEY = "serviceAccessPointForm";

    /**
     * The request scope attribute that holds the serviceAccessPoint list
     */
    public static final String SERVICEACCESSPOINT_LIST = "serviceAccessPointList";

    /**
     * The request scope attribute that holds the active customer list
     */
    public static final String ACTIVESERVICEACCESSPOINT_LIST = "activeServiceAccessPointList";

}
