import mon_utils

def main():
	try:
		collector = mon_utils.getCollector()
		cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "wmiclass=PerfOS_Memory", "properties=CommittedBytes"]);
		response = collector.sendCommand(cmd);

		if(response.startswith("ERROR")):
			result.status = result.STATUS_FAILED
			result.msg = response
		else:
			result.value = long(response.split('=')[1]) / 1048576.0
			result.status = result.STATUS_GOOD
			result.msg = "Memory counter data fetched"

	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)