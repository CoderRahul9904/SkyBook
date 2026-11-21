@echo off
setlocal
call "%~dp0mvnw.cmd" %*
exit /b %errorlevel%

