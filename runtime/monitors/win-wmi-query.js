/*
	File:		win-wmi-query.js
	Project:	Ziz
	Purpose:	Generic monitor - WMI Query
	Author:		Ben Coleman
	Created:	24-04-2010
	Updated:	24-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of Windows host
	username	-	string;	Username to connect to remote host (include domain if necessary)
	password	-	string; Password of supplied user
	query		-	string; WMI query in WQL format
	Optional Parameters:
	count		-	n/a;	If present, return the count of the number of instances in the query result
	Output:
	Varies
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
	cmd = collector.makeCommand(["wmi_query", "hostname="+hostname, "username="+username, "password="+password, "query="+query]);
	// Send the command to the collector and get the output response
	response = collector.sendCommand(cmd);
	


	if(response.indexOf("ERROR") >= 0) {
		// If response string starts with error, something went wrong
		if(response.indexOf("No results") >= 0) {
			if(typeof(count) !== 'undefined') {
				result.value = 0
				result.msg = "WMI query successful";
				result.status = result.STATUS_GOOD;		
				return;			
			} else {
				result.status = result.STATUS_FAILED;
				result.msg = response;			
			}
		} else {
			// Some other error
			result.status = result.STATUS_FAILED;
			result.msg = response;
		}
	} else {
		if(typeof(count) !== 'undefined') {
			result.value = response.split("\t").length;
			result.msg = "WMI query successful";
			result.status = result.STATUS_GOOD;		
			return;
		}
		v = response.split("=")[1];
		result.value = parseFloat(v);
		result.msg = "WMI query successful";
		result.status = result.STATUS_GOOD;
	}
}