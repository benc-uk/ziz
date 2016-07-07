from httplib2 import *
from java.lang import System
import re
from com.bencoleman.ziz import Timer

def main():
	http = Http()
	try:
		timer = Timer()
		resp, content = http.request(str(url), headers={'cache-control':'no-cache'})
		if(resp.status in [200, 201, 202, 203, 304]):

			status = result.STATUS_GOOD
			msg = 'HTTP '+str(resp.status)
			if("check" in globals()):
				m = re.search(check, content)
				if(m == None):
					status = result.STATUS_ERROR
					msg = 'Text check not found on page'

			if("not_check" in globals()):
				m = re.search(not_check, content)
				if(m != None):
					status = result.STATUS_ERROR
					msg = 'Negative text check found on page'
					
			result.value = timer.stop()
			result.msg = msg
			result.status = status
			http.close()
	except Exception, e:
		http.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)
		

