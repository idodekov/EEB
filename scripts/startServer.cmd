@echo off
setLocal EnableDelayedExpansion
SET APPLICATION_PATH=%~dp0

set CLASSPATH=

cd %APPLICATION_PATH:~0,-1%\lib\jars
for /r %%f in (*.jar) do set CLASSPATH=%%f;!CLASSPATH!
cd %APPLICATION_PATH:~0,-1%

set OPTS=

REM Uncomment to enable remote debugging
REM set OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5001,suspend=n,server=y %OPTS%

java.exe %OPTS% -classpath "%CLASSPATH%" org.redoubt.application.Application > logs/stdout.log 2>&1
EndLocal
