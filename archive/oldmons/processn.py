from org.nsclient4j import NSClient4j, NSClient4JException

def main():
	try:
		ns_port = 12489
		if("port" in globals()):
			ns_port = int(port)
			
		client = NSClient4j(hostname, ns_port)
		up = client.isProcessUp(process_name)
		client.close()
		if(not up):
			result.msg = "Process is not running"
			result.status = result.STATUS_ERROR	
			return
			
		if(process_name.endswith(".exe")):
			new_proc_name = process_name[:-4]
	
		client = NSClient4j(hostname, ns_port)
		proc_time = client.getPerfMonCounter("\\Process("+new_proc_name+")\\% Processor Time")
		client.close()
		
		result.value = float(proc_time)
		result.msg = "Process is running"
		result.status = result.STATUS_GOOD
	except NSClient4JException, nse:
		result.status = result.STATUS_FAILED
		result.msg = str(nse)
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)