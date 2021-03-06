from org.nsclient4j import NSClient4j, NSClient4JException

def main():
	try:
		ns_port = 12489
		if("port" in globals()):
			ns_port = int(port)
			
		client = NSClient4j(hostname, ns_port)
		cpu_val = client.getCPUUsage().split('&')[0]
		client.close()
		
		result.value = float(cpu_val)
		result.msg = "CPU OK"
		result.status = result.STATUS_GOOD
	except NSClient4JException, nse:
		result.status = result.STATUS_FAILED
		result.msg = str(nse)
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)