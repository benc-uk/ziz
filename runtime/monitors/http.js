/*
	File:		http.js
	Project:	Ziz
	Purpose:	Generic HTTP URL monitor
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	14-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	url			-	string; URL to monitor, including 
	Optional Parameters:
	timeout		-	int; Timeout for HTTP connection in millisec (default 30 secs)
	post		-	string; HTTP POST data to send
	check		-	string; Text content check expected on the page, a regular expression
	not_check	-	string; Text content check NOT expected on the page, a regular expression
	username	-	string; username if using basic auth
	password	-	string; password if using basic auth
	resources	-	bool; If true then images and other HTML resources will be downloaded
	user_agent	-	string; User-Agent you wish to simulate
	Output:		
	Time taken for HTTP connection and response to be downloaded in milliseconds
	(Note. HTML is not parsed and images and other resources *not* fetched)
*/

// Java imports
importPackage(com.bencoleman.ziz.utils);
importPackage(java.net);
importPackage(java.io);
importClass(java.util.regex.Pattern);

// Set the units returned by this monitor
units = "ms";

/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	try {
		// Set up base HttpURLConnection object from URL
		url_obj = URL(url);

		// Proxy ?
		if(getConfig().containsKey("mon_http_proxy")) {
			proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(getConfig().getStr("mon_http_proxy"), getConfig().getInt("mon_http_proxy_port")));
			http = url_obj.openConnection(proxy);
			if(getConfig().containsKey("mon_http_proxy_user")) {
				proxy_auth = Base64Coder.encodeString(getConfig().getStr("mon_http_proxy_user")+":"+getConfig().getPassword("mon_http_proxy_password"));
				http.setRequestProperty("Proxy-Authorization", "Basic " + proxy_auth);			
			}
		} else {
			http = url_obj.openConnection();
		}
		
		// base settings for HTTP connection
		http.setRequestMethod("GET");
		if(typeof(user_agent) !== 'undefined')
			http.setRequestProperty("User-Agent", user_agent);
		http.setUseCaches(false); 
		
		// Deal with timeout param
		http_timeout = 30000;
		if(typeof(timeout) !== 'undefined')
			http_timeout = timeout;
		http.setReadTimeout(http_timeout);
		
		// Deal with POST data (if post param is present)
		if(typeof(post) !== 'undefined') {
			http.setRequestMethod("POST");
			http.setDoOutput(true);
		}
		
		// Deal with basic auth (if username param is present)
		if(typeof(username) !== 'undefined') {
			encoded_pair = Base64Coder.encodeString(username+":"+password);
			http.setRequestProperty("Authorization", "Basic " + encoded_pair);
		}
			
		// Start timer and connect
		timer = Timer()
		http.connect();
		if(http.getResponseCode() < 200 || http.getResponseCode() >= 300) {
			result.status = result.STATUS_ERROR;
			result.msg = "HTTP "+http.getResponseCode()+" "+http.getResponseMessage();
			return;
		}
		
		// Send post data as raw bytes (if supplied)
		if(typeof(post) !== 'undefined') {
			writer = DataOutputStream(http.getOutputStream());
			writer.writeBytes(post);
			writer.flush();
			writer.close();
		}
		
		// Read the HTTP response, into out string
		reader = BufferedReader(InputStreamReader(http.getInputStream()));
		data = ""
		while ((line = reader.readLine()) != null) {
			data += line+"\n";
		}
		//println(data);
		
		// this is basic resource and image parsing and downloading
		bytes_downloaded = 0;
		if(typeof(resources) !== 'undefined' && resources == 'true') {
			downloader = HttpResourceDownloader(url_obj, 6);
			bytes_downloaded = downloader.downloadAll(data);
		}
		
		// Stop timer and disconnect
		result.value = timer.stop();
		http.disconnect();
		reader.close();
		
		append = "";
		// Positive and negative text checks in output
		check_ok = true;
		if(typeof(check) !== 'undefined') {
			match = data.match(RegExp(check))
			if(match == null) {
				check_ok = false;
			} else if(match.length > 1) {
				if(isNumeric(match[1])) {
					result.value = parseFloat(match[1]);
					units = "";
				} else {
					append = " content: "+match[1];	
				}
			}
		}
		if(typeof(not_check) !== 'undefined') {
			if(data.match(RegExp(not_check)) != null) {
				check_ok = false;
			}
		}			

		// Done! set result object
		if(check_ok) {
			result.msg = "HTTP "+http.getResponseCode()+" "+http.getResponseMessage()+" ("+Math.round((data.length+bytes_downloaded)/1024)+" Kb)"+append;
			result.status = result.STATUS_GOOD;
		} else {
			result.msg = "Text check failed";
			result.status = result.STATUS_ERROR;
		}	

	} catch (err) {	
		// report socket timeouts as error, not failed	
	    if (err.javaException instanceof java.net.SocketTimeoutException) {
			result.msg = "HTTP connection timed out"
			result.status = result.STATUS_ERROR;
			return;
		}
		// Catch all exceptions and fail the result
		result.msg = ""+err;
		result.status = result.STATUS_FAILED;
	}
}

