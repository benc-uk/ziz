/*
	File:		snmp-memory.js
	Project:	Ziz
	Purpose:	Fetch memory usage from server with SNMP
	Author:		Ben Coleman
	Created:	26-05-2010
	Updated:	26-05-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of device to poll
	password	-	string; SNMP community string
	Optional Parameters:
	version		-	int; 1 = use SNMPv1, 2 = use SNMPv2
	port		-	int; SNMP port, default is 161
	mem_type	-	string; 'physical' or 'virtual' - default is physical
	Output:		
	Ammount of committed physical memory (in mega-bytes)
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SNMP);

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
	
	var mem_type_name = 'physical memory';
	if(typeof(mem_type) !== 'undefined') {
		if(mem_type.toLowerCase() == 'virtual') mem_type_name = 'virtual memory';
	}
	
	try {
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
	
		store_names = snmp.getColumnOIDExtra(".1.3.6.1.2.1.25.2.3.1.3");
		store_count = store_names.length;
		
		found = 0;
		for(s = 0; s < store_count; s++) {
			store_name = store_names[s][0];

			if(store_name.toLowerCase().indexOf(mem_type_name) >= 0) {
				store_oid = store_names[s][1];
				store_oid = store_oid.substr(store_oid.lastIndexOf("."));
				found = 1;
				mem_alloc_units = parseInt(snmp.getSingleOID(".1.3.6.1.2.1.25.2.3.1.4" + store_oid));	
				mem_value = parseInt(snmp.getSingleOID(".1.3.6.1.2.1.25.2.3.1.6" + store_oid));	
				
				result.value = (mem_alloc_units * mem_value) / 1048576.0;
				result.status = result.STATUS_GOOD;
				result.msg = "Memory counter data fetched";
				snmp.close();				
			}
		}	
		
	/*
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
		values = snmp.getColumnOID(".1.3.6.1.2.1.25.2.3.1.4");
		alloc_size = values[values.length-type_sub];
		values = snmp.getColumnOID(".1.3.6.1.2.1.25.2.3.1.6");
		phys_mem = values[values.length-type_sub];
		
		result.value = (alloc_size * phys_mem) / 1048576.0;
		result.status = result.STATUS_GOOD;
		result.msg = "Memory counter data fetched";
		snmp.close();
*/

	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}