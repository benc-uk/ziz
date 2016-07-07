var notif = Array();
var popup_delay = 14000;
var sound = true;

function dummyfunc()
{
	if(window.webkitNotifications.checkPermission() == 2)
		document.getElementById('message').innerHTML = "Notifications have been disabled"
	else
		document.getElementById('message').innerHTML = "Notifications have been permitted"
}	

function enableNotification()
{
	window.webkitNotifications.requestPermission(dummyfunc);
}

function showNotification(ts, status, title, msg)
{
	if (window.webkitNotifications.checkPermission() == 0) {
		la = getCookie("last_alert");
		if(la.length > 0)
			last_alert = parseInt(la);
		else
			last_alert = 0;
			
		if(ts > last_alert) {
			//alert("ts="+ts+"  last_alert="+last_alert);
			setCookie("last_alert", ts, false);
			if(sound) {
				var audio = new Audio("/alert.mp3");
				audio.play();
			}
			setInterval("notif[\""+title+"\"].cancel()", popup_delay);
			notif[title] = window.webkitNotifications.createNotification('/img/3/'+status+'.png', title, msg);
			notif[title].show(); 
		}
			
	} else if (window.webkitNotifications.checkPermission() == 1) {
		document.getElementById('message').innerHTML = "To enable desktop notifcations please <a style='color:red;font-weight:bold' href='#' onclick='enableNotification()'>click here</a>";
	}
}				
			
function popup_details(e, mon_name)
{
    mondiv = document.getElementById(mon_name);
    if(mondiv.className == 'mondetails_shown') {
        mondiv.className = 'mondetails_hidden';
    } else {
		if (!e) var e = window.event;

		var win_top = 0;
		if(typeof(window.pageYOffset) == 'undefined') {
			// IE CODE
			win_top = document.body.scrollTop;

		} else {
			win_top = window.pageYOffset;
		}

        mondiv.style.left = (document.body.clientWidth / 2) - 200;
		mondiv.style.top = (win_top + e.clientY) + "px";
		mondiv.className = 'mondetails_shown';
		
		var hi = mondiv.clientHeight;
		if(e.clientY + hi > document.body.clientHeight) {
			mondiv.style.top = document.body.clientHeight - hi - 10;
		}

		if((e.pageY  + hi) > getDocHeight()) {
			mondiv.style.top = (getDocHeight() - hi - 10);
		}
		/*if(!document.getElementsByClassName) {	
			mondiv.style.width = document.body.clientWidth - 100;
		}*/
    }
}

function hide_all_popups()
{
    if(document.getElementsByClassName){
		alldivs = document.getElementsByClassName('mondetails_shown');
		for (var i = 0; i < alldivs.length; ++i) {
			var item = alldivs[i];
			item.className = 'mondetails_hidden';
		}
	} else {
		i = 0;
		a = document.getElementsByTagName("div");
		while (element = a[i++]) {
			if (element.className == "mondetails_shown") {
				element.className = 'mondetails_hidden';
			}
		}
	}
}

function popupDash() {
	window.open("/dash", "ziz_dash", "status=no, width=500, height=400, " +
				"location=no, resizable=yes, menubar=no, toolbar=no, scrollbars=yes")
}

/*
function setCookie(c_name,value,expiredays)
{
var exdate=new Date();
exdate.setDate(exdate.getDate()+expiredays);
document.cookie=c_name+ "=" +escape(value)+
((expiredays==null) ? "" : ";expires="+exdate.toUTCString());
}
*/
function setCookie( name, value, expires, path, domain, secure )
{
	// set time, it's in milliseconds
	var today = new Date();
	today.setTime( today.getTime() );

	/*
	if the expires variable is set, make the correct expires time, the current script below will set
	it for x number of days, to make it for hours, delete * 24, for minutes, delete * 60 * 24
	*/
	if ( expires )
	{
	expires = expires * 1000 * 60 * 60 * 24;
	}
	var expires_date = new Date( today.getTime() + (expires) );

	document.cookie = name + "=" +escape( value ) +
	( ( expires ) ? ";expires=" + expires_date.toGMTString() : "" ) +
	( ( path ) ? ";path=" + path : "" ) +
	( ( domain ) ? ";domain=" + domain : "" ) +
	( ( secure ) ? ";secure" : "" );
}


function getCookie(c_name)
{
	if (document.cookie.length>0)
	  {
	  c_start=document.cookie.indexOf(c_name + "=");
	  if (c_start!=-1)
		{
		c_start=c_start + c_name.length+1;
		c_end=document.cookie.indexOf(";",c_start);
		if (c_end==-1) c_end=document.cookie.length;
		return unescape(document.cookie.substring(c_start,c_end));
		}
	  }
	return "";
}

function warnReload()
{
	if(confirm("Warning!\n\nThis will reload your monitors and reset their status until their next scheduled run.\nNo data will be lost, but short term history and graphs will reset.")) {
		document.location.href = "/admin?action=reload";
	} else {
		return
	}
}

function getDocHeight() {
    var D = document;
    return Math.max(
        Math.max(D.body.scrollHeight, D.documentElement.scrollHeight),
        Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),
        Math.max(D.body.clientHeight, D.documentElement.clientHeight)
    );
}

function changeDate(el_id, val)
{
	ele = mondiv = document.getElementById(el_id);
	d = new Date();
	d.setFullYear(ele.value.split("-")[0]);
	d.setMonth((ele.value.split("-")[1])-1);
	d.setDate((ele.value.split("-")[2]));
	d.setDate(d.getDate() + val);
	
	ele.value = d.getFullYear()+'-'+(d.getMonth()+1)+'-'+d.getDate();
}
