/*
	File:		ssh-command.js
	Project:	Ziz
	Purpose:	Genric monitor
	Author:		Ben Coleman
	Created:	23-04-2010
	Updated:	23-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	hostname	-	string; Hostname or IP address of SSH host
	username	-	string;	Username to connect to SSH
	password	-	string; Password of supplied user, or passphrase of keyfile
	command		-	string;	Command to run
	regex		- 	string; Regex to parse the output
	match		- 	string; Match number to return
	Optional Parameters:
	ssh_port	-	int; SSH port number, 22 is the default
	ssh_keyfile	-	string;	If using keyfile auth, the path to the private keyfile
	set_unit	-	string; Units, e.g. MB or Mbit/s
	div			-	int; Divide the result by this value
	mult		-	int; Multiply the result by this value	
	do_delta	-	bool; Calculate the delta on each poll, for incrementing counters	
	Output:		
	Varies
*/

// Java imports
importClass(com.bencoleman.ziz.utils.SSH);

if(typeof(set_unit) !== 'undefined')
	var units = set_unit;
else
	var units = '';
	
/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	var ssh;
	var port = 22;
	if(typeof(ssh_port) !== 'undefined')
		port = parseInt(ssh_port);
		
	if(typeof(delta) !== 'undefined')
		do_delta = (delta.toLowerCase() == 'true');
	
	try {
		// connect to host via SSH
		if(typeof(ssh_keyfile) !== 'undefined') {
			ssh = SSH(hostname, port, username, password, ssh_keyfile);
		} else {
			ssh = SSH(hostname, port, username, password);
		}
		
		// execute command
		out = ssh.execCommand(command);
		ssh.disconnect();
		
		if(ssh.getExitCode() != 0) {
			result.msg = "Command '"+command+"' failed";
			result.status = result.STATUS_FAILED;	
			return;			
		}

		// pull value out of result with regex
		var re = new RegExp(regex, "m");
		m = out.match(re);
		if(m == null || m.length > 1) {
			val = parseFloat(m[match]);
			println(regex+" VAL = "+ val);
			if(typeof(do_delta) !== 'undefined') {
				old = getPersist("ssh_val");
				savePersist("ssh_val", val);
				if(old != null) {
					old_val = parseFloat(old);
					val = val - old_val;	
					intv = monitor.getInterval() / 1000;
					val /= intv;
				}
			}		
			
			if(typeof(div) !== 'undefined') {
				div_v = parseFloat(div);
				val /= div_v;
			}
			
			if(typeof(mult) !== 'undefined') {
				mult_v = parseFloat(mult);
				val *= mult_v;
			}			

			result_value = val;	
			if(isNaN(result_value)) {
				result.msg = "Error parsing output, captured value must be numeric";
				result.status = result.STATUS_FAILED;	
				return;				
			}
		} else {
			result.msg = "Error parsing output";
			result.status = result.STATUS_FAILED;	
			return;			
		}
		result.value = result_value;
		result.status = result.STATUS_GOOD;	
		result.msg = "Command completed";	
	} catch(err) {
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;		
	}
}