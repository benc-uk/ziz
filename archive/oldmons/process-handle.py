from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# perform WMI query for the Win32_Process class 
		data = wmi.query("SELECT * FROM Win32_Process WHERE Name = \""+str(process)+"\"", ("Name", "HandleCount", "ProcessId"))
		if len(data) < 1:
			result.status = result.STATUS_ERROR
			result.msg = "Process is not running"
		else:
			result.status = result.STATUS_GOOD
			result.msg = "Process PID:"+str(data[0].get("ProcessId"))+" is running OK"
			result.value = float(data[0].get("HandleCount"))
			
		wmi.close()
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		