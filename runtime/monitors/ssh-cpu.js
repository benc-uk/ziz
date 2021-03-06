/*
	File:		ssh-cpu.js
	Project:	Ziz
	Purpose:	Monitor processor usage on a server via SSH (for Unix based hosts)
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
	Percentage of user time calculated across all CPUs over a 6 second period
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SSH);

// Set the units returned by this monitor
units = "%";

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
		// - use mpstat and check 3 times at 2 second intervals
		out = ssh.execCommand("mpstat -P ALL 2 3");
		//java.lang.System.out.println(out);
		ssh.disconnect();
		
		if(ssh.getExitCode() != 0) {
			result.msg = "Command '/usr/bin/mpstat' failed";
			result.status = result.STATUS_FAILED;	
			return;			
		}

		// pull value out of result with regex
		m = out.match(/^Average:\s+all\s+(.*?)\s+/im);
		if(m == null || m.length > 1) {
			cpu = parseFloat(m[1]);
		} else {
			result.msg = "Error parsing output";
			result.status = result.STATUS_FAILED;	
			return;			
		}
		result.value = cpu;
		result.status = result.STATUS_GOOD;	
		result.msg = "CPU usage fetched OK";	
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}