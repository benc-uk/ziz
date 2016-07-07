/*
	File:		ssh-memory.js
	Project:	Ziz
	Purpose:	Monitor memory on a server via SSH (for Unix based hosts)
	Author:		Ben Coleman
	Created:	21-04-2010
	Updated:	21-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of SSH host
	username	-	string;	Username to connect to SSH
	password	-	string; Password of supplied user, or passphrase of keyfile
	Optional Parameters:
	ssh_port	-	int; SSH port number, 22 is the default
	ssh_keyfile	-	string;	If using keyfile auth, the path to the private keyfile
	Output:		
	Number of free megabytes of memory
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SSH);

// Set the units returned by this monitor
units = "MB";

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
		// - use the free command and output as bytes
		out = ssh.execCommand("free");
		//java.lang.System.out.println(out);
		ssh.disconnect();
		
		if(ssh.getExitCode() != 0) {
			result.msg = "Command '/usr/bin/free' failed";
			result.status = result.STATUS_FAILED;	
			return;			
		}

		// pull value out of result with regex
		m = out.match(/Mem:\s+\d+\s+\d+\s+(\d+)/im);
		if(m == null || m.length > 1) {
			free = parseFloat(m[1]);
			free = free / 1048576;
		} else {
			result.msg = "Error parsing output";
			result.status = result.STATUS_FAILED;	
			return;			
		}
		result.value = free;
		result.status = result.STATUS_GOOD;	
		result.msg = "Memory value fetched OK";	
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}