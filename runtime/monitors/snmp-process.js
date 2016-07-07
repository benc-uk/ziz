/*
	File:		snmp-process.js
	Project:	Ziz
	Purpose:	Fetch running process count with SNMP
	Author:		Ben Coleman
	Created:	03-02-2011
	Updated:	03-02-2011
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of SSH host
	username	-	string;	Username to connect to SSH
	password	-	string; Password of supplied user, or passphrase of keyfile
	process		-	string;	Name of process to check for (substrings will match) non case sensitive 
	Optional Parameters:
	version		-	int; 1 = use SNMPv1, 2 = use SNMPv2
	port		-	int; SNMP port, default is 161
	Output:		
	Real system memory allocated to the process, returns ERROR status if process isn't running
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SNMP);
importClass(java.lang.System);

// Set the units returned by this monitor
units = "MB";

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	var snmp;
	var snmp_port = 161;
	if(typeof(port) !== 'undefined')
		snmp_port = parseInt(port);
	var snmp_v = 1;
	if(typeof(version) !== 'undefined') {
		snmp_v = parseInt(version);
	}
	
	try {
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
		proc_names = snmp.getColumnOIDExtra(".1.3.6.1.2.1.25.4.2.1.2");
		proc_count = proc_names.length;
		
		proc_found = 0;
		mem = 0;
		for(p = 0; p < proc_count; p++) {
			proc_name = proc_names[p][0];

			if(proc_name.toLowerCase().indexOf(process.toLowerCase()) >= 0) {
				proc_oid = proc_names[p][1];
				proc_oid = proc_oid.substr(proc_oid.lastIndexOf("."));
				proc_found++;
				mem += parseInt(snmp.getSingleOID(".1.3.6.1.2.1.25.5.1.1.2"+proc_oid));	
				
			}
		}
	
		if(proc_found > 0) {
			result.value = mem / 1024;
			result.msg = proc_found+" processes '"+process+"' found running"
			result.status = result.STATUS_GOOD;
		} else {
			result.value = Number.NaN;
			result.msg = "Process "+process+" was not running";
			result.status = result.STATUS_ERROR;	
		}
		snmp.close();

	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}