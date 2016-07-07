from org.nsclient4j import NSClient4j, NSClient4JException

def main():
	try:
		ns_port = 12489
		if("port" in globals()):
			ns_port = int(port)
			
		client = NSClient4j(hostname, ns_port)
		up = client.isServiceUp(service_name)
		client.close()
		if(up):
			result.value = float(1)
			result.msg = "Service '"+service_name+"' is running"
			result.status = result.STATUS_GOOD
		else:
			result.value = float(0)
			result.msg = "Service '"+service_name+"' is not running"
			result.status = result.STATUS_ERROR		
	except NSClient4JException, nse:
		result.status = result.STATUS_FAILED
		result.msg = str(nse)
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)