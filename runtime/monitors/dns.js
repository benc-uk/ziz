/*
	File:		dns.js
	Project:	Ziz
	Purpose:	DNS monitor, lookup DNS records
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname to lookup in DNS
	dns_server	-	string; Hostname or IP of DNS server to use
	Optional Parameters:
	record		-	string; Type of DNS record to lookup (default: A)
	Output:		
	Time taken for DNS record to be returned in milliseconds
*/

// Java imports
importClass(java.util.Hashtable);
importPackage(javax.naming);
importPackage(javax.naming.directory);
importClass(com.bencoleman.ziz.utils.Timer);

// Set the units returned by this monitor
units = "ms";

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	// DNS record type to query
	rec = "A";
	if(typeof(record) !== 'undefined')
		rec = record;
	
	try {
		// start timer
		timer = Timer();
		
		// How to do a DNS lookup in Java
		env = Hashtable();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		env.put("java.naming.provider.url", "dns://" + dns_server);
		ictx = InitialDirContext(env);
		// Get the record results
		attrs = ictx.getAttributes(hostname, [rec]);
		attr = attrs.get(rec);
		
		// no results error
		if(attr == null) {
			result.msg = "No "+rec+" record found";
			result.status = result.STATUS_ERROR;
			return;
		}
		
		// otherwise all is good stop the timer and return result.
		result.value = timer.stop();
		result.status = result.STATUS_GOOD;
		result.msg = "Result: " + attr.toString();
		ictx.close();
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}