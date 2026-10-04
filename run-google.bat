@echo off
setlocal
cd /d "%~dp0"
if exist ".env" for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"
if not defined GOOGLE_CLIENT_ID (
 echo Set GOOGLE_CLIENT_ID in this Command Prompt first. See README.md.
 pause
 exit /b 1
)
if not defined GOOGLE_CLIENT_SECRET (
 echo Set GOOGLE_CLIENT_SECRET in this Command Prompt first. See README.md.
 pause
 exit /b 1
)
call run.bat --spring.profiles.active=google %*
