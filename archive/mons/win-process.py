import mon_utils, re

def main():
	try:
		rc, out, err = mon_utils.runWinCommand("monitors\\pslist \\\\"+hostname+" "+process+" -accepteula")
		
		if(rc != 0):
			if(out.find("was not found")):
				result.msg = "Process "+process+" is not running"
				result.status = result.STATUS_ERROR
			else:
				result.msg = mon_utils.flattenString(out)
				result.status = result.STATUS_FAILED
		else:
			match = re.search("\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)", out)
			result.value = float(match.group(5)) / 1024
			result.status = result.STATUS_GOOD
			result.msg = "Process is running, Hnd:"+match.group(4)+" Thd:"+match.group(3)
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)