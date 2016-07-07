import mon_utils

def main():
	try:
		collector = mon_utils.getCollector()
		cmd = collector.makeCommand(["wmi_query", "hostname="+hostname, "username="+username, "password="+password, "query=SELECT State FROM Win32_Service WHERE Name=\""+service+"\""]);
		response = collector.sendCommand(cmd)
		
		if(response.startswith("ERROR")):
			if(response.endswith("No results")):
				result.status = result.STATUS_ERROR
				result.msg = "Service "+service+" not found"				
			else:
				result.status = result.STATUS_FAILED
				result.msg = response
		else:
			state = response.split("=")[1]
			result.msg = "Service "+service+" is: "+state	
			if(state.lower() == "running"):
				result.status = result.STATUS_GOOD
				result.value = 1.0
			else:
				result.status = result.STATUS_ERROR
				result.value = 0.0
				

	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)