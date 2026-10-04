@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if errorlevel 1 (
 echo Install Maven and JDK 17+, then reopen Command Prompt. See README.md.
 pause
 exit /b 1
)
call mvn clean verify
if errorlevel 1 exit /b 1
if not exist dist mkdir dist
copy /Y target\food-delivery.jar dist\food-delivery.jar >nul
echo Build complete. Run run.bat.
