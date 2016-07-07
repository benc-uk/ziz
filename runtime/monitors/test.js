/*
	File:		test.js
	Project:	Ziz
	Purpose:	Test monitor, for umm... testing.
	Author:		Ben Coleman
	Created:	05-04-2010
	Updated:	05-04-2010
	Notes:		
	License:	Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
	
	Required Parameters:
	error		-	int; 0-100 percentage of time this monitor will return in ERROR status
	min			-	int; Min for the random return value
	max			-	int; Max for the random return value
	Optional Parameters:
	None
	Output:		
	Random value between min & max
*/


/*
	Main monitoring function, all monitor scripts need to implement this
*/
function main()
{
	min_i = parseInt(min);
	max_i = parseInt(max);
	error_i = parseInt(error);
	
	var rand_num = randomRange(min_i, max_i);
	var rand_err = randomRange(0, 100);
	result.value = rand_num;
	if(rand_err <= error_i) {
		result.status = result.STATUS_ERROR;
		result.msg = "This is a dummy monitor, it is in error";
	} else {
		result.status = result.STATUS_GOOD;
		result.msg = "This is a dummy monitor, it is good";
	}
}

function randomRange(minVal, maxVal, floatVal)
{
  var randVal = minVal+(Math.random()*(maxVal-minVal));
  return typeof floatVal=='undefined'?Math.round(randVal):randVal.toFixed(floatVal);
}