/*
	File:		snmp-cpu.js
	Project:	Ziz
	Purpose:	Fetch CPU usage from server with SNMP
	Author:		Ben Coleman
	Created:	02-02-2011
	Updated:	02-02-2011
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of device to poll
	password	-	string; SNMP community string
	Optional Parameters:
	version		-	int; 1 = use SNMPv1, 2 = use SNMPv2
	port		-	int; SNMP port, default is 161
	cpu			-	int; The CPU id you want to monitor or -1 for an average of all CPUs (default)
	Output:		
	CPU utilisation as a percentage 
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SNMP);

// Set the units returned by this monitor
units = "%";

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
	cpu_index = -1;
	if(typeof(cpu) !== 'undefined') {
		cpu_index = parseInt(cpu);
	}
	
	try {
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
		values = snmp.getColumnOID(".1.3.6.1.2.1.25.3.3.1.2");
		
		// Get single CPU load value
		if(cpu_index >= 0) {
			extra = " for CPU: " + (cpu_index);
			total = values[cpu_index];
		} else {
			extra = ", "+values.length+" CPUs";
			total = values.avg();
		}
		result.value = total;
		result.status = result.STATUS_GOOD;
		result.msg = "CPU counter data fetched"+extra;
		snmp.close();

	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}