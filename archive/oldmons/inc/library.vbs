Const wbemImpersonationLevelImpersonate = 3
Dim WMIService

STATUS_GOOD = 10
STATUS_WARN = 5
STATUS_ERROR = 0
STATUS_FAILED = -3

'
'
'
Sub Init()
	hostname = LCase(GetArgument("hostname"))

	Set ws = WScript.CreateObject("WScript.Shell")
	localname = LCase(ws.ExpandEnvironmentStrings("%COMPUTERNAME%"))
		
	Set Locator = CreateObject("WbemScripting.SWbemLocator")

	If hostname = "localhost" OR hostname = "127.0.0.1" OR hostname = localname Then
		Set WMIService = Locator.ConnectServer(hostname, "root/CIMV2")
	Else
		username = GetArgument("username")
		password = GetArgument("password")
		Set WMIService = Locator.ConnectServer(hostname, "root/CIMV2", username, password)
	End If
	
  WMIService.Security_.ImpersonationLevel = wbemImpersonationLevelImpersonate
End Sub

'
'
'
Function GetArgument(arg)
	temp = WScript.Arguments.Named(arg)
  If(Len(temp) = 0) Then AbortScript("No " & arg & " specified")
  GetArgument = temp
End Function

Sub AbortScript(msg)
	WScript.Echo "!!ERROR!! " & msg
	WScript.Quit(1)
End Sub

'
'
'
Sub OutputResult(status, value, msg)
	WScript.Echo status & Chr(03) & value & Chr(03) & msg
End Sub


'
'
'
Function WMIDateTimeConvertSWbem(dt)
	Set dateTime = CreateObject("WbemScripting.SWbemDateTime")
	dateTime.Value = dt
	WMIDateTimeConvert = dateTime.Hours & ":" & dateTime.Minutes & ":" & dateTime.Seconds & " " & dateTime.Day & "/" & dateTime.Month & "/" & dateTime.Year
End Function

'
'
'
Function WMIDateTimeConvert(dt)
	yyyy = Mid(dt, 1, 4)
	mm = Mid(dt, 5, 2)
	dd = Mid(dt, 7, 2)
	hh = Mid(dt, 9, 2)
	mi = Mid(dt, 11, 2)
	ss = Mid(dt, 13, 2)
	WMIDateTimeConvert = hh &":"& mi &":"& ss &" "& dd &"/"& mm &"/"& yyyy
End Function