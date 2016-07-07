from org.nsclient4j import NSClient4j, NSClient4JException
from java.lang import NumberFormatException

def main():
	try:
		ns_port = 12489
		if("port" in globals()):
			ns_port = int(port)
			
		client = NSClient4j(hostname, ns_port)
		disk_val = client.getFreePercentDiskSpace(str(drive))
		client.close()
		
		result.value = float(disk_val)
		result.msg = "Disk OK"
		result.status = result.STATUS_GOOD
	except NumberFormatException, nfe:
		result.status = result.STATUS_FAILED
		result.msg = str(nfe)		
	except NSClient4JException, nse:
		result.status = result.STATUS_FAILED
		result.msg = str(nse)
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)