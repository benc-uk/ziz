function save() { 
	document.forms[0].elements['action'].value = 'save'; 
	document.forms[0].elements['xml'].value = editor.getCode(); 
	document.forms[0].submit(); 
}

function saveAndReload() { 
	if(confirm("Warning!\n\nThis will reload your monitors and reset their status until their next scheduled run.\nNo data will be lost, but short term history and graphs will reset.")) {
		document.location.href = "/admin?action=reload";

		document.forms[0].elements['action'].value = 'savereload'; 
		document.forms[0].elements['xml'].value = editor.getCode(); 
		document.forms[0].submit(); 
	}
}

function insertGroupShow()
{
    div = document.getElementById('group_div');
	document.getElementById('group_name').value = "";
    if(div.className == 'dialog_shown') {
        div.className = 'dialog_hidden';
    } else {
        div.className = 'dialog_shown';
    }
	document.getElementById('group_name').focus();
}

function insertGroup()
{
	var cur_pos = editor.cursorPosition(); 
	var xml = "<group name=\""+document.getElementById('group_name').value+"\">\n\n</group>\n\n";
	editor.insertIntoLine(cur_pos.line, cur_pos.character, xml);
	editor.reindent();
	editor.selectLines(editor.nextLine(cur_pos.line), 0);
	
	div = document.getElementById('group_div');
	div.className = 'dialog_hidden';
}

function insertMonitorShow()
{
    div = document.getElementById('mon_div');
	document.getElementById('mon_name').value = "";
    if(div.className == 'dialog_shown') {
        div.className = 'dialog_hidden';
    } else {
        div.className = 'dialog_shown';
		
    }
	document.getElementById('mon_name').focus();
}

function insertMonitor()
{
	var cur_pos = editor.cursorPosition(); 
	var xml = "<monitor name=\""+document.getElementById('mon_name').value+"\" type=\""+document.getElementById('mon_type').value+"\" interval=\""+document.getElementById('mon_int').value+"\">\n\n</monitor>\n";
	editor.insertIntoLine(cur_pos.line, cur_pos.character, xml);
	editor.reindent();
	editor.selectLines(editor.nextLine(cur_pos.line), 0);
	div = document.getElementById('mon_div');
	div.className = 'dialog_hidden';
}

function insertServerShow()
{
    div = document.getElementById('server_div');
	document.getElementById('server_name').value = "";
	document.getElementById('server_host').value = "";
	document.getElementById('server_user').value = "";
	document.getElementById('server_pass').value = "";
    if(div.className == 'dialog_shown') {
        div.className = 'dialog_hidden';
    } else {
        div.className = 'dialog_shown';
		
    }
	document.getElementById('server_name').focus();
}

function insertServer()
{
	pass = document.getElementById('server_pass').value;
	
	// make a AJAX call to encrypt the password
	xmlhttp = new XMLHttpRequest();
	xmlhttp.open("GET", "http://localhost:7777/encrypt?password="+pass,false);
	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState==4) {
			encoded = xmlhttp.responseText.replace(/^\s*/, "").replace(/\s*$/, "");;
			document.getElementById('server_pass').value = encoded;
		}
	}
	xmlhttp.send(null);
 
	var cur_pos = editor.cursorPosition(); 
	var xml = "<server name=\""+document.getElementById('server_name').value+"\">\n<hostname>"+document.getElementById('server_host').value+"</hostname>\n<username>"+document.getElementById('server_user').value+"</username>\n<password>"+document.getElementById('server_pass').value+"</password>\n</server>\n\n";
	editor.insertIntoLine(cur_pos.line, cur_pos.character, xml);
	editor.reindent();
	editor.selectLines(editor.nthLine(editor.lineNumber(cur_pos.line)+5), 0);
	
	div = document.getElementById('server_div');
	div.className = 'dialog_hidden';
}

function insertPropShow()
{
    div = document.getElementById('prop_div');
	document.getElementById('prop_name').value = "";
	document.getElementById('prop_value').value = "";
    if(div.className == 'dialog_shown') {
        div.className = 'dialog_hidden';
    } else {
        div.className = 'dialog_shown';
		
    }
	document.getElementById('prop_name').focus();
}

function insertProp()
{
	var cur_pos = editor.cursorPosition(); 
	var xml = "<"+document.getElementById('prop_name').value+">"+document.getElementById('prop_value').value+"</"+document.getElementById('prop_name').value+">\n";
	editor.insertIntoLine(cur_pos.line, cur_pos.character, xml);
	editor.reindent();
	editor.selectLines(editor.nextLine(cur_pos.line), 0);
	
	div = document.getElementById('prop_div');
	div.className = 'dialog_hidden';
}