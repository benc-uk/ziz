/*
	File:		port.js
	Project:	Ziz
	Purpose:	TCP port monitor and tester
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address to connect to
	port		-	int; TCP port to connect to (e.g. 80 for HTTP, 22 for SSH, etc.)
	Optional Parameters:
	timeout		-	int; Timeout in millsec (default: 5000)
	Output:		
	Time taken to connect to port in milliseconds
*/

// Java imports
importClass(com.bencoleman.ziz.utils.Timer);
importClass(java.net.InetSocketAddress);
importClass(java.net.Socket);

// Set the units returned by this monitor
units = "ms";

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	// Optional timeout param
	socket_timeout = 5000
	if(typeof(timeout) !== 'undefined')
		socket_timeout = parseInt(timeout);
		
	try {
		// Create socket and bind to ephemeral port 
		socket = Socket();
		socket.bind(null);
		// Start timer and connect to remote address and port (with timeout)
		timer = Timer();
		socket.connect(InetSocketAddress(hostname, port), socket_timeout);
		socket.close();
	} catch(err) {
		// Something bad happened, host doesn't exist or port is closed
		result.msg = ""+err;
		result.status = result.STATUS_ERROR;
		return;
	}
	// All good - connected OK, report response time;
	result.value = timer.stop();
	result.msg = "Port open, service available";
	result.status = result.STATUS_GOOD;
}