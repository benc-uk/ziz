/*
	File:		win-service.js
	Project:	Ziz
	Purpose:	Monitor if a service is running on a Windows host
	Author:		Ben Coleman
	Created:	04-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	service		-	string; Name of the service to monitor (use the short name, e.g. 'spooler')
	Optional Parameters:
	None
	Output:
	1 and status GOOD  = Service running
	0 and status ERROR = Service isn't running (stopped, paused, starting, etc.)
*/

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	// Get the collector object (see mon_utils)
	collector = getCollector();
	if(collector == null) { 
		result.status = result.STATUS_FAILED;
		result.msg = "Collector process is not running";
		return;
	}

	// Form our command (WMI_QUERY query)
	// Note. Filter instances to only the named service we want
	cmd = collector.makeCommand(["wmi_query", "hostname="+hostname, "username="+username, "password="+password, "query=SELECT State FROM Win32_Service WHERE Name=\""+service+"\""]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			// Zero results means service doesn't exist
			result.status = result.STATUS_ERROR
			result.msg = "Service "+service+" not found"					
		} else {
			// Some other error
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		// Get the state of the service
		svc_state = response.split("=")[1];
		result.msg = "Service "+service+" is "+svc_state;
		if(svc_state.toLowerCase() == "running") {
			// "Running" state is good
			result.status = result.STATUS_GOOD;
			result.value = 1.0;
		} else {
			// All other states are bad
			result.status = result.STATUS_ERROR;
			result.value = 0.0;
		}
	}
}