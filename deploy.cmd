sc \\zeno stop zizsvc

sleep 3

xcopy runtime\web\* T:\Servers\Ziz\web\* /E /Y /H /R /Q
xcopy runtime\monitors\* T:\Servers\Ziz\monitors\* /E /Y /H /R /Q
xcopy runtime\alert\* T:\Servers\Ziz\alert\* /E /Y /H /R /Q
xcopy runtime\bin\* T:\Servers\Ziz\bin\* /E /Y /H /R /Q
xcopy runtime\docs\* T:\Servers\Ziz\docs\* /E /Y /H /R /Q
xcopy dist\lib\* T:\Servers\Ziz\lib\* /E /Y /H /R /Q

copy /Y dist\Ziz.jar T:\servers\ziz\

sc \\zeno start zizsvc
