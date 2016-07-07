import mon_utils, re

def main():
	try:
		rc, out, err = mon_utils.runWinCommand("sc \\\\"+hostname+" query "+service)
		
		if(rc != 0):
			result.msg = mon_utils.flattenString(out)
			result.status = result.STATUS_FAILED
		else:
			match = re.search("STATE\s+:\s+\d\s+(\w+)", out)
			state = match.group(1)
			result.msg = "Service "+service+" is "+state
			if(state != "RUNNING"):
				result.status = result.STATUS_ERROR
				result.value = 0.0
			else:
				result.status = result.STATUS_GOOD
				result.value = 1.0
			
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)