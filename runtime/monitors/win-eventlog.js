/*
	File:		win-eventlog.js
	Project:	Ziz
	Purpose:	Monitor Window event logs
	Author:		Ben Coleman
	Created:	22-04-2010
	Updated:	22-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	logfile		-	string; Name of the eventlog to monitor (System, Application, Security, etc.)
	filter		-	string; WQL clause to filter the events (e.g. Type = "Error")
	Optional Parameters:
	None
	Output:
	Number of event records found 
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

	rec_num = getPersist("lastRecord");
	if(rec_num != null)
		filter += " AND RecordNumber > "+rec_num;
		
	response = "";
	// Form our command (WMI_QUERY query)
	// Note. Filter instances to only the named service we want
	cmd = collector.makeCommand(["wmi_query", "hostname="+hostname, "username="+username, "password="+password, "query=Select RecordNumber From Win32_NTLogEvent Where LogFile=\""+logfile+"\" AND "+filter]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);

	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			result.status = result.STATUS_GOOD;
			result.value = 0;			
			result.msg = "0 events found";
			return;			
		} else {
			// Some other error
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		//println(response);
		events_arr = response.trim().split('\t');
		last_rec = events_arr[0].split('=')[1];
		savePersist("lastRecord", last_rec);
		result.status = result.STATUS_GOOD;
		result.value = events_arr.length;			
		result.msg = events_arr.length+" events found";
	}
}