from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# Get the cooked perf counter for the processor
		wmi.setPerfCounterRefreshDelay(2)
		data = wmi.perfCounterCooked("PerfOS_Processor", ("PercentProcessorTime", "Name"))
		
		cpu = -1
		# loop through the data, there could be many CPU instances on the host
		for instance in data:
			# We're only interested in the instance called '_Total'
			name = instance.get("Name")
			if name == "_Total":
				# Set metric values
				cpu = int(instance.get("PercentProcessorTime"))
				
		wmi.close()
		result.value = cpu
		result.status = result.STATUS_GOOD
		result.msg = "CPU Status OK"
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		