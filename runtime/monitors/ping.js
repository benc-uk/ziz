/*
	File:		ping.js
	Project:	Ziz
	Purpose:	ICMP ping monitor
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	11-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address to ping
	Optional Parameters:
	timeout		-	int; Timeout in millsec (default: 1000)
	count		-	int; Number of ping packets to send (default: 3)
	packet_size	-	int; Size of ping packet in bytes (default: 32)
	Output:		
	Average time taken for all pings in milliseconds	
*/

// Java imports
importPackage(java.net);
importPackage(org.shortpasta.icmp);
importPackage(com.bencoleman.ziz);

// Set the units returned by this monitor
units = "ms";

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{	
	// Get IP address to ping
	try {
		ina = InetAddress.getByName(hostname);
		ip = ina.getHostAddress();
	} catch(e if e.javaException instanceof java.net.UnknownHostException) {
		result.status = result.STATUS_ERROR;
		result.msg = "Unable to resolve destination IP";
		return;
	}
		
	// Sort out all the optional and defualt parmeters
	ping_packet = 32
	if(typeof(packet_size) !== 'undefined')
		ping_packet = parseInt(packet_size);
		
	ping_count = 3
	if(typeof(count) !== 'undefined')
		ping_count = parseInt(count);
		
	ping_timeout = 1000
	if(typeof(timeout) !== 'undefined')
		ping_timeout = parseInt(timeout);
		
	// Arrays to store results (response times & errors)
	times = [];
	errors = [];

	// Ping 'count' number of times
	for(i = 0; i < ping_count; i++) {
		// Execute the ping (ICMP request)
		resp = IcmpUtil.executeIcmpPingRequest(ip, ping_packet, ping_timeout);
		//println(" ### t="+resp.getDelay()+" "+resp.getTimedOutFlag());
		
		// put result in relevant array times or errors
		timedout = 0;
		if(resp.getTimedOutFlag())
			timedout = 1;
		else
			times.push(resp.getDelay());
			
		errors.push(timedout)
	}
	
	// calculate averages of response times and percent of packets that timed out
	if(times.length > 0)
		time = times.avg();
	else
		time = 0.0;
	error_weight = errors.avg();
	
	// Send results based on error_weight (percent of packets that timedout)
	if(error_weight == 1.0) {
		// All packets timed out
		result.status = result.STATUS_ERROR;
		result.msg = 'All pings timed out';
	} else if (error_weight > 0.0 && error_weight < 1.0) {
		// Partial success (some time outs, some responses)
		result.status = result.STATUS_WARN;
		result.msg = Math.round(error_weight * 100) + "% of packets timed out";
		result.value = time;
	} else {
		// All packets were good (no timeouts)
		result.status = result.STATUS_GOOD;
		result.msg = 'Ping OK, 100% packets good';
		result.value = time;
	}
	
	
}