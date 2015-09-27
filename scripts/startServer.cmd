@echo off
setLocal EnableDelayedExpansion

set CLASSPATH=

cd lib\jars

for /r %%f in (*.jar) do set CLASSPATH=%%f;!CLASSPATH!

set OPTS=

REM Uncomment to enable remote debugging
REM set OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5001,suspend=n,server=y %OPTS%

"%JAVA_HOME%\bin\java.exe" %OPTS% -classpath "%CLASSPATH%" org.redoubt.application.Application %1 %2 %3 %4 %5 %6 %7 %8 %9
EndLocal
