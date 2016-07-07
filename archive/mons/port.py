from java.lang import System
import socket

def main():
	try:
		t1 = System.currentTimeMillis();
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		s.connect((str(dest), int(port)))
		t2 = System.currentTimeMillis();
		t = t2 - t1
		s.close()
		
		result.value = t
		result.msg = "Port open, service available"
		result.status = result.STATUS_GOOD
	except Exception, e:
		result.status = result.STATUS_ERROR
		result.msg = "Unable to connect to port"
		result.value = float('nan')
		

