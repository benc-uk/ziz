import mon_utils

def main():
	try:
		collector = mon_utils.getCollector()
		cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "instance="+process, "wmiclass=PerfProc_Process", "properties=WorkingSet,PercentProcessorTime,HandleCount"]);
		response = collector.sendCommand(cmd)
		
		print response
		
		if(response.startswith("ERROR")):
			if(response.endswith("No results")):
				result.status = result.STATUS_ERROR
				result.msg = "Process "+process+" is not running"				
			else:
				result.status = result.STATUS_FAILED
				result.msg = response
		else:
			parts = response.split(",")
			result.value = long(parts[2].split('=')[1]) / 1048576.0
			result.status = result.STATUS_GOOD
			result.msg = "Process "+process+" is running. "+parts[0]+" "+parts[1]

	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)