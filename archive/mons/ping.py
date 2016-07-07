from java.net import *
from org.shortpasta.icmp import IcmpUtil, IcmpPingResponse
import mon_utils

#
#	Main monitor entry point
#	
def main():

	try:		
		ina = InetAddress.getByName(dest)
		ip = ina.getHostAddress()
			
		ping_packet = 32
		if("packet_size" in globals()):
			ping_packet = int(packet_size)
			
		ping_count = 1
		if("count" in globals()):
			ping_count = int(count)
			
		ping_timeout = 1000
		if("timeout" in globals()):
			ping_timeout = int(timeout)
			
		times = []
		errors = []

		for i in range(0, ping_count):
			resp = IcmpUtil.executeIcmpPingRequest(ip, ping_packet, ping_timeout);
			#print " ### t=",resp.getDelay()," ",resp.getTimedOutFlag()
			
			tof = 0.0
			if(resp.getTimedOutFlag()):
				tof = 1.0
			else:
				times.append(float(resp.getDelay()))
				
			errors.append(tof)

		if(len(times) > 0):
			time = mon_utils.average(times)
		else:
			time = 0.0
		error = mon_utils.average(errors)
				
		if(error == 1.0):
			result.status = result.STATUS_ERROR
			result.msg = 'All pings timed out'
		elif(error > 0.0 and error < 1.0):
			result.status = result.STATUS_WARN
			result.msg = str(int(error*100)) + "% of packets timed out"
			result.value = time
		else:
			result.status = result.STATUS_GOOD
			result.msg = 'Ping OK, 100% packets good'	
			result.value = time
	except UnknownHostException, ue:
		result.status = result.STATUS_FAILED
		result.msg = "Unable to resolve name: "+dest
	except Exception, e:
		result.status = result.STATUS_FAILED
		result.msg = str(e)
		