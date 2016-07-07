from com.bencoleman.ziz import WMIHelper
from org.jawin import COMException

def main():
	try:
		# Create WMI helper object and try to connect to remote host
		wmi = WMIHelper()
		wmi.connect(server, user, password)

		# perform WMI query 
		data = wmi.query(query, [property])
		if len(data) < 1:
			result.status = result.STATUS_ERROR
			result.msg = "Query returned no results"
		else:
			result.status = result.STATUS_GOOD
			result.msg = "Query ran OK"
			result.value = float(data[0].get(property))
			
		wmi.close()
	except COMException, ce:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(ce)
	except Exception, e:
		wmi.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)		