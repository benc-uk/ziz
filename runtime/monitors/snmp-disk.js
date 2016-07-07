/*
	File:		snmp-disk.js
	Project:	Ziz
	Purpose:	Fetch disk space with SNMP
	Author:		Ben Coleman
	Created:	03-02-2011
	Updated:	03-02-2011
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of device to poll
	password	-	string; SNMP community string
	disk		-	string; Drive to be monitored
	Optional Parameters:
	version		-	int; 1 = use SNMPv1, 2 = use SNMPv2
	port		-	int; SNMP port, default is 161
	Output:		
	Percentage of free space on the drive
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
	
	try {
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
		disk_descs = snmp.getColumnOID(".1.3.6.1.2.1.25.2.3.1.3");
		disk_count = disk_descs.length;
		
		disk_found = false;
		for(d = 0; d < disk_count; d++) {
			disk_name = disk_descs[d];
			if(disk_name.toLowerCase().indexOf(disk.toLowerCase()) == 0) {
				disk_found = true;
				size = parseInt(snmp.getSingleOID(".1.3.6.1.2.1.25.2.3.1.5."+(d+1)));
				used = parseInt(snmp.getSingleOID(".1.3.6.1.2.1.25.2.3.1.6."+(d+1)));
				full = 100 - ((used / size) * 100);
			}
		}
		
		if(disk_found) {
			result.value = full;
			result.status = result.STATUS_GOOD;
			result.msg = "Free disk space fetched";
		} else {
			result.value = Number.NaN;
			result.msg = "Disk "+disk+" not found";
			result.status = result.STATUS_FAILED;	
		}
		snmp.close();

	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}