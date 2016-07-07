/*
	File:		snmp-get.js
	Project:	Ziz
	Purpose:	Generic SNMP monitor, for polling a single SNMP OID with GET
	Author:		Ben Coleman
	Created:	23-04-2010
	Updated:	23-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of device to poll
	password	-	string; SNMP community string
	oid			-	string;	SNMP OID to fetch
	Optional Parameters:
	version		-	int; Should be 1 or 2
	port		-	int; SNMP port, default is 161
	div			-	int; Divide the result by this value
	mult		-	int; Multiply the result by this value
	do_delta	-	bool; Calculate the delta on each poll, for SNMP counters
	set_unit	-	string; Units, e.g. MB or Mbit/s
	Output:		
	Varies
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SNMP);

if(typeof(set_unit) !== 'undefined')
	var units = set_unit;
else
	var units = '';
	
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
	
	if(typeof(delta) !== 'undefined')
		do_delta = (delta.toLowerCase() == 'true');
		
	try {
		//println(oid);
		snmp = SNMP(hostname, password, snmp_port, snmp_v);
		val_str = snmp.getSingleOID(oid);
		val = parseFloat(val_str);
		snmp.close();
			
		if(typeof(do_delta) !== 'undefined') {
			old = getPersist("snmp_val");
			savePersist("snmp_val", val);
			if(old != null) {
				old_val = parseFloat(old);
				val = val - old_val;	
				intv = monitor.getInterval() / 1000;
				val /= intv;
			}
		}
		
		if(typeof(div) !== 'undefined') {
			div_v = parseFloat(div);
			val /= div_v;
		}
		
		if(typeof(mult) !== 'undefined') {
			mult_v = parseFloat(mult);
			val *= mult_v;
		}
		
		result.value = val;
		result.msg = "SNMP OID fetched";
		result.status = result.STATUS_GOOD;
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}