function Alerter(m, r, c)
{
	this.monitor = m;
	this.result = r;
	this.config = c;
}

Alerter.prototype.getEmailSubject = function()
{
	var grp_name = "";
	var grp = monitor.getParentGroup();
	if(grp != null) 
		grp_name = " ("+grp.getName()+")";
	subj = "Ziz Alert for "+monitor.getName() + grp_name;
	
	return subj;
}

Alerter.prototype.getEmailBody = function(server_url)
{
	importClass(com.bencoleman.ziz.utils.Utils);
	
	body = "<body style='background: #232323; color:white; padding: 20px'>"
	body += "<h2 style='color:"+result.statusColour()+"'>Alert: "+monitor.getName()+" is in "+result.statusName()+" status</h2>";
	body += "<table>";
	body += "<tr><td><b>Name:</b></td><td>"+monitor.getName()+"</td></tr>";
	body += "<tr><td><b>Status:</b></td><td>"+result.statusName()+"</td></tr>";
	body += "<tr><td><b>Value:</b></td><td>"+result.value+"</td></tr>";
	body += "<tr><td><b>Message:</b></td><td>"+result.msg+"</td></tr>";
	body += "<tr><td><b>Occured:</b></td><td>"+result.dt+"</td></tr>";
	if(monitor.getParentGroup() != null)
		body += "<tr><td><b>Group:</b></td><td>"+monitor.getParentGroup().getName()+"</td></tr>";
	body += "<tr><td><b>Type:</b></td><td>"+monitor.getType()+"</td></tr>";
	body += "<tr><td><b>Frequency:</b></td><td>"+monitor.getIntervalMins()+" mins</td></tr>";
	body += "<tr><td><b>Properties:</b></td><td>"+Utils.flattenProperties(monitor.getProperties())+"</td></tr>";
	body += "</table>"
	body += "<br/><br/>For more details, please <a href='"+server_url+"/dash'>click here to view the Ziz monitor dashboard</a><br/>This is an automated email, please do not reply to it</body>";
	
	return body;
}

Alerter.prototype.send = function(to_addr)
{
	importClass(java.net.InetAddress);
	importClass(java.lang.System);
	importClass(com.bencoleman.ziz.Main);
	//importClass(com.bencoleman.ziz.utils.EmailAuth);
	importPackage(javax.mail.internet);
	importPackage(javax.mail);
	
	try {
		port = 25
		if(config.containsKey("alert_email_port")) 
			port = config.getInt("alert_email_port");
		
		Main.log.info("Sending email to "+to_addr+" via "+config.getStr("alert_email_server"));
		props = System.getProperties();
		
		prot = "smtp";
		if(config.containsKey("alert_email_ssl") && config.getStr("alert_email_ssl").toLowerCase() == "true") {
			props.put("mail."+prot+".socketFactory.port", port);
			props.put("mail."+prot+".starttls.enable", "true");
			props.put("mail."+prot+".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail."+prot+".socketFactory.fallback", "false"); 
			prot = "smtps";
		}

		user = ""; password = "";
		if(config.containsKey("alert_email_user")) {
			user = config.getStr("alert_email_user");
			password = config.getPassword("alert_email_password");
			props.put("mail."+prot+".auth", "true");
		}
		props.put("mail.transport.protocol", prot);
		props.put("mail."+prot+".localhost", "home.bencoleman.co.uk" );
		props.put("mail."+prot+".host", config.getStr("alert_email_server"));
		
		session = Session.getInstance(props);
		
		msg = MimeMessage(session);
		msg.setFrom(InternetAddress(config.getStr("alert_email_from_address"), config.getStr("alert_email_from_name")));
		msg.setRecipients(Message.RecipientType.TO, to_addr);
		msg.setSubject(this.getEmailSubject());
		
		body = this.getEmailBody("http://"+InetAddress.getLocalHost().getHostName()+":7777");
		msg.setContent(body, "text/html");
		
		t = session.getTransport(prot);
		t.connect(config.getStr("alert_email_server"), user, password);
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();

	} catch(err) {
		Main.log.error("Error sending email: "+err);
	}
}
