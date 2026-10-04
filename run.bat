@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
 echo Java is not installed or is not on PATH. Install JDK 17 or later.
 pause
 exit /b 1
)
if not exist "dist\food-delivery.jar" (
 echo Executable JAR missing. Run build.bat first.
 pause
 exit /b 1
)
echo FoodExpress will start at http://localhost:8080 unless you change the port.
echo Wait for Started FoodDeliveryApplication, then open the URL in your browser.
echo Press Ctrl+C to stop.
java -jar "dist\food-delivery.jar" %*
if errorlevel 1 pause
