@echo off
setlocal
cd /d "%~dp0"
if exist ".env" for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"
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
if defined GOOGLE_CLIENT_ID if defined GOOGLE_CLIENT_SECRET (
 if not defined SPRING_PROFILES_ACTIVE (set "SPRING_PROFILES_ACTIVE=google") else (set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%,google")
)
java -jar "dist\food-delivery.jar" %*
if errorlevel 1 pause
