import mon_utils

def main():
	try:
		collector = mon_utils.getCollector()
		cmd = collector.makeCommand(["wmi_counter", "hostname="+hostname, "username="+username, "password="+password, "wmiclass=PerfOS_Processor", "properties=PercentProcessorTime", "instance=_Total"]);
		response = collector.sendCommand(cmd);

		if(response.startswith("ERROR")):
			result.status = result.STATUS_FAILED
			result.msg = response
		else:
			result.value = float(response.split('=')[1]);
			result.status = result.STATUS_GOOD
			result.msg = "CPU counter data fetched"

	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)