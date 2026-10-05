@echo off
setlocal
cd /d "%~dp0"
if exist ".env" for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"
if defined GOOGLE_CLIENT_ID if defined GOOGLE_CLIENT_SECRET (
  if not defined SPRING_PROFILES_ACTIVE (set "SPRING_PROFILES_ACTIVE=google") else (set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%,google")
)
set "MAVEN_VERSION=3.9.9"
set "WRAPPER_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_HOME=%WRAPPER_HOME%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_EXE=%MAVEN_HOME%\bin\mvn.cmd"
if not exist "%MAVEN_EXE%" (
  echo Downloading Apache Maven %MAVEN_VERSION% for the first run...
  if not exist "%WRAPPER_HOME%" mkdir "%WRAPPER_HOME%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $zip=Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -LiteralPath $zip -DestinationPath '%WRAPPER_HOME%' -Force; Remove-Item -LiteralPath $zip -Force"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_EXE%" %*
exit /b %errorlevel%
