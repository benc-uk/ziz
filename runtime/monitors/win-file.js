/*
	File:		win-file.js
	Project:	Ziz
	Purpose:	Monitor a file  
	Author:		Ben Coleman
	Created:	22-04-2010
	Updated:	22-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	filename	-	string; Name and full path of the file to monitor
	Optional Parameters:
	ignore_missing	-	bool; Ignore missing files and treat status as good
	Output:
	Size of the file in KB
*/

// Set the units returned by this monitor
units = "KB";

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
	cmd = collector.makeCommand(["wmi_query", "hostname="+hostname, "username="+username, "password="+password, "query=Select LastAccessed, LastModified, FileSize from CIM_DataFile where Name=\""+filename+"\""]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			if(typeof(ignore_missing) !== 'undefined' && ignore_missing.toLowerCase() == 'true') {
				result.status = result.STATUS_GOOD
				result.msg = "File not found, but ignored"	
				result.value = 0;
				return;
			} else { 
				// Zero results means file doesn't exist
				result.status = result.STATUS_ERROR
				result.msg = "File not found"	
			}			
		} else {
			// Some other error
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		parts = response.split(",");
		result.value = parts[0].split('=')[1] / 1024;
		result.msg = "Last modified: "+wmiDateToReadable(parts[2].split('=')[1]);
	}
}