@echo off
setlocal
cd /d "%~dp0"
if not defined DB_USERNAME (
 echo Set DB_USERNAME and DB_PASSWORD first. See README.md.
 pause
 exit /b 1
)
call run.bat --spring.profiles.active=mysql %*
