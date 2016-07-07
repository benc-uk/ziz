/*
	File:		win-memory.js
	Project:	Ziz
	Purpose:	Fetch memory utilisation from a Windows host
	Author:		Ben Coleman
	Created:	04-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	Optional Parameters:
	None
	Output:
	Ammount of committed memory (in mega-bytes)
*/

// Set the units returned by this monitor
units = "MB";

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
	
	// Form our command (WMI_COUNTER query)
	// Note. No need for an instance, there is only ever one instance of PerfOS_Memory
	cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "wmiclass=PerfOS_Memory", "properties=CommittedBytes"]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		result.status = result.STATUS_FAILED;
		result.msg = response;
	} else {
		// When parsing response we convert bytes into mega-bytes
		result.value = response.split('=')[1] / 1048576.0;
		result.status = result.STATUS_GOOD;
		result.msg = "Memory counter data fetched";
	}
}