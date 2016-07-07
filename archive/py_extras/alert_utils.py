import smtplib
from email.MIMEText import MIMEText
from email.MIMEMultipart import MIMEMultipart
from com.bencoleman.ziz import Utils
from com.bencoleman.ziz import Main

class Emailer:
	def __init__(self, mon, res, cfg):
		self.monitor = mon
		self.result = res
		self.config = cfg
		
	def send(self, to):
		body = "<html><head></head><body style='color: black; font-family: Arial'><h3>This is an alert notification from Ziz</h3>"
		body += "Monitor '"+self.monitor.getName()+"' is in "+self.result.statusName()+" status.<br/><br/><u>Details</u><br/>"
		body += "<b>Name: </b>"+self.monitor.getName()+"<br/>"
		body += "<b>Type: </b>"+self.monitor.getType()+"<br/>"
		body += "<b>Status: </b><span style='color:"+self.result.statusColour()+"'>"+self.result.statusName()+" ("+str(self.result.status)+")</span><br/>"
		body += "<b>Time: </b>"+self.result.dt.toString()+"<br/>"
		body += "<b>Value: </b>"+str(self.result.value)+"<br/>"
		body += "<b>Message: </b>"+self.result.msg+"<br/>"
		body += "<b>Properties: </b>"+Utils.flattenProperties(self.monitor.getProperties())+"<br/><br/>"
		body += "For more details please go to the Ziz dashboard: "+contructURL()+"<br/>"
		body += "This is an automated email, please do not reply.</body></html>"
		msg = MIMEMultipart('alternative')
		
		msg['Subject'] = 'Ziz alert - monitor: '+self.monitor.toString()+" - "+self.result.statusName()
		msg['From'] = self.config.getProperty("alert_email_from_name")
		msg['Reply-to'] = self.config.getProperty("alert_email_from_name")
		msg['To'] = to
		part1 = MIMEText("Nothing", 'plain')
		part2 = MIMEText(body, 'html')
		msg.attach(part1)
		msg.attach(part2)
		
		try:
			Main.log.info("Sending email alert to: "+to)
			smtp = smtplib.SMTP()
			smtp.connect(self.config.getProperty("alert_email_server"))
			if(self.config.containsKey("alert_email_user")):
				smtp.login(self.config.getProperty("alert_email_user"), self.config.getProperty("alert_email_password"))
			smtp.sendmail(self.config.getProperty("alert_email_from_address"), to, msg.as_string())
			smtp.close()
		except Exception, e:
			Main.log.error("Unable to send email alert: "+str(e))
			
	def createHtmlMail(self, html, text, subject):
		import MimeWriter
		import mimetools
		import cStringIO
		
		out = cStringIO.StringIO() # output buffer for our message 
		htmlin = cStringIO.StringIO(html)
		txtin = cStringIO.StringIO(text)
		
		writer = MimeWriter.MimeWriter(out)
		#
		# set up some basic headers... we put subject here
		# because smtplib.sendmail expects it to be in the
		# message body
		#
		writer.addheader("Subject", subject)
		writer.addheader("MIME-Version", "1.0")
		#
		# start the multipart section of the message
		# multipart/alternative seems to work better
		# on some MUAs than multipart/mixed
		#
		writer.startmultipartbody("alternative")
		writer.flushheaders()
		#
		# the plain text section
		#
		subpart = writer.nextpart()
		subpart.addheader("Content-Transfer-Encoding", "quoted-printable")
		pout = subpart.startbody("text/plain", [("charset", 'us-ascii')])
		mimetools.encode(txtin, pout, 'quoted-printable')
		txtin.close()
		#
		# start the html subpart of the message
		#
		subpart = writer.nextpart()
		subpart.addheader("Content-Transfer-Encoding", "quoted-printable")
		#
		# returns us a file-ish object we can write to
		#
		pout = subpart.startbody("text/html", [("charset", 'us-ascii')])
		mimetools.encode(htmlin, pout, 'quoted-printable')
		htmlin.close()
		#
		# Now that we're done, close our writer and
		# return the message body
		#
		writer.lastpart()
		msg = out.getvalue()
		out.close()
		print msg
		return msg
		
def contructURL():
	return "http://home.bencoleman.co.uk:7777/"
	