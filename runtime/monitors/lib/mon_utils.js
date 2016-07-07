/*
	File:		mon_utils.js
	Project:	Ziz
	Purpose:	Helper functions for monitors
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	
*/

function getCollector()
{
	importClass(com.bencoleman.ziz.Main);
	return Main.collector;
}

function getConfig()
{
	importClass(com.bencoleman.ziz.Main);
	return Main.config;
}

Array.prototype.avg = function() 
{
	var av = 0;
	var cnt = 0;
	var len = this.length;
	for (var i = 0; i < len; i++) {
		var e = +this[i];
		if(!e && this[i] !== 0 && this[i] !== '0') e--;
		if (this[i] == e) {av += e; cnt++;}
	}
	return av/cnt;
}

function isNumeric(input)
{
   return (input - 0) == input && input.length > 0;
}


function unique(a)
{
   var r = new Array();
   o:for(var i = 0, n = a.length; i < n; i++)
   {
      for(var x = 0, y = r.length; x < y; x++)
      {
         if(r[x]==a[i]) continue o;
      }
      r[r.length] = a[i];
   }
   return r;
}

function wmiDateToReadable(dt)
{
	yyyy = dt.substring(0, 4);
	mm = dt.substring(4, 6);
	dd = dt.substring(6, 8);
	hh = dt.substring(8, 10);
	ii = dt.substring(10, 12);

	return dd+"/"+mm+"/"+yyyy+" "+hh+":"+ii;
}

function savePersist(name, value)
{
	importPackage(java.io);
	try {	
		out = BufferedWriter(FileWriter("persist/"+monitor.getName()+"_"+monitor.getId()+"_"+name+".dat"));
		out.write(""+value);
		out.close();	
	} catch (err) {}
}

function getPersist(name)
{
	importPackage(java.io);
	try {
		infile = BufferedReader(FileReader("persist/"+monitor.getName()+"_"+monitor.getId()+"_"+name+".dat"));
		return infile.readLine();
		infile.close();
	} catch(err) {
		return null;
	}
}