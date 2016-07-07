/*
	File:		win-process.js
	Project:	Ziz
	Purpose:	Monitor a running process on a Windows host
	Author:		Ben Coleman
	Created:	04-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	process		-	string; Name of the process to monitor (.exe should be omitted)
	Optional Parameters:
	None
	Output:
	Working set (memory) used by the process, also returns ERROR status if process isn't running
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
	// Note. Filter instances to only the named process we want
	cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "instance="+process, "wmiclass=PerfProc_Process", "properties=WorkingSet,PercentProcessorTime,HandleCount"]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			// Zero results means process doesn't exist, i.e. not running
			result.status = result.STATUS_ERROR
			result.msg = "Process "+process+" is not running"					
		} else {
			// Some other error
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		// Multi part response, so split on comma and parse
		//  WorkingSet gets converted into MB and returned as our value
		//  Other values (Handles & CPU) get put into the messsage text FYI
		parts = response.split(",");
		result.value = parts[2].split('=')[1] / 1048576.0;
		result.status = result.STATUS_GOOD;
		result.msg = "Process "+process+" is running. "+parts[0]+" "+parts[1];
	}
}