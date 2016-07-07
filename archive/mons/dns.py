from java.util import Hashtable
from javax.naming.directory import *
from javax.naming import *
from com.bencoleman.ziz import Timer

def main():
	try:
		record = "A"
		if("record_type" in globals()):
			record = str(record_type)
			
		timer = Timer()
		env = Hashtable()		
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
		env.put("java.naming.provider.url", "dns://" + dns_server)
		ictx = InitialDirContext(env)	
		attrs = ictx.getAttributes(hostname, [record])
		attr = attrs.get(record)
		result.value = timer.stop()
		result.status = result.STATUS_GOOD
		result.msg = "Query result: " + attr.toString()
		ictx.close()
	except NamingException, e:
		ictx.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e.getMessage())
	except Exception, e:
		ictx.close()
		result.status = result.STATUS_FAILED
		result.msg = str(e)
			