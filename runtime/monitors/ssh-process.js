/*
	File:		ssh-process.js
	Project:	Ziz
	Purpose:	Monitor running process(es) on a SSH (Unix) host
	Author:		Ben Coleman
	Created:	23-04-2010
	Updated:	23-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of SSH host
	username	-	string;	Username to connect to SSH
	password	-	string; Password of supplied user, or passphrase of keyfile
	process		-	string;	Name of process to check for (substrings will match via grep)
	Optional Parameters:
	ssh_port	-	int; SSH port number, 22 is the default
	ssh_keyfile	-	string;	If using keyfile auth, the path to the private keyfile
	Output:		
	Number of processes running
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SSH);

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	var ssh;
	var port = 22;
	if(typeof(ssh_port) !== 'undefined')
		port = parseInt(ssh_port);
	
	try {
		// connect to host via SSH
		if(typeof(ssh_keyfile) !== 'undefined') {
			ssh = SSH(hostname, port, username, password, ssh_keyfile);
		} else {
			ssh = SSH(hostname, port, username, password);
		}
		
		// execute command
		// - use ps to list processes, then grep for the specific process name, then count the lines
		out = ssh.execCommand("/bin/ps -e|grep "+process+"|wc -l");
		ssh.disconnect();
		
		if(ssh.getExitCode() != 0) {
			result.msg = "Command '/bin/ps' failed";
			result.status = result.STATUS_FAILED;	
			return;			
		}

		// pull value out of result with regex
		m = out.match(/^(\d+)/im);
		if(m == null || m.length > 1) {
			count = parseInt(m[1]);			
		} else {
			result.msg = "Error parsing output";
			result.status = result.STATUS_FAILED;	
			return;			
		}
		result.value = count;
		result.status = result.STATUS_GOOD;	
		result.msg = "Process monitor completed";	
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}