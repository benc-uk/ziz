import httplib
from java.lang import System
import re
from com.bencoleman.ziz import Timer
import urlparse 

def main():
	try:
		timer = Timer()
		
		resp = fetchURL(url)

		if(resp.status in [200, 201, 202, 203]):
			status = result.STATUS_GOOD
			content = resp.read()
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
		else:
			status = result.STATUS_ERROR
			content = resp.read()
			msg = 'HTTP '+str(resp.status)
			
		result.value = timer.stop()
		result.msg = msg
		result.status = status
		http.close()
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)
		
def fetchURL(url):
	global http
	if(not url.startswith("http://")):
		url = "http://"+url
		
	urlparts = urlparse.urlparse(url, "http")
	port = 80
	if(urlparts.port != None):
		port = urlparts.port
	
	http = httplib.HTTPConnection(urlparts.hostname, port)
	http.request("GET", urlparts.path)

	resp = http.getresponse()
	if(resp.status >= 300 and resp.status < 400):
		http.close()
		return fetchURL(resp.getheader("location"))
	
	return resp
		