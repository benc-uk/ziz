import sys, re
from com.bencoleman.ziz import Result, Main
from java.lang import *


def average(numbers):
    return sum(numbers) / len(numbers)
	
def fail(res, msg):
	res.status = result.STATUS_FAILED
	res.msg = msg
	res.value = None

def getCollector():
	return Main.collector
	
def fetchPerfCounter(counter, host):     
	res = Result()
	cmd_str = "typeperf \""+counter+"\" -sc 1 -s " + host
	output = "";

	#print("CMD="+cmd_str)
	Main.log.debug("Fetching perfmon counter "+counter+" from "+host)
	try:
		script_proc = Runtime.getRuntime().exec(cmd_str)
		input = script_proc.getInputStream()

		c = input.read()
		while (c != -1):
			output += chr(c)
			c = input.read()
		
		input.close()
		script_proc.destroy()

		#print output
		if(script_proc.exitValue() != 0):
			res.status = Result.STATUS_FAILED
			res.msg = output.replace("\r\n", " ")
			return res
		else:
			match = re.search(",\"(\d+\.\d+)", output)
			res.value = float(match.group(1))
		
	except Exception, e:
		Main.log.error("Failed to collect perfmon data - " +str(e))
	
	return res

def runWinCommand(cmd_str): 
	res = Result()
	output = ""
	stderr = ""

	try:
		Main.log.debug("Executing command: "+cmd_str)
		script_proc = Runtime.getRuntime().exec(cmd_str)
		input = script_proc.getInputStream()

		c = input.read()
		while (c != -1):
			output += chr(c)
			c = input.read()
		
		input.close()

		#print output
		exit_code = script_proc.exitValue()
		#print "rc="+str(exit_code)
		if(exit_code != 0):
			#print "error"
			
			error = script_proc.getErrorStream()
			c = error.read()
			while (c != -1):
				stderr += chr(c)
				c = error.read()
			error.close()
			
		script_proc.destroy()		
		return (exit_code, output, stderr)
		
	except Exception, e:
		Main.log.error("Failed to execute command "+str(e))
	
def flattenString(str):
	temp = str.replace("\n", " ")
	return temp.replace("\r", " ")