import mon_utils

def main():
	try:
		collector = mon_utils.getCollector()
		cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "instance="+disk, "wmiclass=PerfDisk_LogicalDisk", "properties=PercentFreeSpace"]);
		response = collector.sendCommand(cmd)
		
		if(response.startswith("ERROR")):
			if(response.endswith("No results")):
				result.status = result.STATUS_ERROR
				result.msg = "Disk "+disk+" not found"				
			else:
				result.status = result.STATUS_FAILED
				result.msg = response
		else:
			result.value = float(response.split('=')[1])
			result.status = result.STATUS_GOOD
			result.msg = "Disk counter data fetched"

	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)