from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# Get the cooked perf counter for the memory
		wmi.setPerfCounterRefreshDelay(2)
		data = wmi.perfCounterCooked("PerfOS_Memory", ("AvailableMBytes", "CommittedBytes", "PagesPerSec"))

		avail = float(data[0].get("AvailableMBytes"))
		commit = long(data[0].get("CommittedBytes"))
		commit = commit / 1048576
		pps = long(data[0].get("PagesPerSec"))
		result.value = avail
		result.status = result.STATUS_GOOD
		result.msg = "Memory commited:"+str(commit)+"Mb Pages/sec:"+str(pps)
		wmi.close()
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		