from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# perform WMI query for the Win32_Service class 
		data = wmi.query("SELECT * FROM Win32_Service WHERE Name = \""+str(service)+"\"", ("State", "Name", "Caption"))
		if len(data) < 1:
			result.status = result.STATUS_FAILED
			result.msg = "Service was not found"
		else:
			state = str(data[0].get("State"))
			result.msg = str(data[0].get("Caption"))+" is "+str(data[0].get("State"))
			if str(data[0].get("State")) == "Running":
				result.status = result.STATUS_GOOD
				result.value = 1
			else:
				result.status = result.STATUS_ERROR
				result.value = 0
				
		wmi.close()
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		