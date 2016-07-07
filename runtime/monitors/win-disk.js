/*
	File:		win-disk.js
	Project:	Ziz
	Purpose:	Fetch disk space from a Windows host
	Author:		Ben Coleman
	Created:	04-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	disk		-	string; Drive letter to be monitored, with colon (e.g. 'C:')
	Optional Parameters:
	None
	Output:
	Percentage of free space on the drive
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
	// Note. We pass parameter 'disk' as the instance we want
	cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "instance="+disk, "wmiclass=PerfDisk_LogicalDisk", "properties=PercentFreeSpace"]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			// This means disk doesn't exist
			result.status = result.STATUS_ERROR
			result.msg = "Disk "+disk+" not found"					
		} else {
			// Some other problem
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		// All good, send results back
		result.value = response.split('=')[1];
		result.status = result.STATUS_GOOD;
		result.msg = "Disk counter data fetched";
	}
}