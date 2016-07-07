import mon_utils

def main():
	try:
		r = mon_utils.fetchPerfCounter("\Processor(_Total)\% Processor Time", hostname)
		
		if(r.status == r.STATUS_FAILED):
			result.msg = r.msg
			result.status = result.STATUS_FAILED
		else:
			result.msg = "CPU perf counter fetched"
			result.status = r.STATUS_GOOD
			result.value = r.value
			
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)