/*
	File:		win-cpu.js
	Project:	Ziz
	Purpose:	Fetch CPU utilisation from a Windows host
	Author:		Ben Coleman
	Created:	05-04-2010
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
	CPU utilisation as a percentage (avg. across all processors in the host)
*/

// Set the units returned by this monitor
units = "%";

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
	// Note. We fetch the '_Total' instance to get the avg across all the CPUs in the host
	cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, 
								 "wmiclass=PerfOS_Processor", "properties=PercentProcessorTime", "instance=_Total"]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		result.status = result.STATUS_FAILED;
		result.msg = response;
	} else {
		// Otherwise parse value from response and return result
		result.value = response.split('=')[1];
		result.status = result.STATUS_GOOD;
		result.msg = "CPU counter data fetched";
	}
}