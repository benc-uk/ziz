from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# Get the cooked perf counter for the memory
		wmi.setPerfCounterRefreshDelay(2)
		data = wmi.perfCounterCooked("PerfDisk_LogicalDisk", ("Name", "FreeMegabytes", "PercentFreeSpace"))

		instance_found = False
		for instance in data:
			if instance.get("Name") == drive:	# we only want the instance that matches our parameter
				instance_found = True
				free_mb = int(instance.get("FreeMegabytes"))
				free_gb = free_mb / 1024
				result.value = long(instance.get("PercentFreeSpace"))
				result.msg = "Free Space: "+str(free_gb)+"Gb"
				result.status = result.STATUS_GOOD
		
		if not instance_found:
			result.msg = "Specified disk '"+drive+"' not found"
			result.status = result.STATUS_FAILED
			
		wmi.close()
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		